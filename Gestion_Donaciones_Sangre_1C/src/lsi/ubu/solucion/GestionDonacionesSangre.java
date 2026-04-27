package lsi.ubu.solucion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.servicios.GestionDonacionesSangreException;
import lsi.ubu.servicios.Misc;
import lsi.ubu.util.ExecuteScript;
import lsi.ubu.util.PoolDeConexiones;

/**
 * GestionDonacionesSangre:
 * Implementa la gestion de donaciones de sangre siguiendo el esqueleto proporcionado
 */
public class GestionDonacionesSangre {
	
	private static Logger logger = LoggerFactory.getLogger(GestionDonacionesSangre.class);

	private static final String script_path = "sql/";

	public static void main(String[] args) throws SQLException{		
		tests();
		System.out.println("FIN.............");
	}
	
	public static void realizar_donacion(String m_NIF, int m_ID_Hospital,
			float m_Cantidad, Date m_Fecha_Donacion) throws SQLException {
		
		// Comprobamos la cantidad máxima antes de hacer la conexion para ahorrar recursos por si fallara
		if (m_Cantidad <= 0 || m_Cantidad > 0.45f) {
			throw new GestionDonacionesSangreException(GestionDonacionesSangreException.VALOR_CANTIDAD_DONACION_INCORRECTO);
		}

		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			con = pool.getConnection();
			
			// Desactivamos el autocommit para evitar problemas
			con.setAutoCommit(false);
			
			// Comprobamos si existe el hospital
			pst = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
			pst.setInt(1, m_ID_Hospital);
			rs = pst.executeQuery();
			if (!rs.next()) {
				throw new GestionDonacionesSangreException(GestionDonacionesSangreException.HOSPITAL_NO_EXISTE);
			}
			rs.close(); pst.close();

			// Comprobamos la existencia del donante y si existe nos guardamos su tipo de sangre
			pst = con.prepareStatement("SELECT id_tipo_sangre FROM donante WHERE nif = ?");
			pst.setString(1, m_NIF);
			rs = pst.executeQuery();
			if (!rs.next()) {
				throw new GestionDonacionesSangreException(GestionDonacionesSangreException.DONANTE_NO_EXISTE);
			}
			int idTipoSangre = rs.getInt("id_tipo_sangre");
			rs.close(); pst.close();

			// Comprobamos si han pasado minimo 15 dias de su ultima donacion
			pst = con.prepareStatement("SELECT MAX(fecha_donacion) FROM donacion WHERE nif_donante = ?");
			pst.setString(1, m_NIF);
			rs = pst.executeQuery();
			if (rs.next()) {
				Date ultimaDonacion = rs.getDate(1);
				if (ultimaDonacion != null) {
					// Usamos la clase Misc  para calcular los días
					int diasPasados = Misc.howManyDaysBetween(m_Fecha_Donacion, ultimaDonacion);
					if (diasPasados < 15) {
						throw new GestionDonacionesSangreException(GestionDonacionesSangreException.DONANTE_EXCEDE);
					}
				}
			}
			rs.close(); pst.close();

			// Hacemos INSERT de la nueva donacion
			String sqlInsertDonacion = "INSERT INTO donacion (id_donacion, nif_donante, cantidad, fecha_donacion) VALUES (seq_donacion.NEXTVAL, ?, ?, ?)";
			pst = con.prepareStatement(sqlInsertDonacion);
			pst.setString(1, m_NIF);
			pst.setFloat(2, m_Cantidad);
			
			// Convertimos la java.util.Date a java.sql.Date para la base de datos
			pst.setDate(3, new java.sql.Date(m_Fecha_Donacion.getTime()));
			pst.executeUpdate();
			pst.close();

			// Hacemos UPDATE de la reserva del hospital
			String sqlUpdateReserva = "UPDATE reserva_hospital SET cantidad = cantidad + ? WHERE id_hospital = ? AND id_tipo_sangre = ?";
			pst = con.prepareStatement(sqlUpdateReserva);
			pst.setFloat(1, m_Cantidad);
			pst.setInt(2, m_ID_Hospital);
			pst.setInt(3, idTipoSangre);
			int filasAfectadas = pst.executeUpdate();
			pst.close();

			// Comprobamos si las reservas de ese tipo del hospital estaban a cero para hacer INSERT en su lugar
			if (filasAfectadas == 0) {
				String sqlInsertReserva = "INSERT INTO reserva_hospital (id_tipo_sangre, id_hospital, cantidad) VALUES (?, ?, ?)";
				pst = con.prepareStatement(sqlInsertReserva);
				pst.setInt(1, idTipoSangre);
				pst.setInt(2, m_ID_Hospital);
				pst.setFloat(3, m_Cantidad);
				pst.executeUpdate();
				pst.close();
			}

			// Si no ha saltado ninguna excepcion hacemos commit para guardar los cambios al finalizar
			con.commit();
			logger.info("Donación realizada con éxito para el NIF: " + m_NIF);

		} catch (SQLException e) {
			
			// Si algo ha fallado tenemos que hacer rollback
			if (con != null) {
				
				try {
					
					con.rollback();
					logger.info("Rollback ejecutado correctamente.");
					
				} catch (SQLException ex) {
					
					//Si se produce error en el rollback lanzamos otra excepcion
					logger.error("Error al hacer rollback: " + ex.getMessage());
					
				}
			}
			
			// Avisamos de que ha ocurrido un error en la transaccion y lanzamos el error
			logger.error("Error en la transacción de donación: " + e.getMessage());
			throw e;		

		} finally {
			
			// Cerramos todo en el orden inverso al que lo abrimos
			try {
				
				if (rs != null) rs.close();
				if (pst != null) pst.close();
				if (con != null) con.close();
				
			} catch (SQLException e) {
				
				//Si se produce error al cerrar avisamos
				logger.error("Error cerrando recursos: " + e.getMessage());
			}
		}
	}
	
	public static void anular_traspaso(int m_ID_Tipo_Sangre, int m_ID_Hospital_Origen, int m_ID_Hospital_Destino,
        Date m_Fecha_Traspaso) throws SQLException {
    
    PoolDeConexiones pool = PoolDeConexiones.getInstance();
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
        con = pool.getConnection();
        con.setAutoCommit(false); 

        // ---------------------------
        // VALIDACIONES
        // ---------------------------

        // Tipo de sangre existe
        pst = con.prepareStatement("SELECT 1 FROM tipo_sangre WHERE id_tipo_sangre = ?");
        pst.setInt(1, m_ID_Tipo_Sangre);
        rs = pst.executeQuery();
        if (!rs.next()) {
            throw new GestionDonacionesSangreException(GestionDonacionesSangreException.TIPO_SANGRE_NO_EXISTE);
        }
        rs.close(); pst.close();

        // Hospital origen existe
        pst = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
        pst.setInt(1, m_ID_Hospital_Origen);
        rs = pst.executeQuery();
        if (!rs.next()) {
            throw new GestionDonacionesSangreException(GestionDonacionesSangreException.HOSPITAL_NO_EXISTE);
        }
        rs.close(); pst.close();

        // Hospital destino existe
        pst = con.prepareStatement("SELECT 1 FROM hospital WHERE id_hospital = ?");
        pst.setInt(1, m_ID_Hospital_Destino);
        rs = pst.executeQuery();
        if (!rs.next()) {
            throw new GestionDonacionesSangreException(GestionDonacionesSangreException.HOSPITAL_NO_EXISTE);
        }
        rs.close(); pst.close();

        // ---------------------------
        // BUSCAR TRASPASO
        // ---------------------------
        String sql = "SELECT cantidad FROM traspaso " +
                     "WHERE id_tipo_sangre = ? AND id_hospital_origen = ? " +
                     "AND id_hospital_destino = ? AND fecha_traspaso = ?";

        pst = con.prepareStatement(sql);
        pst.setInt(1, m_ID_Tipo_Sangre);
        pst.setInt(2, m_ID_Hospital_Origen);
        pst.setInt(3, m_ID_Hospital_Destino);
        pst.setDate(4, new java.sql.Date(m_Fecha_Traspaso.getTime()));

        rs = pst.executeQuery();

        if (!rs.next()) {
            throw new GestionDonacionesSangreException(GestionDonacionesSangreException.TRASPASO_NO_EXISTE);
        }

        float cantidad = rs.getFloat("cantidad");

        if (cantidad < 0) {
            throw new GestionDonacionesSangreException(GestionDonacionesSangreException.VALOR_CANTIDAD_TRASPASO_INCORRECTO);
        }

        rs.close(); pst.close();

        // ---------------------------
        // REVERTIR MOVIMIENTO
        // ---------------------------

        // Restar al destino
        pst = con.prepareStatement(
            "UPDATE reserva_hospital SET cantidad = cantidad - ? " +
            "WHERE id_tipo_sangre = ? AND id_hospital = ?"
        );
        pst.setFloat(1, cantidad);
        pst.setInt(2, m_ID_Tipo_Sangre);
        pst.setInt(3, m_ID_Hospital_Destino);
        pst.executeUpdate();
        pst.close();

        // Sumar al origen
        pst = con.prepareStatement(
            "UPDATE reserva_hospital SET cantidad = cantidad + ? " +
            "WHERE id_tipo_sangre = ? AND id_hospital = ?"
        );
        pst.setFloat(1, cantidad);
        pst.setInt(2, m_ID_Tipo_Sangre);
        pst.setInt(3, m_ID_Hospital_Origen);
        pst.executeUpdate();
        pst.close();

        // ---------------------------
        // BORRAR TRASPASO
        // ---------------------------
        pst = con.prepareStatement(
            "DELETE FROM traspaso WHERE id_tipo_sangre = ? AND id_hospital_origen = ? " +
            "AND id_hospital_destino = ? AND fecha_traspaso = ?"
        );
        pst.setInt(1, m_ID_Tipo_Sangre);
        pst.setInt(2, m_ID_Hospital_Origen);
        pst.setInt(3, m_ID_Hospital_Destino);
        pst.setDate(4, new java.sql.Date(m_Fecha_Traspaso.getTime()));
        pst.executeUpdate();
        pst.close();

        con.commit();
        logger.info("Traspaso anulado correctamente.");

    } catch (SQLException e) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                logger.error("Error en rollback: " + ex.getMessage());
            }
        }
        logger.error("Error en anular_traspaso: " + e.getMessage());
        throw e;

    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            logger.error("Error cerrando recursos: " + e.getMessage());
        }
    }
}
	
	static public void creaTablas() {
		ExecuteScript.run(script_path + "gestion_donaciones_sangre.sql");
	}

	static void tests() throws SQLException{
		creaTablas();
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();		
		
		CallableStatement cll_reinicia=null;
		Connection conn = null;
		
		try {
			// Reinicio filas
			conn = pool.getConnection();
			cll_reinicia = conn.prepareCall("{call inicializa_test}");
			cll_reinicia.execute();
		} catch (SQLException e) {				
			logger.error(e.getMessage());			
		} finally {
			if (cll_reinicia!=null) cll_reinicia.close();
			if (conn!=null) conn.close();
		}			
	}
}