package org.example.manager.adminmanager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Factura;
import org.example.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.example.utils.Constants.*;

public class ExcelAdminManager {/*
    public static final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
    public static final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + '\\' + FILE_NAME;

    private static final Logger logger =  LoggerFactory.getLogger(ExcelAdminManager.class);

    public static void updateProduct(Producto productoActualizado) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            int lastRowNum = sheet.getLastRowNum();
            boolean found = false;

            for (int i = ONE; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(ZERO);
                    if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                        int idEnExcel = (int) idCell.getNumericCellValue();

                        if (idEnExcel == productoActualizado.getId()) {
                            row.getCell(ONE).setCellValue(productoActualizado.getName());
                            row.getCell(TWO).setCellValue(productoActualizado.getQuantity());
                            row.getCell(THREE).setCellValue(productoActualizado.getPrice());
                            row.getCell(FIVE).setCellValue(productoActualizado.getFoto());
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                Row newRow = sheet.createRow(++lastRowNum);
                newRow.createCell(ZERO).setCellValue(productoActualizado.getId());
                newRow.createCell(ONE).setCellValue(productoActualizado.getName());
                newRow.createCell(TWO).setCellValue(productoActualizado.getQuantity());
                newRow.createCell(THREE).setCellValue(productoActualizado.getPrice());
                newRow.createCell(FIVE).setCellValue(productoActualizado.getFoto());
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            logger.error("Error al actualizar producto: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al actualizar producto: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void deleteProductById(int productId) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(ZERO);
                    if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                        int id = (int) idCell.getNumericCellValue();
                        if (id == productId) {
                            sheet.removeRow(row);
                            break;
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar producto: " + e.getMessage());
        }
    }

    public List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int id = (int) row.getCell(ZERO).getNumericCellValue();
                    String name = row.getCell(ONE).getStringCellValue();
                    int quantity = (int) row.getCell(TWO).getNumericCellValue();
                    double price = row.getCell(THREE).getNumericCellValue();
                    String foto = row.getCell(FIVE).getStringCellValue();

                    products.add(new Producto(id,name, quantity, price,foto));
                }
            }
        } catch (IOException e) {
            logger.error("Error al leer productos: {}", e.getMessage());
        }
        return products;
    }


    public Producto getProductByName(String selectedProduct) {
        List<Producto> products = getProducts();
        for (Producto p : products) {
            if (p.getName().equals(selectedProduct)) {
                return p;
            }
        }
        return null;
    }


    public List<Factura> getFacturas() {
        List<Factura> facturas = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet ventasSheet = workbook.getSheet(VENTAS);
            if (ventasSheet != null) {
                for (int i = ONE; i <= ventasSheet.getLastRowNum(); i++) {
                    Row row = ventasSheet.getRow(i);
                    if (row != null) {
                        String id = row.getCell(ZERO).getStringCellValue();
                        String productos = row.getCell(ONE).getStringCellValue();
                        double total = row.getCell(TWO).getNumericCellValue();
                        String fechaHora = row.getCell(THREE).getStringCellValue();
                        String tipoPago = row.getCell(FOUR).getStringCellValue();

                        Factura factura = new Factura(id, productos, total, fechaHora, tipoPago);
                        facturas.add(factura);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error al leer facturas: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al leer facturas: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return facturas;
    }

    public boolean eliminarFacturaYActualizarProductos(String facturaId) {
        try {

            FileInputStream file = new FileInputStream(FILE_PATH);
            boolean facturaEliminada;
            try (Workbook workbook = new XSSFWorkbook(file)) {
                Sheet ventasSheet = workbook.getSheet(VENTAS);
                Sheet productosSheet = workbook.getSheet("Productos");

                facturaEliminada = false;

                for (int i = ONE; i <= ventasSheet.getLastRowNum(); i++) {
                    Row row = ventasSheet.getRow(i);
                    if (row == null) continue;

                    Cell idCell = row.getCell(ZERO);
                    if (idCell != null && idCell.getStringCellValue().equals(facturaId)) {
                        Cell productosCell = row.getCell(ONE);
                        if (productosCell != null) {
                            String productosStr = productosCell.getStringCellValue();
                            String[] productos = productosStr.split(" ");

                            String nombreProducto = null;
                            int cantidad;

                            for (String part : productos) {
                                if (part.startsWith("x")) {
                                    cantidad = Integer.parseInt(part.substring(ONE).trim());

                                    for (int j = ONE; j <= productosSheet.getLastRowNum(); j++) {
                                        Row productoRow = productosSheet.getRow(j);
                                        if (productoRow == null) continue;

                                        Cell nombreCell = productoRow.getCell(ONE);
                                        if (nombreCell != null) {
                                            String nombreEnProductos = nombreCell.getStringCellValue().trim();
                                            if (nombreEnProductos.equals(nombreProducto)) {
                                                Cell cantidadCell = productoRow.getCell(TWO);
                                                int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                                cantidadCell.setCellValue(cantidadActual + cantidad);
                                                break;
                                            }
                                        }
                                    }
                                    nombreProducto = null;

                                } else if (!part.startsWith("$") && !part.equals("=")) {
                                    nombreProducto = part.trim();
                                }
                            }
                        }

                        ventasSheet.removeRow(row);
                        if (i < ventasSheet.getLastRowNum()) {
                            ventasSheet.shiftRows(i + ONE, ventasSheet.getLastRowNum(), -ONE);
                        }
                        facturaEliminada = true;
                        break;
                    }
                }

                file.close();
                FileOutputStream outputFile = new FileOutputStream(FILE_PATH);
                workbook.write(outputFile);
                outputFile.close();
            }

            return facturaEliminada;

        } catch (Exception e) {
            logger.error("Error al eliminar la factura: {}", e.getMessage());
            return false;
        }
    }*/
}
