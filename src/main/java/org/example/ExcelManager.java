package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {

    private static final String FILE_NAME = "productos.xlsx";
    private static final String SHEET_NAME = "Productos";

    public ExcelManager() {
        // Si el archivo no existe, lo crea
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            createExcelFile();
        }
    }

    // Método para crear el archivo Excel si no existe
    private void createExcelFile() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);

        // Crear cabeceras
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Nombre");
        header.createCell(2).setCellValue("Cantidad");
        header.createCell(3).setCellValue("Precio");

        try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
            workbook.write(fileOut);
            System.out.println("Archivo Excel creado: " + FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para agregar un producto al archivo Excel
    public void addProduct(Producto product) {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int lastRow = sheet.getLastRowNum() + 1;

            Row row = sheet.createRow(lastRow);
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getQuantity());
            row.createCell(3).setCellValue(product.getPrice());

            try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
                workbook.write(fileOut);
                System.out.println("Producto agregado al archivo Excel.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para leer los productos del archivo Excel
    public List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    String name = row.getCell(1).getStringCellValue();
                    int quantity = (int) row.getCell(2).getNumericCellValue();
                    double price = row.getCell(3).getNumericCellValue();

                    products.add(new Producto(id, name, quantity, price));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public void savePurchase(String string, String text, double total) {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("Compras");
            if (sheet == null) {
                sheet = workbook.createSheet("Compras");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Producto");
                header.createCell(1).setCellValue("Cantidad");
                header.createCell(2).setCellValue("Total");
            }

            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);
            row.createCell(0).setCellValue(string);
            row.createCell(1).setCellValue(text);
            row.createCell(2).setCellValue(total);

            try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
                workbook.write(fileOut);
                System.out.println("Compra guardada en el archivo Excel.");
            }
        } catch (IOException e) {
            e.printStackTrace();
    }
}
}
