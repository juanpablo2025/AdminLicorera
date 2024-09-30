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
import org.apache.poi.ss.usermodel.*;
import org.example.model.Mesa;
import org.example.model.Producto;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.ui.UIHelpers.*;
import static org.example.utils.Constants.*;
import static org.example.utils.Constants.EIGHT;

public class VentaMesaManager {
    private List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private List<Integer> cartQuantities = new ArrayList<>(); // Lista para almacenar las cantidades correspondientes

    private static ProductoManager productoManager = new ProductoManager();

    private static JDialog ventaDialog;
    private static JDialog ventaMesaDialog;

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

    public static void initializeMesasSheet() {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Verificar si la pestaña "mesas" ya existe
            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet == null) {
                mesasSheet = workbook.createSheet("mesas");
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

    // Método para cargar las mesas desde el archivo Excel
    private static ArrayList<Mesa> cargarMesasDesdeExcel() {

        final String FILE_NAME = "productos.xlsx";
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

        ArrayList<Mesa> mesas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("mesas"); // Acceder a la hoja llamada "mesas"
            if (mesasSheet != null) {
                for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) { // Empezamos en la fila 1 (saltamos el encabezado)
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        // Leer el ID de la mesa (columna 0)
                        Cell idCell = row.getCell(0);
                        String idText = idCell.getStringCellValue();

                        // Extraer el número de la mesa, por ejemplo, de "Mesa 1" extraer 1
                        int id = extraerNumeroDeTexto(idText);

                        // Leer el estado de la mesa (columna 1)
                        String estado = row.getCell(1).getStringCellValue();
                        Mesa mesa = new Mesa(id);
                        mesa.setOcupada(estado.equalsIgnoreCase("Ocupada"));
                        mesas.add(mesa);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return mesas;
    }

    // Método auxiliar para extraer el número del ID de la mesa
    private static int extraerNumeroDeTexto(String texto) {
        // Remover cualquier cosa que no sea un número del texto
        String numeroTexto = texto.replaceAll("[^0-9]", "");
        return Integer.parseInt(numeroTexto);
    }

    // Método para crear un panel de mesa con botones "Editar" y "Finalizar"
    private static JPanel crearMesaPanel(Mesa mesa) {
        JPanel mesaPanel = new JPanel(new BorderLayout()); // Usar BorderLayout para organizar botones y etiquetas
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        // Bordes más estilizados para las mesas
        mesaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(java.awt.Color.BLACK, 2),
                "Mesa " + 1, // Mostrar el número de la mesa
                TitledBorder.CENTER, TitledBorder.TOP));

        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? java.awt.Color.RED : java.awt.Color.GREEN);

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "Ocupada" : "Libre", SwingConstants.CENTER);
        mesaLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
        mesaLabel.setForeground(java.awt.Color.WHITE);

        // Botón "Editar"
        JButton editarButton = new JButton("Editar");
        editarButton.addActionListener(e -> {
            mostrarProductosMesa(mesa); // Lógica para abrir el diálogo de edición de productos
        });

        // Botón "Finalizar"
        JButton finalizarButton = new JButton("Finalizar");
        finalizarButton.addActionListener(e -> {
            if (mesa.isOcupada()) {
                finalizarMesa(mesa);
                mesaPanel.setBackground(Color.GREEN); // Cambiar el color a libre
                mesaLabel.setText("Libre");
            }
        });

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editarButton);
        buttonPanel.add(finalizarButton);

        // Añadir componentes al panel de la mesa
        mesaPanel.add(mesaLabel, BorderLayout.CENTER); // Etiqueta en el centro
        mesaPanel.add(buttonPanel, BorderLayout.SOUTH); // Botones en la parte inferior

        return mesaPanel;
    }

    private static void agregarMesaAExcel(Mesa nuevaMesa) {
        final String FILE_NAME = "productos.xlsx";
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
        String filePath = FILE_PATH; // Reemplaza con la ruta correcta
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet == null) {
                // Si no existe la hoja "mesas", crearla
                mesasSheet = workbook.createSheet("mesas");
                // Crear encabezado si es una hoja nueva
                Row headerRow = mesasSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Estado");
            }

            // Agregar nueva fila con los datos de la nueva mesa
            int newRowNum = mesasSheet.getLastRowNum() + 1; // La última fila más uno
            Row newRow = mesasSheet.createRow(newRowNum);
            newRow.createCell(0).setCellValue("Mesa " + nuevaMesa.getId()); // ID de la mesa
            newRow.createCell(1).setCellValue(nuevaMesa.isOcupada() ? "Ocupada" : "Libre"); // Estado de la mesa

            // Escribir los cambios en el archivo
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void showVentaMesaDialog() {
        ventaMesaDialog = createDialog(REALIZAR_VENTA, FIVE_HUNDRED, FOUR_HUNDRED, new BorderLayout());

        JPanel inputPanel = createInputPanel();
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);

        JTable table = createProductTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();
        ventaMesaDialog.add(totalPanel, BorderLayout.SOUTH);

        VentaMesaManager ventaMesaManager = new VentaMesaManager();

        JPanel buttonPanel = createButtonPanelMesa(table, ventaMesaManager, ventaMesaDialog);
        ventaMesaDialog.add(buttonPanel, BorderLayout.SOUTH);



        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);

    }

    public static JPanel createButtonPanelMesa(JTable table, VentaMesaManager ventaMesaManager, JDialog compraDialog) {
        JPanel buttonPanel = new JPanel(new GridLayout(ONE, THREE));

        JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaManager);
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaManager);
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaManager, compraDialog);


        buttonPanel.add(agregarProductoButton);
        buttonPanel.add(guardarCompra);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }



    private static JButton createConfirmPurchaseMesaButton(VentaMesaManager ventaMesaManager, JDialog compraDialog) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);
        confirmarCompraButton.addActionListener(e -> {
            try {
                double total = ventaMesaManager.getTotalCartAmount(); // Obtiene el total de la compra
                double dineroRecibido = 0;
                double devuelto = 0;



                // Generar un ID único para la venta
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000);
                LocalDateTime dateTime = LocalDateTime.now();

                // Obtener la lista de productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities(); // Método que debes implementar

// Crear un StringBuilder para construir la lista de productos con nombre y cantidad
                StringBuilder listaProductosEnLinea = new StringBuilder();

// Iterar sobre el mapa de productos y cantidades
                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombreProducto = entrada.getKey(); // Nombre del producto
                    int cantidad = entrada.getValue(); // Cantidad comprada
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    // Añadir la información del producto al StringBuilder
                    listaProductosEnLinea.append(nombreProducto).append(" x").append(cantidad).append(" $").append(precioUnitario).append(" = ").append(precioTotal+"\n");
                }


                //String listaProductosEnLinea = String.join(N, productosComprados.keySet()+" x"+productosComprados.values());

                // Guardar la compra en Excel
                ExcelManager excelManager = new ExcelManager();
                excelManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime);

                // Descontar la cantidad de los productos del stock en el archivo Excel
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    // Actualizar la cantidad del producto en la pestaña de productos
                    Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                        String nombreProducto = entry.getKey();
                        int cantidadComprada = entry.getValue();

                        boolean productoEncontrado = false;

                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                // Suponiendo que el nombre del producto está en la columna 1
                                if (row.getCell(1).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                                    // Asegurarse de que la celda de cantidad no sea nula y sea numérica
                                    Cell cantidadCell = row.getCell(2);
                                    if (cantidadCell != null && cantidadCell.getCellType() == CellType.NUMERIC) {
                                        int cantidadActual = (int) cantidadCell.getNumericCellValue(); // Suponiendo que la cantidad está en la columna 2
                                        int nuevaCantidad = cantidadActual - cantidadComprada;  // Restar la cantidad comprada

                                        // Verificar que no se intente establecer una cantidad negativa
                                        if (nuevaCantidad < 0) {
                                            JOptionPane.showMessageDialog(ventaDialog, "No hay suficiente cantidad del producto '" + nombreProducto + "' en stock.", "Error", JOptionPane.ERROR_MESSAGE);
                                            return; // Salir del método si no hay suficiente stock
                                        } else {
                                            cantidadCell.setCellValue(nuevaCantidad);  // Actualizar la cantidad
                                            productoEncontrado = true;
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(ventaDialog, "La cantidad actual no es válida para el producto '" + nombreProducto + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                                        return; // Salir si la cantidad no es válida
                                    }
                                    break; // Salir del bucle una vez que se actualiza el producto
                                }
                            }
                        }

                        // Verificar si el producto fue encontrado y actualizado
                        if (!productoEncontrado) {
                            JOptionPane.showMessageDialog(ventaDialog, "Producto '" + nombreProducto + "' no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Guardar la actualización de productos
                    try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                        workbook.write(fos);
                        System.out.println("Cantidad de productos actualizada exitosamente.");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Preguntar al usuario si quiere imprimir la factura
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario selecciona 'Sí', generar e imprimir la factura
                    ventaMesaManager.generarFactura(ventaID, Collections.singletonList(String.valueOf(listaProductosEnLinea)), total, dateTime);
                    // Código para imprimir el recibo o mostrar un mensaje indicando que el recibo ha sido generado.
                }

                // Si hay cambio, mostrarlo en un diálogo
                if (devuelto > 0) {
                    JOptionPane.showMessageDialog(ventaDialog, CHANGE + devuelto + PESOS);
                }

                // Mostrar un mensaje de éxito de la compra
                JOptionPane.showMessageDialog(ventaDialog, PURCHASE_SUCCEDED + total);

                // Cerrar el diálogo de la venta
                ventaDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ventaDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });
        return confirmarCompraButton;
    }


    private static JButton createSavePurchaseMesaButton(VentaMesaManager ventaMesaManager) {
        JButton saveCompraButton = new JButton("Guardar Compra");
        saveCompraButton.addActionListener(e -> {
            try {
                double total = ventaMesaManager.getTotalCartAmount(); // Obtener el total de la compra
                LocalDateTime dateTime = LocalDateTime.now();

                // Obtener los productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities();
                StringBuilder listaProductos = new StringBuilder();

                // Construir la lista de productos con cantidad
                for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                    String nombreProducto = entry.getKey();
                    int cantidad = entry.getValue();
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    listaProductos.append(nombreProducto)
                            .append(" x")
                            .append(cantidad)
                            .append(" $")
                            .append(precioUnitario)
                            .append(" = ")
                            .append(precioTotal)
                            .append("\n");
                }

                // Guardar la compra en la pestaña "mesas"
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null && row.getCell(0).getStringCellValue().equalsIgnoreCase("1")) {
                                // Cambiar estado a "Ocupada" y guardar la compra
                                row.getCell(1).setCellValue("Ocupada");
                                row.createCell(2).setCellValue(listaProductos.toString());
                                row.createCell(3).setCellValue(total);
                                break;
                            }
                        }

                        try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                            workbook.write(fos);
                        }

                        JOptionPane.showMessageDialog(null, "Compra guardada para la " + "1" + ".");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return saveCompraButton;
    }

    public void reanudarCompraMesa(String mesaID) {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet != null) {
                for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null && row.getCell(0).getStringCellValue().equalsIgnoreCase(mesaID)) {
                        String estado = row.getCell(1).getStringCellValue();
                        if (estado.equals("Ocupada")) {
                            String productosGuardados = row.getCell(2).getStringCellValue();
                            double totalGuardado = row.getCell(3).getNumericCellValue();
                            // Restaurar productos y total en la interfaz
                            System.out.println("Productos: " + productosGuardados);
                            System.out.println("Total: " + totalGuardado);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void finalizarMesa(Mesa mesa) {
        // Guardar productos y total de la cuenta en Excel
        if (mesa.getTotalCuenta() > 0) {
            String compraID = String.valueOf(System.currentTimeMillis() % 1000);
            LocalDateTime now = LocalDateTime.now();

            List<String> listaProductos = mesa.getProductos().stream()
                    .map(p -> p.getName() + " x" + p.getCantidad())
                    .collect(Collectors.toList());

            String productosEnLinea = String.join("\n", listaProductos);

            ExcelManager excelManager = new ExcelManager();
            excelManager.savePurchase(compraID, productosEnLinea, mesa.getTotalCuenta(), now);
        }

        // Finalizar la mesa y resetear su estado
        mesa.finalizarMesa();
    }

    private static void mostrarProductosMesa(Mesa mesa) {
        /*JDialog productosDialog = new JDialog();
        productosDialog.setTitle("Venta - Mesa " + mesa.getId()); // Asumimos que cada mesa tiene un ID o número
        productosDialog.setSize(400, 300);*/
        showVentaMesaDialog();
        /*/ Implementar lógica para seleccionar productos y añadirlos a la mesa
        JComboBox<String> productComboBox = new JComboBox<>(obtenerNombresProductos());
        JTextField cantidadField = new JTextField(5);
        JButton addButton = new JButton("Añadir Producto");

        addButton.addActionListener(e -> {
            Producto producto = productoManager.getProductByName((String) productComboBox.getSelectedItem());
            int cantidad = Integer.parseInt(cantidadField.getText());
            producto.setCantidad(cantidad);

            mesa.añadirProducto(producto); // Añadir producto a la mesa
        });*/

        // Añadir componentes al diálogo
       /* productosDialog.setLayout(new FlowLayout());
       /* productosDialog.add(new JLabel("Producto:"));
        productosDialog.add(productComboBox);
        productosDialog.add(new JLabel("Cantidad:"));
        productosDialog.add(cantidadField);
        productosDialog.add(addButton);

        productosDialog.setLocationRelativeTo(null);
        productosDialog.setVisible(true);*/
    }
}
