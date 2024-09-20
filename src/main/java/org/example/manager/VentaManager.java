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
import org.example.model.Producto;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.utils.Constants.*;


public class VentaManager {
    private List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private List<Integer> cartQuantities = new ArrayList<>(); // Lista para almacenar las cantidades correspondientes

    ;

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
            productList.add( cantidad + producto.getName() + PRODUCT_NETO + totalProducto + PESOS);
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

    // Método opcional para limpiar el carrito después de una compra
    public void clearCart() {
        cartProducts.clear();
        cartQuantities.clear();
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

    public static void generarFactura(String ventaID, List<String> productos, double totalCompra, double dineroRecibido, double dineroDevuelto, LocalDateTime fechaHora) {
        try {
            // Dimensiones del papel térmico
            float anchoMm = EIGHTY_F;  // ancho en mm
            float altoMm = ONE_HUNDRED_FIFTY_F;  // alto en mm (ajustable)
            float anchoPuntos = anchoMm * WIDE_DOTS;
            float altoPuntos = altoMm * HEIGHT_DOTS;

            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);

            String nombreArchivo = BILL_FILE + ventaID + PDF_FORMAT;
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
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(NIT)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(DIRECCION)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(TELEFONO)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(new String(new char[FOURTY_SIX]).replace(SLASH_ZERO, EQUALS))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            // Detalles de la compra
            document.add(new Paragraph(BILL_ID + ventaID)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT));

            document.add(new Paragraph(BILL_FECHA_HORA + fechaHora)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            document.add(new Paragraph(BILL_PRODUCTS)
                    .setFont(fontBold)
                    .setFontSize(TEN));

            // Productos
            for (String producto : productos) {
                document.add(new Paragraph(producto)
                        .setFont(fontNormal)
                        .setFontSize(EIGHT));
            }

            document.add(new Paragraph(new String(new char[FOURTY_SIX]).replace(SLASH_ZERO, EQUALS))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            // Totales
            Table table = new Table(new float[]{THREE, TWO});
            table.setWidth(UnitValue.createPercentValue(ONE_HUNDRED));

            addTableRow(table, TOTAL_BILL, PESO_SIGN + totalCompra + PESOS);
            /*addTableRow(table, "Dinero Recibido", "$" + dineroRecibido);
            addTableRow(table, "Dinero Devuelto", "$" + dineroDevuelto);*/

            document.add(table);

            document.add(new Paragraph(new String(new char[FOURTY_SIX]).replace(SLASH_ZERO, EQUALS))
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setMarginBottom(FIVE));

            document.add(new Paragraph(THANKS_BILL)
                    .setFont(fontNormal)
                    .setFontSize(EIGHT)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            abrirPDF(nombreArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void abrirPDF(String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
