package org.example.ui;

import org.example.manager.userManager.ProductoUserManager;
import org.example.manager.userManager.VentaMesaUserManager;
import org.example.model.Producto;
import org.example.ui.uiUser.UIUserMain;
import org.example.ui.uiUser.UnifiedEditorRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.formatearMoneda;


public class UIHelpers {

    private static ProductoUserManager productoUserManager = new ProductoUserManager();
    private static VentaMesaUserManager ventaMesaUserManager = new VentaMesaUserManager();

    private static JComboBox<String> productComboBox;
    private static JSpinner cantidadSpinner;
    private static JLabel totalLabel;
    private static JLabel totalCompraLabel;
    private static DefaultTableModel tableModel;
    private static Component compraDialog;




    static class RoundedBorder extends AbstractBorder {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public static JButton createButton(String text, Icon icon, ActionListener listener) {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(180, 140));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(255, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setUI(new RoundedButtonUI(70));
        button.addActionListener(listener);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        separator.setForeground(new Color(200, 170, 100));

        JLabel textLabel = new JLabel(text);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Arial", Font.BOLD, 16));
        textLabel.setForeground(Color.WHITE);

        panel.add(iconLabel);
        panel.add(separator);
        panel.add(textLabel);

        button.setLayout(new BorderLayout());
        button.add(panel, BorderLayout.CENTER);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(220, 40, 40));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 60, 60));
            }
        });

        return button;
    }

    static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final int radius;

        public RoundedButtonUI(int radius) {
            this.radius = radius;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            JButton button = (JButton) c;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(button.getBackground());
            g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), radius, radius);
            super.paint(g, c);
        }
    }

    public static JTextField createTextField() {
        return new JTextField();
    }

    public static JDialog createDialog(String title, int width, int height, LayoutManager layout) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setSize(width, height);
        dialog.setLayout(layout);
        return dialog;
    }
    public static JPanel createInputLista(JTable table, VentaMesaUserManager ventaMesaUserManager) {
        JPanel inputPanel = new JPanel(new BorderLayout()); // Mejor distribución
        inputPanel.setPreferredSize(new Dimension(450, 125)); // Ajuste de tamaño correcto
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Pequeño margen

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        // Panel para los campos de entrada alineados a la izquierda
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        leftPanel.setPreferredSize(new Dimension(350, 125));

        // ComboBox más pequeño
        JComboBox<String> productComboBox = createProductComboBox();
        productComboBox.setPreferredSize(new Dimension(180, 125));
        leftPanel.add(productComboBox);

        // Spinner Cantidad
        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JComponent editor = cantidadSpinner.getEditor();
        ((JSpinner.DefaultEditor) editor).getTextField().setFont(new Font("Arial", Font.BOLD, 14));
        cantidadSpinner.setPreferredSize(new Dimension(80, 125));
        leftPanel.add(cantidadSpinner);

        // Panel para el botón alineado a la derecha
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setPreferredSize(new Dimension(100, 30));

        JButton agregarProductoButton = createAddProductMesaButton(table, productComboBox, cantidadSpinner, ventaMesaUserManager);
        agregarProductoButton.setFont(new Font("Arial", Font.BOLD, 30));
        agregarProductoButton.setPreferredSize(new Dimension(430, 100));
        agregarProductoButton.setBackground(new Color(0, 201, 87));
        agregarProductoButton.setForeground(Color.WHITE);
        buttonPanel.add(agregarProductoButton);

        // Añadir los paneles al panel principal
        inputPanel.add(leftPanel, BorderLayout.NORTH);
        inputPanel.add(buttonPanel);

        return inputPanel;
    }


    public static JPanel createInputPanel(JTable table, VentaMesaUserManager ventaMesaUserManager) {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        inputPanel.setPreferredSize(new Dimension(580, 400));

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        JLabel productLabel = new JLabel("BUSCAR");
        productLabel.setFont(labelFont);
        searchPanel.add(productLabel);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(490, 30));
        searchPanel.add(searchField);
        searchPanel.setBackground(Color.LIGHT_GRAY);

        inputPanel.add(searchPanel, BorderLayout.NORTH);

        // Panel de productos dinámico
        JPanel productPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JScrollPane scrollPane = new JScrollPane(productPanel);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        inputPanel.add(scrollPane, BorderLayout.CENTER);

        List<Producto> productList = productoUserManager.getProducts().stream()
                .filter(producto -> producto.getQuantity() > 0)
                .toList();

        Runnable updateProducts = () -> {
            productPanel.removeAll();
            String query = searchField.getText().toLowerCase();
            productList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(query))
                    .forEach(product -> {
                        JPanel card = new JPanel(new BorderLayout()) {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(getBackground());
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                            }
                        };
                        card.setPreferredSize(new Dimension(200, 220));
                        card.setBackground(Color.WHITE);
                        card.setOpaque(true);
                        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                        JLabel imageLabel = new JLabel();
                        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        card.add(imageLabel, BorderLayout.CENTER);

                        JPanel namePanel = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(Color.BLACK);
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                            }
                        };
                        namePanel.setPreferredSize(new Dimension(200, 30));
                        namePanel.setOpaque(false);
                        namePanel.setLayout(new BorderLayout());

                        JLabel nameLabel = new JLabel(product.getName(), SwingConstants.CENTER);
                        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        nameLabel.setForeground(Color.WHITE);
                        namePanel.add(nameLabel, BorderLayout.CENTER);

                        card.add(namePanel, BorderLayout.SOUTH);
                        card.setBackground(new Color(255, 215, 0));

                        new SwingWorker<>() {
                            @Override
                            protected ImageIcon doInBackground() {
                                try {
                                    File imageFile = new File("C:\\Users\\Desktop\\Downloads/sinfoto.png");
                                    BufferedImage img = ImageIO.read(imageFile);
                                    if (img != null) {
                                        Image scaledImg = img.getScaledInstance(220, 180, Image.SCALE_SMOOTH);
                                        return new ImageIcon(scaledImg);
                                    }
                                } catch (IOException e) {
                                    System.err.println("No se pudo cargar la imagen: " + e.getMessage());
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    ImageIcon icon = (ImageIcon) get();
                                    if (icon != null) {
                                        imageLabel.setIcon(icon);
                                    } else {
                                        imageLabel.setText("Imagen no disponible");
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al cargar imagen: " + e.getMessage());
                                }
                            }
                        }.execute();

                        card.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                card.setBackground(new Color(255, 111, 97));
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                card.setBackground(new Color(255, 215, 0));
                            }

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e)) {
                                    AddProductsToTable(table, product, ventaMesaUserManager);
                                }
                            }
                        });

                        productPanel.add(card);
                    });
            productPanel.revalidate();
            productPanel.repaint();
        };

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateProducts.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateProducts.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateProducts.run(); }
        });

        updateProducts.run();
        return inputPanel;
    }




    public static void agregarProductoATabla(JTable table, Producto producto, VentaMesaUserManager ventaManager) {
        if (producto == null) {
            JOptionPane.showMessageDialog(null, "Producto no válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        boolean productoExistente = false;
        int cantidad = 1;



        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, 0);

            if (nombreProducto.equals(producto.getName())) {
                try {
                    int cantidadExistente = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                    int nuevaCantidad = cantidadExistente + cantidad;

                    if (nuevaCantidad > producto.getQuantity()) {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock para " + producto.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double precioUnitario = producto.getPrice();
                    double nuevoTotal = nuevaCantidad * precioUnitario;

                    tableModel.setValueAt(nuevaCantidad, i, 1);
                    tableModel.setValueAt(formatearMoneda(precioUnitario), i, 2);
                    tableModel.setValueAt((nuevoTotal), i, 3);

                    // Actualizar el producto en el carrito de compra
                    productoUserManager.addProductToCart(producto, nuevaCantidad);

                    productoExistente = true;
                    break;
                } catch (ClassCastException | NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error en la conversión de cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (!productoExistente) {
            double precioUnitario = producto.getPrice();
            double totalProducto = precioUnitario * cantidad;

            tableModel.addRow(new Object[]{
                    producto.getName(),
                    cantidad,
                    precioUnitario,
                    totalProducto,
                    "X"
            });

            // Agregar nuevo producto al carrito de compra
            productoUserManager.addProductToCart(producto, cantidad);
        }
    }

    public static void AddProductsToTable(JTable table, Producto producto, VentaMesaUserManager ventaManager) {
        if (producto == null) {
            JOptionPane.showMessageDialog(null, "Producto no válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        boolean productoExistente = false;
        int cantidad = 1;



        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, 0);

            if (nombreProducto.equals(producto.getName())) {
                try {
                    int cantidadExistente = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                    int nuevaCantidad = cantidadExistente + cantidad;

                    if (nuevaCantidad > producto.getQuantity()) {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock para " + producto.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double precioUnitario = producto.getPrice();
                    double nuevoTotal = nuevaCantidad * precioUnitario;

                    tableModel.setValueAt(nuevaCantidad, i, 1);
                    tableModel.setValueAt(formatearMoneda(precioUnitario), i, 2);
                    tableModel.setValueAt((nuevoTotal), i, 3);

                    // Actualizar el producto en el carrito de compra
                    productoUserManager.updateProductQuantity(producto, nuevaCantidad);

                    productoExistente = true;
                    break;
                } catch (ClassCastException | NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error en la conversión de cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (!productoExistente) {
            double precioUnitario = producto.getPrice();
            double totalProducto = precioUnitario * cantidad;

            tableModel.addRow(new Object[]{
                    producto.getName(),
                    cantidad,
                    precioUnitario,
                    totalProducto,
                    "X"
            });

            // Agregar nuevo producto al carrito de compra
            productoUserManager.updateProductQuantity(producto, cantidad);
        }
    }


    private static JComboBox<String> createProductComboBox() {
        JComboBox<String> productComboBox = new JComboBox<>();
        productComboBox.setEditable(true);
        productComboBox.setFont(new Font("Arial", Font.BOLD, 18));

        JTextField comboBoxEditor = (JTextField) productComboBox.getEditor().getEditorComponent();

        // Obtener lista de productos disponibles
        List<String> productList = productoUserManager.getProducts().stream()
                .filter(producto -> producto.getQuantity() > 0)
                .map(Producto::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();

        // Agregar "Busca un producto" como primer elemento
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Busca un producto");
        productList.forEach(model::addElement);
        productComboBox.setModel(model);
        productComboBox.setSelectedItem("Busca un producto");

        comboBoxEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (comboBoxEditor.getText().equals("Busca un producto")) {
                    comboBoxEditor.setText("");
                    comboBoxEditor.setForeground(Color.BLACK);
                }
            }
        });

        comboBoxEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String input = comboBoxEditor.getText().trim();
                DefaultComboBoxModel<String> filteredModel = new DefaultComboBoxModel<>();

                if (input.isEmpty()) {
                    filteredModel.addElement("Busca un producto");
                    productList.forEach(filteredModel::addElement);
                } else {
                    productList.stream()
                            .filter(product -> product.toLowerCase().contains(input.toLowerCase()))
                            .forEach(filteredModel::addElement);
                }

                productComboBox.setModel(filteredModel);
                productComboBox.setSelectedItem(input.isEmpty() ? "" : input);
                productComboBox.showPopup();
            }
        });

        return productComboBox;
    }




    // Renderer personalizado para formato de moneda con alineación centrada
    public static class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto en la celda
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof Number) {
                value = formatearMoneda(((Number) value).doubleValue());
            }
            super.setValue(value);
        }
    }

    public static JTable createProductTable() {
        String[] columnNames = {PRODUCTO, "Cant.", "Unid. $", "Total $", "Quitar unid."};

        tableModel = new DefaultTableModel(columnNames, ZERO) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == FOUR; // Solo la columna de quitar unidad es editable
            }
        };

        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer(tableModel, ventaMesaUserManager);

        // Ajustar tamaño de columnas
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(250); // Producto (Más grande)
        columnModel.getColumn(1).setPreferredWidth(50);  // Cantidad
        columnModel.getColumn(2).setPreferredWidth(80);  // Precio Unitario
        columnModel.getColumn(3).setPreferredWidth(80);  // Total
        columnModel.getColumn(4).setPreferredWidth(80); // Botón Quitar

        // Centrar texto de las columnas Cantidad, Unid. $, y Total $
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Aplicar un CurrencyRenderer centrado a las columnas de precios
        table.getColumnModel().getColumn(2).setCellRenderer(new CurrencyRenderer()); // PRECIO_UNITARIO
        table.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer()); // TOTALP

        // Asignar editor y renderer personalizados a la columna del botón
        table.getColumnModel().getColumn(4).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(4).setCellEditor(editorRenderer);

        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel(new GridLayout(1, 1));


        return totalPanel;
    }




    public static JButton createAddProductMesaButton(JTable table, JComboBox<String> productComboBox, JSpinner cantidadSpinner, VentaMesaUserManager ventaManager) {
        JButton agregarProductoButton = new JButton("AGREGAR");

        agregarProductoButton.addActionListener(e -> {
            try {
                String selectedProduct = (String) productComboBox.getSelectedItem();

                // Verificar que se seleccione un producto válido
                if (selectedProduct == null || selectedProduct.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Seleccione un producto válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int cantidad = (int) cantidadSpinner.getValue();
                if (cantidad <= 0) {
                    JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor que 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Producto producto = productoUserManager.getProductByName(selectedProduct);
                if ( producto.getQuantity() < cantidad) {
                    JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto: " + selectedProduct, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                boolean productoExistente = false;

                // Verificar si el producto ya está en la tabla
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, 0); // Columna 0 es el nombre del producto
                    if (nombreProducto.equals(selectedProduct)) {
                        // Producto ya existe en la tabla; sumar cantidad y recalcular total
                        int cantidadExistente = (int) tableModel.getValueAt(i, 1); // Columna 1 es la cantidad
                        int nuevaCantidad = cantidadExistente + cantidad;

                        // Verificar que la nueva cantidad no exceda el stock disponible
                        if (nuevaCantidad > producto.getQuantity()) {
                            JOptionPane.showMessageDialog(null, "No hay suficiente stock disponible para aumentar la cantidad del producto: " + selectedProduct, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Actualizar la cantidad y el total en la tabla
                        double precioUnitario = producto.getPrice();
                        double nuevoTotal = nuevaCantidad * precioUnitario;
                        tableModel.setValueAt(nuevaCantidad, i, 1); // Actualizar la cantidad
                        tableModel.setValueAt(nuevoTotal, i, 3); // Actualizar el total

                        productoExistente = true;
                        break;
                    }
                }

                // Si el producto no existe en la tabla, añadirlo como una nueva fila
                if (!productoExistente) {
                    double precioUnitario = producto.getPrice();
                    double totalProducto = precioUnitario * cantidad;
                    tableModel.addRow(new Object[]{selectedProduct, cantidad, precioUnitario, totalProducto, "X"});
                }

                // Calcular el total general sumando los valores de la columna 3 (total del producto)
                double total = 0;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    try {
                        total += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                    } catch (ClassCastException | NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Error al leer el total del producto. Verifique los datos.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Añadir el producto al carrito de ventas en el productoUserManager
                productoUserManager.addProductToCart(producto, cantidad);
// Reiniciar el valor del spinner a 1
                cantidadSpinner.setValue(1);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Cantidad o precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return agregarProductoButton;
    }

}
