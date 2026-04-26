package lsi.ubu.solucion;

import java.sql.Date;
import java.sql.SQLException;

public class GestionDonacionesSangre {

	// Usaremos este main más adelante para probar si nuestra donación funciona
	public static void main(String[] args) {
		System.out.println("Aquí probaremos las donaciones luego...");
	}

	//metodo para realizar las donaciones
	public static void realizar_donacion(String m_NIF, float m_Cantidad, int m_ID_Hospital, Date m_Fecha_Donacion) throws SQLException {
		
		// Aquí escribiremos la lógica paso a paso
		
		//Primero tenemos que hacer todas las validaciones
		
		
		//Comprobar que la cantidad es valida
		
		
		//Buscamos el donante con el NIF para ver si existe, y si existe guardamos su tipo de sangre
		
		
		//Si todavia no hemos lanzado error buscamos la fecha de su ultima donacion y miramos que hayan pasado minimo 15 dias
		
		
		//Podemos comprobar si el hospital existe
		
		
		//Si todo es correcto insertmos la nueva donacion en la tabla donacion
		
		
		// hacemos update en la tabla reserva_hospital para actualizar sus valores
		
		
		//habria que confirmar haciendo commit
		
		
		
		//Si saltase cualquier error hariamos rollback en el catch
		
	}
	
	
	public static void anular_traspaso(int m_ID_Tipo_Sangre, int m_ID_Hospital_Origen, int m_ID_Hospital_Destino, Date m_Fecha_Traspaso) throws SQLException{
		
		//Validaciones Previas
		
		
		//Comprobar que el Tipo de Sangre existe 
		
		
		//Comprobar que el Hospital Origen existe
		
		
		
		//Comprobar que el Hospital Destino existe
		
		
		//Buscar el traspaso en la tabla traspaso con los 4 datos y si existe leemos y guardamos su cantidad
		
		
		//comprobar si la cantidad es mayor que cero
		
		
		//si todo es correcto empezamos las modificaciones
		
		
		//restar la cantidad a la tabla del hospital origen y sumarsela al de destino
		
		
		//delete del registro de la tabla traspaso
		
		
		//si todo esta bien hacemos commit 
		
		
		//si saltase alguna excepcion hacemos rollback y capturamos en el catch
		
	}
	
	public static void consulta_traspasos(String m_Tipo_Sangre) throws SQLException{
		
		//validaciones
		
		
		//buscar en tipo_sangre si la descripcion pasada existe
		
		
		
		//consultas
		
		
		//consulta que haga join de las 4 tablas implicadas con el where del tipo de sangre que buscamos
		
		
		//ejecutar la consulta con executeQuery para tener el resultado de la consulta
		
		
		//recorrer el resultset con un while 
		
		
		//dentro del while imprimir los datos de la consulta
		
		
		//hacemos rollback si algo sale mal por peticion del enunciado
		//si todo va bien se hace commit
		
	}
	
	
}