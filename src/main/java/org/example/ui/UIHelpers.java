package org.example.ui;

import org.example.manager.usermanager.ProductoUserManager;
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

    private UIHelpers() {}

    private static final ProductoUserManager productoUserManager = new ProductoUserManager();

    public static final Component compraDialog = null;

    public static JButton createButton(String text, Icon icon, ActionListener listener) {
        JButton button = new JButton();
        button.setFont(new Font(ARIAL_FONT, Font.BOLD, SIXTEEN));
        button.setPreferredSize(new Dimension(180, TEN));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(186, 27, 26));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setUI(new RoundedButtonUI(FIFTY));
        button.addActionListener(listener);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(TWO, ZERO, ONE, ZERO));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, FOUR));
        separator.setForeground(new Color(200, 170, 100));

        JLabel textLabel = new JLabel(text);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setForeground(Color.WHITE);

        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, 18);
            textLabel.setFont(customFont);

        } catch (Exception e) {
            e.fillInStackTrace();
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
            g2.fillRoundRect(ZERO, ZERO, button.getWidth(), button.getHeight(), radius, radius);
            super.paint(g, c);
        }
    }

    public static JComboBox<String> createProductAdminComboBox() {
        JComboBox<String> productComboBox = new JComboBox<>();
        productComboBox.setEditable(true);
        productComboBox.setFont(new Font(ARIAL_FONT, Font.BOLD, EIGHTEEN));

        JTextField comboBoxEditor = (JTextField) productComboBox.getEditor().getEditorComponent();

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
        inputPanel.setBorder(BorderFactory.createEmptyBorder(TWO, TWO, TWO, TWO));
        inputPanel.setBackground(new Color(28, 28, 28));

        Font labelFont = new Font(ARIAL_FONT, Font.BOLD, EIGHTEEN);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(new Color(28, 28, 28));
        searchPanel.setMaximumSize(new Dimension(400, 50));

        JTextField searchField = getSearchField();
        searchPanel.add(searchField);
        inputPanel.add(searchPanel);

        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quantityPanel.setBackground(new Color(28, 28, 28));
        quantityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel quantityLabel = new JLabel("Cantidad x");
        quantityLabel.setForeground(Color.WHITE);
        quantityLabel.setFont(labelFont);

        SpinnerNumberModel model = new SpinnerNumberModel(ONE, ONE, 999, ONE);
        JSpinner cantidadSpinner = new JSpinner(model);
        JComponent editor = cantidadSpinner.getEditor();
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();

        spinnerTextField.setColumns(THREE);
        spinnerTextField.setFont(new Font(ARIAL_FONT, Font.PLAIN, TWENTY));
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

        JPanel productPanel = new JPanel(new GridLayout(ZERO, TWO, TWO, FIVE));
        productPanel.setBackground(new Color(28, 28, 28));
        JScrollPane scrollPane = new JScrollPane(productPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getVerticalScrollBar().setUnitIncrement(THIRTY);

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
                button.setPreferredSize(new Dimension(ZERO, ZERO));
                return button;
            }
        });

        inputPanel.add(scrollPane);

        List<Producto> productList = ProductoUserManager.getProducts();

        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(FIFTEEN, ZERO));

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
                                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), TWENTY, TWENTY);
                            }

                            @Override
                            protected void paintBorder(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(new Color(74, 50, 28));
                                g2.drawRoundRect(ZERO, ZERO, getWidth() - ONE, getHeight() - ONE, 20, 20);
                            }
                        };

                        card.setOpaque(false);
                        card.setBackground(new Color(58, 58, 58));
                        card.setPreferredSize(new Dimension(100, 220));
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
                                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), TWENTY, TWENTY);
                            }
                        };

                        namePanel.setPreferredSize(new Dimension(100, 65));
                        namePanel.setOpaque(false);
                        namePanel.setLayout(new BorderLayout());

                        String formattedName = Arrays.stream(product.getName().replace("_", " ").toLowerCase().split(" "))
                                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                                .collect(Collectors.joining(" "));

                        // Usa <html> para permitir salto de línea automático
                        JLabel nameLabel = new JLabel("<html><div style='text-align:center;'>" + formattedName + "</div></html>");
                        namePanel.setLayout(new BorderLayout());
                        namePanel.setBorder(BorderFactory.createEmptyBorder(FOUR, EIGHT, FOUR, EIGHT));
                        nameLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 15));
                        nameLabel.setForeground(Color.WHITE);
                        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        nameLabel.setVerticalAlignment(SwingConstants.TOP);
                        namePanel.add(nameLabel, BorderLayout.NORTH);

                        //card.setToolTipText("<html>" + formattedName + "</html>");




                        JLabel quantityPLabel = new JLabel("x" + product.getQuantity());
                        quantityPLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, FOURTEEN));
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
                                    String imagePath = System.getProperty(FOLDER_PATH) + product.getFoto();
                                    File imageFile = new File(imagePath);
                                    BufferedImage img;
                                    if (!imageFile.exists() || !imageFile.isFile()) {
                                        InputStream is = getClass().getResourceAsStream(NO_FOTO);
                                        if (is != null) {
                                            img = ImageIO.read(is);
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        img = ImageIO.read(imageFile);
                                    }

                                    if (img != null) {
                                        Image scaledImg = img.getScaledInstance(170, 140, Image.SCALE_SMOOTH);
                                        return makeRoundedImage(scaledImg);

                                    }
                                } catch (IOException e) {
                                    e.fillInStackTrace();
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
                                    e.fillInStackTrace();
                                }
                            }
                        }.execute();

                        card.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                card.setBackground(new Color(186, 27, 26));
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                card.setBackground(new Color(147, 89, 49));
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                card.setBackground(new Color(220, TWENTY, 60));
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                card.setBackground(new Color(186, 27, 26));
                            }

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e)) {
                                    int cantidad = (Integer) cantidadSpinner.getValue();
                                    addProductsToTable(table, product, cantidad);
                                    cantidadSpinner.setValue(ONE);
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

    private static JTextField getSearchField() {
        JTextField searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setFont(new Font(ARIAL_FONT, Font.PLAIN, TWENTY));
                    g2.setColor(Color.GRAY);
                    g2.drawString(COMBO_BOX_TEXT, FIVE, getHeight() - TEN);
                    g2.dispose();
                }
            }
        };
        searchField.setFont(new Font(ARIAL_FONT, Font.PLAIN, TWENTY));
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, FORTY));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), ONE),
                BorderFactory.createEmptyBorder(FIVE, FIVE, FIVE, FIVE)
        ));
        searchField.setForeground(Color.black);
        return searchField;
    }

    private static ImageIcon makeRoundedImage(Image img) {
        BufferedImage roundedImage = new BufferedImage(170, 140, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setClip(new RoundRectangle2D.Float(ZERO, ZERO, 170, 140, 20, 20));
        g2.drawImage(img, ZERO, ZERO, 170, 140, null);
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

        for (int i = ZERO; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, ZERO);

            if (nombreProducto.equals(producto.getName())) {
                try {
                    int cantidadExistente = Integer.parseInt(tableModel.getValueAt(i, ONE).toString());
                    int nuevaCantidad = cantidadExistente + cantidad;

                    double precioUnitario = producto.getPrice();
                    double nuevoTotal = nuevaCantidad * precioUnitario;

                    tableModel.setValueAt(nuevaCantidad, i, ONE);
                    tableModel.setValueAt(precioUnitario, i, TWO);
                    tableModel.setValueAt(nuevoTotal, i, THREE);

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

            productoUserManager.updateProductQuantity(producto, cantidad);
        }
    }

    public static class CurrencyRenderer extends DefaultTableCellRenderer {

        public CurrencyRenderer() {setHorizontalAlignment(SwingConstants.CENTER);}

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
                return column == FOUR;
            }
        };

        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer();

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(ZERO).setPreferredWidth(250);
        columnModel.getColumn(ONE).setPreferredWidth(FIFTY);
        columnModel.getColumn(TWO).setPreferredWidth(80);
        columnModel.getColumn(THREE).setPreferredWidth(80);

        TableColumn quitarColumn = columnModel.getColumn(FOUR);
        quitarColumn.setPreferredWidth(ONE_HUNDRED);
        quitarColumn.setMinWidth(ONE_HUNDRED);
        quitarColumn.setMaxWidth(ONE_HUNDRED);

        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(ONE).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(TWO).setCellRenderer(new CurrencyRenderer());
        table.getColumnModel().getColumn(THREE).setCellRenderer(new CurrencyRenderer());
        table.getColumnModel().getColumn(FOUR).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(FOUR).setCellEditor(editorRenderer);

        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(ONE,ONE));

        return totalPanel;
    }
}
