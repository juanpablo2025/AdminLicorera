package org.example.manager.adminManager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    public static final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);

    public static final String DIRECTORY_PATH_FACTURACION = System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion" + fechaFormateada + ".xlsx";


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
                    String foto = row.getCell(FIVE).getStringCellValue();

                    products.add(new Producto(name, quantity, price,foto));
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
                        String tipoPago = row.getCell(4).getStringCellValue();

                        Factura factura = new Factura(id, productos, total, fechaHora, tipoPago);
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


    public boolean eliminarFacturaYActualizarProductos(String facturaId) {
        try {
            // Carga el archivo Excel y accede a las hojas "Ventas" y "Productos"
            FileInputStream file = new FileInputStream(FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet ventasSheet = workbook.getSheet("Ventas");
            Sheet productosSheet = workbook.getSheet("Productos");

            boolean facturaEliminada = false;

            // Busca la factura en la hoja "Ventas" por su Id
            for (int i = 1; i <= ventasSheet.getLastRowNum(); i++) { // Empieza en 1 para evitar encabezados
                Row row = ventasSheet.getRow(i);
                if (row == null) continue;

                Cell idCell = row.getCell(0); // Columna "Id"
                if (idCell != null && idCell.getStringCellValue().equals(facturaId)) {
                    // Extrae los productos de la factura en la columna "Productos"
                    Cell productosCell = row.getCell(1);
                    if (productosCell != null) {
                        String productosStr = productosCell.getStringCellValue();
                        String[] productos = productosStr.split(" "); // Separa por espacio

                        // Variables temporales para almacenar producto y cantidad
                        String nombreProducto = null;
                        int cantidad = 0;

                        // Itera sobre cada segmento del string de productos
                        for (String part : productos) {
                            if (part.startsWith("x")) { // Si comienza con 'x', es la cantidad
                                cantidad = Integer.parseInt(part.substring(1).trim()); // Convierte "x1" a 1

                                // Sumar la cantidad eliminada a la hoja "Productos"
                                boolean productoActualizado = false;
                                for (int j = 1; j <= productosSheet.getLastRowNum(); j++) {
                                    Row productoRow = productosSheet.getRow(j);
                                    if (productoRow == null) continue;

                                    // Verificar y limpiar espacios en blanco del nombre del producto
                                    Cell nombreCell = productoRow.getCell(1); // Columna "Nombre"
                                    if (nombreCell != null) {
                                        String nombreEnProductos = nombreCell.getStringCellValue().trim();
                                        if (nombreEnProductos.equals(nombreProducto)) {
                                            // Obtener la cantidad actual y sumarle la cantidad eliminada
                                            Cell cantidadCell = productoRow.getCell(2); // Columna "Cantidad"
                                            int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                            cantidadCell.setCellValue(cantidadActual + cantidad);
                                            productoActualizado = true;
                                            break;
                                        }
                                    }
                                }

                                // Si el producto no se encuentra en la hoja "Productos", mostrar un mensaje detallado
                                if (!productoActualizado) {
                                    System.out.println("Producto no encontrado en hoja 'Productos': " + nombreProducto);
                                }

                                // Reiniciar para el siguiente producto
                                nombreProducto = null;
                                cantidad = 0;

                            } else if (!part.startsWith("$") && !part.equals("=")) {
                                // Asume que este segmento es el nombre del producto si no es un símbolo de precio o total
                                nombreProducto = part.trim(); // Limpiar espacios en blanco en el nombre del producto
                            }
                        }
                    }

                    // Eliminar la fila de la factura en la hoja "Ventas"
                    ventasSheet.removeRow(row);
                    if (i < ventasSheet.getLastRowNum()) {
                        ventasSheet.shiftRows(i + 1, ventasSheet.getLastRowNum(), -1);
                    }

                    facturaEliminada = true;
                    break;
                }
            }

            // Guarda los cambios en el archivo Excel
            file.close();
            FileOutputStream outputFile = new FileOutputStream(FILE_PATH);
            workbook.write(outputFile);
            outputFile.close();
            workbook.close();

            return facturaEliminada;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateProducts(List<Producto> existingProducts) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            int lastRowNum = sheet.getLastRowNum(); // Última fila con datos

            for (Producto producto : existingProducts) {
                boolean exists = false;

                // Verifica si el producto ya existe en la hoja
                for (int i = 1; i <= lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String name = row.getCell(ONE).getStringCellValue();

                        if (name.equalsIgnoreCase(producto.getName())) {
                            // Producto encontrado, actualizar cantidad y precio
                            row.getCell(TWO).setCellValue(producto.getQuantity());
                            row.getCell(THREE).setCellValue(producto.getPrice());
                            exists = true;
                            break;
                        }
                    }
                }

                // Si el producto no existe, lo añadimos en una nueva fila
                if (!exists) {
                    Row newRow = sheet.createRow(++lastRowNum);
                    newRow.createCell(ONE).setCellValue(producto.getName());
                    newRow.createCell(TWO).setCellValue(producto.getQuantity());
                    newRow.createCell(THREE).setCellValue(producto.getPrice());
                    newRow.createCell(FIVE).setCellValue(producto.getFoto());
                }
            }

            // Guardar cambios
            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
                workbook.write(fileOut);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
