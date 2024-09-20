package org.example.manager;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FacturacionManager {

    private ExcelManager excelManager;

    public FacturacionManager() {
        this.excelManager = new ExcelManager();
    }

    /**
     * Método para verificar si el usuario ha ingresado la palabra correcta para facturar.
     *
     * @param input Texto ingresado por el usuario.
     * @return true si la palabra es "Facturar", false en caso contrario.
     */
    public boolean verificarFacturacion(String input) {
        return "Facturar".equals(input);
    }

    /**
     * Realiza la facturación y limpieza de los datos.
     * Luego, termina la ejecución del programa.
     */
    public void facturarYSalir() {
        excelManager.facturarYLimpiar();
        System.exit(0);
        // Salir del programa después de la facturación
    }

    /**
     * Muestra un mensaje de error si la palabra ingresada es incorrecta.
     */
    public void mostrarErrorFacturacion() {
        javax.swing.JOptionPane.showMessageDialog(null, "Palabra incorrecta. Regresando al menú principal.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }


     //cambiar a pdf

    public void guardarTotalFacturadoEnArchivo(double totalFacturado) {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String nombreArchivo = "Total_Facturado_" + fechaActual.format(formatter) + ".txt";

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write("Total facturado en el día: " + totalFacturado);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
