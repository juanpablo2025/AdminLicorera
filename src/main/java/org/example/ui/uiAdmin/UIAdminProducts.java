package org.example.ui.uiAdmin;

import org.example.manager.adminManager.ExcelAdminManager;
import org.example.manager.adminManager.GastosAdminManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.ui.uiUser.UIUserMesas;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static org.example.model.Producto.getFotoByName;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiAdmin.GastosAdminUI.productoAdminManager;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;

public class UIAdminProducts {
    static void showProductosDialog() {
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
                        Producto nuevoProducto = new Producto(nombre, cantidad, precio, rutaFoto);
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


    private static final NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));


    public static JPanel getAdminProductListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Inventario", JLabel.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        try {


            // Cargar la fuente desde los recursos dentro del JAR
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");


            // Crear la fuente desde el InputStream
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, 50); // Ajustar tamaño y peso
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }


        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][3];

        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = p.getName();
            data[i][1] = p.getQuantity();
            data[i][2] = formatCOP.format(p.getPrice());
        }



        // Modelo de tabla personalizado
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 ||  column == 2; // ✅ Ahora se pueden editar Nombre, Cantidad y Precio
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? Integer.class : String.class;
            }
        };

        JTable productTable = new JTable(tableModel);
        setupTableAppearance(productTable);
        setupTableEditors(productTable);

        // Centrar las cantidades en la celda
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(createButtonPanel(tableModel, productTable), BorderLayout.SOUTH);

        return panel;
    }

    private static void setupTableAppearance(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 18));
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBackground(Color.LIGHT_GRAY);
        header.setForeground(Color.BLACK);
    }

    private static void setupTableEditors(JTable table) {
       //table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(createQuantityModel())));
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    Double.parseDouble(value.replace(".", "").replace(",", ""));
                    return super.stopCellEditing();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(table, "Formato de precio inválido", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });
    }


    private static JPanel createButtonPanel(DefaultTableModel tableModel, JTable productTable) {




        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("Añadir Producto");

        addButton.addActionListener(e -> {
            tableModel.addRow(new Object[]{"Nuevo_Producto", 0, "0"});
            productTable.scrollRectToVisible(productTable.getCellRect(productTable.getRowCount() - 1, 0, true));
        });


        addButton.setFont(new Font("Arial", Font.BOLD, 22));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(new Color(255, 111, 97));
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        // Agregar el MouseListener para cambiar el color al pasar el mouse
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addButton.setBackground(new Color(201, 41, 41)); // Rojo más oscuro
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addButton.setBackground(new Color(255, 111, 97)); // Color original
            }
        });




        JButton saveButton = new JButton("Guardar Cambios");
        saveButton.addActionListener(e -> saveProducts(tableModel,productTable));

        saveButton.setFont(new Font("Arial", Font.BOLD, 18));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(0, 204, 136));
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JButton reabastecimientoButton = new JButton("Reabastecer"){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Color de fondo normal
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, 7)); // Amarillo oscuro al presionar
                } else {
                    g2.setColor(new Color(228, 185, 42)); // Amarillo Material Design
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        reabastecimientoButton.setEnabled(false); // Inicialmente deshabilitado

        reabastecimientoButton.setPreferredSize(new Dimension(160, 40)); // Más grande
        reabastecimientoButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        reabastecimientoButton.setForeground(Color.WHITE); // Texto negro
        reabastecimientoButton.setFocusPainted(false);
        reabastecimientoButton.setContentAreaFilled(false);
        reabastecimientoButton.setBorderPainted(false);
        reabastecimientoButton.setOpaque(false);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                reabastecimientoButton.setEnabled(true);
            }
        });

        reabastecimientoButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                String nombre = (String) tableModel.getValueAt(selectedRow, 0);
                int cantidad = (Integer) tableModel.getValueAt(selectedRow, 1);




                // Pasamos la lista de productos correctamente
                showReabastecimientoDialog(productTable, nombre, cantidad);
            }
        });



        buttonPanel.add(reabastecimientoButton);
        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private static void addNewProduct(DefaultTableModel tableModel, JTable table) {
        tableModel.addRow(new Object[]{"Nuevo_Producto", 0, "0"});
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
    }
//TODO quitar cantidads
    private static void saveProducts(DefaultTableModel tableModel,JTable table) {
        try {
            List<Producto> existingProducts = productoAdminManager.getProducts();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = ((String) tableModel.getValueAt(i, 0)).toUpperCase().replace(" ", "_");
                int quantity = (Integer) tableModel.getValueAt(i, 1);
                double price = Double.parseDouble(((String) tableModel.getValueAt(i, 2)).replace(".", "").replace(",", ""));
                Producto product = existingProducts.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                if (product != null) {
                    product.setCantidad(quantity);
                    product.setPrecio(price);
                } else {
                    existingProducts.add(new Producto(name, quantity, price, "\\Calculadora del Administrador\\Fotos\\" + name + ".png"));
                }
            }
            ExcelAdminManager.updateProducts(existingProducts);
            JOptionPane.showMessageDialog(null, "Cambios guardados exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
                        Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
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
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout(10, 10));

        // Construcción de la ruta de la imagen
        String rutaImagen = System.getProperty("user.home") + File.separator + "Calculadora del Administrador" +
                File.separator + "Fotos" + File.separator + nombre + ".png";

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        cargarImagen(rutaImagen, imageLabel);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel productLabel = new JLabel("Producto: " + nombre);
        productLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(productLabel, gbc);

        gbc.gridy++;
        JLabel quantityLabel = new JLabel("Cantidad:");
        centerPanel.add(quantityLabel, gbc);

        gbc.gridy++;
        int min = 1, max = 1000;
        int initial = Math.max(min, Math.min(cantidad, max));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
        centerPanel.add(quantitySpinner, gbc);

        gbc.gridy++;
        JLabel priceLabel = new JLabel("Valor de compra:");
        centerPanel.add(priceLabel, gbc);

        gbc.gridy++;
        JTextField priceField = new JTextField(10);
        centerPanel.add(priceField, gbc);

        JButton confirmButton = new JButton("Confirmar");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(200, 40));
        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);

        confirmButton.addActionListener(e ->
                handleReplenishment(dialog, productTable, nombre, (int) quantitySpinner.getValue(), priceField.getText())
        );

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(confirmButton);

        dialog.add(imageLabel, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }


    private static void handleReplenishment(JDialog dialog, JTable productTable, String productName, int quantity, String priceText) {
        try {
            // Validar que el precio no esté vacío
            priceText = priceText.trim().replace(".", "").replace(",", "");
            if (priceText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El precio no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convertir a número
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                JOptionPane.showMessageDialog(dialog, "El precio debe ser mayor que 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Formato de precio inválido. Use solo números.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateProductTable(JTable productTable) {
        List<Producto> products = productoAdminManager.getProducts();
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();

        // Limpiar la tabla y volver a llenarla
        model.setRowCount(0);
        for (Producto p : products) {
            model.addRow(new Object[]{p.getName(), p.getQuantity(), formatCOP.format(p.getPrice())});
        }
    }


    private static Integer[] createQuantityModel() {
        Integer[] quantities = new Integer[101];
        for (int i = 0; i <= 100; i++) quantities[i] = i;
        return quantities;
    }
}
