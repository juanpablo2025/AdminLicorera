package org.example.manager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Producto;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.example.utils.Constants.*;


public class ExcelManager {

    public ExcelManager() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            createExcelFile();
        }
    }

    // Método para crear el archivo Excel si no existe
    private void createExcelFile() {
        Workbook workbook = new XSSFWorkbook();

        // Crear hoja de productos
        Sheet productsSheet = workbook.createSheet(PRODUCTS_SHEET_NAME);
        Row header = productsSheet.createRow(0);
        header.createCell(0).setCellValue(ID);
        header.createCell(1).setCellValue(NOMBRE);
        header.createCell(2).setCellValue(CANTIDAD);
        header.createCell(3).setCellValue(PRECIO);

        // Crear hoja de compras
        Sheet purchasesSheet = workbook.createSheet(PURCHASES_SHEET_NAME);
        Row purchasesHeader = purchasesSheet.createRow(0);
        purchasesHeader.createCell(0).setCellValue(ID);
        purchasesHeader.createCell(1).setCellValue(PRODUCTOS);
        purchasesHeader.createCell(2).setCellValue(TOTAL);
        purchasesHeader.createCell(3).setCellValue(FECHA_HORA);

        try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para agregar un producto al archivo Excel
    public void addProduct(Producto product) {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            int lastRow = sheet.getLastRowNum() + 1;

            Row row = sheet.createRow(lastRow);
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getQuantity());
            row.createCell(3).setCellValue(product.getPrice());

            try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
                workbook.write(fileOut);
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

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
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

    // Método para guardar una compra en el archivo Excel
    public void savePurchase(String compraID, String productos, double total, LocalDateTime now) {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.createSheet(PURCHASES_SHEET_NAME);
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue(ID);
                header.createCell(1).setCellValue(PRODUCTOS);
                header.createCell(2).setCellValue(TOTAL);
                header.createCell(3).setCellValue(FECHA_HORA);
            }
            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);

            row.createCell(0).setCellValue(compraID);
            row.createCell(1).setCellValue(productos);  // Los productos se listan en líneas nuevas dentro de la misma celda
            row.createCell(2).setCellValue(total);
            row.createCell(3).setCellValue(now.toString());

            try (FileOutputStream fileOut = new FileOutputStream(FILE_NAME)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener el número de compras realizadas
    public int getPurchasesCount() {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            if (sheet != null) {
                return sheet.getLastRowNum();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    // Método para sumar los totales de la hoja "Compras"
    private double sumarTotalesCompras(Sheet purchasesSheet) {
        double totalSum = 0.0;
        for (int i = 1; i <= purchasesSheet.getLastRowNum(); i++) {
            Row row = purchasesSheet.getRow(i);
            if (row != null && row.getCell(2) != null) {
                totalSum += row.getCell(2).getNumericCellValue();
            }
        }
        return totalSum;
    }

    // Método para crear una copia de la hoja "Compras" y renombrarla
    private void copiarHojaCompras(Workbook workbook, Sheet purchasesSheet, double totalCompra) {
        // Crear una nueva hoja con el nombre "Facturacion_<fecha>"
        String nuevaHojaNombre = FACTURACION + LocalDateTime.now().toString().replace(DOS_PUNTOS ,GUION);
        Sheet nuevaHoja = workbook.createSheet(nuevaHojaNombre);

        // Copiar el contenido de la hoja "Compras" a la nueva hoja
        for (int i = 0; i <= purchasesSheet.getLastRowNum(); i++) {
            Row oldRow = purchasesSheet.getRow(i);
            Row newRow = nuevaHoja.createRow(i);
            if (oldRow != null) {
                for (int j = 0; j < oldRow.getLastCellNum(); j++) {
                    Cell oldCell = oldRow.getCell(j);
                    Cell newCell = newRow.createCell(j);

                    if (oldCell != null) {
                        switch (oldCell.getCellType()) {
                            case STRING:
                                newCell.setCellValue(oldCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                newCell.setCellValue(oldCell.getNumericCellValue());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        // Crear un estilo de celda para resaltar en rojo
        CellStyle redStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());  // Establecer el color de la fuente en rojo
        font.setBold(true); // Poner en negrita
        redStyle.setFont(font);

        // Agregar una fila extra con el total al final de la nueva hoja
        int lastRow = purchasesSheet.getLastRowNum() + 1; // La siguiente fila vacía
        Row totalRow = nuevaHoja.createRow(lastRow);
        Cell totalLabelCell = totalRow.createCell(1); // Columna 1 para la etiqueta
        totalLabelCell.setCellValue(REALIZO);

        Cell totalValueCell = totalRow.createCell(2); // Columna 2 para el valor total
        totalValueCell.setCellValue(totalCompra);

        // Aplicar el estilo de color rojo a la celda del total
        totalValueCell.setCellStyle(redStyle);
    }

    // Método para limpiar la hoja "Compras"
    private void limpiarHojaCompras(Sheet purchasesSheet) {
        for (int i = purchasesSheet.getLastRowNum(); i >= 1; i--) {
            Row row = purchasesSheet.getRow(i);
            if (row != null) {
                purchasesSheet.removeRow(row);
            }
        }
    }

    // Método para facturar y limpiar la hoja "Compras"
    public void facturarYLimpiar() {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);

            if (purchasesSheet != null) {
                // Sumar los totales
                double totalCompra = sumarTotalesCompras(purchasesSheet);

                // Copiar la hoja "Compras" y renombrarla, pasando el total de la compra
                copiarHojaCompras(workbook, purchasesSheet, totalCompra);

                // Limpiar la hoja "Compras"
                limpiarHojaCompras(purchasesSheet);

                // Guardar el archivo actualizado
                try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
                    workbook.write(fos);
                    guardarTotalFacturadoEnArchivo(totalCompra);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void guardarTotalFacturadoEnArchivo(double totalFacturado) {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String nombreArchivo = "Total_Facturado_" + fechaActual.format(formatter) + ".txt";

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write("Total facturado en el día: " + totalFacturado);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

