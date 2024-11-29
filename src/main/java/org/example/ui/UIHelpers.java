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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

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

        Font labelFont = new Font("Arial", Font.BOLD, 16);

        // First row: Product ComboBox with search functionality
        JLabel productLabel = new JLabel("Producto");
        productLabel.setFont(labelFont);
        inputPanel.add(productLabel);

        JComboBox<String> productComboBox = createProductComboBox();
        inputPanel.add(productComboBox);

        // Second row: Quantity Spinner
        JLabel cantidadLabel = new JLabel("Cantidad");
        cantidadLabel.setFont(labelFont);
        inputPanel.add(cantidadLabel);

        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        ((JSpinner.DefaultEditor) cantidadSpinner.getEditor()).getTextField().setFont(new Font("Arial", Font.BOLD, 18));
        inputPanel.add(cantidadSpinner);

        // Third row: Spacer and Add Product Button
        inputPanel.add(new JLabel("")); // Spacer

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton agregarProductoButton = createAddProductMesaButton(table, productComboBox, cantidadSpinner, ventaMesaUserManager);
        buttonPanel.add(agregarProductoButton);
        inputPanel.add(buttonPanel);

        return inputPanel;
    }

    private static JComboBox<String> createProductComboBox() {
        JComboBox<String> productComboBox = new JComboBox<>();
        productComboBox.setEditable(true);
        productComboBox.setFont(new Font("Arial", Font.BOLD, 18));
        JTextField comboBoxEditor = (JTextField) productComboBox.getEditor().getEditorComponent();

        // Usar DefaultListModel para cargar los nombres de los productos
        DefaultListModel<String> productList = new DefaultListModel<>();
        for (Producto producto : productoUserManager.getProducts()) {
            // Verifica que la cantidad sea mayor que 0 antes de agregarlo
            if (producto.getQuantity() > 0) {
                productList.addElement(producto.getName());
            }
        }

        // Ordenar la lista en orden ascendente
        ArrayList<String> productArrayList = Collections.list(productList.elements());
        Collections.sort(productArrayList);  // Orden ascendente
        DefaultListModel<String> sortedProductList = new DefaultListModel<>();
        for (String product : productArrayList) {
            sortedProductList.addElement(product);
        }

        // Inicializar el ComboBox con todos los productos
        updateComboBox(productComboBox, sortedProductList);

        // Listener para detectar cuando se presiona Enter
        comboBoxEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Verifica si la tecla presionada es Enter
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String input = comboBoxEditor.getText();
                    DefaultListModel<String> filteredList = new DefaultListModel<>();

                    // Filtrar la lista de productos si el input no está vacío
                    if (!input.isEmpty()) {
                        // Filtra la lista de productos por el texto ingresado (productos que empiezan con el texto)
                        for (int i = 0; i < sortedProductList.size(); i++) {
                            String item = sortedProductList.getElementAt(i);
                            // Comprobar si el nombre del producto empieza con el texto ingresado (case-insensitive)
                            if (item.toLowerCase().startsWith(input.toLowerCase())) {
                                filteredList.addElement(item);
                            }
                        }
                    } else {
                        filteredList = sortedProductList; // Mostrar todos los productos si el campo está vacío
                    }

                    // Actualizar el ComboBox con la lista filtrada
                    updateComboBox(productComboBox, filteredList);

                    // Mostrar el ComboBox con los resultados filtrados
                    productComboBox.showPopup();
                }
            }
        });

        // Deshabilitar la selección automática
        productComboBox.setSelectedIndex(-1); // No se selecciona ningún elemento automáticamente

        // Habilitar el comportamiento de selección normal al hacer clic
        productComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aquí puedes obtener el ítem seleccionado manualmente, si es necesario
                String selectedItem = (String) productComboBox.getSelectedItem();
                if (selectedItem != null && !selectedItem.isEmpty()) {
                    // Procesar la selección manual
                    System.out.println("Producto seleccionado: " + selectedItem);
                }
            }
        });

        return productComboBox;
    }

    // Actualiza los elementos del ComboBox basado en los resultados de la búsqueda
    private static void updateComboBox(JComboBox<String> comboBox, DefaultListModel<String> itemList) {
        comboBox.removeAllItems(); // Borra todos los elementos antes de agregar nuevos
        comboBox.addItem(""); // Selección vacía predeterminada

        for (int i = 0; i < itemList.size(); i++) {
            comboBox.addItem(itemList.getElementAt(i)); // Agrega los productos filtrados
        }
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
                // Permitir edición en la columna de cantidad y en la columna del botón (FOUR)
                return column == FOUR;
            }
        };

        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer(tableModel, ventaMesaUserManager);

        // Asignar el renderer de moneda a las columnas de PRECIO_UNITARIO y TOTALP
        table.getColumnModel().getColumn(TWO).setCellRenderer(new CurrencyRenderer()); // PRECIO_UNITARIO
        table.getColumnModel().getColumn(THREE).setCellRenderer(new CurrencyRenderer()); // TOTALP

        // Asignar el editor y renderer personalizados a la columna del botón
        table.getColumnModel().getColumn(FOUR).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(FOUR).setCellEditor(editorRenderer);

        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel(new GridLayout(1, 1));


        return totalPanel;
    }




    public static JButton createAddProductMesaButton(JTable table, JComboBox<String> productComboBox, JSpinner cantidadSpinner, VentaMesaUserManager ventaManager) {
        JButton agregarProductoButton = new JButton(AGREGAR_PRODUCTO);

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
                if (producto.getQuantity() < cantidad) {
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
