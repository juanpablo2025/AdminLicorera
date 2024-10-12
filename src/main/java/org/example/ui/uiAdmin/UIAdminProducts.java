package org.example.ui.uiAdmin;

import org.example.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
        JDialog listProductsDialog = createDialog(LISTAR_PRODUCTO, 1000, 600, new BorderLayout());
        listProductsDialog.setResizable(true); // Permitir que la ventana sea redimensionable
        listProductsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainAdmin() ; // Llamada a showMesas cuando se cierra la ventana
            }
        });
        // Obtener la lista de productos
        List<Producto> products = productoAdminManager.getProducts();
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][3];

        // Llenar los datos en la matriz
        for (int i = 0; i < products.size(); i++) {
            NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
            Producto p = products.get(i);
            double precio = p.getPrice();
            data[i][0] = p.getName(); // Nombre
            data[i][1] = p.getQuantity(); // Cantidad
            data[i][2] = formatCOP.format(precio); // Precio formateado
        }

        // Crear un DefaultTableModel personalizado que haga que las celdas no sean editables
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer todas las celdas no editables
            }
        };

        // Crear el JTable usando el modelo personalizado
        JTable productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajustar automáticamente el tamaño de las columnas

        // Establecer la fuente y el tamaño
        Font font = new Font("Arial", Font.PLAIN, 18); // Cambiar el tipo y tamaño de fuente
        productTable.setFont(font);
        productTable.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente más grande para el encabezado
        header.setBackground(Color.LIGHT_GRAY); // Fondo para el encabezado
        header.setForeground(Color.BLACK); // Color del texto del encabezado

        // Configuración de borde para mejorar la visibilidad
        productTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        productTable.setBackground(Color.WHITE); // Fondo de la tabla
        productTable.setSelectionBackground(Color.CYAN); // Color de selección
        productTable.setSelectionForeground(Color.BLACK); // Color del texto seleccionado

        // Añadir el JTable dentro de un JScrollPane
        JScrollPane scrollPane = new JScrollPane(productTable);
        listProductsDialog.add(scrollPane, BorderLayout.CENTER);

        // Panel para añadir nuevos productos
        JPanel addProductPanel = new JPanel(new GridLayout(1, 6, 10, 10)); // Panel con GridLayout para campos de entrada
        addProductPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Añadir márgenes

        JTextField nameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();

        addProductPanel.add(new JLabel("Nombre:"));
        addProductPanel.add(nameField);
        addProductPanel.add(new JLabel("Cantidad:"));
        addProductPanel.add(quantityField);
        addProductPanel.add(new JLabel("Precio:"));
        addProductPanel.add(priceField);

        listProductsDialog.add(addProductPanel, BorderLayout.NORTH);

        // Botón para añadir productos
        JButton addProductButton = createButton("Añadir Producto", e -> {
            try {
                String name = nameField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());

                // Crear nuevo producto y añadirlo al productoManager
                Producto newProduct = new Producto(name, quantity, price);
                productoAdminManager.addProduct(newProduct); // Método que debes tener en productoManager para añadir productos

                // Añadir el nuevo producto a la tabla
                tableModel.addRow(new Object[]{name, quantity, NumberFormat.getInstance(new Locale("es", "CO")).format(price)});

                // Limpiar los campos de entrada
                nameField.setText("");
                quantityField.setText("");
                priceField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(listProductsDialog, "Error al agregar el producto. Asegúrate de ingresar datos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Panel para los botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addProductButton);


        listProductsDialog.add(buttonPanel, BorderLayout.SOUTH);


        // Mostrar el diálogo
        listProductsDialog.setVisible(true);
        listProductsDialog.setLocationRelativeTo(null);
    }
}