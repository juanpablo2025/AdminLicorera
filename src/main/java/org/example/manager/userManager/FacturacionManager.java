package org.example.manager.userManager;



import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.example.manager.userManager.ExcelManager.FILE_PATH;
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
        eliminarMesasConIdMayorA10();
        System.exit(ZERO);
        // Salir del programa después de la facturación
    }

    /**
     * Muestra un mensaje de error si la palabra ingresada es incorrecta.
     */
    public void mostrarErrorFacturacion() {
        javax.swing.JOptionPane.showMessageDialog(null, ERROR_MENU, ERROR_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    // Método para eliminar mesas con ID mayor a 10
    public void eliminarMesasConIdMayorA10() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("Mesas"); // Nombre de la hoja donde se encuentran las mesas

            if (mesasSheet != null) {
                // Iterar a través de las filas de la hoja de mesas
                for (int i = mesasSheet.getLastRowNum(); i >= 1; i--) { // Comenzar desde la última fila
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(0); // Suponiendo que el ID de la mesa está en la columna A (índice 0)
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String mesaID = idCell.getStringCellValue(); // Obtener el ID de la mesa como String
                            if (mesaID.startsWith("Mesa ")) {
                                // Extraer el número después de "Mesa "
                                int idNumero = Integer.parseInt(mesaID.split(" ")[1]);
                                if (idNumero > 10) {
                                    mesasSheet.removeRow(row); // Eliminar la fila de la mesa
                                    // Mover el resto de las filas hacia arriba
                                    for (int j = i + 1; j <= mesasSheet.getLastRowNum(); j++) {
                                        Row nextRow = mesasSheet.getRow(j);
                                        if (nextRow != null) {
                                            mesasSheet.shiftRows(j, j + 1, -1); // Desplazar filas hacia arriba
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Guardar los cambios en el archivo Excel
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toString())) {
                    workbook.write(fos);
                }

               // JOptionPane.showMessageDialog(null, "Mesas con ID mayor a 10 han sido eliminadas.");

            } else {
                JOptionPane.showMessageDialog(null, "La hoja de mesas no se encontró.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
