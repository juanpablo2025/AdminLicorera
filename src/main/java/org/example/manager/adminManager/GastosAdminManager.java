package org.example.manager.adminManager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.model.Producto;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.utils.Constants.PRODUCTS_SHEET_NAME;

public class GastosAdminManager {

    /*public void reabastecerProducto(Producto producto, int cantidad, double precioCompra) {
        try (FileInputStream fis = new FileInputStream(ExcelAdminManager.FILE_PATH);
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
                try (FileOutputStream fos = new FileOutputStream(ExcelAdminManager.FILE_PATH)) {
                    workbook.write(fos);
                   // System.out.println("Cantidad del producto actualizada exitosamente.");
                }
            } else {
               // System.out.println("Producto no encontrado.");
            }

            // Crear la pestaña de gastos si no existe
            String GASTOS_SHEET_NAME = "Reabastecimiento";
            Sheet reabastecimientoSheet = workbook.getSheet(GASTOS_SHEET_NAME);
            if (reabastecimientoSheet == null) {
                // Crear la hoja de gastos
                reabastecimientoSheet = workbook.createSheet(GASTOS_SHEET_NAME);

                // Crear fila de encabezado
                Row headerRow = reabastecimientoSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID Producto");
                headerRow.createCell(1).setCellValue("Nombre Producto");
                headerRow.createCell(2).setCellValue("Cantidad Reabastecida");
                headerRow.createCell(3).setCellValue("Precio Compra");
                headerRow.createCell(4).setCellValue("Fecha y Hora");
            }

            LocalDateTime fechaHora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);

            // Añadir el registro del gasto
            int lastRowNum = reabastecimientoSheet.getLastRowNum();
            Row newRow = reabastecimientoSheet.createRow(lastRowNum + 1);
            newRow.createCell(0).setCellValue(producto.getId());
            newRow.createCell(1).setCellValue(producto.getName());
            newRow.createCell(2).setCellValue(cantidad);

            // Si el precio es -1, escribir "N/A" o dejar vacío
            if (precioCompra == -10) {
                newRow.createCell(3).setCellValue("N/A");
            } else {
                newRow.createCell(3).setCellValue(precioCompra);
            }

            newRow.createCell(4).setCellValue(fechaFormateada);

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelAdminManager.FILE_PATH)) {
                workbook.write(fos);
                //System.out.println("Reabastecimiento registrado en la pestaña de reabastecimiento.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    public void reabastecerProducto(Producto producto, int cantidad, double precioCompra) {
        String sqlUpdateProducto = "UPDATE Productos SET cantidad = cantidad + ? WHERE id = ?";
        String sqlInsertReabastecimiento = "INSERT INTO reabastecimiento ( producto_nombre, cantidad_reabastecida, precio_compra, fecha_hora) VALUES ( ?, ?, ?, ?)";

        // Conexión a la base de datos
        try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL)) {

            // Actualizar la cantidad del producto en la tabla de productos
            try (PreparedStatement pstmtUpdate = connection.prepareStatement(sqlUpdateProducto)) {
                pstmtUpdate.setInt(1, cantidad);
                pstmtUpdate.setInt(2, producto.getId());
                int rowsUpdated = pstmtUpdate.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Cantidad del producto actualizada exitosamente.");
                } else {
                    System.out.println("Producto no encontrado en la base de datos.");
                }
            }

            // Registrar el reabastecimiento en la tabla de reabastecimientos
            try (PreparedStatement pstmtInsert = connection.prepareStatement(sqlInsertReabastecimiento)) {
                LocalDateTime fechaHora = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String fechaFormateada = fechaHora.format(formatter);


                pstmtInsert.setString(1, producto.getName());
                pstmtInsert.setInt(2, cantidad);
                pstmtInsert.setDouble(3, (precioCompra == -10) ? 0.0 : precioCompra); // Si el precio es -1, se coloca 0.0 o "N/A" en base de datos
                pstmtInsert.setString(4, fechaFormateada);
                pstmtInsert.executeUpdate();

                System.out.println("Reabastecimiento registrado en la base de datos.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
