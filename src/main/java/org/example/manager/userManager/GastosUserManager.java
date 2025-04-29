package org.example.manager.userManager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.manager.userDBManager.DatabaseUserManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class GastosUserManager {

   /* public static void saveGasto(String nombreGasto, int i, double precio) {
        try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
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
            LocalDateTime fechaHora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            // Añadir el registro del gasto
            int lastRowNum = gastosSheet.getLastRowNum();
            Row newRow = gastosSheet.createRow(lastRowNum + 1);
            newRow.createCell(0).setCellValue(System.currentTimeMillis() % 1000);
            newRow.createCell(1).setCellValue(nombreGasto);
            newRow.createCell(2).setCellValue("n/a");
            newRow.createCell(3).setCellValue(precio);  // Guardar el precio ingresado
            newRow.createCell(4).setCellValue(formatter.format(fechaHora));

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                workbook.write(fos);
                System.out.println("Reabastecimiento registrado en la pestaña de gastos.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
   public static void saveGasto(String nombreGasto, int i, double precio) {
       String sqlCreateTable = "CREATE TABLE IF NOT EXISTS Gastos (" +
               "idProducto INTEGER PRIMARY KEY AUTOINCREMENT, " +
               "nombreProducto TEXT, " +
               "precioCompra REAL, " +
               "fechaHora TEXT" +
               ");";

       String sqlInsertGasto = "INSERT INTO Gastos (nombreProducto, precioCompra, fechaHora) VALUES (?, ?, ?)";

       try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
            Statement stmt = connection.createStatement()) {

           // Crear la tabla si no existe
           stmt.execute(sqlCreateTable);

           // Obtener la fecha y hora actual
           LocalDateTime fechaHora = LocalDateTime.now();
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

           // Insertar el nuevo gasto
           try (PreparedStatement pstmt = connection.prepareStatement(sqlInsertGasto)) {
               pstmt.setString(1, nombreGasto);
               pstmt.setDouble(2, precio);
               pstmt.setString(3, formatter.format(fechaHora));

               pstmt.executeUpdate();
               System.out.println("Gasto registrado en la base de datos.");
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }
   }
}
