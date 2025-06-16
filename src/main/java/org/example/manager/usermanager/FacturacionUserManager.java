package org.example.manager.usermanager;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.example.manager.adminmanager.ConfigAdminManager;
import org.example.model.Factura;
import org.example.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.example.manager.usermanager.ExcelUserManager.*;
import static org.example.manager.usermanager.PrintUserManager.abrirPDF;
import static org.example.manager.usermanager.PrintUserManager.imprimirPDF;
import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class FacturacionUserManager {

    private FacturacionUserManager() {
    }

    private static final ExcelUserManager excelUserManager = new ExcelUserManager();

    private static final Logger logger =  LoggerFactory.getLogger(FacturacionUserManager.class);

    public static boolean verificarFacturacion(String input) {
        return "Facturar".equals(input);
    }


    public static void facturarYSalir() {

        List<Factura> facturas = getFacturas();

        if (facturas.isEmpty()) {

            JOptionPane.showMessageDialog(null, "No se pueden cerrar las ventas, ya que no hay ventas registradas.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        excelUserManager.facturarYLimpiar();
        eliminarMesasConIdMayorA15();
        System.exit(ZERO);

    }


    public static void mostrarErrorFacturacion() {
        JOptionPane.showMessageDialog(null, ERROR_MENU, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    public static void generarFacturadeCompra(String ventaID, List<String> productos, double totalCompra, LocalDateTime fechaHora, String tipoPago) {
        try {
            InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            assert fontStream != null;
            byte[] fontBytes = fontStream.readAllBytes();
            PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);
            String paperSize = ConfigAdminManager.getPaperSize();
            String outputType = ConfigAdminManager.getOutputType();

            PageSize pageSize = getPageSize(productos, paperSize);
            String nombreArchivo = System.getProperty(FOLDER_PATH) + "\\Calculadora del Administrador\\Facturas\\" + BILL_FILE + ventaID + PDF_FORMAT;
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            try (Document document = new Document(pdfDoc, pageSize)) {

                PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                float margenIzquierdo = TEN * HEIGHT_DOTS;
                document.setMargins(FIVE, FIVE, FIVE, margenIzquierdo);
                document.add(new Paragraph(EMPRESA_NAME)
                        .setFont(lobsterFont)
                        .setFontSize(13)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(-2));

                document.add(new Paragraph("La 70")
                        .setFont(lobsterFont)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(-5)
                        .setMarginBottom(-2));

                document.add(new Paragraph(NIT)
                        .setFont(fontNormal)
                        .setFontSize(SIX)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(-2));

                document.add(new Paragraph(DIRECCION)
                        .setFont(fontNormal)
                        .setFontSize(SIX)
                        .setTextAlignment(TextAlignment.CENTER).setMarginBottom(-2));

                document.add(new Paragraph(TELEFONO)
                        .setFont(fontNormal)
                        .setFontSize(SIX)
                        .setTextAlignment(TextAlignment.CENTER).setMarginBottom(-2));

                document.add(new Paragraph(new String(new char[22]).replace(SLASH_ZERO, "_"))
                        .setFont(fontNormal)
                        .setFontSize(EIGHT)
                        .setMarginBottom(FIVE));


                document.add(new Paragraph(BILL_ID + "N°" + ventaID)
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

                for (String producto : productos) {
                    String[] detallesProducto = producto.split(" ");
                    if (detallesProducto.length >= 3) {

                        String nombreProducto = detallesProducto[0];
                        String cantidadStr = detallesProducto[1];
                        String precioStr = detallesProducto[2];

                        double precioUnitario = Double.parseDouble(precioStr.substring(1));

                        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                        String formattedPrice = formatCOP.format(precioUnitario);

                        String productoConPrecioFormateado = nombreProducto + " " + cantidadStr + " " + formattedPrice;

                        document.add(new Paragraph(productoConPrecioFormateado)
                                .setFont(fontNormal)
                                .setFontSize(6));
                    }
                }

                NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                String formattedPrice = formatCOP.format(totalCompra);

                document.add(new Paragraph(TOTAL_BILL + PESO_SIGN + formattedPrice + PESOS)
                        .setFont(fontNormal)
                        .setFontSize(9)
                        .setMarginBottom(2)
                        .setBold());

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


            }

            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }
        } catch (IOException e) {
            logger.error("Error al generar la factura: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al generar la factura: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static PageSize getPageSize(List<String> productos, String paperSize) {
        float anchoMm = paperSize.equals("48mm") ? 48 : (paperSize.equals("A4") ? 210 : 80);
        float anchoPuntos = anchoMm * WIDE_DOTS;
        float altoBaseMm = 60;
        float altoPorProductoMm = 10;
        float extraSpaceMm = 25;
        float altoMinimoMm = 60;


        float altoTotalMm = Math.max(altoBaseMm + (productos.size() * altoPorProductoMm) + extraSpaceMm, altoMinimoMm);
        float altoPuntos = altoTotalMm * HEIGHT_DOTS;

        PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);
        return pageSize;
    }

    public static void generarResumenDiarioEstilizadoPDF() throws InterruptedException {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String carpetaPath = System.getProperty(FOLDER_PATH) + "\\Calculadora del Administrador\\Resumen del día";
        File carpeta = new File(carpetaPath);

        if (!carpeta.exists()) {
            boolean wasSuccessful = carpeta.mkdirs();
            if (!wasSuccessful) {
                return;
            }
        }

        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String nombreArchivo = carpetaPath + "\\Resumen_Diario_" + fechaActual.format(formatter) + "_" + horaActual.format(horaFormatter) + ".pdf";

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            Sheet gastosSheet = workbook.getSheet(EXPENSES_SHEET_NAME);
            Sheet productsSheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            Sheet empleadosSheet = workbook.getSheet("Empleados");

            Sheet reabastecimientoSheet = workbook.getSheet("reabastecimiento");

            double totalVentas = sumarTotalesCompras(purchasesSheet);
            double totalGastos = restarTotalesGastos(gastosSheet);
            List<Producto> productosAgotados = obtenerProductosAgotados(productsSheet);
            int totalProductos = productsSheet.getLastRowNum();
            int productosAgotadosCount = productosAgotados.size();

            double porcentajeAgotados = ((double) productosAgotadosCount / totalProductos) * 100;



            String paperSize = ConfigAdminManager.getPaperSize();


            float anchoMm;

            if ("58mm".equals(paperSize)) {
                anchoMm = 58;
            } else if ("A4".equals(paperSize)) {
                anchoMm = 210;
            } else {
                anchoMm = 80;
            }

            float altoMm = 200;
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoMm * 2.83465f;

            PageSize cardSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, cardSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            document.setMargins(5, 5, 5, 5);
            LocalTime horaResumen = LocalTime.now();
            DateTimeFormatter horaResumenFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            document.add(new Paragraph("Resumen Diario \n" + fechaActual.format(formatter) + " " + horaResumen.format(horaResumenFormatter))
                    .setFont(fontBold)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            String textoTotal = "REALIZO DEL SISTEMA: $" + formatearMoneda(totalVentas) + PESOS;

            document.add(new Paragraph(textoTotal)
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            document.add(new Paragraph("GASTOS: $" + formatearMoneda(totalGastos) + PESOS)
                    .setFont(fontBold)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

            document.add(new Paragraph("Detalle de GASTOS:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                Row row = gastosSheet.getRow(i);
                if (row != null) {
                    String producto = row.getCell(1).getStringCellValue();
                    String precioTexto = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                    double precioGasto = 0;
                    try {
                        precioGasto = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                    } catch (NumberFormatException e) {
                        // Si el valor no es numérico (por ejemplo, "N/A"), simplemente lo dejas en 0 o lo omites

                    }

                    document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioGasto) + PESOS)
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }

            document.add(new Paragraph("Detalles de Reabastecimiento:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            for (int i = 1; i <= reabastecimientoSheet.getLastRowNum(); i++) {
                Row row = reabastecimientoSheet.getRow(i);
                if (row != null) {
                    String producto = row.getCell(1).getStringCellValue();
                    String precioTexto = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                    double precioreabastecimiento;
                    try {
                        precioreabastecimiento = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                    } catch (NumberFormatException e) {
                       logger .error("Error al convertir el precio de reabastecimiento: {}", e.getMessage());
                        precioreabastecimiento = 0;
                    }

                    document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioreabastecimiento) + " pesos")
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }
            document.add(new Paragraph("Productos Agotados: " + productosAgotadosCount + " productos (" + String.format("%.2f", porcentajeAgotados) + "%) del inventario")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10));

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

            document.add(new Paragraph("\nEmpleados y Hora de apertura:")
                    .setFont(fontBold)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5));

            for (int i = 1; i <= empleadosSheet.getLastRowNum(); i++) {
                Row row = empleadosSheet.getRow(i);
                if (row != null) {
                    String nombreEmpleado = row.getCell(0).getStringCellValue();
                    String horaInicio = row.getCell(1).getStringCellValue();
                    String fechaInicio = row.getCell(2).getStringCellValue();
                    document.add(new Paragraph("- " + nombreEmpleado + ": " + horaInicio + " - " + fechaInicio)
                            .setFont(fontNormal)
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }
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

            document.add(new Paragraph(new String(new char[30]).replace('\0', '-'))
                    .setFont(fontNormal)
                    .setFontSize(6)
                    .setMarginTop(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Sistema "+EMPRESA_NAME)
                    .setFont(fontBold)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5));

            document.close();
            formatearMoneda(totalVentas);
            DataFormatter formatteo = new DataFormatter();
            StringBuilder gastosNA = new StringBuilder();
            try (FileInputStream fisGastos = new FileInputStream(FILE_PATH);
                 Workbook wbGastos = WorkbookFactory.create(fisGastos)) {
                 wbGastos.getSheet("Gastos");
                for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                    Row row = gastosSheet.getRow(i);
                    if (row != null) {
                        String cantidad = formatteo.formatCellValue(row.getCell(2)).trim().toLowerCase();
                        if ("n/a".equals(cantidad)) {
                            String producto = formatteo.formatCellValue(row.getCell(1));
                            cantidad = String.valueOf(0);
                            String valor = formatteo.formatCellValue(row.getCell(3));
                            gastosNA.append("\n- ").append(producto).append(": $").append(valor).append(" pesos");
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error al leer la hoja de gastos: {}", e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al leer la hoja de gastos: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
            String[] numeros = { "+573226094632","+573112599560"};
            String mensaje = "*[Licorera CR]*\n¡Hola! se ha generado el realizo del día de hoy por un total de: $ "
                    + formatearMoneda(totalVentas) + " pesos.\nPuedes consultar los detalles en los resúmenes adjuntos en Google Drive: https://drive.google.com/drive/folders/1-mklq_6xIUVZz8osGDrBtvYXEu-RNGYH";
            if (!gastosNA.isEmpty()) {
                mensaje += "\n\n*Gastos del día:*" + gastosNA;
            }
            enviarMensaje(numeros,mensaje);
        } catch (IOException e) {
            logger.error("Error al generar el resumen diario: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al generar el resumen diario: " + e.getMessage(), ERROR_TITLE,JOptionPane.ERROR_MESSAGE);
        }
    }

public static void guardarTotalFacturadoEnArchivo( Map<String,Double>totalesPorPago,double totalFacturado) throws IOException {
        Map<String, Integer> productosVendidos = obtenerProductosVendidos();

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String carpetaPath = System.getProperty(FOLDER_PATH) + "\\Calculadora del Administrador\\Realizo";
        File carpeta = new File(carpetaPath);

        if (!carpeta.exists()) {
            boolean wasSuccessful = carpeta.mkdirs();
            if (!wasSuccessful) {
                return;
            }
        }
        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String nombreArchivo = carpetaPath + "\\REALIZO_" + fechaActual.format(formatter) + EMPTY + horaActual.format(horaFormatter) + ".pdf";

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet gastosSheet = workbook.getSheet("Gastos");
            for (int i = 1; i <= gastosSheet.getLastRowNum(); i++) {
                Row row = gastosSheet.getRow(i);
                if (row != null) {
                     new DataFormatter().formatCellValue(row.getCell(3));
                }
            }
            double totalGastos = restarTotalesGastos(gastosSheet);
            float anchoMm = 48;
            float altoBaseMm = 80;
            float altoPorProductoMm = 8;
            float altoFooterMm = 2;
            float altoMinimoMm = 80;
            float altoTotalMm = Math.max(altoBaseMm + (productosVendidos.size() * altoPorProductoMm) +  altoFooterMm, altoMinimoMm);
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoTotalMm * 2.83465f;

            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            try (Document document = new Document(pdfDoc, pageSize)) {

                PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
                assert fontStream != null;
                byte[] fontBytes = fontStream.readAllBytes();
                PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

                float margenIzquierdo = 10 * HEIGHT_DOTS;
                document.setMargins(FIVE, FIVE, FIVE, margenIzquierdo);


                document.add(new Paragraph("Realizo del Día")
                        .setFont(lobsterFont)
                        .setFontSize(15)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(0));

                LocalTime horaNueva = LocalTime.now();
                DateTimeFormatter horaFormateada = DateTimeFormatter.ofPattern("HH:mm:ss");
                document.add(new Paragraph(fechaActual.format(formatter) + "\n" + horaNueva.format(horaFormateada))
                        .setFont(fontNormal)
                        .setFontSize(8)
                        .setMarginBottom(-1)
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph(new String(new char[22]).replace('\0', '_'))
                        .setFont(fontNormal)
                        .setFontSize(8)
                        .setMarginBottom(2));

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
                            double precioGasto;
                            try {
                                precioGasto = Double.parseDouble(precioTexto.replace(".", "").replace(",", "."));
                            } catch (NumberFormatException e) {
                                continue;
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

                if (!productosVendidos.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                        String producto = entry.getKey();
                        int cantidad = entry.getValue();

                        document.add(new Paragraph(producto + " x" + cantidad)
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

                document.add(new Paragraph(EMPRESA_NAME)
                        .setFont(lobsterFont)
                        .setFontSize(13)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(-2));

                document.add(new Paragraph("La 70")
                        .setFont(lobsterFont)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(-5)
                        .setMarginBottom(-2));

            }
            String outputType = ConfigAdminManager.getOutputType();
            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }

        } catch (IOException e) {
            logger.error("Error al generar el archivo Realizo: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al generar el archivo Realizo: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void limpiarFacturas() {
        String rutaFacturas = System.getProperty(FOLDER_PATH) +"\\Calculadora del Administrador\\Facturas";
        borrarContenidoCarpeta(rutaFacturas);
    }


    private static void borrarContenidoCarpeta(String carpetaPath) {
        File carpeta = new File(carpetaPath);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] elementos = carpeta.listFiles();
            if (elementos != null) {
                for (File elemento : elementos) {
                    if (elemento.isDirectory()) {
                        borrarContenidoCarpeta(elemento.getPath());
                    }
                    elemento.delete();
                }
            }
        }
    }

    public static Map<String, Integer> obtenerProductosVendidos() {
        Map<String, Integer> productosVendidos = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            if (sheet != null) {

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

                if (cantidadVendidaCol == -1 || nombreProductoCol == -1) {
                    throw new IllegalStateException("No se encontraron las columnas necesarias ('Cantidad Vendida', 'Nombre').");
                }

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell nombreProductoCell = row.getCell(nombreProductoCol);
                        Cell cantidadVendidaCell = row.getCell(cantidadVendidaCol);

                        if (nombreProductoCell != null && cantidadVendidaCell != null) {
                            String nombreProducto = nombreProductoCell.getStringCellValue();

                            if (cantidadVendidaCell.getCellType() == CellType.NUMERIC) {
                                int cantidadVendida = (int) cantidadVendidaCell.getNumericCellValue();

                                if (cantidadVendida > 0) {
                                    productosVendidos.put(nombreProducto, cantidadVendida);
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error al obtener productos vendidos: {}", e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Error de estado ilegal: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
        }

        return productosVendidos;
    }

    public static void limpiarCantidadVendida() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            if (sheet != null) {
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new IllegalStateException("La fila de encabezado no existe en la hoja de productos.");
                }

                int cantidadVendidaCol = -1;
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

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(cantidadVendidaCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellValue(0);
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            logger.error("Error al limpiar la cantidad vendida: {}", e.getMessage());
        }
    }

    public static void enviarMensaje(String[] numeros, String mensaje) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        if (!ConfigAdminManager.isMessageSendingEnabled()) {
            return;
        }

        for (String numero : numeros) {
            String url = String.format("https://api.callmebot.com/whatsapp.php?phone=%s&text=%s&apikey=2596189",
                    URLEncoder.encode(numero, StandardCharsets.UTF_8),
                    URLEncoder.encode(mensaje, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (logger.isErrorEnabled()) {logger.error("Enviado a {} → Respuesta: {}", numero, response.body());}
        }
    }


}

