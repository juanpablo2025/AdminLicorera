package org.example.ui.uiAdmin;

import org.example.manager.adminManager.ExcelAdminManager;
import org.example.manager.adminManager.GastosAdminManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.ui.uiUser.UIUserMesas;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import static org.example.manager.adminManager.ExcelAdminManager.saveSelectedProduct;
import static org.example.manager.userManager.ProductoUserManager.getProducts;
import static org.example.model.Producto.getFotoByName;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiAdmin.GastosAdminUI.productoAdminManager;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;

public class UIAdminProducts {
  /*  static void showProductosDialog() {
        // Crear el diálogo
        JDialog listProductsDialog = createDialog(LISTAR_PRODUCTO, 1280, 720, new BorderLayout());
        listProductsDialog.setResizable(true);
        listProductsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainAdmin();
            }
        });

        // Obtener la lista de productos
        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][3];

// Llenar los datos en la matriz
        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = p.getName();
            data[i][1] = p.getQuantity(); // La cantidad se manejará con JComboBox
            data[i][2] = formatCOP.format(p.getPrice());
        }

// Modelo de tabla personalizado
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2; // ✅ Ahora se pueden editar Nombre, Cantidad y Precio
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? Integer.class : String.class;
            }
        };

        // Crear la tabla
        JTable productTable = new JTable(tableModel);
        // Centrar las cantidades en la celda
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);


        // Editor para la columna Cantidad
        JComboBox<Integer> quantityCombo = new JComboBox<>();
        for (int i = 0; i <= 100; i++) quantityCombo.addItem(i);
        productTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));

        // Editor para la columna Precio con validación
        productTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    String cleanValue = value.replace(".", "").replace(",", "");
                    Double.parseDouble(cleanValue);
                    return super.stopCellEditing();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(productTable, "Formato de precio inválido", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });

        // Estilos de la tabla
        productTable.setFont(new Font("Arial", Font.PLAIN, 18));
        productTable.setRowHeight(30);
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBackground(Color.LIGHT_GRAY);
        header.setForeground(Color.BLACK);
        productTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        productTable.setSelectionBackground(Color.CYAN);
        productTable.setSelectionForeground(Color.BLACK);
// Aplicar renderizador solo a la columna de cantidad
        // Aplicar renderizador a toda la fila si la cantidad es menor a 0
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                try {
                    int cantidad = (int) table.getValueAt(row, 1); // Obtener cantidad de la fila
                    if (cantidad < 0) {
                        cell.setBackground(new Color(255, 102, 102)); // Rojo sutil para toda la fila
                    } else {
                        cell.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    }
                } catch (Exception e) {
                    cell.setBackground(Color.WHITE); // En caso de error, mantener el color normal
                }

                return cell;
            }
        });
        // Panel principal
        JScrollPane scrollPane = new JScrollPane(productTable);
        listProductsDialog.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Botón para añadir fila
        JButton addButton = new JButton("Añadir Producto");
        addButton.addActionListener(e -> {
            tableModel.addRow(new Object[]{"Nuevo_Producto", 0, "0"});
            productTable.scrollRectToVisible(productTable.getCellRect(productTable.getRowCount() - 1, 0, true));
        });

        // Botón para guardar cambios
        JButton saveButton = new JButton("Guardar Cambios");
        saveButton.addActionListener(e -> {
            try {
                List<Producto> existingProducts = productoAdminManager.getProducts();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String nombre = ((String) tableModel.getValueAt(i, 0))
                            .toUpperCase()        // Convertir a mayúsculas
                            .replace(" ", "_");
                    int cantidad = (Integer) tableModel.getValueAt(i, 1);

                    // Validar y formatear el precio
                    String precioStr = ((String) tableModel.getValueAt(i, 2)).replace(".", "").replace(",", "");
                    double precio = Double.parseDouble(precioStr);

                    String rutaFoto = "\\\\Calculadora del Administrador\\\\Fotos\\\\" + nombre + ".png";

                    // Buscar si el producto ya existe
                    Producto productoExistente = existingProducts.stream()
                            .filter(p -> p.getName().equalsIgnoreCase(nombre))
                            .findFirst()
                            .orElse(null);

                    if (productoExistente != null) {
                        // ✅ Actualizar cantidad y precio si el producto ya existe
                        productoExistente.setCantidad(cantidad);
                        productoExistente.setPrecio(precio);
                    } else {
                        // ✅ Crear nuevo producto y agregarlo a la lista
                        Producto nuevoProducto = new Producto(id,nombre, cantidad, precio, rutaFoto);
                        existingProducts.add(nuevoProducto);
                    }
                }

                // ✅ Guardar la lista actualizada
                ExcelAdminManager.updateProducts(existingProducts);

                // ✅ Recargar la tabla con los productos actualizados
                tableModel.setRowCount(0); // Limpiar la tabla antes de actualizar
                for (Producto p : existingProducts) {
                    tableModel.addRow(new Object[]{p.getName(), p.getQuantity(), formatCOP.format(p.getPrice())});
                }

                JOptionPane.showMessageDialog(listProductsDialog, "Cambios guardados exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(listProductsDialog, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Agregar botones al panel
        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);
        listProductsDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Hacer visible el diálogo
        listProductsDialog.setVisible(true);
    }

*/
    private static final NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));


    // Panel principal con tabla y botones
    public static JPanel getAdminProductListPanel() {

        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(new Color(250, 240, 230));
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventario", JLabel.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));

        try {
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Id", "Nombre", "Cantidad", "Precio"};

        Object[][] data = new Object[products.size()][4];
        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getName();
            data[i][2] = p.getQuantity();
            data[i][3] = formatCOP.format(p.getPrice());
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 2) ? Integer.class : String.class;
            }
        };

        JTable productTable = new JTable(tableModel);

        // 🔧 Renderer personalizado para colorear filas
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Color fondo = new Color(250, 240, 230); // Fondo por defecto
                Color texto = Color.BLACK;

                if (!isSelected) {
                    try {
                        Object cantidadObj = table.getValueAt(row, 2);
                        int cantidad = (cantidadObj instanceof Integer)
                                ? (Integer) cantidadObj
                                : Integer.parseInt(cantidadObj.toString());

                        if (cantidad <= -1) {  // 🔧 Condición: cantidad menor o igual a -1
                            fondo = new Color(255, 150, 150); // Fondo rojo
                            texto = Color.BLACK;              // Texto blanco
                        } else if (cantidad == 0) {
                            fondo = new Color(255, 200, 100); // Naranja claro para 0
                            texto = Color.BLACK;
                        }

                    } catch (Exception e) {
                        fondo = Color.WHITE;
                    }

                    cell.setBackground(fondo);
                    ((JLabel) cell).setForeground(texto);

                } else {
                    cell.setBackground(table.getSelectionBackground());
                    ((JLabel) cell).setForeground(table.getSelectionForeground());
                }

                // Alineación
                if (column == 2 || column == 3) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }

                return cell;
            }
        };

        // 🔧 Asignar el renderer a todas las columnas
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        // Ocultar columna Id
        productTable.getColumnModel().getColumn(0).setMinWidth(0);
        productTable.getColumnModel().getColumn(0).setMaxWidth(0);
        productTable.getColumnModel().getColumn(0).setWidth(0);

        productTable.setFont(new Font("Arial", Font.PLAIN, 18));
        productTable.setRowHeight(40);
        productTable.setBackground( new Color(250, 240, 230));
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setFillsViewportHeight(true);

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
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(productTable);
        productListPanel.add(titleLabel, BorderLayout.NORTH);
        productListPanel.add(scrollPane, BorderLayout.CENTER);
        productListPanel.add(createButtonPanel(tableModel, productTable), BorderLayout.SOUTH);

        return productListPanel;
    }






    // Botones con mismo estilo redondeado y hover
    private static JButton createStyledButton(String text, Color baseColor, Color hoverColor, int fontSize) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                g2.setColor(getModel().isRollover() ? hoverColor : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, fontSize));
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // ← centrado

        JButton addButton = createStyledButton("Nuevo", new Color(66, 133, 244), new Color(30, 70, 160), 22);
        addButton.addActionListener(e -> {
            int tempId = -System.identityHashCode(new Object());
            tableModel.addRow(new Object[]{tempId, "NUEVO_PRODUCTO", 0, "0"});

            SwingUtilities.invokeLater(() -> {
                int row = tableModel.getRowCount() - 1;
                productTable.setRowSelectionInterval(row, row);
                showEditProductDialog(tableModel, row,
                        tableModel.getValueAt(row, 1),
                        tableModel.getValueAt(row, 2),
                        tableModel.getValueAt(row, 3),
                        productTable);
                productTable.clearSelection(); // si quieres limpiar después del diálogo
            });
        });

        JButton editButton = createStyledButton("Editar", new Color(76, 175, 80), new Color(56, 142, 60), 18);
        editButton.setEnabled(false);
        editButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                Object nombre = tableModel.getValueAt(selectedRow, 1);
                Object cantidad = tableModel.getValueAt(selectedRow, 2);
                Object precio = tableModel.getValueAt(selectedRow, 3);
                showEditProductDialog(tableModel, selectedRow, nombre, cantidad, precio, productTable);
            }
        });

        JButton saveButton = createStyledButton("Guardar Cambios", new Color(0, 204, 136), new Color(0, 153, 102), 18);
        saveButton.addActionListener(e -> saveProducts(tableModel, productTable));

        JButton reabastecimientoButton = createStyledButton("Reabastecer", new Color(228, 185, 42), new Color(255, 193, 7), 22);
        reabastecimientoButton.setEnabled(false);
        reabastecimientoButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                String nombre = (String) tableModel.getValueAt(selectedRow, 1);
                int cantidad = (Integer) tableModel.getValueAt(selectedRow, 2);
                showReabastecimientoDialog(productTable, nombre, cantidad);
            }
        });

        // Botón eliminar
        JButton eliminarBtn = createStyledButton("Eliminar", new Color(255, 111, 97), new Color(201, 41, 41), 22);
        eliminarBtn.setBackground(new Color(220, 53, 69));
        eliminarBtn.setForeground(Color.WHITE);
        eliminarBtn.setFocusPainted(false);
        eliminarBtn.setEnabled(false); // deshabilitado por defecto

// Listener para habilitar o deshabilitar el botón según selección
        productTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = productTable.getSelectedRow() != -1;
            eliminarBtn.setEnabled(isSelected);
        });

        eliminarBtn.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) return;

            int modelRow = productTable.convertRowIndexToModel(selectedRow);
            int cantidad = Integer.parseInt(tableModel.getValueAt(modelRow, 2).toString());

            if (cantidad > 0 || cantidad == -1) {
                JOptionPane.showMessageDialog(null,
                        "No puedes eliminar un producto con cantidad en inventario o en deuda.",
                        "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "¿Estás seguro de que deseas eliminar este producto?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int productId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

                tableModel.removeRow(modelRow);
                ExcelAdminManager.deleteProductById(productId);

                JOptionPane.showMessageDialog(null,
                        "Producto eliminado correctamente.",
                        "Eliminado", JOptionPane.INFORMATION_MESSAGE);

                updateProductTable(productTable);
            }
        });

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = productTable.getSelectedRow() != -1;
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


    private static void showEditProductDialog(DefaultTableModel model, int row, Object nombre, Object cantidad, Object precio, JTable table) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Editar Producto");
        dialog.setSize(520, 710);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setModal(true);
        String nombreProducto = nombre.toString();
        String rutaImagen = System.getProperty("user.home") + File.separator + "Calculadora del Administrador"
                + File.separator + "Fotos" + File.separator + nombreProducto + ".png";

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        cargarImagen(rutaImagen, imageLabel);

        JButton examinarBtn = new JButton("Examinar imagen");
        examinarBtn.setFont(new Font("Arial", Font.BOLD, 14));
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
                    ex.printStackTrace();
                }
            }
        });

        JPanel infoBar = new JPanel();
        infoBar.setBackground(new Color(220, 53, 69));
        infoBar.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 10));
        infoBar.setPreferredSize(new Dimension(400, 50));

        JLabel cantidadLabel = new JLabel("x" + cantidad + " Uds");
        cantidadLabel.setForeground(Color.WHITE);
        cantidadLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel precioLabel = new JLabel("$ " + precio.toString() + " Pesos");
        precioLabel.setForeground(Color.WHITE);
        precioLabel.setFont(new Font("Arial", Font.BOLD, 22));

        infoBar.add(cantidadLabel);
        infoBar.add(precioLabel);

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        imageContainer.add(infoBar, BorderLayout.SOUTH);
        imageContainer.setBackground(new Color(80, 80, 80));

        JLabel productLabel = new JLabel(nombre.toString());
        productLabel.setFont(new Font("Arial", Font.BOLD, 20));
        productLabel.setHorizontalAlignment(SwingConstants.CENTER);
        productLabel.setForeground(Color.WHITE);

        Box topBox = Box.createVerticalBox();
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(productLabel);
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(imageContainer);
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(examinarBtn);

        JPanel topContainer = new JPanel();
        topContainer.setBackground(new Color(80, 80, 80));
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topBox);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension campoTamano = new Dimension(400, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JTextField nameField = new JTextField(nombre.toString());
        nameField.setPreferredSize(campoTamano);

        JTextField quantityField = new JTextField(cantidad.toString());
        quantityField.setPreferredSize(campoTamano);

        JTextField priceField = new JTextField(precio.toString());
        priceField.setPreferredSize(campoTamano);

        gbc.gridy = 0;
        centerPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridy++;
        centerPanel.add(nameField, gbc);
        gbc.gridy++;
        centerPanel.add(new JLabel("Precio:"), gbc);
        gbc.gridy++;
        centerPanel.add(priceField, gbc);

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 18));
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

                model.setValueAt(nombreEditado, row, 1);
                model.setValueAt(cantidadEditada, row, 2);
                model.setValueAt(precioEditado, row, 3);

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
    }

    public static void updateProductTable(JTable productTable) {
        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Id", "Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][4];

        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getName();
            data[i][2] = p.getQuantity();
            data[i][3] = formatCOP.format(p.getPrice());
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 2) ? Integer.class : String.class;
            }
        };

        productTable.setModel(model);


        productTable.getColumnModel().getColumn(0).setMinWidth(0);
        productTable.getColumnModel().getColumn(0).setMaxWidth(0);
        productTable.getColumnModel().getColumn(0).setWidth(0);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Color fondo = new Color(250, 240, 230);
                Color texto = Color.BLACK;

                if (!isSelected) {
                    try {
                        int cantidad = Integer.parseInt(table.getValueAt(row, 2).toString());
                        if (cantidad <= -1) {
                            fondo = new Color(255, 100, 100);
                            texto = Color.BLACK;
                        } else if (cantidad == 0) {
                            fondo = new Color(255, 200, 100); // Naranja claro para 0
                            texto = Color.BLACK;
                        }
                    } catch (Exception e) {
                        fondo = Color.WHITE;
                    }
                    cell.setBackground(fondo);
                    ((JLabel) cell).setForeground(texto);
                } else {
                    cell.setBackground(table.getSelectionBackground());
                    ((JLabel) cell).setForeground(table.getSelectionForeground());
                }

                if (column == 2 || column == 3) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }

                return cell;
            }
        };

        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }

//TODO quitar cantidads
private static void saveProducts(DefaultTableModel tableModel, JTable table) {
    try {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona un producto para guardar los cambios", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow); // por si la tabla está ordenada o filtrada
        int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        String name = tableModel.getValueAt(modelRow, 1).toString().toUpperCase().replace(" ", "_");
        int quantity = Integer.parseInt(tableModel.getValueAt(modelRow, 2).toString());
        double price = Double.parseDouble(tableModel.getValueAt(modelRow, 3).toString().replace(".", "").replace(",", ""));

        // Obtener producto existente
        Producto productoActualizado = new Producto(id, name, quantity, price,
                "\\Calculadora del Administrador\\Fotos\\" + name + ".png");

        // Actualizar solo ese producto en el archivo Excel
        ExcelAdminManager.updateProduct(productoActualizado);

        JOptionPane.showMessageDialog(null, "Producto actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        updateProductTable(table);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}




    private static void cargarImagen(String ruta, JLabel imageLabel) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    File archivo = new File(ruta);
                    BufferedImage img = null;

                    // Si la imagen no existe, usar imagen de respaldo
                    if (!archivo.exists() || !archivo.isFile()) {
                        InputStream is = getClass().getResourceAsStream("/icons/sinfoto.png");
                        if (is != null) {
                            img = ImageIO.read(is);
                        } else {
                            System.err.println("❌ No se encontró la imagen de respaldo.");
                            return new ImageIcon();
                        }
                    } else {
                        img = ImageIO.read(archivo);
                    }

                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Error al cargar la imagen: " + e.getMessage());
                }
                return new ImageIcon();
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageLabel.setIcon(icon); // ✅ Ahora actualiza correctamente el JLabel
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Error al asignar imagen: " + e.getMessage());
                }
            }
        }.execute();
    }
    public static void showReabastecimientoDialog(JTable productTable, String nombre, int cantidad) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Reabastecimiento de Productos");
        dialog.setSize(520, 710);
        dialog.setLayout(new BorderLayout(10, 10));
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
        cantidadLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel precioLabel = new JLabel();
        precioLabel.setForeground(Color.WHITE);
        precioLabel.setFont(new Font("Arial", Font.BOLD, 22));

        infoBar.add(cantidadLabel);
        infoBar.add(precioLabel);



        JComboBox<String> searchBox = UIHelpers.createProductComboBox();
        searchBox.setPreferredSize(new Dimension(400, 35));
        searchBox.setMaximumSize(new Dimension(400, 35));
        searchBox.setFont(new Font("Arial", Font.PLAIN, 16));
        searchBox.setSelectedItem(nombre);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(80, 80, 80));
        searchPanel.add(searchBox);

        JLabel productLabel = new JLabel(nombre);
        productLabel.setFont(new Font("Arial", Font.BOLD, 20));
        productLabel.setHorizontalAlignment(SwingConstants.CENTER);
        productLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        productLabel.setForeground(Color.WHITE);

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        imageContainer.add(infoBar, BorderLayout.SOUTH);
        imageContainer.setBackground(new Color(80, 80, 80));

        Box topBox = Box.createVerticalBox();
        topBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        topBox.add(Box.createVerticalStrut(8));
        topBox.add(searchPanel);
        topBox.add(Box.createVerticalStrut(8));
        topBox.add(productLabel);
        topBox.add(Box.createVerticalStrut(8));
        topBox.add(imageContainer);

        JPanel topContainer = new JPanel();
        topContainer.setBackground(new Color(80, 80, 80));
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topBox);

        Runnable updateProductInfo = () -> {
            String selected = (String) searchBox.getSelectedItem();
            if (selected != null && !selected.equals("Busca un producto")) {
                String rutaImagen = System.getProperty("user.home") + File.separator + "Calculadora del Administrador"
                        + File.separator + "Fotos" + File.separator + selected + ".png";

                cargarImagen(rutaImagen, imageLabel);
                productLabel.setText(selected);
                double precio = productoAdminManager.getProductByName(selected).getPrice();
                precioLabel.setText("$ " + formatCOP.format(precio) + " Pesos");
            }
        };

        searchBox.addActionListener(e -> updateProductInfo.run());

        updateProductInfo.run();

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
                        File draggedFile = files.get(0);
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
                    ex.printStackTrace();
                }
                return false;
            }
        });

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridy++;
        JLabel quantityLabel = new JLabel("Cantidad:");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(quantityLabel, gbc);

        gbc.gridy++;
        int min = 1, max = 1000;
        int initial = Math.max(min, Math.min(min, max));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
        quantitySpinner.setFont(new Font("Arial", Font.PLAIN, 16));
        quantitySpinner.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(quantitySpinner, gbc);

        JComponent editor = quantitySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setFont(new Font("Arial", Font.PLAIN, 16));
        }

        SwingUtilities.invokeLater(() -> {
            for (Component comp : quantitySpinner.getComponents()) {
                if (comp instanceof JButton button) {
                    button.setPreferredSize(new Dimension(60, 60));
                    button.setFont(new Font("Arial", Font.BOLD, 16));
                }
            }
        });

        gbc.gridy++;
        JLabel priceLabel = new JLabel("Valor de compra");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(priceLabel, gbc);

        gbc.gridy++;
        JTextField priceField = new JTextField(10);
        priceField.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(priceField, gbc);

        JButton confirmButton = new JButton("Confirmar");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 18));
        confirmButton.setPreferredSize(new Dimension(400, 40));
        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);

        confirmButton.addActionListener(e -> {
            String selected = (String) searchBox.getSelectedItem();
            String precioTexto = priceField.getText().trim();
            String precioCompra = precioTexto.isEmpty() ? "-10" : (precioTexto);
            handleReplenishment(dialog, productTable, selected, (int) quantitySpinner.getValue(), precioCompra);
            quantitySpinner.setValue(1);
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
            // Validar que el precio no esté vacío
            priceText = priceText.trim().replace(".", "").replace(",", "");


            // Convertir a número
            double price = Double.parseDouble(priceText);


            // Buscar el producto en la lista
            Producto product = productoAdminManager.getProductByName(productName);
            if (product == null) {
                JOptionPane.showMessageDialog(dialog, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Reabastecer producto
            new GastosAdminManager().reabastecerProducto(product, quantity, price);

            // Actualizar cantidad en la tabla (si aplica)
            product.setCantidad(product.getQuantity() + quantity);

            JOptionPane.showMessageDialog(dialog, "Producto reabastecido correctamente.");

            // Actualizar la tabla
            updateProductTable(productTable);
            productTable.clearSelection();
        }  catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private static Integer[] createQuantityModel() {
        Integer[] quantities = new Integer[101];
        for (int i = 0; i <= 100; i++) quantities[i] = i;
        return quantities;
    }
}
