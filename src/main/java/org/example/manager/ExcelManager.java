package org.example.manager;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Producto;

import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.example.manager.VentaManager.abrirPDF;
import static org.example.utils.Constants.*;


public class ExcelManager {
    public static final String FILE_NAME = "productos.xlsx";
    public static final String DIRECTORY_PATH = "C:\\Users\\DesktopPC\\Documentos\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

    public ExcelManager() {
        // Verificar si la carpeta existe, si no, crearla
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs(); // Crear la carpeta y las subcarpetas necesarias
        }

        // Verificar si el archivo existe
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();
        }
    }

    // Método para crear el archivo Excel si no existe
    private void createExcelFile() {
        Workbook workbook = new XSSFWorkbook();

        // Crear hoja de productos
        Sheet productsSheet = workbook.createSheet(PRODUCTS_SHEET_NAME);
        Row header = productsSheet.createRow(0); // Utiliza constantes para los índices si las tienes
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

        // Guarda el archivo en la ruta especificada
        try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
            workbook.write(fileOut);
            System.out.println("Archivo Excel creado: " + FILE_PATH.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close(); // Cierra el workbook para liberar recursos
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
                    int id = (int) row.getCell(ZERO).getNumericCellValue();
                    String name = row.getCell(ONE).getStringCellValue();
                    int quantity = (int) row.getCell(TWO).getNumericCellValue();
                    double price = row.getCell(THREE).getNumericCellValue();

                    products.add(new Producto(name,/* quantity,*/ price));
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
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
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

            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
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
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);

            if (purchasesSheet != null) {
                // Sumar los totales
                double totalCompra = sumarTotalesCompras(purchasesSheet);

                // Copiar la hoja "Compras" y renombrarla, pasando el total de la compra
                copiarHojaCompras(workbook, purchasesSheet, totalCompra);

                // Limpiar la hoja "Compras"
                limpiarHojaCompras(purchasesSheet);

                // Borrar el contenido de la carpeta Facturas
                limpiarFacturas();

                // Guardar el archivo actualizado
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toString())) {
                    workbook.write(fos);
                    guardarTotalFacturadoEnArchivo(totalCompra);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para limpiar la carpeta de facturas
    public void limpiarFacturas() {
        String rutaFacturas = "C:\\Users\\DesktopPC\\documentos\\Calculadora del Administrador\\Facturas";
        borrarContenidoCarpeta(rutaFacturas);
    }

    // Método para borrar el contenido de la carpeta
    private void borrarContenidoCarpeta(String carpetaPath) {
        File carpeta = new File(carpetaPath);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] elementos = carpeta.listFiles();
            if (elementos != null) {
                for (File elemento : elementos) {
                    if (elemento.isDirectory()) {
                        borrarContenidoCarpeta(elemento.getPath()); // Llamada recursiva para eliminar el contenido del subdirectorio
                    }
                    boolean deleted = elemento.delete(); // Borrar el archivo o directorio
                    if (!deleted) {
                        System.out.println("No se pudo eliminar: " + elemento.getPath());
                    }
                }
            }
        } else {
            System.out.println("La carpeta no existe o no es un directorio: " + carpetaPath);
        }
    }


    public void guardarTotalFacturadoEnArchivo(double totalFacturado) {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Ruta del archivo
        String carpetaPath = "C:\\Users\\DesktopPC\\documentos\\Calculadora del Administrador\\Realizo";
        File carpeta = new File(carpetaPath);

        // Crear la carpeta si no existe
        if (!carpeta.exists()) {
            boolean wasSuccessful = carpeta.mkdirs();
            if (!wasSuccessful) {
                System.err.println("No se pudo crear la carpeta 'Realizo'.");
                return; // Salir del método si no se puede crear la carpeta
            }
        }

        String nombreArchivo = carpetaPath + "\\REALIZO_" + fechaActual.format(formatter) + ".pdf";

        try {
            // Dimensiones del papel
            float anchoMm = 80;
            float altoMm = 150;
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoMm * 2.83465f;

            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Márgenes ajustados
            document.setMargins(5, 5, 5, 5);

            // Encabezado del PDF
            document.add(new Paragraph("Total Generado Durante el Día")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            document.add(new Paragraph("Fecha: " + fechaActual.format(formatter))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(new String(new char[45]).replace('\0', '='))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(10));

            // Detalles del total facturado
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalFacturado);
            document.add(new Paragraph("Realizo Sistema: $" + formattedPrice + " pesos")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            document.add(new Paragraph(new String(new char[45]).replace('\0', '='))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(10));

            // Sección para el cierre de caja
            document.add(new Paragraph("Cierre de Caja")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(2));

            document.add(new Paragraph("Realiza el cierre de caja en el siguiente espacio:")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            // Espacios para el cierre de caja
            document.add(new Paragraph("=====================================")
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginBottom(5));

            document.add(new Paragraph("=====================================")
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginTop(160)
                    .setMarginBottom(5));

            // Agradecimiento o información adicional
            document.add(new Paragraph("Sistema Licorera CR La 70")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            abrirPDF(nombreArchivo); // Método para abrir el PDF después de generarlo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

