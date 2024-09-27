package org.example.manager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.model.Producto;
import org.example.ui.UIHelpers;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



import static org.example.utils.Constants.*;
import static org.example.utils.Constants.PESOS;

public class GastosManager {

    private ProductoManager productoManager = new ProductoManager();

    public static void saveGasto(String nombreGasto, int i, double precio) {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {
            // Crear la pestaña de gastos si no existe
            String GASTOS_SHEET_NAME = "Gastos";
            Sheet gastosSheet = workbook.getSheet(GASTOS_SHEET_NAME);
            if (gastosSheet == null) {
                // Crear la hoja de gastos
                gastosSheet = workbook.createSheet(GASTOS_SHEET_NAME);

                // Crear fila de encabezado
                Row headerRow = gastosSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID Producto");
                headerRow.createCell(1).setCellValue("Nombre Producto");
                headerRow.createCell(3).setCellValue("Precio Compra");
                headerRow.createCell(4).setCellValue("Fecha y Hora");
            }

            // Añadir el registro del gasto
            int lastRowNum = gastosSheet.getLastRowNum();
            Row newRow = gastosSheet.createRow(lastRowNum + 1);
            newRow.createCell(1).setCellValue(nombreGasto);
            newRow.createCell(2).setCellValue(i);
            newRow.createCell(3).setCellValue(precio);  // Guardar el precio ingresado
            newRow.createCell(4).setCellValue(java.time.LocalDateTime.now().toString());

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                workbook.write(fos);
                System.out.println("Reabastecimiento registrado en la pestaña de gastos.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void reabastecerProducto(Producto producto, int cantidad, double precioCompra) {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Actualizar la cantidad del producto en la pestaña de productos
            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            boolean productoEncontrado = false;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Suponiendo que el nombre del producto está en la columna 1
                    if (row.getCell(1).getStringCellValue().equals(producto.getName())) {
                        int cantidadActual = (int) row.getCell(2).getNumericCellValue(); // Suponiendo que la cantidad está en la columna 2
                        row.getCell(2).setCellValue(cantidadActual + cantidad);  // Sumar la cantidad
                        productoEncontrado = true;
                        break;
                    }
                }
            }

            if (productoEncontrado) {
                // Guardar la actualización de productos
                try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                    workbook.write(fos);
                    System.out.println("Cantidad del producto actualizada exitosamente.");
                }
            } else {
                System.out.println("Producto no encontrado.");
            }

            // Crear la pestaña de gastos si no existe
            String GASTOS_SHEET_NAME = "Gastos";
            Sheet gastosSheet = workbook.getSheet(GASTOS_SHEET_NAME);
            if (gastosSheet == null) {
                // Crear la hoja de gastos
                gastosSheet = workbook.createSheet(GASTOS_SHEET_NAME);

                // Crear fila de encabezado
                Row headerRow = gastosSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID Producto");
                headerRow.createCell(1).setCellValue("Nombre Producto");
                headerRow.createCell(2).setCellValue("Cantidad Reabastecida");
                headerRow.createCell(3).setCellValue("Precio Compra");
                headerRow.createCell(4).setCellValue("Fecha y Hora");
            }

            // Añadir el registro del gasto
            int lastRowNum = gastosSheet.getLastRowNum();
            Row newRow = gastosSheet.createRow(lastRowNum + 1);
            newRow.createCell(0).setCellValue(producto.getId());
            newRow.createCell(1).setCellValue(producto.getName());
            newRow.createCell(2).setCellValue(cantidad);
            newRow.createCell(3).setCellValue(precioCompra);  // Guardar el precio ingresado
            newRow.createCell(4).setCellValue(java.time.LocalDateTime.now().toString());

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                workbook.write(fos);
                System.out.println("Reabastecimiento registrado en la pestaña de gastos.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
