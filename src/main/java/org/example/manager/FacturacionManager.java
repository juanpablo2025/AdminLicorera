package org.example.manager;



import static org.example.utils.Constants.*;

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
        System.exit(ZERO);
        // Salir del programa después de la facturación
    }

    /**
     * Muestra un mensaje de error si la palabra ingresada es incorrecta.
     */
    public void mostrarErrorFacturacion() {
        javax.swing.JOptionPane.showMessageDialog(null, ERROR_MENU, ERROR_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
    }



}
