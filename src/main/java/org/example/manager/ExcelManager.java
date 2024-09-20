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
        Row header = productsSheet.createRow(ZERO);
        header.createCell(ZERO).setCellValue(ID);
        header.createCell(ONE).setCellValue(NOMBRE);
        header.createCell(TWO).setCellValue(CANTIDAD);
        header.createCell(THREE).setCellValue(PRECIO);

        // Crear hoja de compras
        Sheet purchasesSheet = workbook.createSheet(PURCHASES_SHEET_NAME);
        Row purchasesHeader = purchasesSheet.createRow(ZERO);
        purchasesHeader.createCell(ZERO).setCellValue(ID);
        purchasesHeader.createCell(ONE).setCellValue(PRODUCTOS);
        purchasesHeader.createCell(TWO).setCellValue(TOTAL);
        purchasesHeader.createCell(THREE).setCellValue(FECHA_HORA);

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
            int lastRow = sheet.getLastRowNum() + ONE;

            Row row = sheet.createRow(lastRow);
            row.createCell(ZERO).setCellValue(product.getId());
            row.createCell(ONE).setCellValue(product.getName());
            row.createCell(TWO).setCellValue(product.getQuantity());
            row.createCell(THREE).setCellValue(product.getPrice());

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
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int id = (int) row.getCell(ZERO).getNumericCellValue();
                    String name = row.getCell(ONE).getStringCellValue();
                    int quantity = (int) row.getCell(TWO).getNumericCellValue();
                    double price = row.getCell(THREE).getNumericCellValue();

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaFormateada = now.format(formatter);
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.createSheet(PURCHASES_SHEET_NAME);
                Row header = sheet.createRow(ZERO);
                header.createCell(ZERO).setCellValue(ID);
                header.createCell(ONE).setCellValue(PRODUCTOS);
                header.createCell(TWO).setCellValue(TOTAL);
                header.createCell(THREE).setCellValue(FECHA_HORA);
            }
            int lastRow = sheet.getLastRowNum() + ONE;
            Row row = sheet.createRow(lastRow);

            row.createCell(ZERO).setCellValue(compraID);
            row.createCell(ONE).setCellValue(productos);  // Los productos se listan en líneas nuevas dentro de la misma celda
            row.createCell(TWO).setCellValue(total);
            row.createCell(THREE).setCellValue(fechaFormateada);

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
        return ZERO;
    }
    // Método para sumar los totales de la hoja "Compras"
    private double sumarTotalesCompras(Sheet purchasesSheet) {
        double totalSum = ZERO_DOUBLE;
        for (int i = ONE; i <= purchasesSheet.getLastRowNum(); i++) {
            Row row = purchasesSheet.getRow(i);
            if (row != null && row.getCell(TWO) != null) {
                totalSum += row.getCell(TWO).getNumericCellValue();
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
        for (int i = ZERO; i <= purchasesSheet.getLastRowNum(); i++) {
            Row oldRow = purchasesSheet.getRow(i);
            Row newRow = nuevaHoja.createRow(i);
            if (oldRow != null) {
                for (int j = ZERO; j < oldRow.getLastCellNum(); j++) {
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
        int lastRow = purchasesSheet.getLastRowNum() + ONE; // La siguiente fila vacía
        Row totalRow = nuevaHoja.createRow(lastRow);
        Cell totalLabelCell = totalRow.createCell(ONE); // Columna 1 para la etiqueta
        totalLabelCell.setCellValue(REALIZO);

        Cell totalValueCell = totalRow.createCell(TWO); // Columna 2 para el valor total
        totalValueCell.setCellValue(totalCompra);

        // Aplicar el estilo de color rojo a la celda del total
        totalValueCell.setCellStyle(redStyle);
    }

    // Método para limpiar la hoja "Compras"
    private void limpiarHojaCompras(Sheet purchasesSheet) {
        for (int i = purchasesSheet.getLastRowNum(); i >= ONE; i--) {
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


    //CAMBIAR a PDF
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

