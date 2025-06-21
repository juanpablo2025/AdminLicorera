package org.example.ui.uiadmin;

import org.example.manager.adminDBManager.DatabaseAdminManager;
//import org.example.manager.adminmanager.ExcelAdminManager;
import org.example.manager.adminmanager.GastosAdminManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.ui.uiuser.UIUserMesas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import static org.example.ui.uiadmin.UIGastosAdmin.productoAdminManager;
import static org.example.utils.Constants.*;


public class UIAdminProducts {

    private UIAdminProducts() {
    }
    private static final Logger logger =  LoggerFactory.getLogger(UIAdminProducts.class);

    private static final NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

    public static JPanel getAdminProductListPanel() {
        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(FONDO_PRINCIPAL);
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventario", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));

        try {
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            logger.error("Error loading font", e);
        }

        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Id", "Nombre", CANTIDAD, "Precio"};

        Object[][] data = new Object[products.size()][FOUR];
        for (int i = ZERO; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][ZERO] = p.getId();
            data[i][ONE] = p.getName();
            data[i][TWO] = p.getQuantity();
            data[i][THREE] = formatCOP.format(p.getPrice());
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == TWO) ? Integer.class : String.class;
            }
        };

        JTable productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI Variable", Font.PLAIN, 18));
        productTable.setRowHeight(35);
        productTable.setBackground(FONDO_PRINCIPAL);
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setFillsViewportHeight(true);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);

        JTextField searchField = new JTextField(COMBO_BOX_TEXT);
        searchField.setForeground(Color.GRAY);
        searchField.setPreferredSize(new Dimension(760, 35));
        searchField.setFont(new Font("Segoe UI Variable", Font.PLAIN, 18));

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(COMBO_BOX_TEXT)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(COMBO_BOX_TEXT);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText();
                if (text.trim().isEmpty() || text.equals(COMBO_BOX_TEXT)) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), ONE));
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(FONDO_PRINCIPAL);
        searchPanel.add(searchField);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Color fondo = FONDO_PRINCIPAL;
                Color texto = Color.BLACK;

                if (!isSelected) {
                    try {
                        Object cantidadObj = table.getValueAt(row, TWO);
                        int cantidad = (cantidadObj instanceof Integer integer)
                                ? integer
                                : Integer.parseInt(cantidadObj.toString());

                        if (cantidad <= -ONE) {fondo = new Color(255, 150, 150);
                        } else if (cantidad == ZERO) {fondo = new Color(255, 200, 100);}
                    } catch (Exception e) {
                        fondo = Color.WHITE;
                    }
                    cell.setBackground(fondo);
                    cell.setForeground(texto);
                } else {
                    cell.setBackground(table.getSelectionBackground());
                    cell.setForeground(table.getSelectionForeground());
                }

                if (column == TWO || column == THREE) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return cell;
            }
        };

        for (int i = ONE; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        productTable.getColumnModel().getColumn(ZERO).setMinWidth(ZERO);
        productTable.getColumnModel().getColumn(ZERO).setMaxWidth(ZERO);
        productTable.getColumnModel().getColumn(ZERO).setWidth(ZERO);

        JTableHeader header = productTable.getTableHeader();
        header.setForeground(new Color(201, 41, 41));
        header.setBackground(new Color(28, 28, 28));

        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            if (fontStream != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.ITALIC, 26);
                header.setFont(customFont);
            }
        } catch (Exception e) {
            logger.error("Error loading font", e);
        }

        JScrollPane scrollPane = new JScrollPane(productTable);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(FONDO_PRINCIPAL);

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(TEN));

        searchField.setMaximumSize(new Dimension(300, 30));
        searchPanel.setBackground(FONDO_PRINCIPAL);
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(searchPanel);

        productListPanel.add(topPanel, BorderLayout.NORTH);
        productListPanel.add(scrollPane, BorderLayout.CENTER);
        productListPanel.add(createButtonPanel(tableModel, productTable), BorderLayout.SOUTH);

        return productListPanel;
    }

    private static JButton createStyledButton(String text, Color baseColor, Color hoverColor, int fontSize) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(ZERO, ZERO, ZERO, 30));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);

                g2.setColor(getModel().isRollover() ? hoverColor : baseColor);
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI Variable", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private static JPanel createButtonPanel(DefaultTableModel tableModel, JTable productTable) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton addButton = createStyledButton("Nuevo", new Color(66, 133, 244), new Color(30, 70, 160), 22);
        addButton.addActionListener(e -> {
            int tempId = -System.identityHashCode(new Object());
            tableModel.addRow(new Object[]{tempId, "NUEVO_PRODUCTO", 0, "0"});

            SwingUtilities.invokeLater(() -> {
                int row = tableModel.getRowCount() - 1;
                if (row >= 0 && row < tableModel.getRowCount()) {
                    productTable.setRowSelectionInterval(row, row);

                    Object nombre = tableModel.getValueAt(row, 1);
                    Object cantidad = tableModel.getValueAt(row, 2);
                    Object precio = tableModel.getValueAt(row, 3);

                    boolean productoEditado = showEditProductDialog(tableModel, row, nombre, cantidad, precio, productTable);

                    if (productoEditado) {
                        // ✅ solo actualiza si se editó correctamente
                        updateProductTable(productTable);
                    } else {
                        // ❌ elimina si se canceló
                        tableModel.removeRow(row);
                    }

                    productTable.clearSelection();
                    productTable.revalidate();
                    productTable.repaint();
                }
            });
        });

        JButton editButton = createStyledButton("Editar", new Color(76, 175, 80), new Color(56, 142, 60), 18);
        editButton.setEnabled(false);

        editButton.addActionListener(e -> {
                int viewRow = productTable.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = productTable.convertRowIndexToModel(viewRow);
                    Object nombre = tableModel.getValueAt(modelRow, ONE);
                    Object cantidad = tableModel.getValueAt(modelRow, TWO);
                    Object precio = tableModel.getValueAt(modelRow, THREE);
                    showEditProductDialog(tableModel, modelRow, nombre, cantidad, precio, productTable);
                }
        });

        JButton saveButton = createStyledButton("Guardar Cambios", new Color(ZERO, 204, 136), new Color(ZERO, 153, 102), 18);
        saveButton.addActionListener(e -> saveProducts(tableModel, productTable));

        JButton reabastecimientoButton = createStyledButton("Reabastecer", new Color(228, 185, 42), new Color(255, 193, SEVEN), 22);
        reabastecimientoButton.setEnabled(false);
        reabastecimientoButton.addActionListener(e -> {
            int viewRow = productTable.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = productTable.convertRowIndexToModel(viewRow);

                DefaultTableModel currentModel = (DefaultTableModel) productTable.getModel(); // 🔁 usar modelo actual
                if (modelRow >= 0 && modelRow < currentModel.getRowCount()) {
                    String nombre = currentModel.getValueAt(modelRow, 1).toString();
                    int cantidad = Integer.parseInt(currentModel.getValueAt(modelRow, 2).toString());
                    showReabastecimientoDialog(productTable, nombre, cantidad);
                }
            }
        });

        JButton eliminarBtn = createStyledButton("Eliminar", new Color(255, 111, 97), new Color(201, 41, 41), 22);
        eliminarBtn.setBackground(new Color(220, 53, 69));
        eliminarBtn.setForeground(Color.WHITE);
        eliminarBtn.setFocusPainted(false);
        eliminarBtn.setEnabled(false);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = productTable.getSelectedRow() != -ONE;
            eliminarBtn.setEnabled(isSelected);
        });

        eliminarBtn.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) return;

            int modelRow = productTable.convertRowIndexToModel(selectedRow);
            int cantidad = Integer.parseInt(tableModel.getValueAt(modelRow, TWO).toString());

            if (cantidad > ZERO || cantidad == -ONE) {
                JOptionPane.showMessageDialog(null,
                        "No puedes eliminar un producto con cantidad en inventario o en deuda.",
                        "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "¿Estás seguro de que deseas eliminar este producto?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int productId = Integer.parseInt(tableModel.getValueAt(modelRow, ZERO).toString());

                tableModel.removeRow(modelRow);
                //ExcelAdminManager.deleteProductById(productId);
                DatabaseAdminManager.deleteProductById(productId);

                JOptionPane.showMessageDialog(null,
                        "Producto eliminado correctamente.",
                        "Eliminado", JOptionPane.PLAIN_MESSAGE);

                updateProductTable(productTable);
            }
        });

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = productTable.getSelectedRow() != -ONE;
                editButton.setEnabled(selected);
                reabastecimientoButton.setEnabled(selected);
            }
        });

        buttonPanel.add(reabastecimientoButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(eliminarBtn);
        return buttonPanel;
    }


    private static boolean showEditProductDialog(DefaultTableModel model, int row, Object nombre, Object cantidad, Object precio, JTable table) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Editar Producto");
        dialog.setSize(520, 710);
        dialog.setLayout(new BorderLayout(TEN, TEN));
        dialog.setModal(true);
        String nombreProducto = nombre.toString();
        String rutaImagen = System.getProperty(FOLDER_PATH) + File.separator + FOLDER
                + File.separator + FOTOS + File.separator + nombreProducto + ".png";

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        cargarImagen(rutaImagen, imageLabel);

        JButton examinarBtn = getJButton(dialog, rutaImagen, imageLabel);

        JPanel infoBar = new JPanel();
        infoBar.setBackground(new Color(220, 53, 69));
        infoBar.setLayout(new FlowLayout(FlowLayout.CENTER, 60, TEN));
        infoBar.setPreferredSize(new Dimension(400, 50));

        JLabel cantidadLabel = new JLabel("x" + cantidad + " Uds");
        cantidadLabel.setForeground(Color.WHITE);
        cantidadLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));

        JLabel precioLabel = new JLabel("$ " + precio.toString() + " Pesos");
        precioLabel.setForeground(Color.WHITE);
        precioLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));

        infoBar.add(cantidadLabel);
        infoBar.add(precioLabel);

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        imageContainer.add(infoBar, BorderLayout.SOUTH);
        imageContainer.setBackground(new Color(80, 80, 80));

        JLabel productLabel = new JLabel(nombre.toString());
        productLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 20));
        productLabel.setHorizontalAlignment(SwingConstants.CENTER);
        productLabel.setForeground(Color.WHITE);

        Box topBox = Box.createVerticalBox();
        topBox.add(Box.createVerticalStrut(TEN));
        topBox.add(productLabel);
        topBox.add(Box.createVerticalStrut(TEN));
        topBox.add(imageContainer);
        topBox.add(Box.createVerticalStrut(TEN));
        topBox.add(examinarBtn);

        JPanel topContainer = new JPanel();
        topContainer.setBackground(new Color(80, 80, 80));
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topBox);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(FIVE, FIVE, FIVE, FIVE);
        Dimension campoTamano = new Dimension(400, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JTextField nameField = new JTextField(nombre.toString());
        nameField.setPreferredSize(campoTamano);

        JTextField quantityField = new JTextField(cantidad.toString());
        quantityField.setPreferredSize(campoTamano);

        JTextField priceField = new JTextField(precio.toString());
        priceField.setPreferredSize(campoTamano);

        gbc.gridy = ZERO;
        centerPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridy++;
        centerPanel.add(nameField, gbc);
        gbc.gridy++;
        centerPanel.add(new JLabel("Precio:"), gbc);
        gbc.gridy++;
        centerPanel.add(priceField, gbc);

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setFont(new Font("Segoe UI Variable", Font.BOLD, 18));
        saveBtn.setBackground(new Color(76, 175, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(400, 40));
        saveBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> {
            try {
                int cantidadEditada = Integer.parseInt(quantityField.getText().trim());
                String nombreEditado = nameField.getText().trim().toUpperCase().replace(" ", "_");
                String precioEditado = priceField.getText().trim();

                if (nombreEditado.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "El nombre no puede estar vacío.",
                            "Error de validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                model.setValueAt(nombreEditado, row, ONE);
                model.setValueAt(cantidadEditada, row, TWO);
                model.setValueAt(precioEditado, row, THREE);

                saveProducts(model, table);
                table.clearSelection();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Cantidad debe ser un número entero.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            }

        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveBtn);

        dialog.add(topContainer, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return false;
    }

    private static JButton getJButton(JDialog dialog, String rutaImagen, JLabel imageLabel) {
        JButton examinarBtn = new JButton("Examinar imagen");
        examinarBtn.setFont(new Font("Segoe UI Variable", Font.BOLD, 14));
        examinarBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg"));
            int resultado = fileChooser.showOpenDialog(dialog);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                try {
                    Path destino = Paths.get(rutaImagen);
                    Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                    cargarImagen(rutaImagen, imageLabel);
                } catch (IOException ex) {
                    logger.error("Error al copiar la imagen: {}", ex.getMessage());
                    JOptionPane.showMessageDialog(dialog, "Error al copiar la imagen: " + ex.getMessage(),
                            ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    logger.error("Error inesperado: {}", ex.getMessage());
                    JOptionPane.showMessageDialog(dialog, "Error inesperado: " + ex.getMessage(),
                            ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return examinarBtn;
    }

    public static void updateProductTable(JTable productTable) {
        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Id", "Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][FOUR];

        for (int i = ZERO; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][ZERO] = p.getId();
            data[i][ONE] = p.getName();
            data[i][TWO] = p.getQuantity();
            data[i][THREE] = formatCOP.format(p.getPrice());
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == TWO) ? Integer.class : String.class;
            }
        };

        productTable.setModel(model);


        productTable.getColumnModel().getColumn(ZERO).setMinWidth(ZERO);
        productTable.getColumnModel().getColumn(ZERO).setMaxWidth(ZERO);
        productTable.getColumnModel().getColumn(ZERO).setWidth(ZERO);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Color fondo = FONDO_PRINCIPAL;
                Color texto = Color.BLACK;

                if (!isSelected) {
                    try {
                        int cantidad = Integer.parseInt(table.getValueAt(row, 2).toString());
                        if (cantidad <= -ONE) {
                            fondo = new Color(255, 100, 100);

                        } else if (cantidad == ZERO) {
                            fondo = new Color(255, 200, 100);

                        }
                        }catch (Exception e) {
                        fondo = Color.WHITE;
                    }
                    cell.setBackground(fondo);
                    cell.setForeground(texto);
                } else {
                    cell.setBackground(table.getSelectionBackground());
                    cell.setForeground(table.getSelectionForeground());
                }

                if (column == TWO || column == THREE) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }

                return cell;
            }
        };

        for (int i = ZERO; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }


private static void saveProducts(DefaultTableModel tableModel, JTable table) {
    try {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -ONE) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto para guardar los cambios", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = Integer.parseInt(tableModel.getValueAt(modelRow, ZERO).toString());
        String name = tableModel.getValueAt(modelRow, ONE).toString().toUpperCase().replace(" ", "_");
        int quantity = Integer.parseInt(tableModel.getValueAt(modelRow, TWO).toString());
        double price = Double.parseDouble(tableModel.getValueAt(modelRow, THREE).toString().replace(".", "").replace(",", ""));

        Producto productoActualizado = new Producto(id, name, quantity, price,
                "\\Calculadora del Administrador\\Fotos\\" + name + ".png");

        //ExcelAdminManager.updateProduct(productoActualizado);
        DatabaseAdminManager.updateProduct(productoActualizado);

        JOptionPane.showMessageDialog(null, "Producto actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        updateProductTable(table);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
}


    private static void cargarImagen(String ruta, JLabel imageLabel) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    File archivo = new File(ruta);
                    BufferedImage img;

                    if (!archivo.exists() || !archivo.isFile()) {
                        InputStream is = getClass().getResourceAsStream("/icons/sinfoto.png");
                        if (is != null) {
                            img = ImageIO.read(is);
                        } else {
                            return new ImageIcon();
                        }
                    } else {
                        img = ImageIO.read(archivo);
                    }

                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (IOException e) {
                    logger.error("Error al cargar la imagen: {}", e.getMessage());

                } catch (IllegalArgumentException e) {
                    logger.error("Error al escalar la imagen: {}", e.getMessage());

                }

                return new ImageIcon();
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
                    logger.error("Error al cargar la imagen: {}", e.getMessage());
                    JOptionPane.showMessageDialog(imageLabel, "Error al cargar la imagen: " + e.getMessage(),
                            ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    logger.error("Error inesperado: {}", e.getMessage());
                    JOptionPane.showMessageDialog(imageLabel, "Error inesperado: " + e.getMessage(),
                            ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void showReabastecimientoDialog(JTable productTable, String nombre, int cantidad) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Reabastecimiento de Productos");
        dialog.setSize(520, 710);
        dialog.setLayout(new BorderLayout(TEN, TEN));
        dialog.setModal(true);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(240, 240));

        JPanel infoBar = new JPanel();
        infoBar.setBackground(new Color(220, 53, 69));
        infoBar.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 10));
        infoBar.setPreferredSize(new Dimension(400, 50));

        JLabel cantidadLabel = new JLabel("x" + cantidad + " Uds");
        cantidadLabel.setForeground(Color.WHITE);
        cantidadLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));

        JLabel precioLabel = new JLabel();
        precioLabel.setForeground(Color.WHITE);
        precioLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));

        infoBar.add(cantidadLabel);
        infoBar.add(precioLabel);

        JComboBox<String> searchBox = UIHelpers.createProductAdminComboBox();
        searchBox.setPreferredSize(new Dimension(400, 35));
        searchBox.setMaximumSize(new Dimension(400, 35));
        searchBox.setFont(new Font("Segoe UI Variable", Font.PLAIN, 16));
        searchBox.setSelectedItem(nombre);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(80, 80, 80));
        searchPanel.add(searchBox);

        JLabel productLabel = new JLabel(nombre);
        productLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 20));
        productLabel.setHorizontalAlignment(SwingConstants.CENTER);
        productLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        productLabel.setForeground(Color.WHITE);

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        imageContainer.add(infoBar, BorderLayout.SOUTH);
        imageContainer.setBackground(new Color(80, 80, 80));

        Box topBox = Box.createVerticalBox();
        topBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        topBox.add(Box.createVerticalStrut(EIGHT));
        topBox.add(searchPanel);
        topBox.add(Box.createVerticalStrut(EIGHT));
        topBox.add(productLabel);
        topBox.add(Box.createVerticalStrut(EIGHT));
        topBox.add(imageContainer);

        JPanel topContainer = new JPanel();
        topContainer.setBackground(new Color(80, 80, 80));
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topBox);

        Consumer<String> updateProductInfo = selected -> {
            if (selected != null && !selected.equals("Busca un producto")) {
                String rutaImagen = System.getProperty(FOLDER_PATH) + File.separator + "Calculadora del Administrador"
                        + File.separator + "Fotos" + File.separator + selected + ".png";

                cargarImagen(rutaImagen, imageLabel);
                productLabel.setText(selected);
                double precio = productoAdminManager.getProductByName(selected).getPrice();
                precioLabel.setText("$ " + formatCOP.format(precio) + " Pesos");
            }
        };

        searchBox.addActionListener(e -> {
            String selected = (String) searchBox.getSelectedItem();
            if (selected != null && !selected.equals("Busca un producto")) {
                updateProductInfo.accept(selected);
            }
        });

        updateProductInfo.accept(nombre);

        imageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File draggedFile = files.get(ZERO);
                        if (draggedFile.getName().toLowerCase().endsWith(".png") ||
                                draggedFile.getName().toLowerCase().endsWith(".jpg") ||
                                draggedFile.getName().toLowerCase().endsWith(".jpeg")) {

                            String selected = (String) searchBox.getSelectedItem();
                            String rutaImagen = System.getProperty("user.home") + File.separator + "Calculadora del Administrador"
                                    + File.separator + "Fotos" + File.separator + selected + ".png";

                            Path destino = Paths.get(rutaImagen);
                            Files.copy(draggedFile.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                            cargarImagen(rutaImagen, imageLabel);
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Error al importar la imagen: {}", ex.getMessage());
                    JOptionPane.showMessageDialog(dialog, "Error al importar la imagen: " + ex.getMessage(),
                            ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        });

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(TEN, TEN, TEN, TEN);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = ZERO;
        gbc.gridy = ZERO;

        gbc.gridy++;
        JLabel quantityLabel = new JLabel("Cantidad");
        quantityLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 16));
        centerPanel.add(quantityLabel, gbc);

        gbc.gridy++;
        int min = ONE;
        int max = 1000;
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
        quantitySpinner.setFont(new Font("Segoe UI Variable", Font.PLAIN, 16));
        quantitySpinner.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(quantitySpinner, gbc);

        JComponent editor = quantitySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setFont(new Font("Segoe UI Variable", Font.PLAIN, 16));
        }

        SwingUtilities.invokeLater(() -> {
            for (Component comp : quantitySpinner.getComponents()) {
                if (comp instanceof JButton button) {
                    button.setPreferredSize(new Dimension(60, 60));
                    button.setFont(new Font("Segoe UI Variable", Font.BOLD, 16));
                }
            }
        });

        gbc.gridy++;
        JLabel priceLabel = new JLabel("Valor de compra");
        priceLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 16));
        centerPanel.add(priceLabel, gbc);

        gbc.gridy++;
        JTextField priceField = new JTextField(TEN);
        priceField.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(priceField, gbc);

        JButton confirmButton = new JButton("Confirmar");
        confirmButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 18));
        confirmButton.setPreferredSize(new Dimension(400, 40));
        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);

        confirmButton.addActionListener(e -> {
            String selected = (String) searchBox.getSelectedItem();
            String precioTexto = priceField.getText().trim();
            String precioCompra = precioTexto.isEmpty() ? "-10" : precioTexto;

            handleReplenishment(dialog, productTable, selected, (int) quantitySpinner.getValue(), precioCompra);

            // Mostrar opción de seguir o salir
            int opcion = JOptionPane.showOptionDialog(
                    dialog,
                    "¿Deseas seguir reabasteciendo productos?",
                    "Reabastecimiento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Seguir", "Volver"},
                    "Seguir"
            );

            if (opcion == JOptionPane.NO_OPTION) {
                dialog.dispose(); // cerrar si elige volver
            } else {
                // resetear campos para seguir
                quantitySpinner.setValue(ONE);
                priceField.setText("");
                searchBox.requestFocusInWindow();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(confirmButton);

        dialog.add(topContainer, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static void handleReplenishment(JDialog dialog, JTable productTable, String productName, int quantity, String priceText) {
        try {
            priceText = priceText.trim().replace(".", "").replace(",", "");

            double price = Double.parseDouble(priceText);

            Producto product = productoAdminManager.getProductByName(productName);
            if (product == null) {
                JOptionPane.showMessageDialog(dialog, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new GastosAdminManager().reabastecerProducto(product, quantity, price);

            product.setCantidad(product.getQuantity() + quantity);

            JOptionPane.showMessageDialog(dialog, "Producto reabastecido correctamente.");

            updateProductTable(productTable);
            productTable.clearSelection();
        }  catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
