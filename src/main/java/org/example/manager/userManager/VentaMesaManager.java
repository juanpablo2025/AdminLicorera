package org.example.manager.userManager;

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
import org.apache.poi.ss.usermodel.*;

import org.example.model.Mesa;
import org.example.model.Producto;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


import static org.example.manager.userManager.ExcelManager.agregarMesaAExcel;
import static org.example.manager.userManager.ExcelManager.cargarMesasDesdeExcel;
import static org.example.manager.userManager.PrintManager.abrirPDF;
import static org.example.ui.uiUser.UIMesas.crearMesaPanel;
import static org.example.utils.Constants.*;
import static org.example.utils.Constants.EIGHT;

public class VentaMesaManager {
    private List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private List<Integer> cartQuantities = new ArrayList<>(); // Lista para almacenar las cantidades correspondientes
    // Método para mostrar las mesas en la interfaz
    private static void showMesas() {
        JFrame mesasFrame = new JFrame("Administrar Mesas");
        mesasFrame.setSize(1200, 600);
        mesasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mesasPanel = new JPanel();
        mesasPanel.setLayout(new GridLayout(0, 5)); // Filas de 5 mesas

        // Cargar las mesas desde el archivo Excel
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        // Mostrar las mesas cargadas desde el archivo Excel
        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf((i + 1))); // Asignar ID basado en la posición
            JPanel mesaPanel = crearMesaPanel(mesa,mesasFrame); // Pasar el objeto Mesa
            mesasPanel.add(mesaPanel);
        }

        // Botón para añadir más mesas
        JButton addMesaButton = new JButton("Añadir Mesa");
        addMesaButton.addActionListener(e -> {
            // Generar un nuevo ID basado en la cantidad actual de mesas
            String nuevoID = String.valueOf(mesas.size() + 1); // Asegurarse de que el ID sea único
            Mesa nuevaMesa = new Mesa(nuevoID); // Crear la nueva mesa con el ID basado en el nuevo ID

            // Añadir la nueva mesa a la lista de mesas
            mesas.add(nuevaMesa);

            // Crear el panel para la nueva mesa
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa,mesasFrame);
            mesasPanel.add(nuevaMesaPanel);

            // Actualizar el panel de mesas
            mesasPanel.revalidate();
            mesasPanel.repaint();

            // Guardar la nueva mesa en el archivo Excel
            agregarMesaAExcel(nuevaMesa);
        });

        // Panel inferior con el botón para añadir mesas
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);

        mesasFrame.setLayout(new BorderLayout());
        mesasFrame.add(mesasPanel, BorderLayout.CENTER);
        mesasFrame.add(bottomPanel, BorderLayout.SOUTH);

        mesasFrame.setLocationRelativeTo(null);
        mesasFrame.setVisible(true);
    }

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



    public Map<String, Integer> getProductListWithQuantities() {
        Map<String, Integer> productsAndQuantities = new HashMap<>();
        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            productsAndQuantities.put(producto.getName(), cantidad);
        }
        return productsAndQuantities;
    }

    public static void initializeMesasSheet() {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Verificar si la pestaña "Mesas" ya existe
            Sheet mesasSheet = workbook.getSheet("Mesas");
            if (mesasSheet == null) {
                mesasSheet = workbook.createSheet("Mesas");
                Row headerRow = mesasSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Mesa ID");
                headerRow.createCell(1).setCellValue("Estado");
                headerRow.createCell(2).setCellValue("Productos");
                headerRow.createCell(3).setCellValue("Total");

                // Crear 10 mesas por defecto
                for (int i = 1; i <= 10; i++) {
                    Row row = mesasSheet.createRow(i);
                    row.createCell(0).setCellValue("Mesa " + i);
                    row.createCell(1).setCellValue("Libre");  // Estado inicial
                }

                try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                    workbook.write(fos);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void removeProductFromCart(int row) {
    }
}
