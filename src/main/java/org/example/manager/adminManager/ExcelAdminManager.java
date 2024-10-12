package org.example.manager.adminManager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.model.Factura;
import org.example.model.Producto;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.example.utils.Constants.*;

public class ExcelAdminManager {
    public static final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
    public static final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);

    public static final String DIRECTORY_PATH_FACTURACION = System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion"+ fechaFormateada+".xlsx";







    // Método para agregar un producto al archivo Excel
    public void addProduct(Producto product) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            int lastRow = sheet.getLastRowNum() + 1; // Usa 1 para el siguiente índice

            Row row = sheet.createRow(lastRow);
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getQuantity());
            row.createCell(3).setCellValue(product.getPrice());

            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para leer los productos del archivo Excel
    public List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String name = row.getCell(ONE).getStringCellValue();
                    int quantity = (int) row.getCell(TWO).getNumericCellValue();
                    double price = row.getCell(THREE).getNumericCellValue();

                    products.add(new Producto(name, quantity,price));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Método para obtener un producto por nombre
    public Producto getProductByName(String selectedProduct) {
        List<Producto> products = getProducts();
        for (Producto p : products) {
            if (p.getName().equals(selectedProduct)) {
                return p;
            }
        }
        return null;
    }



        // Método para obtener todas las facturas desde la hoja de "compras"
        public List<Factura> getFacturas() {
            List<Factura> facturas = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(FILE_PATH);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet ventasSheet = workbook.getSheet("Ventas");
                if (ventasSheet != null) {
                    for (int i = 1; i <= ventasSheet.getLastRowNum(); i++) {
                        Row row = ventasSheet.getRow(i);
                        if (row != null) {
                            String id = row.getCell(0).getStringCellValue();
                            String productos = row.getCell(1).getStringCellValue();
                            double total = row.getCell(2).getNumericCellValue();
                            String fechaHora = row.getCell(3).getStringCellValue();

                            Factura factura = new Factura(id, productos, total, fechaHora);
                            facturas.add(factura);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return facturas;
        }

        // Método para eliminar una factura de la hoja de "compras"
        public void eliminarFactura(String facturaID) {
            try (FileInputStream fis = new FileInputStream(FILE_PATH);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet comprasSheet = workbook.getSheet("Ventas");
                if (comprasSheet != null) {
                    for (int i = 1; i <= comprasSheet.getLastRowNum(); i++) {
                        Row row = comprasSheet.getRow(i);
                        if (row != null) {
                            String id = row.getCell(0).getStringCellValue();
                            if (id.equals(facturaID)) {
                                comprasSheet.removeRow(row); // Eliminar la fila que contiene la factura
                                break;
                            }
                        }
                    }
                }

                // Guardar el archivo actualizado
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
