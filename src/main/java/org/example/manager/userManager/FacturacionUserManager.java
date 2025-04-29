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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.manager.adminManager.ConfigAdminManager;
import org.example.manager.userDBManager.DatabaseUserManager;
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
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static org.example.manager.userDBManager.DatabaseUserManager.*;
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
    public static void facturarYSalir() throws IOException, InterruptedException, SQLException {
       //excelUserManager.facturarYLimpiar();
        DatabaseUserManager.facturarYLimpiar();

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

            document.add(new Paragraph(LICORERA_NAME)
                    .setFont(lobsterFont)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(NIT)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(DIRECCION)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(TELEFONO)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setTextAlignment(TextAlignment.CENTER));

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
                    .setMarginBottom(FIVE)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Tipo de Pago: " + tipoPago)
                    .setFont(fontNormal)
                    .setFontSize(SIX)
                    .setMarginBottom(FIVE)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(BILL_PRODUCTS)
                    .setFont(fontBold)
                    .setFontSize(8));

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
                            .setFontSize(EIGHT));
                }}

            /*document.add(new Paragraph(new String(new char[30]).replace(SLASH_ZERO, "_"))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));*/

            // Totales
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(new float[]{THREE, TWO});
            table.setWidth(UnitValue.createPercentValue(ONE_HUNDRED));

            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalCompra);
            addTableRow(table, TOTAL_BILL, PESO_SIGN + formattedPrice + PESOS);


            document.add(table);

            document.add(new Paragraph(new String(new char[22]).replace(SLASH_ZERO, "_"))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            document.add(new Paragraph(THANKS_BILL)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
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


    /*public static void generarResumenDiarioEstilizadoPDF() {
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
            List<Producto> productosAgotados = DatabaseUserManager.obtenerProductosAgotados(productsSheet);
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
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

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
            /*document.add(new Paragraph("Total en GASTOS: $" + formatearMoneda(totalGastos) + " pesos")
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
    }*/


    public static void generarResumenDiarioEstilizadoPDF() {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");

        String carpetaPath = System.getProperty("user.home") + "\\Calculadora del Administrador\\Resumen del día";
        new File(carpetaPath).mkdirs();
        String nombreArchivo = carpetaPath + "\\Resumen_Diario_" + fechaActual.format(formatter) + "_" + LocalTime.now().format(horaFormatter) + ".pdf";

        try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL)) {
            double totalVentas = 0.0;
            double totalGastos = 0.0;
            double totalReabastecimiento = 0.0;
            List<Producto> productosAgotados = new ArrayList<>();
            List<String[]> listaGastos = new ArrayList<>();
            List<String[]> listaReabastecimientos = new ArrayList<>();
            List<String[]> listaEmpleados = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement("SELECT SUM(total) FROM compras"); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) totalVentas = rs.getDouble(1);
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT descripcion, monto FROM gastos"); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaGastos.add(new String[]{rs.getString(1), String.valueOf(rs.getDouble(2))});
                    totalGastos += rs.getDouble(2);
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre_producto, precio_compra FROM reabastecimientos"); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaReabastecimientos.add(new String[]{rs.getString(1), String.valueOf(rs.getDouble(2))});
                    totalReabastecimiento += rs.getDouble(2);
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM productos WHERE cantidad = 0"); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) productosAgotados.add(new Producto(rs.getString("nombre"), 0));
            }
            try (PreparedStatement stmt = connection.prepareStatement("SELECT nombre, hora_inicio, fecha FROM empleados"); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) listaEmpleados.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3)});
            }

            float anchoMm = ConfigAdminManager.getPaperSize().equals("58mm") ? 58 : 210;
            float altoMm = 200;
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoMm * 2.83465f;

            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, new PageSize(anchoPuntos, altoPuntos));
            document.setMargins(5, 5, 5, 5);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            document.add(new Paragraph("Resumen Diario\n" + fechaActual.format(formatter) + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .setFont(fontBold).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            document.add(new Paragraph("REALIZO DEL SISTEMA: $" + FormatterHelpers.formatearMoneda(totalVentas) + " pesos")
                    .setFont(fontBold).setFontSize(12).setTextAlignment(TextAlignment.LEFT).setMarginBottom(10));

            document.add(new Paragraph("Total en GASTOS: $" + FormatterHelpers.formatearMoneda(totalGastos) + " pesos")
                    .setFont(fontBold).setFontSize(8).setTextAlignment(TextAlignment.LEFT).setMarginBottom(10));

            document.add(new Paragraph("Detalle de GASTOS:").setFont(fontBold).setFontSize(9).setMarginBottom(5));
            for (String[] gasto : listaGastos) {
                document.add(new Paragraph("- " + gasto[0] + ": $" + FormatterHelpers.formatearMoneda(Double.parseDouble(gasto[1])) + " pesos")
                        .setFont(fontNormal).setFontSize(7).setTextAlignment(TextAlignment.LEFT));
            }

            document.add(new Paragraph("Detalles de Reabastecimiento:").setFont(fontBold).setFontSize(9).setMarginBottom(5));
            for (String[] item : listaReabastecimientos) {
                document.add(new Paragraph("- " + item[0] + ": $" + FormatterHelpers.formatearMoneda(Double.parseDouble(item[1])) + " pesos")
                        .setFont(fontNormal).setFontSize(7).setTextAlignment(TextAlignment.LEFT));
            }

            double porcentajeAgotados = productosAgotados.isEmpty() ? 0 : ((double) productosAgotados.size() / obtenerTotalProductos(connection)) * 100;
            document.add(new Paragraph("Productos Agotados: " + productosAgotados.size() + " productos (" + String.format("%.2f", porcentajeAgotados) + "%)")
                    .setFont(fontNormal).setFontSize(8).setMarginBottom(10));

            if (productosAgotados.isEmpty()) {
                document.add(new Paragraph("No hay productos agotados.").setFont(fontItalic).setFontSize(8).setMarginBottom(5));
            } else {
                for (Producto p : productosAgotados) {
                    document.add(new Paragraph("- " + p.getName()).setFont(fontNormal).setFontSize(7));
                }
            }

            document.add(new Paragraph("\nEmpleados y Hora de apertura:").setFont(fontBold).setFontSize(9).setMarginBottom(5));
            for (String[] emp : listaEmpleados) {
                document.add(new Paragraph("- " + emp[0] + ": " + emp[1] + " - " + emp[2])
                        .setFont(fontNormal).setFontSize(7));
            }

            document.add(new Paragraph("\nCierre y facturación:").setFont(fontBold).setFontSize(9).setMarginBottom(5));
            document.add(new Paragraph(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                    .setFont(fontNormal).setFontSize(7));

            document.add(new Paragraph("------------------------------").setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Sistema Licorera CR La 70").setFont(fontBold).setFontSize(7).setTextAlignment(TextAlignment.CENTER));

            document.close();

            StringBuilder gastosNA = new StringBuilder();
            for (String[] gasto : listaGastos) {
                if (gasto[1].equalsIgnoreCase("n/a")) {
                    gastosNA.append("\n- ").append(gasto[0]).append(": $").append(gasto[1]).append(" pesos");
                }
            }

         /*   String numeroDestino = "+573112599560";
            String mensaje = "*[Licorera CR]* \u00a1Hola! se ha generado la liquidaci\u00f3n del d\u00eda de hoy por un total de: $ " +
                    FormatterHelpers.formatearMoneda(totalVentas) + " pesos.\nPuedes consultar los detalles en los res\u00famenes adjuntos en Google Drive: https://drive.google.com/drive/folders/1-mklq_6xIUVZz8osGDrBtvYXEu-RNGYH";

            if (!gastosNA.isEmpty()) {
                mensaje += "\n\n*Gastos del día:*" + gastosNA;
            }

            enviarMensaje(numeroDestino, mensaje);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int obtenerTotalProductos(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM productos");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static void guardarTotalFacturadoEnArchivo(Map<String, Double> totalesPorPago, double totalFacturado) throws IOException {
        Map<String, Integer> productosVendidos = obtenerProductosVendidos();

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String carpetaPath = System.getProperty("user.home") + "\\Calculadora del Administrador\\Realizo";
        File carpeta = new File(carpetaPath);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            System.err.println("No se pudo crear la carpeta 'Realizo'.");
            return;
        }

        String nombreArchivo = carpetaPath + "\\REALIZO_" + fechaActual.format(formatter) + EMPTY + LocalTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")) + ".pdf";

        try (Connection conn = DriverManager.getConnection(DatabaseUserManager.URL)) {
            double totalGastos = 0.0;
            List<String[]> listaGastos = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement("SELECT descripcion, monto FROM gastos"); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String descripcion = rs.getString("descripcion");
                    double monto = rs.getDouble("monto");
                    listaGastos.add(new String[]{descripcion, String.valueOf(monto)});
                    totalGastos += monto;
                }
            }

            float anchoMm = 48;
            float altoTotalMm = 250 + (productosVendidos.size() * 12) + 40;
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoTotalMm * 2.83465f;

            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            document.setMargins(FIVE, FIVE, FIVE, 10 * HEIGHT_DOTS);

            document.add(new Paragraph("Total Generado Durante el Día")
                    .setFont(fontBold).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
            document.add(new Paragraph(fechaActual.format(formatter) + "\n" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .setFont(fontNormal).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("______________________").setFont(fontNormal).setFontSize(8).setMarginBottom(10));

            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            document.add(new Paragraph("Realizo: $" + formatCOP.format(totalFacturado) + " pesos")
                    .setFont(fontBold).setFontSize(12).setTextAlignment(TextAlignment.LEFT).setMarginBottom(5));

            document.add(new Paragraph("\nTotales por Forma de Pago:")
                    .setFont(fontBold).setFontSize(10).setMarginBottom(5));
            if (totalesPorPago != null && !totalesPorPago.isEmpty()) {
                for (Map.Entry<String, Double> entry : totalesPorPago.entrySet()) {
                    document.add(new Paragraph(entry.getKey() + ": $" + formatCOP.format(entry.getValue()))
                            .setFont(fontNormal).setFontSize(10).setMarginBottom(3));
                }
            } else {
                document.add(new Paragraph("No hay datos de pagos registrados.")
                        .setFont(fontNormal).setFontSize(10).setMarginBottom(5));
            }

            document.add(new Paragraph("______________________").setFont(fontNormal).setFontSize(8).setMarginBottom(10));

            if (totalGastos > 0) {
                document.add(new Paragraph("Total en GASTOS: $" + FormatterHelpers.formatearMoneda(totalGastos) + " pesos")
                        .setFont(fontBold).setFontSize(8).setTextAlignment(TextAlignment.LEFT).setMarginBottom(10));
                document.add(new Paragraph("Detalle de GASTOS:")
                        .setFont(fontBold).setFontSize(9).setTextAlignment(TextAlignment.LEFT).setMarginBottom(5));
                for (String[] gasto : listaGastos) {
                    document.add(new Paragraph("- " + gasto[0] + ": $" + FormatterHelpers.formatearMoneda(Double.parseDouble(gasto[1])) + " pesos")
                            .setFont(fontNormal).setFontSize(7).setTextAlignment(TextAlignment.LEFT));
                }
            }

            document.add(new Paragraph("Cierre de Caja")
                    .setFont(fontBold).setFontSize(10).setTextAlignment(TextAlignment.LEFT).setMarginBottom(2));
            document.add(new Paragraph("Resumen de las ventas:")
                    .setFont(fontNormal).setFontSize(8).setMarginBottom(5));
            document.add(new Paragraph("_______________").setFont(fontNormal).setFontSize(10).setMarginBottom(5));

            document.add(new Paragraph("Productos Vendidos:")
                    .setFont(fontBold).setFontSize(10).setMarginBottom(5));
            if (productosVendidos != null && !productosVendidos.isEmpty()) {
                for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                    document.add(new Paragraph("- " + entry.getKey() + ": X" + entry.getValue())
                            .setFont(fontNormal).setFontSize(8).setMarginBottom(2));
                }
            } else {
                document.add(new Paragraph("No se registraron ventas en este periodo.")
                        .setFont(fontNormal).setFontSize(8).setMarginBottom(5));
            }
            document.add(new Paragraph("_______________").setFont(fontNormal).setFontSize(10).setMarginTop(5).setMarginBottom(5));

            InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            byte[] fontBytes = fontStream.readAllBytes();
            PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.add(new Paragraph("Licorera CR La 70")
                    .setFont(lobsterFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER));

            document.close();
            String outputType = ConfigAdminManager.getOutputType();
            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }

            limpiarCantidadVendida();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* public static void guardarTotalFacturadoEnArchivo( Map<String,Double>totalesPorPago,double totalFacturado) throws IOException {


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

            // Ajustar margen izquierdo a 6mm (1cm equivalente)
            float margenIzquierdo = 10 * HEIGHT_DOTS;
            document.setMargins(FIVE, FIVE, FIVE, margenIzquierdo);


            // Encabezado del PDF
            document.add(new Paragraph("Total Generado Durante el Día")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            LocalTime horaNueva = LocalTime.now();
            DateTimeFormatter horaFormateada= DateTimeFormatter.ofPattern("HH:mm:ss");
            document.add(new Paragraph(fechaActual.format(formatter) + "\n" + horaNueva.format(horaFormateada))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(new String(new char[22]).replace('\0', '_'))
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
                    .setMarginBottom(5));


            document.add(new Paragraph("\nTotales por Forma de Pago:")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setMarginBottom(5));

            if (totalesPorPago != null && !totalesPorPago.isEmpty()) {
                for (Map.Entry<String, Double> entry : totalesPorPago.entrySet()) {
                    String metodoPago = entry.getKey();
                    double total = entry.getValue();

                    // Formateo del total en pesos colombianos
                    String totalFormateado = formatCOP.format(total);

                    document.add(new Paragraph(metodoPago + ": $" + totalFormateado)
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setMarginBottom(3));
                }
            } else {
                document.add(new Paragraph("No hay datos de pagos registrados.")
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setMarginBottom(5));
            }

            document.add(new Paragraph(new String(new char[22]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(10));
            if (totalGastos > 0) {
                document.add(new Paragraph("Total en GASTOS: $" + formatearMoneda(totalGastos) + " pesos")
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
                            continue; // Omitir si no es numérico
                        }

                        document.add(new Paragraph("- " + producto + ": $" + formatearMoneda(precioGasto) + " pesos")
                                .setFont(fontNormal)
                                .setFontSize(7)
                                .setTextAlignment(TextAlignment.LEFT));
                    }
                }
            }
            // Sección para el cierre de caja
            document.add(new Paragraph("Cierre de Caja")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(2));

            document.add(new Paragraph("Resumen de las ventas:")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            // Espacios para el cierre de caja
            document.add(new Paragraph(new String(new char[15]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginBottom(5));
            document.add(new Paragraph("Productos Vendidos:")
                    .setFont(fontBold)
                    .setFontSize(10)
                    .setMarginBottom(5));

            if (productosVendidos != null && !productosVendidos.isEmpty()) {
                for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                    String producto = entry.getKey();
                    int cantidad = entry.getValue();

                    document.add(new Paragraph("- " + producto + ": " + " X"+ cantidad)
                            .setFont(fontNormal)
                            .setFontSize(8)
                            .setMarginBottom(2));
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
                    .setMarginTop(5)
                    .setMarginBottom(5));

            // Agradecimiento o información adicional

            InputStream fontStream = FacturacionUserManager.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            // Leer la fuente desde el InputStream
            byte[] fontBytes = fontStream.readAllBytes();
            PdfFont lobsterFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.add(new Paragraph("Licorera CR La 70")
                    .setFont(lobsterFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            String outputType = ConfigAdminManager.getOutputType();
            if ("IMPRESORA".equals(outputType)) {
                imprimirPDF(nombreArchivo);
            } else {
                abrirPDF(nombreArchivo);
            }
            limpiarCantidadVendida();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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

   /* public static Map<String, Integer> obtenerProductosVendidos() {
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
    }*/


    public static Map<String, Integer> obtenerProductosVendidos() {
        Map<String, Integer> productosVendidos = new HashMap<>();

        String sql = "SELECT nombreProducto, SUM(cantidadVendida) AS cantidadTotal " +
                "FROM Ventas GROUP BY nombreProducto";

        try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreProducto = rs.getString("nombreProducto");
                int cantidadVendida = rs.getInt("cantidadTotal");

                // Solo agregar productos que tienen una cantidad mayor a 0
                if (cantidadVendida > 0) {
                    productosVendidos.put(nombreProducto, cantidadVendida);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productosVendidos;
    }


   /* public static void limpiarCantidadVendida() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Abrir la hoja de productos
            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            if (sheet != null) {
                // Identificar la columna "Cantidad Vendida"
                Row headerRow = sheet.getRow(0);
                int cantidadVendidaCol = -1;

                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell != null && "Cantidad Vendida".equalsIgnoreCase(headerCell.getStringCellValue())) {
                        cantidadVendidaCol = i;
                        break;
                    }
                }

                if (cantidadVendidaCol == -1) {
                    throw new IllegalStateException("No se encontró la columna 'Cantidad Vendida'.");
                }

                // Limpiar la columna "Cantidad Vendida"
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Saltar la fila de encabezado
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(cantidadVendidaCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellValue(0); // Establecer el valor a 0
                    }
                }
            }

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

          //  System.out.println("Se ha limpiado la columna 'Cantidad Vendida'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public static void limpiarCantidadVendida() {
        String sql = "UPDATE Ventas SET cantidadVendida = 0";

        try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Se han limpiado las cantidades vendidas para " + rowsUpdated + " productos.");

        } catch (SQLException e) {
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

