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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import static org.example.manager.VentaManager.abrirPDF;
import static org.example.manager.VentaManager.imprimirPDF;
import static org.example.utils.Constants.*;


public class ExcelManager {
    public static final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
    public static final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);

    public static final String DIRECTORY_PATH_FACTURACION = System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion"+ fechaFormateada+".xlsx";

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

        // Crear hoja de gastos
        Sheet gastosSheet = workbook.createSheet("Gastos");
        Row gastosHeader = gastosSheet.createRow(0);
        gastosHeader.createCell(0).setCellValue("ID Producto");
        gastosHeader.createCell(1).setCellValue("Nombre Producto");
        gastosHeader.createCell(2).setCellValue("Cantidad");
        gastosHeader.createCell(3).setCellValue("Precio Compra");
        gastosHeader.createCell(4).setCellValue("Fecha y Hora");

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
            Sheet gastosSheet = workbook.getSheet("Gastos"); // Asegúrate de que el nombre coincida

            if (purchasesSheet != null) {
                // Sumar los totales
                double totalCompra = sumarTotalesCompras(purchasesSheet);

                // Restar los totales de gastos
                double totalGastos = restarTotalesGastos(gastosSheet);
                double totalFinal = totalCompra - totalGastos; // Calcular el total final

                // Copiar la hoja "Compras" y renombrarla, pasando el total de la compra
                //copiarHojaCompras(workbook, purchasesSheet, totalFinal); // Cambiar totalCompra a totalFinal



                crearArchivoFacturacionYGastos(purchasesSheet, gastosSheet, totalCompra, totalGastos);
                generarResumenDiarioEstilizadoPDF();

                // Limpiar la hoja "Compras"
                limpiarHojaCompras(purchasesSheet);
                limpiarHojaCompras(gastosSheet);
                // Guardar el archivo actualizado
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toString())) {
                    workbook.write(fos);
                    guardarTotalFacturadoEnArchivo(totalFinal); // Cambiar totalCompra a totalFinal
                }


                // Borrar el contenido de la carpeta Facturas
                limpiarFacturas();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para restar los totales de la hoja "Gastos"
    public double restarTotalesGastos(Sheet gastosSheet) {
        double totalGastos = ZERO_DOUBLE; // Usar la constante como en el ejemplo

        // Iterar a través de las filas de la hoja de gastos
        for (int i = ONE; i <= gastosSheet.getLastRowNum(); i++) {
            Row row = gastosSheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(3); // La columna D es el índice 3
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    totalGastos += cell.getNumericCellValue();
                }
            }
        }
        return totalGastos;
    }


    // Método para limpiar la carpeta de facturas
    public void limpiarFacturas() {
        String rutaFacturas = System.getProperty("user.home") +"\\Calculadora del Administrador\\Facturas";
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
        String carpetaPath = System.getProperty("user.home") + "\\Calculadora del Administrador\\Realizo";
        File carpeta = new File(carpetaPath);

        // Crear la carpeta si no existe
        if (!carpeta.exists()) {
            boolean wasSuccessful = carpeta.mkdirs();
            if (!wasSuccessful) {
                System.err.println("No se pudo crear la carpeta 'Realizo'.");
                return; // Salir del método si no se puede crear la carpeta
            }
        }
        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String nombreArchivo = carpetaPath + "\\REALIZO_" + fechaActual.format(formatter) +EMPTY+ horaActual.format(horaFormatter)+".pdf";

        try {
            // Dimensiones del papel
            float anchoMm = 58;  // Ancho fijo de 58 mm para impresora POS-58
            float altoMm = 220;  // Puedes ajustar el alto según la longitud del recibo
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

            document.add(new Paragraph(new String(new char[33]).replace('\0', '='))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(10));

            // Detalles del total facturado
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalFacturado);
            document.add(new Paragraph("Realizo: $" + formattedPrice + " pesos")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            document.add(new Paragraph(new String(new char[33]).replace('\0', '='))
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
            document.add(new Paragraph(new String(new char[26]).replace('\0', '='))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginBottom(5));

            document.add(new Paragraph(new String(new char[26]).replace('\0', '='))
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
            //abrirPDF(nombreArchivo);
            imprimirPDF(nombreArchivo);// Método para abrir el PDF después de generarlo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para crear el archivo Excel independiente con las hojas "Facturacion" y "Gastos"
    public void crearArchivoFacturacionYGastos(Sheet purchasesSheet, Sheet gastosSheet, double totalCompra, double totalGastos) throws IOException {
        // Crear un nuevo Workbook (archivo Excel)
        Workbook workbook = new XSSFWorkbook();
        LocalDateTime fechaHoraFacturacion = LocalDateTime.now();
        DateTimeFormatter formatterFac = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
        String formatterFacturacion = fechaHoraFacturacion.format(formatterFac);
        // Crear la hoja "Facturacion"
        String facturacionHojaNombre = "Ventas_" + formatterFacturacion;
        Sheet facturacionSheet = workbook.createSheet(facturacionHojaNombre);

        // Copiar el contenido de la hoja "Compras" a la hoja "Facturacion"
        copiarContenidoHoja(purchasesSheet, facturacionSheet);

        // Crear un estilo de celda para resaltar en rojo
        CellStyle redStyle = crearEstiloRojo(workbook);

        // Agregar una fila extra con el total al final de la hoja "Facturacion"
        agregarTotal(facturacionSheet, totalCompra, "Total Compra", redStyle);
        LocalDateTime fechaHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
        String fechaFormateada = fechaHora.format(formatter);

        // Crear la hoja "Gastos"
        String gastosHojaNombre = "Gastos_" + fechaFormateada;
        Sheet gastosSheetNueva = workbook.createSheet(gastosHojaNombre);

        // Copiar el contenido de la hoja "Gastos" a la nueva hoja "Gastos"
        copiarContenidoHoja(gastosSheet, gastosSheetNueva);

        // Agregar una fila extra con el total al final de la hoja "Gastos"
        agregarTotal(gastosSheetNueva, totalGastos, "Total Gastos", redStyle);

        // Guardar el archivo Excel en el directorio especificado
        guardarArchivo(workbook);
    }

    // Método auxiliar para copiar el contenido de una hoja a otra
    private void copiarContenidoHoja(Sheet oldSheet, Sheet newSheet) {
        for (int i = 0; i <= oldSheet.getLastRowNum(); i++) {
            Row oldRow = oldSheet.getRow(i);
            Row newRow = newSheet.createRow(i);
            if (oldRow != null) {
                for (int j = 0; j < oldRow.getLastCellNum(); j++) {
                    Cell oldCell = oldRow.getCell(j);
                    Cell newCell = newRow.createCell(j);

                    if (oldCell != null) {
                        // Copiar el tipo de celda
                        switch (oldCell.getCellType()) {
                            case STRING:
                                newCell.setCellValue(oldCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                newCell.setCellValue(oldCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                newCell.setCellValue(oldCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                newCell.setCellFormula(oldCell.getCellFormula());
                                break;
                            case BLANK:
                                newCell.setBlank();
                                break;
                            // Otros tipos de celda según sea necesario
                            default:
                                break;
                        }

                        // Aplicar el estilo a la nueva celda
                        newCell.setCellStyle(crearEstiloParaCelda(oldCell, newSheet.getWorkbook()));
                    }
                }
            }
        }
    }

    // Método para crear un nuevo estilo de celda basado en el estilo de otra celda
    private CellStyle crearEstiloParaCelda(Cell oldCell, Workbook newWorkbook) {
        CellStyle newStyle = newWorkbook.createCellStyle();

        // Copia las propiedades que consideres necesarias
        newStyle.cloneStyleFrom(oldCell.getCellStyle());

        return newStyle;
    }

    // Método auxiliar para crear un estilo de celda en rojo
    private CellStyle crearEstiloRojo(Workbook workbook) {
        CellStyle redStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());  // Establecer el color de la fuente en rojo
        font.setBold(true); // Poner en negrita
        redStyle.setFont(font);
        return redStyle;
    }

    // Método auxiliar para agregar el total al final de la hoja
    private void agregarTotal(Sheet sheet, double total, String label, CellStyle style) {
        int lastRow = sheet.getLastRowNum() + 1; // La siguiente fila vacía
        Row totalRow = sheet.createRow(lastRow);
        Cell totalLabelCell = totalRow.createCell(0); // Columna 0 para la etiqueta
        totalLabelCell.setCellValue(label);

        Cell totalValueCell = totalRow.createCell(1); // Columna 1 para el valor total
        totalValueCell.setCellValue(total);

        // Aplicar el estilo de color rojo a la celda del total
        totalValueCell.setCellStyle(style);
    }

    // Método auxiliar para guardar el archivo en el directorio
    private void guardarArchivo(Workbook workbook) throws IOException {
        // Crear el directorio si no existe
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Crear el archivo con el nombre Facturacion.xlsx
        File file = new File(directory, FACTURACION_FILENAME);
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }

        // Cerrar el Workbook para liberar recursos
        workbook.close();
    }

    public void generarResumenDiarioEstilizadoPDF() {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Ruta del archivo
        String carpetaPath = System.getProperty("user.home") + "\\Calculadora del Administrador\\Resumen del día";
        File carpeta = new File(carpetaPath);

        // Crear la carpeta si no existe
        if (!carpeta.exists()) {
            boolean wasSuccessful = carpeta.mkdirs();
            if (!wasSuccessful) {
                System.err.println("No se pudo crear la carpeta 'Resumen del día'.");
                return; // Salir si no se puede crear la carpeta
            }
        }

        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String nombreArchivo = carpetaPath + "\\Resumen_Diario_" + fechaActual.format(formatter) + "_" + horaActual.format(horaFormatter) + ".pdf";

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Obtener las hojas de compras, gastos y productos
            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            Sheet gastosSheet = workbook.getSheet("Gastos");
            Sheet productsSheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            // Calcular totales
            double totalVentas = sumarTotalesCompras(purchasesSheet);
            double totalGastos = restarTotalesGastos(gastosSheet);
            List<Producto> productosAgotados = obtenerProductosAgotados(productsSheet);
            int totalProductos = productsSheet.getLastRowNum();  // Total de productos
            int productosAgotadosCount = productosAgotados.size();  // Total de productos con cantidad 0

            // Calcular porcentaje de productos agotados
            double porcentajeAgotados = ((double) productosAgotadosCount / totalProductos) * 100;

            // Crear el PDF con tamaño de tarjeta
            float anchoMm = 85;  // Ancho de tarjeta (en mm)
            float altoMm = 120;  // Alto de tarjeta (en mm)
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoMm * 2.83465f;

            PageSize cardSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, cardSize);

            // Fuentes y estilos
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // Márgenes ajustados
            document.setMargins(5, 5, 5, 5);
            LocalTime horaResumen = LocalTime.now();
            DateTimeFormatter horaResumenFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            // Encabezado del PDF
            document.add(new Paragraph("Resumen Diario "+ fechaActual.format(formatter) + " " + horaResumen.format(horaResumenFormatter))
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            // Mostrar el total de ventas
            document.add(new Paragraph("Total en VENTAS: $" + formatearMoneda(totalVentas)+ " pesos")
                    .setFont(fontBold)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            // Mostrar el total de gastos
            document.add(new Paragraph("Total en GASTOS: $" + formatearMoneda(totalGastos) + " pesos")
                    .setFont(fontBold)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            // Mostrar la lista de todos los gastos
            document.add(new Paragraph("Detalle de GASTOS:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            // Recorrer la hoja de gastos y listar cada uno
            for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                Row row = gastosSheet.getRow(i);
                if (row != null) {
                    String producto = row.getCell(1).getStringCellValue();  // Nombre del producto
                    double precioGasto = row.getCell(3).getNumericCellValue();  // Precio de gasto

                    document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioGasto) + " pesos")
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }

            // Calcular el total de ventas menos gastos
            double total = totalVentas - totalGastos;

            String textoTotal = (total < 0)
                    ? "REALIZO DEL SISTEMA: -$" + formatearMoneda(Math.abs(total)) + " pesos"
                    : "REALIZO DEL SISTEMA: $" + formatearMoneda(total) + " pesos";

            // Añadir el texto al documento PDF
            document.add(new Paragraph(textoTotal)
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            // Mostrar estadísticas de productos agotados
            document.add(new Paragraph("Productos Agotados: " + productosAgotadosCount + " productos (" + String.format("%.2f", porcentajeAgotados) + "%) del inventario")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            // Mostrar lista de productos agotados
            if (productosAgotados.isEmpty()) {
                document.add(new Paragraph("No hay productos agotados.")
                        .setFont(fontItalic)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(5));
            } else {
                for (Producto producto : productosAgotados) {
                    document.add(new Paragraph("- " + producto.getName())
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }

            // Línea divisoria
            document.add(new Paragraph(new String(new char[33]).replace('\0', '-'))
                    .setFont(fontNormal)
                    .setFontSize(6)
                    .setMarginTop(10)
                    .setTextAlignment(TextAlignment.CENTER));

            // Pie de página
            document.add(new Paragraph("Sistema Licorera CR La 70")
                    .setFont(fontBold)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5));

            // Cerrar el documento
            document.close();
            System.out.println("Archivo PDF de resumen creado: " + nombreArchivo);
            //abrirPDF(nombreArchivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para obtener productos con cantidad 0
    private List<Producto> obtenerProductosAgotados(Sheet productsSheet) {
        List<Producto> productosAgotados = new ArrayList<>();
        for (int i = ONE; i <= productsSheet.getLastRowNum(); i++) {
            Row row = productsSheet.getRow(i);
            if (row != null) {
                String name = row.getCell(ONE).getStringCellValue();
                int quantity = (int) row.getCell(TWO).getNumericCellValue();

                if (quantity == 0) {
                    productosAgotados.add(new Producto(name, quantity, row.getCell(THREE).getNumericCellValue()));
                }
            }
        }
        return productosAgotados;
    }

    // Método auxiliar para formatear el valor de la moneda
    private String formatearMoneda(double valor) {
        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
        return formatCOP.format(valor);
    }

}

