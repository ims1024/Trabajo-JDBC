package lsi.ubu.tests;

import java.sql.Date;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.servicios.GestionDonacionesSangreException;
import lsi.ubu.solucion.GestionDonacionesSangre;

/**
 * Clase encargada de ejecutar la batería de pruebas (Happy Paths y Casos Extremos)
 * para las transacciones de gestión de donaciones de sangre.
 */
public class TestDonacionesSangre {

    private static Logger logger = LoggerFactory.getLogger(TestDonacionesSangre.class);

    // MÉTODO PRINCIPAL DE EJECUCIÓN
    public static void ejecutarTodosLosTests() {
        logger.info("Comenzando ejecución de la batería de tests...\n");

        // Tests: realizar_donacion
        logger.info("--- INICIANDO TESTS: realizar_donacion ---");
        testRealizarDonacion_OK();
        testRealizarDonacion_CantidadIncorrecta();
        testRealizarDonacion_DonanteNoExiste();
        testRealizarDonacion_HospitalNoExiste();
        testRealizarDonacion_ExcedeCupo();
        
        // Tests: anular_traspaso
        logger.info("\n--- INICIANDO TESTS: anular_traspaso ---");
        testAnularTraspaso_OK();
        testAnularTraspaso_TipoSangreNoExiste();
        testAnularTraspaso_HospitalNoExiste();
        testAnularTraspaso_TraspasoNoExiste();

        // Tests: consulta_traspasos
        logger.info("\n--- INICIANDO TESTS: consulta_traspasos ---");
        testConsultaTraspasos_OK();
        testConsultaTraspasos_TipoSangreNoExiste();
        
        logger.info("\nBatería de tests finalizada.");
    }

    // Tests: realizar_donacion
    private static void testRealizarDonacion_OK() {
        logger.info("Ejecutando testRealizarDonacion_OK...");
        try {
            // Preparamos el escenario ideal 
            Date fechaHoy = new Date(System.currentTimeMillis());
            
            // Ejecutamos el método que queremos probar
            GestionDonacionesSangre.realizar_donacion("12345678A", 1, 0.4f, fechaHoy);
            
            // Si el código llega hasta aquí sin saltar al catch, significa que todo ha ido perfecto
            logger.info("-> EXITO: El test ha funcionado.\n");
            
        } catch (SQLException e) {
            // Si salta cualquier error, el test ha fallado porque esperábamos un éxito absoluto
            logger.error("-> FALLO: Excepción inesperada.\n");
        }
    }

    private static void testRealizarDonacion_CantidadIncorrecta() {
        logger.info("Ejecutando testRealizarDonacion_CantidadIncorrecta...");
        try {
            // Forzamos el error indicando una cantidad que supera el límite legal de 0.45f
            Date fechaHoy = new Date(System.currentTimeMillis());
            GestionDonacionesSangre.realizar_donacion("12345678A", 1, 0.9f, fechaHoy); 
            
            // Si el código llega aquí, nuestro programa ha fallado porque NO ha detectado el error
            logger.error("-> FALLO: No ha saltado la excepción.\n");
            
        } catch (GestionDonacionesSangreException e) {
            // Se conforma con que salte la excepción, sin comprobar el código exacto
            logger.info("-> EXITO: Ha saltado una excepción controlada.\n");
        } catch (SQLException e) {
            logger.error("-> FALLO: Error SQL genérico.\n");
        }
    }

    private static void testRealizarDonacion_DonanteNoExiste() {
        logger.info("Ejecutando testRealizarDonacion_DonanteNoExiste...");
        try {
            // Forzamos el error pasando un NIF inventado que sabemos que no está en las tablas
            Date fechaHoy = new Date(System.currentTimeMillis());
            GestionDonacionesSangre.realizar_donacion("00000000T", 1, 0.4f, fechaHoy); 
            
            // Si no salta el catch, es que el programa ha dejado donar a un fantasma
            logger.error("-> FALLO: No ha saltado la excepción.\n");
            
        } catch (GestionDonacionesSangreException e) {
            // Se conforma con que salte la excepción, sin comprobar el código exacto
            logger.info("-> EXITO: Ha saltado una excepción controlada.\n");
        } catch (SQLException e) {
            logger.error("-> FALLO: Error SQL genérico.\n");
        }
    }

    private static void testRealizarDonacion_HospitalNoExiste() {
        logger.info("Ejecutando testRealizarDonacion_HospitalNoExiste...");
        try {
            // Forzamos el error pasando un ID de hospital que está fuera de rango
            Date fechaHoy = new Date(System.currentTimeMillis());
            GestionDonacionesSangre.realizar_donacion("12345678A", 10000, 0.4f, fechaHoy); 
            
            // Si el código no es bloqueado por la BD, el test falla
            logger.error("-> FALLO: No ha saltado la excepción.\n");
            
        } catch (GestionDonacionesSangreException e) {
            // Se conforma con que salte la excepción, sin comprobar el código exacto
            logger.info("-> EXITO: Ha saltado una excepción controlada.\n");
        } catch (SQLException e) {
            logger.error("-> FALLO: Error SQL genérico.\n");
        }
    }

    private static void testRealizarDonacion_ExcedeCupo() {
        logger.info("Ejecutando testRealizarDonacion_ExcedeCupo...");

        // Usar un NIF válido.
        // Hacer una donación correcta.
        // Volver a llamar a realizar_donacion inmediatamente con el MISMO NIF y fecha.
        // Capturar GestionDonacionesSangreException y comprobar que el código es DONANTE_EXCEDE
    }


    // Tests: anular_traspaso
    private static void testAnularTraspaso_OK() {
        logger.info("Ejecutando testAnularTraspaso_OK...");

        // Buscar en tu script sql un traspaso que exista realmente 
        // Llamar a GestionDonacionesSangre.anular_traspaso(...) con esos datos exactos
        // Comprobar que no lanza excepciones, try-catch 
    }

    private static void testAnularTraspaso_TipoSangreNoExiste() {
        logger.info("Ejecutando testAnularTraspaso_TipoSangreNoExiste...");
        
        // Llamar a anular_traspaso pasándole un ID de tipo de sangre falso
        // Capturar GestionDonacionesSangreException y comprobar que es TIPO_SANGRE_NO_EXISTE 
    }

    private static void testAnularTraspaso_HospitalNoExiste() {
        logger.info("Ejecutando testAnularTraspaso_HospitalNoExiste...");

        // Llamar a anular_traspaso con un tipo de sangre real, pero un Hospital Origen falso
        // Capturar GestionDonacionesSangreException y comprobar que es HOSPITAL_NO_EXISTE 
    }

    private static void testAnularTraspaso_TraspasoNoExiste() {
        logger.info("Ejecutando testAnularTraspaso_TraspasoNoExiste...");

        // Llamar a anular_traspaso con datos reales pero con una fecha inventada.
        // Capturar SQLException 
    }


    // Tests: consulta_traspasos
    private static void testConsultaTraspasos_OK() {
        logger.info("Ejecutando testConsultaTraspasos_OK...");

        // Llamar a GestionDonacionesSangre.consulta_traspasos(); 
        // Comprobar que no lanza excepciones.
    }

    private static void testConsultaTraspasos_TipoSangreNoExiste() {
        logger.info("Ejecutando testConsultaTraspasos_TipoSangreNoExiste...");
    
        // Llamar a consulta_traspasos con una descripción inventada
        // Capturar GestionDonacionesSangreException y comprobar que es TIPO_SANGRE_NO_EXISTE
    }
}