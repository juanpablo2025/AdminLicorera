package org.example.manager.adminmanager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.utils.Constants.*;

public class GastosAdminManager {

    private static final Logger logger =  LoggerFactory.getLogger(GastosAdminManager.class);

    public void reabastecerProducto(Producto producto, int cantidad, double precioCompra) {
        try (FileInputStream fis = new FileInputStream(ExcelAdminManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            boolean productoEncontrado = false;

            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row != null) {
                    if (!row.getCell(ONE).getStringCellValue().equals(producto.getName())) {
                        continue;
                    }
                    int cantidadActual = (int) row.getCell(TWO).getNumericCellValue();
                    row.getCell(TWO).setCellValue(cantidadActual + cantidad);
                    productoEncontrado = true;
                    break;
                }
            }

            if (productoEncontrado) {

                try (FileOutputStream fos = new FileOutputStream(ExcelAdminManager.FILE_PATH)) {
                    workbook.write(fos);
                }
            }

            String gastosSheetName = "Reabastecimiento";
            Sheet reabastecimientoSheet = workbook.getSheet(gastosSheetName);
            if (reabastecimientoSheet == null) {

                reabastecimientoSheet = workbook.createSheet(gastosSheetName);

                Row headerRow = reabastecimientoSheet.createRow(ZERO);
                headerRow.createCell(ZERO).setCellValue("ID Producto");
                headerRow.createCell(ONE).setCellValue("Nombre Producto");
                headerRow.createCell(TWO).setCellValue("Cantidad Reabastecida");
                headerRow.createCell(THREE).setCellValue("Precio Compra");
                headerRow.createCell(FOUR).setCellValue("Fecha y Hora");
            }

            LocalDateTime fechaHora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);

            int lastRowNum = reabastecimientoSheet.getLastRowNum();
            Row newRow = reabastecimientoSheet.createRow(lastRowNum + ONE);
            newRow.createCell(ZERO).setCellValue(producto.getId());
            newRow.createCell(ONE).setCellValue(producto.getName());
            newRow.createCell(TWO).setCellValue(cantidad);

            if (precioCompra == -TEN) {
                newRow.createCell(THREE).setCellValue(ZERO);
            } else {
                newRow.createCell(THREE).setCellValue(precioCompra);
            }

            newRow.createCell(FOUR).setCellValue(fechaFormateada);

            try (FileOutputStream fos = new FileOutputStream(ExcelAdminManager.FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            logger.error("Error al guardar el gasto: {}", e.getMessage());
            JOptionPane.showMessageDialog(null,"Error al guardar el gasto: " + e.getMessage());
        }
    }
}
