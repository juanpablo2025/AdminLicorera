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

public class CompraManager {
    private List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private List<Integer> cartQuantities = new ArrayList<>(); // Lista para almacenar las cantidades correspondientes

    ;

    // Método para añadir un producto al carrito
    public void addProductToCart(Producto producto, int cantidad) {
        // Buscar si el producto ya está en el carrito
        int index = cartProducts.indexOf(producto);
        if (index != -1) {
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
        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            double totalProducto = producto.getPrice() * cantidad;
            productList.add(producto.getName() + " | Cantidad: " + cantidad + " | Total: $" + totalProducto);
        }
        return productList;
    }

    // Método para obtener el monto total de la compra
    public double getTotalCartAmount() {
        double total = 0;
        for (int i = 0; i < cartProducts.size(); i++) {
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
            double total = 0.0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                total += (double) tableModel.getValueAt(i, 3); // Columna 3 es el total por producto
            }

            // Verificar si el campo de "dinero recibido" está vacío
            String dineroRecibidoTexto = dineroRecibidoField.getText();
            if (dineroRecibidoTexto.isEmpty()) {
                JOptionPane.showMessageDialog(compraDialog, "Por favor, ingresa el dinero recibido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convertir el texto a un número
            double dineroRecibido = Double.parseDouble(dineroRecibidoTexto);

            // Calcular el dinero devuelto
            double dineroDevuelto = dineroRecibido - total;

            // Mostrar el resultado con un diálogo
            if (dineroDevuelto < 0) {
                JOptionPane.showMessageDialog(compraDialog, "El cliente necesita pagar más: $" + Math.abs(dineroDevuelto), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                devueltoLabel.setText("Devuelto: $" + dineroDevuelto);
                JOptionPane.showMessageDialog(compraDialog, "El dinero devuelto es: $" + dineroDevuelto, "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(compraDialog, "Dinero recibido debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addTableRow(Table table, String key, String value) {
        table.addCell(new Paragraph(key).setFontSize(8));
        table.addCell(new Paragraph(value).setFontSize(8));
    }

    public static void generarFactura(String compraID, List<String> productos, double totalCompra, double dineroRecibido, double dineroDevuelto, LocalDateTime fechaHora) {
        try {
            // Dimensiones del papel térmico
            float anchoMm = 80;  // ancho en mm
            float altoMm = 150;  // alto en mm (ajustable)
            float anchoPuntos = anchoMm * 2.83465f;
            float altoPuntos = altoMm * 2.83465f;

            PageSize pageSize = new PageSize(anchoPuntos, altoPuntos);

            String nombreArchivo = "factura_" + compraID + ".pdf";
            File pdfFile = new File(nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, pageSize);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Márgenes ajustados
            document.setMargins(5, 5, 5, 5);

            // Encabezado de la factura
            document.add(new Paragraph("Factura de Compra")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            document.add(new Paragraph("Licorera CR La 70")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("NIT: 21468330-1")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Dirección: CR 70 # 46 - 80")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Teléfono: 411 19 00")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(new String(new char[46]).replace("\0", "="))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            // Detalles de la compra
            document.add(new Paragraph("ID de la compra: " + compraID)
                    .setFont(fontNormal)
                    .setFontSize(8));

            document.add(new Paragraph("Fecha y Hora: " + fechaHora)
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            document.add(new Paragraph("Productos:")
                    .setFont(fontBold)
                    .setFontSize(10));

            // Productos
            for (String producto : productos) {
                document.add(new Paragraph(producto)
                        .setFont(fontNormal)
                        .setFontSize(8));
            }

            document.add(new Paragraph(new String(new char[46]).replace("\0", "="))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            // Totales
            Table table = new Table(new float[]{3, 2});
            table.setWidth(UnitValue.createPercentValue(100));

            addTableRow(table, "Total Compra", "$" + totalCompra);
            addTableRow(table, "Dinero Recibido", "$" + dineroRecibido);
            addTableRow(table, "Dinero Devuelto", "$" + dineroDevuelto);

            document.add(table);

            document.add(new Paragraph(new String(new char[46]).replace("\0", "="))
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setMarginBottom(5));

            document.add(new Paragraph("Gracias por su compra.")
                    .setFont(fontNormal)
                    .setFontSize(8)
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
            } else {
                System.out.println("No se pudo abrir el archivo PDF: " + pdfFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
