package org.example.manager.usermanager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.utils.Constants.*;


public class GastosUserManager {

    private GastosUserManager() {}

    private static final Logger logger =  LoggerFactory.getLogger(GastosUserManager.class);

    /*public static void saveGasto(String nombreGasto, double precio) {
        try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {
            String gastosSheetName = "Gastos";
            Sheet gastosSheet = workbook.getSheet(gastosSheetName);
            if (gastosSheet == null) {
                gastosSheet = workbook.createSheet(gastosSheetName);

                Row headerRow = gastosSheet.createRow(ZERO);
                headerRow.createCell(ZERO).setCellValue("ID Producto");
                headerRow.createCell(ONE).setCellValue("Nombre Producto");
                headerRow.createCell(THREE).setCellValue("Precio Compra");
                headerRow.createCell(FOUR).setCellValue("Fecha y Hora");
            }
            LocalDateTime fechaHora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int lastRowNum = gastosSheet.getLastRowNum();
            Row newRow = gastosSheet.createRow(lastRowNum + ONE);
            newRow.createCell(ZERO).setCellValue(System.currentTimeMillis() % 1000);
            newRow.createCell(ONE).setCellValue(nombreGasto);
            newRow.createCell(TWO).setCellValue("n/a");
            newRow.createCell(THREE).setCellValue(precio);
            newRow.createCell(FOUR).setCellValue(formatter.format(fechaHora));

            try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            logger.error("Error al guardar el gasto: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al guardar el gasto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }*/

    public static void saveGasto(String nombreGasto, double precio) {
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
