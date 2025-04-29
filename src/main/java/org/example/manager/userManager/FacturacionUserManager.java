package org.example.manager.userManager;



import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.manager.adminManager.ConfigAdminManager;
import org.example.model.Factura;
import org.example.model.Producto;
import org.example.utils.FormatterHelpers;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.manager.userManager.PrintUserManager.abrirPDF;
import static org.example.manager.userManager.PrintUserManager.imprimirPDF;
import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class FacturacionUserManager {

    private static ExcelUserManager excelUserManager = new ExcelUserManager(); // Inicializar directamente

    public static void setExcelUserManager(ExcelUserManager manager) {
        excelUserManager = manager;
    }

    /**
     * Método para verificar si el usuario ha ingresado la palabra correcta para facturar.
     *
     * @param input Texto ingresado por el usuario.
     * @return true si la palabra es "Facturar", false en caso contrario.
     */
    public static boolean verificarFacturacion(String input) {
        return "Facturar".equals(input);
    }

    /**
     * Realiza la facturación y limpieza de los datos.
     * Luego, termina la ejecución del programa.
     */
    public static void facturarYSalir() throws IOException, InterruptedException {
        // Verifica si hay ventas registradas antes de continuar
        List<Factura> facturas = ExcelUserManager.getFacturas();  // Obtiene todas las facturas

        if (facturas.isEmpty()) {
            // Si no hay ventas, muestra un mensaje y no termina el día
            JOptionPane.showMessageDialog(null, "No se pueden cerrar las ventas, ya que no hay ventas registradas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;  // Salir del método sin proceder
        }

        // Si hay ventas, procede con la facturación
        excelUserManager.facturarYLimpiar();

        //TODO: validar que no este ocupada
        eliminarMesasConIdMayorA15();
        //enviarMensaje("+573226094632", "Se ha realizado la facturación del día de hoy, por favor verifica el archivo en la carpeta de 'Facturas'.");
        System.exit(ZERO);
        // Salir del programa después de la facturación
    }

    /**
     * Muestra un mensaje de error si la palabra ingresada es incorrecta.
     */
    public static void mostrarErrorFacturacion() {
        javax.swing.JOptionPane.showMessageDialog(null, ERROR_MENU, ERROR_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
    }



    public static void generarFacturadeCompra(String ventaID, List<String> productos, double totalCompra, LocalDateTime fechaHora, String tipoPago) {
        try {
            InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            // Leer la fuente desde el InputStream
            byte[] fontBytes = fontStream.readAllBytes();
            PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);
            String paperSize = ConfigAdminManager.getPaperSize();
            String outputType = ConfigAdminManager.getOutputType();

            // Definir el ancho del papel basado en la configuración
            float anchoMm = paperSize.equals("48mm") ? 48 : (paperSize.equals("A4") ? 210 : 80);
            float anchoPuntos = anchoMm * WIDE_DOTS;  // Conversión de mm a puntos

            // Definir el alto dinámico según el número de productos
            float altoBaseMm = 130;   // Base mínima de la factura
            float altoPorProductoMm = 10;  // Espacio por cada producto
            float extraSpaceMm = 50;   // Espacio extra para el total y otras secciones
            float altoMinimoMm = 150;  // Mínimo para evitar recortes

// Calcular la altura total garantizando un mínimo suficiente
            float altoTotalMm = Math.max(altoBaseMm + (productos.size() * altoPorProductoMm) + extraSpaceMm, altoMinimoMm);
            float altoPuntos = altoTotalMm * HEIGHT_DOTS; // Conversión a puntos

// Definir el tamaño de la página con el alto corregido
            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);



            String nombreArchivo = System.getProperty("user.home") + "\\Calculadora del Administrador\\Facturas\\" + BILL_FILE + ventaID + PDF_FORMAT;
            File pdfFile = new File(nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Ajustar margen izquierdo a 10mm (1cm)
            float margenIzquierdo = 10 * HEIGHT_DOTS;
            document.setMargins(FIVE, FIVE, FIVE, margenIzquierdo);


            /*/ Encabezado de la factura
            document.add(new Paragraph(BILL_TITLE)
                    .setFont(fontBold)
                    .setFontSize(TWELVE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(FIVE));*/

            document.add(new Paragraph("Licorera CR")
                    .setFont(lobsterFont)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(-2)); // Margen inferior ajustado a 0

            document.add(new Paragraph("La 70")
                    .setFont(lobsterFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(-5)
                    .setMarginBottom(-2));// Margen inferior ajustado a 0

            document.add(new Paragraph(NIT)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(-2)); // Margen inferior ajustado a 0

            document.add(new Paragraph(DIRECCION)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(-2)); // Margen inferior ajustado a 0

            document.add(new Paragraph(TELEFONO)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(-2)); // Margen inferior ajustado a 0

            document.add(new Paragraph(new String(new char[22]).replace(SLASH_ZERO, "_"))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            // Detalles de la compra
            document.add(new Paragraph(BILL_ID +"N°"+ ventaID)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(fechaFormateada)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setMarginBottom(1)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(tipoPago)
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setBold()
                    .setMarginBottom(5)
                    .setTextAlignment(TextAlignment.CENTER));

          /*  document.add(new Paragraph(BILL_PRODUCTS)
                    .setFont(fontBold)
                    .setFontSize(8));*/

            // Crear un formateador de moneda para Colombia
            NumberFormat formatoColombiano = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

            // Agregar productos con formato de moneda colombiano
            for (String producto : productos) {
                // Suponiendo que el formato de cada producto es "nombreProducto xCantidad $precioUnitario"
                String[] detallesProducto = producto.split(" ");
                if (detallesProducto.length >= 3) {
                    // Extraer el nombre del producto y el precio unitario
                    String nombreProducto = detallesProducto[0];
                    String cantidadStr = detallesProducto[1]; // Ejemplo: "x2"
                    String precioStr = detallesProducto[2];   // Ejemplo: "$1000.0"

                    // Convertir el precio a double (sin el símbolo "$")
                    double precioUnitario = Double.parseDouble(precioStr.substring(1));

                    // Formatear el precio en el formato de moneda colombiano (COP)
                    NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                    String formattedPrice = formatCOP.format(precioUnitario);
                    // Crear el texto del producto con el formato de moneda colombiano
                    String productoConPrecioFormateado = nombreProducto + " " + cantidadStr + " " + formattedPrice;

                    // Agregar el producto al documento con el nuevo formato de precio
                    document.add(new Paragraph(productoConPrecioFormateado)
                            .setFont(fontNormal)
                            .setFontSize(6));
                }}

            /*document.add(new Paragraph(new String(new char[30]).replace(SLASH_ZERO, "_"))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));*/

            /* Totales
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(new float[]{THREE, TWO});
            table.setWidth(UnitValue.createPercentValue(ONE_HUNDRED));
*/
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalCompra);
           // addTableRow(table, TOTAL_BILL, PESO_SIGN + formattedPrice + PESOS);


            //document.add(table);


            document.add(new Paragraph(TOTAL_BILL + PESO_SIGN + formattedPrice + PESOS)
                    .setFont(fontNormal) // Puedes ajustar la fuente
                    .setFontSize(9)    // Puedes ajustar el tamaño de la fuente
                    //.setTextAlignment(TextAlignment.ALIGN_RIGHT) // O la alineación que prefieras
                    .setMarginBottom(2)
                    .setBold()); // O el margen que necesites

            document.add(new Paragraph(new String(new char[22]).replace(SLASH_ZERO, "_"))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            document.add(new Paragraph(THANKS_BILL)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("IVA incluido.")
                    .setFont(fontNormal)
                    .setFontSize(5)
                    .setTextAlignment(TextAlignment.CENTER));

            // Cerrar el documento
            document.close();

            // Método para abrir el PDF después de generarlo
            //abrirPDF(nombreArchivo);
            //imprimirPDF(nombreArchivo);// Método para abrir el PDF después de generarlo

            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void addTableRow(com.itextpdf.layout.element.Table table, String key, String value) {
        table.addCell(new Paragraph(key).setFontSize(EIGHT));
        table.addCell(new Paragraph(value).setFontSize(EIGHT));
        table.setBorder(Border.NO_BORDER);
    }


    public static void generarResumenDiarioEstilizadoPDF() {
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
            Sheet empleadosSheet = workbook.getSheet("Empleados");  // Hoja de empleados

            // Obtener las hojas de compras, gastos y productos

            Sheet reabastecimientoSheet = workbook.getSheet("reabastecimiento");
            // Calcular totales
            double totalVentas = sumarTotalesCompras(purchasesSheet);
            double totalGastos = restarTotalesGastos(gastosSheet);
            List<Producto> productosAgotados = obtenerProductosAgotados(productsSheet);
            int totalProductos = productsSheet.getLastRowNum();  // Total de productos
            int productosAgotadosCount = productosAgotados.size();  // Total de productos con cantidad 0

            // Calcular porcentaje de productos agotados
            double porcentajeAgotados = ((double) productosAgotadosCount / totalProductos) * 100;


            // Obtener configuración desde config.properties
            String paperSize = ConfigAdminManager.getPaperSize();
            //String outputType = ConfigAdminManager.getOutputType();
            //String printerName = ConfigAdminManager.getPrinterName();

            // Definir el ancho del papel basado en la configuración
            float anchoMm = paperSize.equals("58mm") ? 58 : (paperSize.equals("A4") ? 210 : 80);


            // Crear el PDF con tamaño de tarjeta
            //float anchoMm = 100;  // Ancho de tarjeta (en mm)
            float altoMm = 200;  // Alto de tarjeta (en mm)
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
            document.add(new Paragraph("Resumen Diario \n" + fechaActual.format(formatter) + " " + horaResumen.format(horaResumenFormatter))
                    .setFont(fontBold)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            String textoTotal = "REALIZO DEL SISTEMA: $" + formatearMoneda(totalVentas) + " pesos";

            // Añadir el texto al documento PDF
            document.add(new Paragraph(textoTotal)
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            /* Mostrar el total de ventas
            document.add(new Paragraph("Total en VENTAS: $" + formatearMoneda(totalVentas)+ " pesos")
                    .setFont(fontBold)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));*/

            // Mostrar el total de gastos
            document.add(new Paragraph("GASTOS: $" + formatearMoneda(totalGastos) + " pesos")
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
                    String precioTexto = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                    double precioGasto = 0;
                    try {
                        precioGasto = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                    } catch (NumberFormatException e) {
                        // Si el valor no es numérico (por ejemplo, "N/A"), simplemente lo dejas en 0 o lo omites
                    }  // Precio de gasto

                    document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioGasto) + " pesos")
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }

            // Mostrar la lista de todos los gastos
            document.add(new Paragraph("Detalles de Reabastecimiento:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            // Recorrer la hoja de gastos y listar cada uno
            for (int i = 1; i <= reabastecimientoSheet.getLastRowNum(); i++) {
                Row row = reabastecimientoSheet.getRow(i);
                if (row != null) {
                    String producto = row.getCell(1).getStringCellValue();  // Nombre del producto
                    String precioTexto = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                    double precioreabastecimiento = 0;
                    try {
                        precioreabastecimiento = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                    } catch (NumberFormatException e) {
                        // Si el valor no es numérico (por ejemplo, "N/A"), simplemente lo dejas en 0 o lo omites
                    }  // Precio de gasto

                    document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioreabastecimiento) + " pesos")
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }

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


            // Sección de Empleados y Horas de Inicio
            document.add(new Paragraph("\nEmpleados y Hora de apertura:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            // Recorrer la hoja de empleados y listar cada uno con su hora de inicio
            for (int i = 1; i <= empleadosSheet.getLastRowNum(); i++) {
                Row row = empleadosSheet.getRow(i);
                if (row != null) {
                    String nombreEmpleado = row.getCell(0).getStringCellValue();  // Asumiendo que el nombre está en la primera columna
                    String horaInicio = row.getCell(1).getStringCellValue();
                    String fechaInicio = row.getCell(2).getStringCellValue();      // Asumiendo que la hora de inicio está en la segunda columna
                    document.add(new Paragraph("- " + nombreEmpleado + ": " + horaInicio + " - " + fechaInicio)
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }
            // Sección de Empleados y Horas de Inicio
            document.add(new Paragraph("\nCierre y facturación:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));
            LocalDateTime horasActual = LocalDateTime.now();
            document.add(new Paragraph(horasActual.format(formatter) + " " + horaResumen.format(horaResumenFormatter))
                    .setFont(fontNormal)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.LEFT));

            // Línea divisoria
            document.add(new Paragraph(new String(new char[30]).replace('\0', '-'))
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
           // System.out.println("Archivo PDF de resumen creado: " + nombreArchivo);
            FormatterHelpers.formatearMoneda(totalVentas);


            // Agrega justo antes de construir el mensaje de WhatsApp
            DataFormatter formatteo = new DataFormatter();
            StringBuilder gastosNA = new StringBuilder();
            try (FileInputStream fisGastos = new FileInputStream(FILE_PATH);
                 Workbook wbGastos = WorkbookFactory.create(fisGastos)) {
                 wbGastos.getSheet("Gastos");
                if (gastosSheet != null) {
                    for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                        Row row = gastosSheet.getRow(i);
                        if (row != null) {
                            String cantidad = formatteo.formatCellValue(row.getCell(2)).trim().toLowerCase();
                            if ("n/a".equals(cantidad)) {
                                String producto = formatteo.formatCellValue(row.getCell(1));
                                String valor = formatteo.formatCellValue(row.getCell(3));
                                gastosNA.append("\n- ").append(producto).append(": $").append(valor).append(" pesos");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String numeroDestino = "+573112599560";  //"+573146704316" Número al que quieres enviar el mensaje
            String mensaje = "*[Licorera CR]* ¡Hola! se ha generado la liquidación del día de hoy por un total de: $ "
                    + FormatterHelpers.formatearMoneda(totalVentas) + " pesos.\nPuedes consultar los detalles en los resúmenes adjuntos en Google Drive: https://drive.google.com/drive/folders/1-mklq_6xIUVZz8osGDrBtvYXEu-RNGYH";
            if (!gastosNA.isEmpty()) {
                mensaje += "\n\n*Gastos del d\u00eda:*" + gastosNA;
            }
           enviarMensaje(numeroDestino,mensaje);

        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void guardarTotalFacturadoEnArchivo( Map<String,Double>totalesPorPago,double totalFacturado) throws IOException {


        Map<String, Integer> productosVendidos = obtenerProductosVendidos();

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
        String nombreArchivo = carpetaPath + "\\REALIZO_" + fechaActual.format(formatter) + EMPTY + horaActual.format(horaFormatter) + ".pdf";

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet gastosSheet = workbook.getSheet("Gastos");
           // System.out.println("GastosSheet tiene " + gastosSheet.getLastRowNum() + " filas");
            for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                Row row = gastosSheet.getRow(i);
                if (row != null) {
                    String raw = new DataFormatter().formatCellValue(row.getCell(3));
                  //  System.out.println("Fila " + i + " valor gasto: " + raw);
                }
            }
            double totalGastos = restarTotalesGastos(gastosSheet);
            float anchoMm = 48;
            float altoBaseMm = 250;
            float altoPorProductoMm = 12;
            float altoFooterMm = 40;
            float altoTotalMm = altoBaseMm + (productosVendidos.size() * altoPorProductoMm) + altoFooterMm;
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoTotalMm * 2.83465f;

            // Crear el PDF con la altura ajustada dinámicamente
            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            // Leer la fuente desde el InputStream
            byte[] fontBytes = fontStream.readAllBytes();
            PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            // Ajustar margen izquierdo a 6mm (1cm equivalente)
            float margenIzquierdo = 10 * HEIGHT_DOTS;
            document.setMargins(FIVE, FIVE, FIVE, margenIzquierdo);


            // Encabezado del PDF
            document.add(new Paragraph("Realizo del Día")
                    .setFont(lobsterFont)
                    .setFontSize(15)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(0));

            LocalTime horaNueva = LocalTime.now();
            DateTimeFormatter horaFormateada= DateTimeFormatter.ofPattern("HH:mm:ss");
            document.add(new Paragraph(fechaActual.format(formatter) + "\n" + horaNueva.format(horaFormateada))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(-1)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(new String(new char[22]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(2));

            // Detalles del total facturado
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalFacturado);
            document.add(new Paragraph("Total $ " + formattedPrice)
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));




            if (totalesPorPago != null && !totalesPorPago.isEmpty()) {
                for (Map.Entry<String, Double> entry : totalesPorPago.entrySet()) {
                    String metodoPago = entry.getKey().replace(" - Transferencia", "");
                    double total = entry.getValue();

                    // Formateo del total en pesos colombianos
                    String totalFormateado = formatCOP.format(total);

                    Paragraph pagoParrafo = new Paragraph()
                            .add(new Text(metodoPago + ": ").setFont(fontNormal).setBold())
                            .add(new Text("$ " + totalFormateado).setFont(fontNormal))
                            .setFontSize(8)
                            .setMarginBottom(2);

                    document.add(pagoParrafo);
                }

            } else {
                document.add(new Paragraph("No hay datos de pagos registrados.")
                        .setFont(fontNormal)
                        .setFontSize(8)
                        .setMarginBottom(2));
            }



            // Espacios para el cierre de caja
            document.add(new Paragraph(new String(new char[18]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginBottom(5));

            if (totalGastos > 0) {
                document.add(new Paragraph("Gastos $ " + formatearMoneda(totalGastos))
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(0));

                document.add(new Paragraph("Detalles")
                        .setFont(fontBold)
                        .setFontSize(7)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(0));

                for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                    Row row = gastosSheet.getRow(i);
                    if (row != null) {
                        String producto = row.getCell(1).getStringCellValue();
                        String precioTexto = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                        double precioGasto = 0;
                        try {
                            precioGasto = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                        } catch (NumberFormatException e) {
                            continue; // Omitir si no es numérico
                        }

                        document.add(new Paragraph(producto.toUpperCase() + " $ " + formatearMoneda(precioGasto))
                                .setFont(fontNormal)
                                .setFontSize(6)
                                .setTextAlignment(TextAlignment.LEFT));
                    }
                }
            }
            document.add(new Paragraph("Vendidos")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setMarginBottom(5));

            if (productosVendidos != null && !productosVendidos.isEmpty()) {
                for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                    String producto = entry.getKey();
                    int cantidad = entry.getValue();

                    document.add(new Paragraph( producto + " x"+ cantidad)
                            .setFont(fontNormal)
                            .setFontSize(6)
                            .setMarginBottom(0));
                }
            } else {
                document.add(new Paragraph("No se registraron ventas en este periodo.")
                        .setFont(fontNormal)
                        .setFontSize(8)
                        .setMarginBottom(5));
            }
            document.add(new Paragraph(new String(new char[15]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginTop(2)
                    .setMarginBottom(2));

            // Agradecimiento o información adicional

            document.add(new Paragraph("Licorera CR")
                    .setFont(lobsterFont)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(-2)); // Margen inferior ajustado a 0

            document.add(new Paragraph("La 70")
                    .setFont(lobsterFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(-5)
                    .setMarginBottom(-2));// Margen inferior ajustado a 0

            document.close();
            String outputType = ConfigAdminManager.getOutputType();
            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Método para limpiar la carpeta de facturas
    public static void limpiarFacturas() {
        String rutaFacturas = System.getProperty("user.home") +"\\Calculadora del Administrador\\Facturas";
        borrarContenidoCarpeta(rutaFacturas);
    }

    // Método para borrar el contenido de la carpeta
    private static void borrarContenidoCarpeta(String carpetaPath) {
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
                      //  System.out.println("No se pudo eliminar: " + elemento.getPath());
                    }
                }
            }
        } else {
          //  System.out.println("La carpeta no existe o no es un directorio: " + carpetaPath);
        }
    }

    public static Map<String, Integer> obtenerProductosVendidos() {
        Map<String, Integer> productosVendidos = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Abre la hoja de productos
            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            if (sheet != null) {
                // Identificar la columna "Cantidad Vendida" y "Nombre"
                Row headerRow = sheet.getRow(0);
                int cantidadVendidaCol = -1;
                int nombreProductoCol = -1;

                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null) {
                        String headerValue = headerCell.getStringCellValue();
                        if ("Cantidad Vendida".equalsIgnoreCase(headerValue)) {
                            cantidadVendidaCol = i;
                        } else if ("Nombre".equalsIgnoreCase(headerValue)) {
                            nombreProductoCol = i;
                        }
                    }
                }

                // Verifica si las columnas necesarias se encontraron
                if (cantidadVendidaCol == -1 || nombreProductoCol == -1) {
                    throw new IllegalStateException("No se encontraron las columnas necesarias ('Cantidad Vendida', 'Nombre').");
                }

                // Leer filas y extraer productos vendidos
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Salta la fila de encabezados
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell nombreProductoCell = row.getCell(nombreProductoCol);
                        Cell cantidadVendidaCell = row.getCell(cantidadVendidaCol);

                        if (nombreProductoCell != null && cantidadVendidaCell != null) {
                            String nombreProducto = nombreProductoCell.getStringCellValue();

                            // Verificar si "Cantidad Vendida" es un número
                            if (cantidadVendidaCell.getCellType() == CellType.NUMERIC) {
                                int cantidadVendida = (int) cantidadVendidaCell.getNumericCellValue();

                                // Agregar al mapa solo si la cantidad vendida es mayor a 0
                                if (cantidadVendida > 0) {
                                    productosVendidos.put(nombreProducto, cantidadVendida);
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return productosVendidos;
    }

    public static void limpiarCantidadVendida() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Abrir la hoja de productos
            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            if (sheet != null) {
                // Obtener la fila de encabezado; generalmente es la primera (índice 0)
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new IllegalStateException("La fila de encabezado no existe en la hoja de productos.");
                }

                int cantidadVendidaCol = -1;
                // Recorrer las celdas del encabezado para encontrar la columna "Cantidad Vendida"
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null && "Cantidad Vendida".equalsIgnoreCase(headerCell.getStringCellValue().trim())) {
                        cantidadVendidaCol = i;
                        break;
                    }
                }

                if (cantidadVendidaCol == -1) {
                    throw new IllegalStateException("No se encontró la columna 'Cantidad Vendida'.");
                }

                // Limpiar la columna "Cantidad Vendida": asignamos 0 a cada celda de dicha columna
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Saltar la fila de encabezado
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(cantidadVendidaCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellValue(0);
                    }
                }
            } else {
                System.err.println("La hoja de productos no se encontró.");
            }

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
            // System.out.println("Se ha limpiado la columna 'Cantidad Vendida'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String INSTANCE_ID = "instance115037";  // Reemplazar con tu instancia de UltraMsg
    private static final String TOKEN = "w0xz1xtb14195z9u";  // Reemplazar con tu token de UltraMsg
    private static final String API_URL = "https://api.ultramsg.com/" + INSTANCE_ID + "/messages/chat";

    public static void enviarMensaje(String numero, String mensaje) throws IOException, InterruptedException {


        if (!ConfigAdminManager.isMessageSendingEnabled()) {
           // System.out.println("Envío de mensajes desactivado. Mensaje no enviado.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String data = "token=" + TOKEN +
                "&to=" + URLEncoder.encode("+" + numero, StandardCharsets.UTF_8) +
                "&body=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Respuesta del servidor: " + response.body());
    }
}

