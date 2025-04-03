package org.example.ui.uiAdmin;

import org.example.manager.adminManager.ExcelAdminManager;
import org.example.manager.adminManager.GastosAdminManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.ui.uiUser.UIUserMesas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
                return column == 0 || column == 1 || column == 2; // ✅ Ahora se pueden editar Nombre, Cantidad y Precio
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? Integer.class : String.class;
            }
        };

        JTable productTable = new JTable(tableModel);
        setupTableAppearance(productTable);
        setupTableEditors(productTable);

        JScrollPane scrollPane = new JScrollPane(productTable);
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
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(createQuantityModel())));
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

    private static Integer[] createQuantityModel() {
        Integer[] quantities = new Integer[101];
        for (int i = 0; i <= 100; i++) quantities[i] = i;
        return quantities;
    }

    private static JPanel createButtonPanel(DefaultTableModel tableModel, JTable productTable) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Añadir Producto");
        addButton.addActionListener(e -> addNewProduct(tableModel, productTable));

        JButton saveButton = new JButton("Guardar Cambios");
        saveButton.addActionListener(e -> saveProducts(tableModel));

        JButton reabastecimientoButton = new JButton("Reabastecer");
        reabastecimientoButton.setEnabled(false); // Inicialmente deshabilitado

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
                showReabastecimientoDialog(productTable,nombre, cantidad);
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

    private static void saveProducts(DefaultTableModel tableModel) {
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showReabastecimientoDialog(JTable productTable,String nombre, int cantidad) {
        JDialog dialog = createDialog("Reabastecimiento de Productos", 500, 300, new GridLayout(4, 2));

        // Campo de producto (no editable para mantener consistencia)
        JComboBox<String> productCombo = new JComboBox<>(new String[]{nombre});
        productCombo.setEnabled(false);


        int min = -100; // Permitir valores negativos
        int max = 100;
        int initial = Math.max(min, Math.min(cantidad, max)); // Ajusta cantidad dentro del rango permitido
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));

        // Campo de precio total
        JTextField priceField = new JTextField();

        // Botón de confirmación
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> handleReplenishment(dialog,productTable, nombre, (int) quantitySpinner.getValue(), priceField.getText()));

        // Agregar componentes al diálogo
        dialog.add(new JLabel("PRODUCTO:"));
        dialog.add(productCombo);
        dialog.add(new JLabel("CANTIDAD:"));
        dialog.add(quantitySpinner);
        dialog.add(new JLabel("PRECIO TOTAL:"));
        dialog.add(priceField);
        dialog.add(confirmButton);

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
}
