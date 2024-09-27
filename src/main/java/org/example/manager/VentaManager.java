package org.example.manager;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.example.model.Producto;


import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.example.utils.Constants.*;


public class VentaManager {
    private List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private List<Integer> cartQuantities = new ArrayList<>(); // Lista para almacenar las cantidades correspondientes


    // Método para añadir un producto al carrito
    public void addProductToCart(Producto producto, int cantidad) {
        // Buscar si el producto ya está en el carrito
        int index = cartProducts.indexOf(producto);
        if (index != MINUS_ONE) {
            // Si el producto ya está en el carrito, actualizar la cantidad
            cartQuantities.set(index, cartQuantities.get(index) + cantidad);
        } else {
            // Si el producto no está en el carrito, añadirlo
            cartProducts.add(producto);
            cartQuantities.add(cantidad);
        }
    }

    // Método para obtener la lista de productos formateada para guardar en Excel
    public List<String> getProductListForExcel() {
        List<String> productList = new ArrayList<>();
        for (int i = ZERO; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            double totalProducto = producto.getPrice() * cantidad;
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalProducto);
            productList.add( cantidad+EMPTY + producto.getName() + PRODUCT_NETO + formattedPrice + PESOS);
        }
        return productList;
    }

    // Método para obtener el monto total de la compra
    public double getTotalCartAmount() {
        double total = ZERO;
        for (int i = ZERO; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            total += producto.getPrice() * cantidad;
        }

        return total;
    }

    public void removeProductFromCart(int row) {
    }

    public static void calcularDineroDevuelto(JTextField dineroRecibidoField, JLabel devueltoLabel, DefaultTableModel tableModel, JDialog compraDialog) {
        try {
            // Calcular el total general de los productos en la tabla
            double total = ZERO_DOUBLE;
            for (int i = ZERO; i < tableModel.getRowCount(); i++) {
                total += (double) tableModel.getValueAt(i, THREE); // Columna 3 es el total por producto
            }

            // Verificar si el campo de "dinero recibido" está vacío
            String dineroRecibidoTexto = dineroRecibidoField.getText();
            if (dineroRecibidoTexto.isEmpty()) {
                JOptionPane.showMessageDialog(compraDialog, ENTER_MONEY_RECEIVED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convertir el texto a un número
            double dineroRecibido = Double.parseDouble(dineroRecibidoTexto);

            // Calcular el dinero devuelto
            double dineroDevuelto = dineroRecibido - total;

            // Mostrar el resultado con un diálogo
            if (dineroDevuelto < ZERO) {
                JOptionPane.showMessageDialog(compraDialog, NEED_MORE + Math.abs(dineroDevuelto)+PESOS, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } else {
                devueltoLabel.setText(CHANGE + dineroDevuelto);
                JOptionPane.showMessageDialog(compraDialog, MONEY_CHANGED + dineroDevuelto+PESOS, INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(compraDialog, INVALID_NUMBER, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addTableRow(Table table, String key, String value) {
        table.addCell(new Paragraph(key).setFontSize(EIGHT));
        table.addCell(new Paragraph(value).setFontSize(EIGHT));
    }

    public static void generarFactura(String ventaID, List<String> productos, double totalCompra, LocalDateTime fechaHora) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaFormateada = fechaHora.format(formatter);

            // Ancho del papel térmico
            float anchoMm = 58;  // Ancho en mm
            float anchoPuntos = anchoMm * WIDE_DOTS;  // Conversión de mm a puntos

            // Calcular el alto dinámico según el número de productos
            float altoBaseMm = 110;  // Altura base en mm (puedes ajustarlo)
            float altoPorProductoMm = 10;  // Espacio adicional por cada producto en mm (ajustable)
            float altoTotalMm = altoBaseMm + (productos.size() * altoPorProductoMm);
            float altoPuntos = altoTotalMm * HEIGHT_DOTS;  // Conversión de mm a puntos

            // Definir el tamaño de la página con el alto dinámico
            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);

            String nombreArchivo = System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador\\Facturas\\" + BILL_FILE + ventaID + PDF_FORMAT;
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

            document.add(new Paragraph(new String(new char[33]).replace(SLASH_ZERO, EQUALS))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            // Detalles de la compra
            document.add(new Paragraph(BILL_ID + ventaID)
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

            // Agregar productos
            for (String producto : productos) {
                document.add(new Paragraph(String.valueOf(producto))
                        .setFont(fontNormal)
                        .setFontSize(EIGHT));
            }

            document.add(new Paragraph(new String(new char[33]).replace(SLASH_ZERO, EQUALS))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            // Totales
            Table table = new Table(new float[]{THREE, TWO});
            table.setWidth(UnitValue.createPercentValue(ONE_HUNDRED));

            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            String formattedPrice = formatCOP.format(totalCompra);
            addTableRow(table, TOTAL_BILL, PESO_SIGN + formattedPrice + PESOS);


            document.add(table);

            document.add(new Paragraph(new String(new char[33]).replace(SLASH_ZERO, EQUALS))
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
    static void abrirPDF(String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void imprimirPDF(String pdfFilePath) {
        try {
            // Cargar el archivo PDF
            PDDocument document = PDDocument.load(new File(pdfFilePath));

            // Crear un trabajo de impresión
            PrinterJob printerJob = PrinterJob.getPrinterJob();

            // Usar la impresora predeterminada del sistema
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

            if (defaultPrintService != null) {
                printerJob.setPrintService(defaultPrintService);
            } else {
                System.out.println("No hay una impresora predeterminada configurada.");
                return;
            }

            // Configurar el documento PDF para la impresión
            printerJob.setPageable(new PDFPageable(document));

            // Realizar la impresión
            if (printerJob.printDialog()) {  // Si deseas mostrar el diálogo de impresión, puedes cambiarlo a true
                printerJob.print();
            }

            // Cerrar el documento
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar el archivo PDF.");
        } catch (PrinterException e) {
            e.printStackTrace();
            System.out.println("Error al imprimir el archivo PDF.");
        }
    }

    public List<Producto> getProducts() {
        return cartProducts;
    }


    public Map<String, Integer> getProductListWithQuantities() {
        Map<String, Integer> productsAndQuantities = new HashMap<>();
        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            productsAndQuantities.put(producto.getName(), cantidad);
        }
        return productsAndQuantities;
    }
}
