package org.example.ui.uiuser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.usermanager.ExcelUserManager;
import org.example.manager.usermanager.ProductoUserManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.utils.FormatterHelpers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.manager.usermanager.ExcelUserManager.actualizarCantidadStockExcel;
import static org.example.manager.usermanager.ExcelUserManager.cargarProductosMesaDesdeExcel;
import static org.example.manager.usermanager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiuser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;

import static org.example.utils.FormatterHelpers.ConfigurationGlobal.TRM;

public class UIUserVenta extends Panel {

    private static final ProductoUserManager productoUserManager = new ProductoUserManager();

    private static final Logger logger =  LoggerFactory.getLogger(UIUserVenta.class);

    public UIUserVenta(List<String[]> productos, String mesaID, JPanel mainPanel, JFrame frame) {
        AtomicReference<Double> sumaTotal = new AtomicReference<>(0.0);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1366, 720));

        JLabel titleLabel = createTitleLabel(mesaID);
        JTable table = createConfiguredTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        loadProductsToTable(productos, tableModel, sumaTotal);
        JTextField totalField = createTotalField(sumaTotal.get());
        JPanel totalPanel = createTotalPanel();
        totalPanel.add(totalField, BorderLayout.CENTER);

        setupTableListener(tableModel, totalField);

        JScrollPane tableScrollPane = new JScrollPane(table);
        JPanel inputPanel = UIHelpers.createInputPanel(table);

        add(titleLabel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.EAST);

        JPanel buttonPanel = createButtonPanel(table, (JDialog) compraDialog, mesaID, mainPanel, frame);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JLabel createTitleLabel(String mesaID) {
        JLabel titleLabel = new JLabel("Venta " + mesaID, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(FONDO_PRINCIPAL);
        try (InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return titleLabel;
    }

    private JTable createConfiguredTable() {
        JTable table = createProductTable();
        table.getColumnModel().getColumn(TWO).setCellRenderer(new UIHelpers.CurrencyRenderer());

        Font font = new Font(ARIAL_FONT, Font.PLAIN, 18);
        table.setFont(font);
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        header.setBackground(new Color(28, 28, 28));
        header.setForeground(new Color(201, 41, 41));

        table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        table.setBackground(FONDO_PRINCIPAL);
        table.setSelectionBackground(new Color(173, 216, 255));
        table.setSelectionForeground(Color.BLACK);

        return table;
    }

    private void loadProductsToTable(List<String[]> productos, DefaultTableModel tableModel, AtomicReference<Double> sumaTotal) {
        tableModel.setRowCount(ZERO);
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[ZERO].trim();
                int cantidad = Integer.parseInt(productoDetalles[ONE].substring(ONE).trim());
                double precioUnitario = Double.parseDouble(productoDetalles[TWO].substring(ONE).trim());
                double total = cantidad * precioUnitario;

                tableModel.addRow(new Object[]{nombreProducto, cantidad, FormatterHelpers.formatearMoneda(precioUnitario), total});
                sumaTotal.updateAndGet(v -> v + total);
            } catch (NumberFormatException ex) {
                logger.error("Error al cargar el producto: {}", Arrays.toString(productoDetalles), ex);
            }
        }
    }

    private JTextField createTotalField(double total) {
        JTextField totalField = new JTextField(TOTAL_PRICE + FormatterHelpers.formatearMoneda(total) + PESOS);
        totalField.setFont(new Font(ARIAL_FONT, Font.BOLD, 26));
        totalField.setForeground(Color.RED);
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(SwingConstants.CENTER);
        totalField.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));
        totalField.setVisible(total > ZERO);
        totalField.setBackground(FONDO_PRINCIPAL);

        try (InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            if (fontStream == null) throw new IOException("No se encontró la fuente");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);
            totalField.setFont(customFont);
        } catch (Exception e) {
            logger.error("Error al cargar la fuente", e);
        }

        return totalField;
    }

    private void setupTableListener(DefaultTableModel tableModel, JTextField totalField) {
        tableModel.addTableModelListener(new TableModelListener() {
            private boolean updatingTable = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (updatingTable) return;

                updatingTable = true;
                try {
                    double nuevoTotalGeneral = ZERO;
                    for (int i = ZERO; i < tableModel.getRowCount(); i++) {
                        String nombreProducto = (String) tableModel.getValueAt(i, ZERO);
                        int cantidad = (int) tableModel.getValueAt(i, ONE);
                        Producto producto = ProductoUserManager.getProductByName(nombreProducto);

                        double precioUnitario = producto != null ? producto.getPrice() : 0.0;
                        double subtotal = cantidad * precioUnitario;
                        tableModel.setValueAt(subtotal, i, THREE);
                        nuevoTotalGeneral += subtotal;
                    }

                    totalField.setText(TOTAL_PRICE + FormatterHelpers.formatearMoneda(nuevoTotalGeneral) + PESOS);
                    totalField.setVisible(nuevoTotalGeneral > ZERO);
                } finally {
                    updatingTable = false;
                }
            }
        });
    }


    private JPanel createButtonPanel(JTable table, JDialog compraDialog, String mesaID, JPanel mainPanel,JFrame frame) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel topButtonsPanel = new JPanel(new GridLayout(ONE, TWO, ONE, TEN));
        JButton guardarCompra = createSavePurchaseMesaButton( mesaID, table);
        guardarCompra.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));

        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(compraDialog, mesaID, table,frame);
        confirmarCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));

        List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
        boolean productosEnExcel = !productosPrevios.isEmpty();
        confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > ZERO);

        table.getModel().addTableModelListener(e -> confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0));

        guardarCompra.setPreferredSize(new Dimension(ZERO, 50));
        confirmarCompraButton.setPreferredSize(new Dimension(ZERO, 50));

        topButtonsPanel.add(guardarCompra);
        topButtonsPanel.add(confirmarCompraButton);

        buttonPanel.add(topButtonsPanel, BorderLayout.CENTER);

        JButton closeButton = getCloseButton(mainPanel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(closeButton);

        buttonPanel.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setBackground(FONDO_PRINCIPAL);
        return buttonPanel;
    }

    private static JButton getCloseButton(JPanel mainPanel) {
        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, SEVEN) : new Color(228, 185, 42));
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(FONDO_PRINCIPAL);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, MESAS);
        });
        return closeButton;
    }


    private static Map<String, Integer> actualizarProductosDesdeTabla(JTable productosTable) {
        Map<String, Integer> productosActualizados = new HashMap<>();
        DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();

        for (int i = ZERO; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, ZERO);
            int cantidad = (int) tableModel.getValueAt(i, ONE);
            productosActualizados.put(nombreProducto, cantidad);
        }

        return productosActualizados;
    }

    public static JButton createConfirmPurchaseMesaButton(JDialog compraDialog, String mesaID,JTable productosTable,JFrame frame) {

        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);

        confirmarCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        confirmarCompraButton.setForeground(Color.WHITE);
        confirmarCompraButton.setBackground(new Color(168, 230, 207));
        confirmarCompraButton.setBorderPainted(false);
        confirmarCompraButton.setFocusPainted(false);
        confirmarCompraButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmarCompraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, TWO),
                BorderFactory.createEmptyBorder(TEN, 20, TEN, 20)
        ));

        confirmarCompraButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmarCompraButton.setBackground(new Color(ZERO, 201, 87));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                confirmarCompraButton.setBackground(new Color(168, 230, 207));
            }
        });

        confirmarCompraButton.addActionListener(e -> {
            try {

                Map<String, Integer> productosComprados = actualizarProductosDesdeTabla(productosTable);
                
                double total = ZERO;
                String ventaID = System.currentTimeMillis() % 1000 + " " + mesaID;
                LocalDateTime dateTime = LocalDateTime.now();
                StringBuilder listaProductosEnLinea = new StringBuilder();

                Map<String, Integer> cantidadTotalPorProducto = new HashMap<>();
                Map<String, Double> precioUnitarioPorProducto = new HashMap<>();



                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombre = entrada.getKey();
                    int cantidad = entrada.getValue();
                    Producto producto = ProductoUserManager.getProductByName(nombre);

                    cantidadTotalPorProducto.put(nombre, cantidad);
                    precioUnitarioPorProducto.put(nombre, producto.getPrice());
                }

                for (Map.Entry<String, Integer> entrada : cantidadTotalPorProducto.entrySet()) {
                    String nombreProducto = entrada.getKey();
                    int cantidadTotal = entrada.getValue();
                    double precioUnitario = precioUnitarioPorProducto.get(nombreProducto);
                    double precioTotal = precioUnitario * cantidadTotal;

                    listaProductosEnLinea.append(nombreProducto)
                            .append(" x").append(cantidadTotal)
                            .append(" $").append(precioUnitario)
                            .append(" = ").append(precioTotal).append("\n");

                    total += precioTotal;
                }

                String tipoPago = paySelection(compraDialog, total,frame);
                if (tipoPago == null) {
                    return;
                }

                ExcelUserManager excelUserManager = new ExcelUserManager();
                excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total ,tipoPago);
                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet(MESAS);
                    if (mesasSheet != null) {
                        for (int i = ONE; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null) {
                                Cell idCell = row.getCell(ZERO);
                                if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(mesaID)) {
                                    Cell estadoCell = row.getCell(ONE);
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(ONE);
                                    }
                                    estadoCell.setCellValue("Libre");

                                    Cell productosCell = row.getCell(TWO);
                                    if (productosCell != null) {
                                        productosCell.setCellValue("");
                                    }

                                    Cell totalCell = row.getCell(THREE);
                                    if (totalCell != null) {
                                        totalCell.setCellValue(0.0);
                                    }
                                    break;
                                }
                            }
                        }

                        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                            workbook.write(fos);
                        }
                    }
                } catch (IOException ex) {
                    logger.error("Error al guardar la compra en Excel", ex);
                }

                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime, tipoPago);
                }

                NumberFormat formatusd = NumberFormat.getCurrencyInstance(Locale.US);
                Double totalDolar = total/TRM;
                String mensaje = String.format(
                        "<html>" +
                                "<div style='font-size:16pt;'>" +
                                "✔ <b>%s</b><br><br>" +
                                "Por un total de:<br>" +
                                "<span style='font-size:28pt; color:#2ecc71;'><b>$%s Pesos</b></span><br>" +
                                "<span style='font-size:20pt; color:#000080;'><b>%s USD</b></span>" +
                                "</div></html>",
                        PURCHASE_SUCCEDED,
                        NumberFormat.getInstance(new Locale("es", "CO")).format(total),
                        formatusd.format(totalDolar)
                );

                JOptionPane.showMessageDialog(
                        compraDialog,
                        mensaje,
                        "Compra Exitosa",
                        JOptionPane.INFORMATION_MESSAGE
                );
                actualizarCantidadStockExcel(cantidadTotalPorProducto);
                ProductoUserManager.limpiarCarrito();
                SwingUtilities.invokeLater(() -> {
                    Window window = SwingUtilities.getWindowAncestor(confirmarCompraButton);
                    if (window != null) {
                        window.setVisible(false);
                        window.dispose();
                    }
                    mainUser();
                });



            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, "Monto inválido.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return confirmarCompraButton;
    }

    public static JButton createSavePurchaseMesaButton( String mesaID, JTable productosTable) {
        JButton saveCompraButton = new JButton("Guardar Compra");


        saveCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        saveCompraButton.setForeground(Color.WHITE);
        saveCompraButton.setBackground(new Color(255, 111, 97));
        saveCompraButton.setOpaque(true);
        saveCompraButton.setBorderPainted(false);
        saveCompraButton.setFocusPainted(false);
        saveCompraButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveCompraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, TWO),
                BorderFactory.createEmptyBorder(TEN, 20, TEN, 20)
        ));

        saveCompraButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveCompraButton.setBackground(new Color(201, 41, 41));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveCompraButton.setBackground(new Color(255, 111, 97));
            }
        });

        saveCompraButton.addActionListener(e -> {
            try {
                DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();
                int rowCount = tableModel.getRowCount();

                if (rowCount == ZERO) {
                    limpiarMesaEnExcel(mesaID);
                    ProductoUserManager.limpiarCarrito();
                    JOptionPane.showMessageDialog(null, "La mesa " + mesaID + " ha sido limpiada.");
                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                        if (window != null) {
                            window.dispose();
                        }
                        mainUser();
                    });

                    return;
                }


                Map<String, Integer> productosComprados = new HashMap<>();
                for (int i = ZERO; i < rowCount; i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, ZERO);
                    int cantidad = (int) tableModel.getValueAt(i, ONE);
                    productosComprados.put(nombreProducto, cantidad);
                }


                double total = productoUserManager.getTotalCartAmount();

                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        boolean mesaEncontrada = false;
                        for (int i = ONE; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null) {
                                Cell idCell = row.getCell(ZERO);
                                if (idCell != null && idCell.getStringCellValue().trim().equalsIgnoreCase(mesaID.trim())) {
                                    mesaEncontrada = true;

                                    Cell estadoCell = row.getCell(ONE);
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(ONE);
                                    }
                                    estadoCell.setCellValue("Ocupada");

                                    Cell productosCell = row.getCell(TWO);
                                    if (productosCell == null) {
                                        productosCell = row.createCell(TWO);
                                    }

                                    StringBuilder listaProductos = new StringBuilder();
                                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                                        String nombreProducto = entry.getKey();
                                        int cantidadComprada = entry.getValue();
                                        Producto producto = ProductoUserManager.getProductByName(nombreProducto);
                                        double precioUnitario = producto.getPrice();
                                        double precioTotal = precioUnitario * cantidadComprada;

                                        listaProductos.append(nombreProducto)
                                                .append(" x")
                                                .append(cantidadComprada)
                                                .append(" $")
                                                .append(precioUnitario)
                                                .append(" = ")
                                                .append(precioTotal)
                                                .append("\n");
                                    }

                                    productosCell.setCellValue(listaProductos.toString());

                                    Cell totalCell = row.getCell(THREE);
                                    if (totalCell == null) {
                                        totalCell = row.createCell(THREE);
                                    }
                                    totalCell.setCellValue(total);

                                    break;
                                }
                            }
                        }

                        if (!mesaEncontrada) {
                            JOptionPane.showMessageDialog(null, "Mesa " + mesaID + " no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        guardarCambiosWorkbook(workbook);

                        JOptionPane.showMessageDialog(null, "Compra guardada para la mesa: " + mesaID + ".");
                        tableModel.setRowCount(ZERO);
                        ProductoUserManager.limpiarCarrito();

                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                            if (window != null) {
                                window.dispose();
                            }
                            mainUser();
                        });

                    } else {
                        JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    logger.error("Error al guardar la compra en Excel", ex);
                    JOptionPane.showMessageDialog(null, "Error al guardar la compra.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return saveCompraButton;
    }

    private static void guardarCambiosWorkbook(Workbook workbook) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
            workbook.write(fos);
        }
    }

    public static JSONObject mostrarDialogoFactura(JFrame parentFrame) {
        JTextField nombreField = new JTextField(20);
        JTextField apellidoField = new JTextField(20);
        JTextField identificacionField = new JTextField(15);
        JTextField direccionField = new JTextField(30);
        JTextField ciudadField = new JTextField("11001"); // Bogotá
        JTextField estadoField = new JTextField("11");    // Cundinamarca
        JTextField paisField = new JTextField("CO");
        JTextField telefonoField = new JTextField("3001234567");
        JTextField correoField = new JTextField(25);

        JPanel panel = new JPanel(new GridLayout(ZERO, TWO, FIVE, FIVE));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Apellido:"));
        panel.add(apellidoField);
        panel.add(new JLabel("Identificación:"));
        panel.add(identificacionField);
        panel.add(new JLabel("Dirección:"));
        panel.add(direccionField);
        panel.add(new JLabel("Ciudad (Código):"));
        panel.add(ciudadField);
        panel.add(new JLabel("Departamento (Código):"));
        panel.add(estadoField);
        panel.add(new JLabel("País (Código):"));
        panel.add(paisField);
        panel.add(new JLabel("Teléfono:"));
        panel.add(telefonoField);
        panel.add(new JLabel("Correo electrónico:"));
        panel.add(correoField);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Datos de Facturación Electrónica",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new JSONObject()
                    .put("person_type", "Person")
                    .put("id_type", "13")
                    .put("identification", identificacionField.getText().trim())
                    .put("branch_office", 0)
                    .put("name", new JSONArray().put(nombreField.getText().trim()).put(apellidoField.getText().trim()))
                    .put("address", new JSONObject()
                            .put("address", direccionField.getText().trim())
                            .put("city", new JSONObject()
                                    .put("country_code", paisField.getText().trim())
                                    .put("state_code", estadoField.getText().trim())
                                    .put("city_code", ciudadField.getText().trim())
                            )
                    )
                    .put("phones", new JSONArray()
                            .put(new JSONObject().put("number", telefonoField.getText().trim()))
                    )
                    .put("contacts", new JSONArray()
                            .put(new JSONObject()
                                    .put("first_name", nombreField.getText().trim())
                                    .put("last_name", apellidoField.getText().trim())
                                    .put("email", correoField.getText().trim())
                            )
                    );
        } else {
            return null;
        }
    }

    private static void limpiarMesaEnExcel(String mesaID) {
        try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet != null) {
                for (int i = ONE; i <= mesasSheet.getLastRowNum(); i++) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(ZERO);
                        if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(mesaID)) {

                            Cell estadoCell = row.getCell(ONE);
                            if (estadoCell == null) {
                                estadoCell = row.createCell(ONE);
                            }
                            estadoCell.setCellValue("Libre");

                            Cell productosCell = row.getCell(TWO);
                            if (productosCell == null) {
                                productosCell = row.createCell(TWO);
                            }
                            productosCell.setCellValue("");

                            Cell totalCell = row.getCell(THREE);
                            if (totalCell == null) {
                                totalCell = row.createCell(THREE);
                            }
                            totalCell.setCellValue(0.0);

                            break;
                        }
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                    workbook.write(fos);
                }

            } else {
                JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            logger.error("Error al limpiar la mesa en Excel", ex);
            JOptionPane.showMessageDialog(null, "Error al limpiar la mesa.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String paySelection(JDialog compraDialog, double total,JFrame frame) {
        ImageIcon iconoBancolombia = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/bancolombia.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        ImageIcon iconoNequi = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/nequi.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        ImageIcon iconoEfectivo = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/dinero.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        ImageIcon iconoDaviplata = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/Daviplata.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
        ImageIcon iconoDatafono = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/datafono.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
        ImageIcon iconoPaypal = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/Paypal.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));

        JDialog dialogoPago = new JDialog(compraDialog, "Seleccione el método de pago", true);
        dialogoPago.setSize(1300, 400);
        dialogoPago.setLayout(new BorderLayout(20, 20));
        dialogoPago.setResizable(false);
        double totalDollar = total/TRM;
        JLabel totalLabel = getJLabel(total, totalDollar);

        JPanel panelPago = new JPanel();
        panelPago.setLayout(new GridLayout(TWO, THREE, 20, 20));
        panelPago.setBorder(BorderFactory.createEmptyBorder(TEN, 20, 20, 20));
        panelPago.setBackground(Color.WHITE);

        JButton botonEfectivo = new JButton(EFECTIVO, iconoEfectivo);
        JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
        JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
        JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
        JButton botonDatafono = new JButton("Datafono", iconoDatafono);
        JButton botonPaypal = new JButton("Paypal", iconoPaypal);

        JButton[] botones = { botonEfectivo, botonBancolombia, botonNequi, botonDaviplata, botonDatafono, botonPaypal };
        for (JButton btn : botones) {
            btn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            btn.setBackground(new Color(245, 245, 245));
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(TEN);
            panelPago.add(btn);
        }

        dialogoPago.add(totalLabel, BorderLayout.NORTH);
        dialogoPago.add(panelPago, BorderLayout.CENTER);
        dialogoPago.setLocationRelativeTo(null);
        final String[] tipoPagoSeleccionado = {null};
        final double finalTotal = total;

        configurarBotonConQR(dialogoPago, botonBancolombia, finalTotal, totalDollar, () -> {
            tipoPagoSeleccionado[ZERO] = "Bancolombia - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonNequi, finalTotal, totalDollar, () -> {
            tipoPagoSeleccionado[ZERO] = "Nequi - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonDaviplata, finalTotal, totalDollar, () -> {
            tipoPagoSeleccionado[ZERO] = "Daviplata - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonPaypal, finalTotal, totalDollar, () -> {
                tipoPagoSeleccionado[ZERO] = "Paypal - Transferencia";
                dialogoPago.dispose();
        });

        botonEfectivo.addActionListener(event -> {

            JTextField inputField = new JTextField(TWELVE);
            inputField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));

            JLabel title = new JLabel("Ingrese el dinero recibido:");
            title.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
            title.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            content.add(title);
            content.add(Box.createVerticalStrut(15));
            content.add(inputField);
            content.add(Box.createVerticalStrut(25));

            JButton continuarBtn = new JButton("Continuar");
            continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            continuarBtn.setBackground(new Color(ZERO, 153, ZERO));
            continuarBtn.setForeground(Color.WHITE);
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(150, 40));


            JButton omitirBtn = new JButton("Omitir");
            omitirBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            omitirBtn.setBackground(Color.red);
            omitirBtn.setForeground(Color.WHITE);
            omitirBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            omitirBtn.setFocusPainted(false);
            omitirBtn.setPreferredSize(new Dimension(150, 40));

            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(Color.WHITE);
            btnPanel.add(continuarBtn);
            btnPanel.add(omitirBtn);

            JDialog dialog = new JDialog(compraDialog, "Calcula la Devuelta", true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(content, BorderLayout.CENTER);
            dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(compraDialog);
            dialog.setResizable(false);

            continuarBtn.addActionListener(es -> {
                String input = inputField.getText().trim();
                dialog.dispose();
                if (input.isEmpty()) {
                    tipoPagoSeleccionado[ZERO] = EFECTIVO;
                    dialogoPago.dispose();
                    return;
                }

                try {
                    double dineroRecibido = Double.parseDouble(input);
                    if (dineroRecibido < finalTotal) {
                        JOptionPane.showMessageDialog(compraDialog,
                                "El monto recibido es insuficiente.",
                                ERROR_TITLE,
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    double cambio = dineroRecibido - finalTotal;
                    String cambioFormateado = FormatterHelpers.formatearMoneda(cambio);

                    JLabel label = new JLabel("<html><div style='text-align: center;'>"
                            + "<span style='font-size:16pt;font-weight:bold;'>¿Desea calcular el cambio?</span><br><br>"
                            + "<span style='font-size:20pt;color:green;'>$" + cambioFormateado + " Pesos</span>"
                            + "</div></html>");
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    Object[] options = {"Calcular cambio", "Omitir"};

                    int option = JOptionPane.showOptionDialog(
                            compraDialog,
                            label,
                            "Devuelta",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[ZERO]
                    );

                    if (option == JOptionPane.YES_OPTION) {

                        JOptionPane.showMessageDialog(compraDialog, label, "Cambio devuelto", JOptionPane.INFORMATION_MESSAGE);
                    }

                    dialog.dispose();
                    tipoPagoSeleccionado[ZERO] = EFECTIVO;
                    dialogoPago.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(compraDialog,
                            "Monto inválido.",
                            ERROR_TITLE,
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });


            omitirBtn.addActionListener(es -> {
                tipoPagoSeleccionado[ZERO] = EFECTIVO;
                dialogoPago.dispose();
                dialog.dispose();
            });
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dialog.dispose();
                }
            });
            dialog.setVisible(true);
        });

        botonDatafono.addActionListener(event -> {

            ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/NoDatafono.png"))
                    .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


            JLabel montoLabel = new JLabel(TOTAL_PRICE + FormatterHelpers.formatearMoneda(finalTotal) + PESOS);

            montoLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
            montoLabel.setForeground(new Color(ZERO, 153, ZERO));
            montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel montoLabel2 = new JLabel(FormatterHelpers.formatearMoneda(totalDollar) + " USD");
            montoLabel2.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            montoLabel2.setForeground(new Color(ZERO, ZERO, 128));
            montoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);


            JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                    + "Registra la compra en el Datafono.<br>Y verifica sí la transacción fue exitosa antes de continuar."
                    + "</div></html>");
            instruccion.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
            instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton continuarBtn = new JButton("Continuar");
            continuarBtn.setBackground(new Color(ZERO, 153, ZERO));
            continuarBtn.setForeground(Color.WHITE);
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 18));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(200, 50));
            continuarBtn.setMaximumSize(new Dimension(200, 50));
            continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago Datafono", true);
            dialogoQR.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialogoQR.setLayout(new BoxLayout(dialogoQR.getContentPane(), BoxLayout.Y_AXIS));

            continuarBtn.addActionListener(es -> {
                dialogoQR.dispose();
                tipoPagoSeleccionado[0] = "Datafono";
                dialogoPago.dispose();

            });

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            content.add(montoLabel);
            content.add(Box.createVerticalStrut(15));
            content.add(montoLabel2);
            content.add(Box.createVerticalStrut(15));
            content.add(qrLabel);
            content.add(Box.createVerticalStrut(20));
            content.add(instruccion);
            content.add(Box.createVerticalStrut(25));
            content.add(continuarBtn);

            dialogoQR.getContentPane().add(content);
            dialogoQR.pack();
            dialogoQR.setResizable(false);
            dialogoQR.setLocationRelativeTo(compraDialog);
            dialogoQR.setVisible(true);
        });

        dialogoPago.setLocationRelativeTo(compraDialog);
        dialogoPago.setVisible(true);

        if (tipoPagoSeleccionado[ZERO] == null) {
            JOptionPane.showMessageDialog(compraDialog, "No se seleccionó un método de pago.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return null;
        }

         /*if(ConfigAdminManager.isElectronicBillingEnabled()) {

             JSONObject clienteSiigo = UIUserVenta.mostrarDialogoFactura(frame);
                   if (clienteSiigo != null) {
                       // Usar clienteSiigo en la creación de la factura
                        mostrarDialogoFactura(frame);
                    }
        }*/

        return tipoPagoSeleccionado[ZERO];
    }

    private static JLabel getJLabel(double total, Double totalDolar) {
        String textoTotal = String.format(
                "<html><div style='text-align:center; font-size:28px; color:#2ecc71;'>" +
                        "<b>Total: $%,.0f Pesos</b><br>" +
                        "<span style='font-size:20pt; color:#000080;'><b>%.2f USD</b></span>" +
                        "</div></html>",
                total, totalDolar
        );
        JLabel totalLabel = new JLabel(textoTotal, SwingConstants.CENTER);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(TWELVE, TEN, ZERO, TEN));
        return totalLabel;
    }

    private static void configurarBotonConQR(JDialog parentDialog, JButton boton, double total, double totalDolar, Runnable onSuccess) {
        boton.addActionListener(event -> {
            ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource(QR))
                    .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel montoLabel = new JLabel(TOTAL_PRICE + FormatterHelpers.formatearMoneda(total) + PESOS);
            montoLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
            montoLabel.setForeground(new Color(ZERO, 153, ZERO));
            montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel montoLabel2 = new JLabel(FormatterHelpers.formatearMoneda(totalDolar) + " USD");
            montoLabel2.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            montoLabel2.setForeground(new Color(ZERO, ZERO, 128));
            montoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                    + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                    + "</div></html>");
            instruccion.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
            instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton continuarBtn = new JButton(CONTINUE);
            continuarBtn.setBackground(new Color(ZERO, 153, ZERO));
            continuarBtn.setForeground(Color.WHITE);
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 18));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(200, 50));
            continuarBtn.setMaximumSize(new Dimension(200, 50));
            continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JDialog dialogoQR = new JDialog(parentDialog, GUIA_QR, true);
            dialogoQR.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            continuarBtn.addActionListener(e -> {
                dialogoQR.dispose();
                onSuccess.run();
            });

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            content.add(montoLabel);
            content.add(Box.createVerticalStrut(15));
            content.add(montoLabel2);
            content.add(Box.createVerticalStrut(15));
            content.add(qrLabel);
            content.add(Box.createVerticalStrut(20));
            content.add(instruccion);
            content.add(Box.createVerticalStrut(25));
            content.add(continuarBtn);

            dialogoQR.getContentPane().add(content);
            dialogoQR.pack();
            dialogoQR.setResizable(false);
            dialogoQR.setLocationRelativeTo(parentDialog);
            dialogoQR.setVisible(true);
        });
    }
}
