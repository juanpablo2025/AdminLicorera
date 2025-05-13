package org.example.ui;

import org.example.manager.usermanager.ProductoUserManager;
import org.example.manager.usermanager.VentaMesaUserManager;
import org.example.model.Producto;
import org.example.ui.uiuser.UnifiedEditorRenderer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.formatearMoneda;


public class UIHelpers {

    private UIHelpers() {
    }

    private static ProductoUserManager productoUserManager = new ProductoUserManager();
    public static final VentaMesaUserManager ventaMesaUserManager = new VentaMesaUserManager();

    public static final Component compraDialog = null;


    public static JButton createButton(String text, Icon icon, ActionListener listener) {
        JButton button = new JButton();
        button.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
        button.setPreferredSize(new Dimension(180, 10));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(186, 27, 26));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setUI(new RoundedButtonUI(50));
        button.addActionListener(listener);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 1, 0));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        separator.setForeground(new Color(200, 170, 100));

        JLabel textLabel = new JLabel(text);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setForeground(Color.WHITE);

        try {
            // Cargar la fuente desde el classpath dentro del JAR
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            // Crear la fuente desde el InputStream
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, 18); // Ajustar tamaño

            // Aplicar la fuente al JLabel
            textLabel.setFont(customFont);

        } catch (Exception e) {
            e.printStackTrace();
        }


        panel.add(iconLabel);
        panel.add(separator);
        panel.add(textLabel);

        button.setLayout(new BorderLayout());
        button.add(panel, BorderLayout.CENTER);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(220, 40, 40));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(186, 27, 26));
            }
        });

        return button;
    }

    static class RoundedButtonUI extends BasicButtonUI {
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

    public static JComboBox<String> createProductAdminComboBox() {
        JComboBox<String> productComboBox = new JComboBox<>();
        productComboBox.setEditable(true);
        productComboBox.setFont(new Font(ARIAL_FONT, Font.BOLD, 18));

        JTextField comboBoxEditor = (JTextField) productComboBox.getEditor().getEditorComponent();

        // Admin: No filtrar por stock
        List<String> productList = ProductoUserManager.getProducts().stream()
                .map(Producto::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(COMBO_BOX_TEXT);
        productList.forEach(model::addElement);
        productComboBox.setModel(model);
        productComboBox.setSelectedItem(COMBO_BOX_TEXT);

        comboBoxEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (comboBoxEditor.getText().equals(COMBO_BOX_TEXT)) {
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
                    filteredModel.addElement(COMBO_BOX_TEXT);
                    productList.forEach(filteredModel::addElement);
                } else {
                    productList.stream()
                            .filter(product -> product.toLowerCase().contains(input.toLowerCase()))
                            .forEach(filteredModel::addElement);
                }

                productComboBox.setModel(filteredModel);
                comboBoxEditor.setText(input);
                productComboBox.showPopup();
            }
        });

        return productComboBox;
    }


    public static JPanel createInputPanel(JTable table) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        inputPanel.setBackground(new Color(28, 28, 28));

        Font labelFont = new Font(ARIAL_FONT, Font.BOLD, 18);

        // 🟦 Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(new Color(28, 28, 28));
        searchPanel.setMaximumSize(new Dimension(400, 50));

        JTextField searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setFont(new Font(ARIAL_FONT, Font.PLAIN, 20));
                    g2.setColor(Color.GRAY);
                    g2.drawString(COMBO_BOX_TEXT, 5, getHeight() - 10);
                    g2.dispose();
                }
            }
        };
        searchField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 20));
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        searchField.setForeground(Color.black);
        searchPanel.add(searchField);
        inputPanel.add(searchPanel);

        // 🟦 Quantity Panel
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quantityPanel.setBackground(new Color(28, 28, 28));
        quantityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel quantityLabel = new JLabel("Cantidad x");
        quantityLabel.setForeground(Color.WHITE);
        quantityLabel.setFont(labelFont);

        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 999, 1);
        JSpinner cantidadSpinner = new JSpinner(model);
        JComponent editor = cantidadSpinner.getEditor();
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();

        spinnerTextField.setColumns(3);
        spinnerTextField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 20));
        spinnerTextField.setPreferredSize(new Dimension(80, 40));
        spinnerTextField.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                SwingUtilities.invokeLater(() -> {
                    int caret = spinnerTextField.getCaretPosition();

                    try {
                        spinnerTextField.commitEdit();
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Formato inválido en el campo spinner", e);                    }
                    spinnerTextField.setCaretPosition(Math.min(caret, spinnerTextField.getText().length()));
                });
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
        quantityPanel.add(quantityLabel);
        quantityPanel.add(cantidadSpinner);
        inputPanel.add(quantityPanel);

        // 🟦 Scrollable Product Panel
        JPanel productPanel = new JPanel(new GridLayout(0, 2, 2, 5));
        productPanel.setBackground(new Color(28, 28, 28));
        JScrollPane scrollPane = new JScrollPane(productPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // se adapta en pack()
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(28,28,28);
                this.trackColor = new Color(200, 200, 200);
            }
            @Override protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }
            @Override protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }
            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        inputPanel.add(scrollPane);

        // 🎯 Cargar productos
        List<Producto> productList = ProductoUserManager.getProducts();

        // 📌 Aumentar el grosor de la barra de desplazamiento
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));

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

                                // Crear un fondo con esquinas redondeadas
                                g2.setColor(getBackground());
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                            }

                            @Override
                            protected void paintBorder(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                                // Dibujar borde redondeado
                                g2.setColor(new Color(74, 50, 28)); // Color del borde
                                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                            }
                        };

                        // 🔹 Ajustar la transparencia del fondo para evitar el color cuadrado
                        card.setOpaque(false);
                        card.setBackground(new Color(58, 58, 58)); // Fondo oscuro
                        card.setPreferredSize(new Dimension(80, 210));
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
                                g2.setColor(new Color(186, 27, 26));
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                            }
                        };
                        namePanel.setPreferredSize(new Dimension(240, 50));
                        namePanel.setOpaque(false);
                        namePanel.setLayout(new BorderLayout());

                        String formattedName = Arrays.stream(product.getName().replace("_", " ").toLowerCase().split(" "))
                                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                                .collect(Collectors.joining(" "));

                        card.setToolTipText(formattedName);

                        namePanel.setLayout(new BorderLayout());
                        namePanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); // Espaciado interno

                        JLabel nameLabel = new JLabel(formattedName);
                        nameLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
                        nameLabel.setForeground(Color.WHITE);
                        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        namePanel.add(nameLabel, BorderLayout.NORTH);

                        JLabel quantityPLabel = new JLabel("x" + product.getQuantity());
                        quantityPLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 14));
                        quantityPLabel.setForeground(Color.WHITE);
                        quantityPLabel.setHorizontalAlignment(SwingConstants.LEFT);

                        JLabel priceLabel = new JLabel("$" + formatearMoneda(product.getPrice()));
                        priceLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 14));
                        priceLabel.setForeground(Color.WHITE);
                        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                        JPanel subInfoPanel = new JPanel(new BorderLayout());
                        subInfoPanel.setOpaque(false);
                        subInfoPanel.add(quantityPLabel, BorderLayout.WEST);
                        subInfoPanel.add(priceLabel, BorderLayout.EAST);

                        namePanel.add(subInfoPanel, BorderLayout.SOUTH);

                        card.add(namePanel, BorderLayout.SOUTH);
                        card.setBackground(new Color(147, 89, 49));
                        new SwingWorker<ImageIcon, Void>() {
                            @Override
                            protected ImageIcon doInBackground() {
                                try {
                                    // Obtener la ruta de la imagen del producto
                                    String imagePath = System.getProperty("user.home") + product.getFoto();
                                    File imageFile = new File(imagePath);
                                    BufferedImage img = null;
                                    // Si la imagen del producto no existe, usar la imagen de respaldo
                                    if (!imageFile.exists() || !imageFile.isFile()) {
                                        InputStream is = getClass().getResourceAsStream("/icons/sinfoto.png");
                                        if (is != null) {
                                            img = ImageIO.read(is);
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        img = ImageIO.read(imageFile);
                                    }

                                    if (img != null) {
                                        Image scaledImg = img.getScaledInstance(180, 150, Image.SCALE_SMOOTH);
                                        return makeRoundedImage(scaledImg, 180, 150);

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }


                            @Override
                            protected void done() {
                                try {
                                    ImageIcon icon = get();
                                    if (icon != null) {
                                        imageLabel.setIcon(icon);
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.execute();

                        // 🟢 Aplicar efecto de pulsado al botón (Cambio de color temporal)
                        card.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                card.setBackground(new Color(186, 27, 26)); // Color al pasar el mouse
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                card.setBackground(new Color(147, 89, 49)); // Volver al color original
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                card.setBackground(new Color(220, 20, 60)); // Color al hacer clic
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                card.setBackground(new Color(186, 27, 26)); // Volver al color hover
                            }

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e)) {
                                    int cantidad = (Integer) cantidadSpinner.getValue();
                                    addProductsToTable(table, product, cantidad);
                                    cantidadSpinner.setValue(1);
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

    // Método para redondear imágenes
    private static ImageIcon makeRoundedImage(Image img, int width, int height) {
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Crear una máscara circular
        g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));
        g2.drawImage(img, 0, 0, width, height, null);
        g2.dispose();

        return new ImageIcon(roundedImage);
    }

    public static void addProductsToTable(JTable table, Producto producto, int cantidad) {
        if (producto == null) {
            JOptionPane.showMessageDialog(null, "Producto no válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        boolean productoExistente = false;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, 0);

            if (nombreProducto.equals(producto.getName())) {
                try {
                    int cantidadExistente = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                    int nuevaCantidad = cantidadExistente + cantidad;

                    double precioUnitario = producto.getPrice();
                    double nuevoTotal = nuevaCantidad * precioUnitario;

                    tableModel.setValueAt(nuevaCantidad, i, 1);
                    tableModel.setValueAt(precioUnitario, i, 2);
                    tableModel.setValueAt(nuevoTotal, i, 3);

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

    // Renderer personalizado para formato de moneda con alineación centrada
    public static class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto en la celda
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof Number n) {
                value = formatearMoneda(n.doubleValue());
            }
            super.setValue(value);
        }
    }

    public static JTable createProductTable() {
        String[] columnNames = {PRODUCTO, "Cant.", "Unid. $", "Total $", "Quitar unid."};
        DefaultTableModel tableModel;
        tableModel = new DefaultTableModel(columnNames, ZERO) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == FOUR; // Solo la columna de quitar unidad es editable
            }
        };

        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer();

        // Ajustar tamaño de columnas
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(250);
        columnModel.getColumn(1).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(80);
        columnModel.getColumn(3).setPreferredWidth(80);

        TableColumn quitarColumn = columnModel.getColumn(4);
        quitarColumn.setPreferredWidth(100);
        quitarColumn.setMinWidth(100);
        quitarColumn.setMaxWidth(100);

        // Evitar que se puedan mover las columnas
        table.getTableHeader().setReorderingAllowed(false);

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
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(1,1));

        return totalPanel;
    }
}
