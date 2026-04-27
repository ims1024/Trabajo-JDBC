package lsi.ubu.tests;

import java.sql.Date;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.solucion.GestionDonacionesSangre;

public class TestDonacionesSangre {

    private static Logger logger = LoggerFactory.getLogger(TestDonacionesSangre.class);

    // Metodo al que llamamos en el main para ejecutar los test
    public static void ejecutarTodosLosTests() {
        logger.info("Comenzando ejecución de tests...");

        testRealizarDonacion_OK();
        
        // Falta por añadir el resto de test
    }

    // TESTS PARA REALIZAR_DONACION
    private static void testRealizarDonacion_OK() {
        logger.info("Ejecutando testRealizarDonacion_OK...");
        try {
            // Inventamos unos datos correctos: 
            Date fechaHoy = new Date(System.currentTimeMillis());
            
            // Llamamos al método de nuestra clase
            GestionDonacionesSangre.realizar_donacion("12345678A", 1, 0.4f, fechaHoy);
            
            logger.info("-> EXITO: El testRealizarDonacion_OK ha funcionado sin lanzar excepciones.");
            
        } catch (SQLException e) {
            logger.error("-> FALLO: El testRealizarDonacion_OK ha lanzado una excepción inesperada: " + e.getMessage());
        }
    }

}