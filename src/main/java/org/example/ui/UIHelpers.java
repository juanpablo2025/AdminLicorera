package org.example.ui;

import org.example.manager.userManager.ProductoUserManager;
import org.example.manager.userManager.VentaMesaUserManager;
import org.example.model.Producto;
import org.example.ui.uiUser.UnifiedEditorRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.ActionListener;

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


    public static JButton createButton(String text, Icon icon, ActionListener listener) {
        JButton button = new JButton();
        button.addActionListener(listener);

        // Estilo del botón
        button.setFont(new Font("Arial", Font.BOLD, 18));  // Fuente y tamaño
        button.setFocusPainted(false);                     // Eliminar borde de foco
        button.setBackground(Color.WHITE);                 // Fondo blanco
        button.setForeground(Color.DARK_GRAY);             // Texto color gris oscuro
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));  // Márgenes del botón más delgados

        // Crear un panel para contener el icono y el texto
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // Layout vertical
        panel.setBackground(Color.WHITE);  // Fondo blanco

        // Crear una etiqueta para el icono
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Centrar icono
        iconLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        // Crear un separador
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2)); // Ancho máximo
        separator.setForeground(Color.LIGHT_GRAY); // Color de la línea

        separator.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Margen superior e inferior
        // Crear una etiqueta para el texto
        JLabel textLabel = new JLabel(text);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Centrar texto
        textLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Asegurar que el texto tenga el mismo estilo

        // Establecer un margen superior al texto
        textLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Margen superior de 10 píxeles

        // Ajustar el tamaño preferido del texto
        textLabel.setPreferredSize(new Dimension(150, 30)); // Cambia las dimensiones según sea necesario

        // Agregar el icono y el separador al panel
        panel.add(iconLabel);
        panel.add(separator); // Agregar separador
        panel.add(textLabel); // Agregar texto

        // Agregar el panel al botón
        button.setLayout(new BorderLayout());
        button.add(panel, BorderLayout.CENTER);

        // Sombra para el botón
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Márgenes más delgados
        ));

        // Efecto de "levantarse" al pasar el mouse
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));  // Cambiar a gris claro al pasar el mouse
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(3, 7, 3, 7)  // Márgenes más delgados
                ));
                button.setBounds(button.getX(), button.getY() - 2, button.getWidth(), button.getHeight() + 2); // Levantar efecto
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);  // Volver al color blanco
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10) // Volver a los márgenes originales
                ));
                button.setBounds(button.getX(), button.getY() + 2, button.getWidth(), button.getHeight() - 2); // Volver a la posición original
            }
        });

        // Establecer tamaño preferido del botón
        button.setPreferredSize(new Dimension(150, 120));  // Dimensiones personalizadas

        return button;
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

    public static JPanel createInputPanel(JTable table, VentaMesaUserManager ventaMesaUserManager) {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2)); // Tres filas, dos columnas

        // Definir la fuente que se aplicará a todos los componentes
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font inputFont = new Font("Arial", Font.PLAIN, 14); // Para los campos de entrada

        // Primera fila: ComboBox de productos
        JLabel productLabel = new JLabel(PRODUCT_FIELD);
        productLabel.setFont(labelFont);
        inputPanel.add(productLabel);

        productComboBox = new JComboBox<>();
        productComboBox.setFont(new Font("Arial", Font.BOLD, 18)); // Cambiar la fuente del ComboBox
        java.util.List<Producto> productos = productoUserManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }
        inputPanel.add(productComboBox);

        // Segunda fila: Spinner de cantidad
        JLabel cantidadLabel = new JLabel(CANTIDAD_FIELD);
        cantidadLabel.setFont(labelFont);
        inputPanel.add(cantidadLabel);

        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) cantidadSpinner.getEditor();
        spinnerEditor.getTextField().setFont(new Font("Arial", Font.BOLD, 18)); // Cambiar la fuente del Spinner
        inputPanel.add(cantidadSpinner);

        // Tercera fila: Espacio vacío para alineación y el botón alineado a la derecha
        inputPanel.add(new JLabel("")); // Espacio vacío en la primera celda de la fila

        // Crear un panel para el botón con FlowLayout alineado a la derecha
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaUserManager);
        agregarProductoButton.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente del botón
        buttonPanel.add(agregarProductoButton);

        // Añadir el buttonPanel al inputPanel (segunda celda de la tercera fila)
        inputPanel.add(buttonPanel);

        return inputPanel;
    }

    // Renderer personalizado para formato de moneda
    public static class CurrencyRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            if (value instanceof Number) {
                value = formatearMoneda(((Number) value).doubleValue());
            }
            super.setValue(value);
        }
    }

    public static JTable createProductTable() {
        String[] columnNames = {PRODUCTO, CANTIDAD, PRECIO_UNITARIO, TOTALP, "Eliminar una unidad"};

        tableModel = new DefaultTableModel(columnNames, ZERO) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permite edición en la columna de cantidad, por ejemplo.
                return column == ONE;
            }
        };

        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer(tableModel, ventaMesaUserManager);

        // Asignar el renderer de moneda a las columnas de PRECIO_UNITARIO y TOTALP
        table.getColumnModel().getColumn(TWO).setCellRenderer(new CurrencyRenderer()); // PRECIO_UNITARIO
        table.getColumnModel().getColumn(THREE).setCellRenderer(new CurrencyRenderer()); // TOTALP

        table.getColumnModel().getColumn(FOUR).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(FOUR).setCellEditor(editorRenderer);

        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel(new GridLayout(1, 1));


        return totalPanel;
    }




    public static JButton createAddProductMesaButton(JTable table, VentaMesaUserManager ventaManager) {
        JButton agregarProductoButton = new JButton(AGREGAR_PRODUCTO);
        agregarProductoButton.addActionListener(e -> {
            try {
                // Obtener el producto seleccionado y la cantidad del Spinner
                String selectedProduct = (String) productComboBox.getSelectedItem();
                int cantidad = (int) cantidadSpinner.getValue();

                // Validar que la cantidad sea mayor que 0
                if (cantidad <= 0) {
                    JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor que 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Obtener el producto por su nombre desde el productoUserManager
                Producto producto = productoUserManager.getProductByName(selectedProduct);

                // Verificar si el producto tiene suficiente stock
                if (producto.getCantidad() < cantidad) {
                    JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto: " + selectedProduct, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Acceder al DefaultTableModel de la tabla
                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

                // Verificar si el producto ya está en la tabla
                boolean productoExistente = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, 0); // Columna 0 es el nombre del producto

                    if (nombreProducto.equals(selectedProduct)) {
                        // Si el producto ya existe en la tabla, sumar la cantidad
                        int cantidadExistente = (int) tableModel.getValueAt(i, 1); // Columna 1 es la cantidad
                        int nuevaCantidad = cantidadExistente + cantidad;

                        // Verificar que no se exceda el stock disponible
                        if (nuevaCantidad > producto.getCantidad()) {
                            JOptionPane.showMessageDialog(null, "No hay suficiente stock disponible para aumentar la cantidad del producto: " + selectedProduct, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Actualizar la cantidad y el total en la tabla
                        double precioUnitario = (double) tableModel.getValueAt(i, 2); // Columna 2 es el precio unitario
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
                    total += (double) tableModel.getValueAt(i, 3);  // Sumar el total de cada producto
                }

                // Actualizar las etiquetas o campos que muestran el total general (descomentando según tu implementación)
            /*totalLabel.setText(TOTAL_PESO + total);
            totalCompraLabel.setText(TOTAL_COMPRA_PESO + total);*/

                // Añadir el producto al carrito de ventas en el productoUserManager
                productoUserManager.addProductToCart(producto, cantidad);

            } catch (NumberFormatException ex) {
                // Manejar posibles errores de formato en el precio o cantidad
                JOptionPane.showMessageDialog(null, "Cantidad o precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return agregarProductoButton;
    }

}
