package org.example.manager.userManager;



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
import org.example.model.Producto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.manager.userManager.PrintUserManager.abrirPDF;
import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class FacturacionUserManager {

    private ExcelUserManager excelUserManager;

    public FacturacionUserManager() {
        this.excelUserManager = new ExcelUserManager();
    }

    /**
     * Método para verificar si el usuario ha ingresado la palabra correcta para facturar.
     *
     * @param input Texto ingresado por el usuario.
     * @return true si la palabra es "Facturar", false en caso contrario.
     */
    public boolean verificarFacturacion(String input) {
        return "Facturar".equals(input);
    }

    /**
     * Realiza la facturación y limpieza de los datos.
     * Luego, termina la ejecución del programa.
     */
    public void facturarYSalir() {
        excelUserManager.facturarYLimpiar();
        eliminarMesasConIdMayorA10();
        System.exit(ZERO);
        // Salir del programa después de la facturación
    }

    /**
     * Muestra un mensaje de error si la palabra ingresada es incorrecta.
     */
    public void mostrarErrorFacturacion() {
        javax.swing.JOptionPane.showMessageDialog(null, ERROR_MENU, ERROR_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
    }



    public static void generarFacturadeCompra(String ventaID, List<String> productos, double totalCompra, LocalDateTime fechaHora) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);


            // Ancho del papel térmico
            float anchoMm = 55;  // Ancho en mm
            float anchoPuntos = anchoMm * WIDE_DOTS;  // Conversión de mm a puntos

            // Calcular el alto dinámico según el número de productos
            float altoBaseMm = 130;  // Altura base en mm (puedes ajustarlo)
            float altoPorProductoMm = 10;  // Espacio adicional por cada producto en mm (ajustable)
            float altoTotalMm = altoBaseMm + (productos.size() * altoPorProductoMm);
            float altoPuntos = altoTotalMm * HEIGHT_DOTS;  // Conversión de mm a puntos

            // Definir el tamaño de la página con el alto dinámico
            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);

            String nombreArchivo = System.getProperty("user.home") + "\\Calculadora del Administrador\\Facturas\\" + BILL_FILE + ventaID + PDF_FORMAT;
            File pdfFile = new File(nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Márgenes ajustados
            document.setMargins(FIVE, FIVE, FIVE, FIVE);

            // Encabezado de la factura
            document.add(new Paragraph(BILL_TITLE)
                    .setFont(fontBold)
                    .setFontSize(TWELVE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(FIVE));

            document.add(new Paragraph(LICORERA_NAME)
                    .setFont(fontBold)
                    .setFontSize(TEN)
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

            document.add(new Paragraph(new String(new char[30]).replace(SLASH_ZERO, "_"))
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

            document.add(new Paragraph(BILL_PRODUCTS)
                    .setFont(fontBold)
                    .setFontSize(TEN));

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

            document.add(new Paragraph(new String(new char[30]).replace(SLASH_ZERO, "_"))
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
            abrirPDF(nombreArchivo);
            //imprimirPDF(nombreArchivo);// Método para abrir el PDF después de generarlo
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

            // Calcular totales
            double totalVentas = sumarTotalesCompras(purchasesSheet);
            double totalGastos = restarTotalesGastos(gastosSheet);
            List<Producto> productosAgotados = obtenerProductosAgotados(productsSheet);
            int totalProductos = productsSheet.getLastRowNum();  // Total de productos
            int productosAgotadosCount = productosAgotados.size();  // Total de productos con cantidad 0

            // Calcular porcentaje de productos agotados
            double porcentajeAgotados = ((double) productosAgotadosCount / totalProductos) * 100;

            // Crear el PDF con tamaño de tarjeta
            float anchoMm = 60;  // Ancho de tarjeta (en mm)
            float altoMm = 160;  // Alto de tarjeta (en mm)
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
            document.add(new Paragraph("Resumen Diario \n"+ fechaActual.format(formatter) + " " + horaResumen.format(horaResumenFormatter))
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



            String textoTotal = "REALIZO DEL SISTEMA: $" + formatearMoneda(totalVentas) + " pesos";

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
            document.add(new Paragraph(horasActual.format(formatter)+ " " + horaResumen.format(horaResumenFormatter))
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
            System.out.println("Archivo PDF de resumen creado: " + nombreArchivo);
            //abrirPDF(nombreArchivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void guardarTotalFacturadoEnArchivo(double totalFacturado) {
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
            float anchoMm = 55;  // Ancho fijo de 58 mm para impresora POS-58
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

            document.add(new Paragraph(new String(new char[31]).replace('\0', '_'))
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

            document.add(new Paragraph(new String(new char[31]).replace('\0', '_'))
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
            document.add(new Paragraph(new String(new char[25]).replace('\0', '_'))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setMarginBottom(5));

            document.add(new Paragraph(new String(new char[25]).replace('\0', '_'))
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
            abrirPDF(nombreArchivo);
            //imprimirPDF(nombreArchivo);// Método para abrir el PDF después de generarlo
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
                        System.out.println("No se pudo eliminar: " + elemento.getPath());
                    }
                }
            }
        } else {
            System.out.println("La carpeta no existe o no es un directorio: " + carpetaPath);
        }
    }


}
