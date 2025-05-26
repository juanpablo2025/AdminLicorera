package org.example.ui.uiuser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.adminmanager.ConfigAdminManager;
import org.example.manager.usermanager.ExcelUserManager;
import org.example.manager.usermanager.ProductoUserManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.utils.FormatterHelpers;
import org.example.utils.SiigoInvoice;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

                // Cargar la imagen
                ImageIcon iconBill = new ImageIcon(UIUserMain.class.getResource("/icons/assistant/ImprimirFactura.png")); // Reemplaza con la ruta de tu imagen

                if (iconBill.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    // Manejar error si la imagen no carga
                    iconBill = null; // o podrías usar una imagen de marcador de posición
                }
                iconBill = new ImageIcon(iconBill.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));


                JLabel textLabelBill = new JLabel(PRINT_BILL, SwingConstants.CENTER);
                textLabelBill.setFont(new Font(LOBSTER_FONT, Font.BOLD, 30));

                JLabel countdownLabel = new JLabel("Imprimiendo en 7...", SwingConstants.CENTER);
                countdownLabel.setFont(new Font(LOBSTER_FONT, Font.BOLD, 18));
                countdownLabel.setForeground(Color.RED);

                JPanel panelBill = new JPanel();
                panelBill.setLayout(new BoxLayout(panelBill, BoxLayout.Y_AXIS));
                panelBill.add(textLabelBill);
                panelBill.add(Box.createVerticalStrut(10));
                panelBill.add(new JLabel(iconBill));
                panelBill.add(Box.createVerticalStrut(10));
                panelBill.add(countdownLabel);

                JOptionPane optionPane = new JOptionPane(
                        panelBill,
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_OPTION
                );

                JDialog dialog = optionPane.createDialog(null, COMFIRM_TITLE);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                // CORRECTO: usar javax.swing.Timer
                final int[] secondsLeft = {7};
                double finalTotal = total;
                javax.swing.Timer countdownTimer = new javax.swing.Timer(1000, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        secondsLeft[0]--;
                        countdownLabel.setText("Imprimiendo en " + secondsLeft[0] + "...");
                        if (secondsLeft[0] <= 0) {
                            ((javax.swing.Timer) e.getSource()).stop();
                            optionPane.setValue(JOptionPane.YES_OPTION);
                            dialog.dispose();
                            generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), finalTotal, dateTime, tipoPago);
                        }
                    }
                });
                countdownTimer.setRepeats(true);
                countdownTimer.start();

                dialog.setVisible(true);

                Object selectedValue = optionPane.getValue();
                if (Integer.valueOf(JOptionPane.YES_OPTION).equals(selectedValue)) {
                    countdownTimer.stop();
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime, tipoPago);
                }


                NumberFormat formatusd = NumberFormat.getCurrencyInstance(Locale.US);
                Double totalDolar = total/TRM;

                // Cargar la imagen
                ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/assistant/VentaRealizada.png")); // Ruta de la imagen
                if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    // Manejar error.
                    icon = null;
                }
                icon = new ImageIcon(icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));

                mostrarDialogoCompraExitosa(
                        compraDialog,
                        total,
                        TRM,
                        icon,
                        null  // Puedes pasar null si no quieres ejecutar nada al cerrar
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
    public static void mostrarDialogoCompraExitosa(Window parent, double total, double trm, Icon icon, Runnable onClose) {
        NumberFormat formatusd = NumberFormat.getCurrencyInstance(Locale.US);
        double totalDolar = total / trm;

        Font lobsterFont = new Font("SansSerif", Font.BOLD, 28); // fallback
        try (InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            if (fontStream != null) {
                lobsterFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 28f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel tituloLabel = new JLabel("¡Venta realizada con éxito!", SwingConstants.CENTER);
        tituloLabel.setFont(lobsterFont);
        tituloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String mensaje = String.format(
                "<html><div style='font-size:14pt; text-align:center;'>"
                        + "<span style='font-size:30pt; color:#2ecc71;'><b>$%s Pesos</b></span><br>"
                        + "<span style='font-size:16pt; color:#000080;'><b>%s USD</b></span>"
                        + "</div></html>",
                NumberFormat.getInstance(new Locale("es", "CO")).format(total),
                formatusd.format(totalDolar)
        );
        JLabel mensajeLabel = new JLabel(mensaje, SwingConstants.CENTER);
        mensajeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel imageLabel = new JLabel(icon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countdownLabel = new JLabel("Limpiando mesa en 5...", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 16));
        countdownLabel.setForeground(Color.RED);
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton aceptarBtn = new JButton("Aceptar");
        aceptarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        aceptarBtn.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.add(Box.createVerticalStrut(10));
        panel.add(tituloLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(mensajeLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(countdownLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(aceptarBtn);

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = optionPane.createDialog(parent, "Venta Exitosa");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final int[] countdown = {5};
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown[0]--;
                countdownLabel.setText("Limpiando mesa en " + countdown[0] + "...");
                if (countdown[0] <= 0) {
                    ((Timer) e.getSource()).stop();
                    dialog.dispose();
                    if (onClose != null) onClose.run();
                }
            }
        });
        timer.start();

        aceptarBtn.addActionListener(e -> {
            timer.stop();
            dialog.dispose();
            if (onClose != null) onClose.run();
        });

        dialog.setVisible(true);
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
        Font lobsterFont = new Font("SansSerif", Font.BOLD, 26);
        try (InputStream fontStream = UIUserVenta.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            if (fontStream != null) {
                lobsterFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(32f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel titulo = new JLabel("Facturación Electrónica", SwingConstants.CENTER);
        titulo.setFont(lobsterFont);

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(UIUserVenta.class.getResource("/icons/siigo.png"));
            Image resized = icon.getImage().getScaledInstance(330, 70, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(resized));
        } catch (Exception e) {
            logoLabel.setText("Siigo");
        }

        JTextField nombreField = new JTextField(15);
        JTextField apellidoField = new JTextField(15);
        JTextField identificacionField = new JTextField(15);
        JTextField direccionField = new JTextField(15);
        JTextField ciudadField = new JTextField("11001", 15);
        JTextField estadoField = new JTextField("11", 15);
        JTextField paisField = new JTextField("CO", 15);
        JTextField telefonoField = new JTextField("3001234567", 15);
        JTextField correoField = new JTextField(15);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; formPanel.add(nombreField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; formPanel.add(apellidoField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Identificación:"), gbc);
        gbc.gridx = 1; formPanel.add(identificacionField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; formPanel.add(direccionField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Ciudad (Código):"), gbc);
        gbc.gridx = 1; formPanel.add(ciudadField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Departamento (Código):"), gbc);
        gbc.gridx = 1; formPanel.add(estadoField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("País (Código):"), gbc);
        gbc.gridx = 1; formPanel.add(paisField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; formPanel.add(telefonoField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Correo electrónico:"), gbc);
        gbc.gridx = 1; formPanel.add(correoField, gbc);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrapper.add(titulo);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(logoLabel);
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(formPanel);

        int result = JOptionPane.showConfirmDialog(parentFrame, wrapper, "Datos de Facturación Electrónica",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            if (Stream.of(nombreField, apellidoField, identificacionField, direccionField,
                            ciudadField, estadoField, paisField, telefonoField, correoField)
                    .anyMatch(f -> f.getText().trim().isEmpty())) {
                JOptionPane.showMessageDialog(parentFrame, "⚠ Por favor complete todos los campos.");
                return mostrarDialogoFactura(parentFrame); // Reintentar
            }

            return new JSONObject()
                    .put("person_type", "Person")
                    .put("id_type", "13")
                    .put("identification", identificacionField.getText().trim())
                    .put("branch_office", 0)
                    .put("name", new JSONArray()
                            .put(nombreField.getText().trim())
                            .put(apellidoField.getText().trim()))
                    .put("address", new JSONObject()
                            .put("address", direccionField.getText().trim())
                            .put("city", new JSONObject()
                                    .put("country_code", paisField.getText().trim())
                                    .put("state_code", estadoField.getText().trim())
                                    .put("city_code", ciudadField.getText().trim())))
                    .put("phones", new JSONArray()
                            .put(new JSONObject().put("number", telefonoField.getText().trim())))
                    .put("contacts", new JSONArray()
                            .put(new JSONObject()
                                    .put("first_name", nombreField.getText().trim())
                                    .put("last_name", apellidoField.getText().trim())
                                    .put("email", correoField.getText().trim())));
        }

        return null;
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
        dialogoPago.setSize(910, 400);
        dialogoPago.setLayout(new BorderLayout(20, 20));
        dialogoPago.setResizable(false);
        double totalDollar = total/TRM;
        JLabel totalLabel = getJLabel(total, totalDollar);

        String tilte = "Seleccione el método de pago";
        JLabel titleLabelMetodo = new JLabel(tilte);
        titleLabelMetodo.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        dialogoPago.add( titleLabelMetodo, BorderLayout.CENTER);

        JPanel panelPago = new JPanel();
        panelPago.setLayout(new GridLayout(TWO, THREE, 5, 5));
        panelPago.setBorder(BorderFactory.createEmptyBorder(TEN, 20, 20, 20));
        panelPago.setBackground(Color.WHITE);
        JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
        JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
        JButton botonPaypal = new JButton("Paypal", iconoPaypal);
        JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
        JButton botonDatafono = new JButton("Datafono", iconoDatafono);
        JButton botonEfectivo = new JButton(EFECTIVO, iconoEfectivo);

        JButton[] botones = { botonDaviplata,  botonBancolombia, botonEfectivo, botonPaypal, botonNequi, botonDatafono};
        for (JButton btn : botones) {
            btn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            btn.setBackground(new Color(245, 245, 245));
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(5);
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
            inputField.setText("Ingrese el dinero recibido:"); // Establecer el texto inicial
            inputField.setForeground(Color.GRAY); // Establecer el color del texto inicial como gris

            // Agregar un listener para borrar el texto cuando el campo de texto obtiene el foco
            inputField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (inputField.getText().equals("Ingrese el dinero recibido:")) {
                        inputField.setText(""); // Borrar el texto
                        inputField.setForeground(Color.BLACK); // Cambiar el color del texto a negro
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (inputField.getText().isEmpty()) {
                        inputField.setText("Ingrese el dinero recibido:"); // Restaurar el texto
                        inputField.setForeground(Color.GRAY); // Restaurar el color del texto a gris
                    }
                }
            });

            JLabel title = new JLabel(""); // Eliminar el JLabel title
            title.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
            title.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            // Cargar la imagen
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/assistant/CalcularDevuelta.png"));
            // Crear un JLabel para mostrar la imagen
            icon = new ImageIcon(icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setPreferredSize(new Dimension(300, 300)); // Ajustar el tamaño de la imagen
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Crear la fuente personalizada
            Font customFont = null;
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream(LOBSTER_FONT); // Reemplaza con la ruta de tu fuente
                if (fontStream != null) {
                    customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 24); // Tamaño 24
                } else {
                    customFont = new Font(ARIAL_FONT, Font.BOLD, 48); // Si la fuente no se carga, usa Arial
                }
            } catch (IOException | FontFormatException e) {
                e.printStackTrace();
                customFont = new Font(ARIAL_FONT, Font.BOLD, 24); // Si hay un error, usa Arial
            }

            // Crear el JLabel para el título
            JLabel titleLabel = new JLabel("¿Quieres calcular la devuelta?");
            titleLabel.setFont(customFont);
            titleLabel.setForeground(Color.BLACK);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(titleLabel);       // Agregar el título primero
            content.add(Box.createVerticalStrut(10)); // Espacio entre el título y la imagen
            content.add(imageLabel);         // Luego la imagen
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

            // Crear un JPanel para contener los botones
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Apilar verticalmente
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(btnPanel);

            JDialog dialog = new JDialog(compraDialog, "Calcula la Devuelta", true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(content, BorderLayout.CENTER);
            dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH); // Usar el panel de botones
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

        if (ConfigAdminManager.isElectronicBillingEnabled()) {

            JSONObject clienteSiigo = UIUserVenta.mostrarDialogoFactura(frame);

            if (clienteSiigo != null) {

                JSONObject facturaJson = new JSONObject()
                        .put("document", new JSONObject().put("id", 1)) // Ajustar ID documento
                        .put("date", LocalDate.now().toString())
                        .put("customer", clienteSiigo)
                        .put("seller", new JSONObject().put("id", 1)) // Ajustar ID vendedor
                        .put("payments", new JSONArray().put(
                                new JSONObject()
                                        .put("payment_method", new JSONObject().put("id", 1)) // efectivo
                                        .put("value", 10000) // valor ejemplo
                                        .put("due_date", LocalDate.now().toString())
                        ))
                        .put("items", new JSONArray().put(
                                new JSONObject()
                                        .put("code", "001")
                                        .put("description", "Producto Genérico")
                                        .put("quantity", 1)
                                        .put("price", 10000)
                                        .put("discount", 0)
                                        .put("taxes", new JSONArray().put(new JSONObject().put("id", 1)))
                        ));

                try {
                    new SiigoInvoice().crearFactura(facturaJson);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "❌ Error al enviar factura: " + e.getMessage());
                }
            }
        }

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
