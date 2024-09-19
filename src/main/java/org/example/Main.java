package org.example;

import org.example.manager.CompraManager;
import org.example.manager.ExcelManager;
import org.example.manager.ProductoManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.example.ui.UIHelpers.*;

public class Main {
    private static ProductoManager productoManager = new ProductoManager();


    private static JDialog compraDialog;


    public static void main(String[] args) {
        JFrame frame = new JFrame("Administrador de Licorera");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(3, 1));

        // Botones principales
        //JButton mesasButton = UIHelpers.createButton("Mesas", e -> showMesas());
        JButton compraButton = UIHelpers.createButton("Compra", e -> showCompraDialog());
        JButton adminProductosButton = UIHelpers.createButton("Administrar Productos", e -> showAdminProductosDialog());
        JButton salirButton = UIHelpers.createButton("Salir/Facturar", e -> {
            // Mostrar un cuadro de diálogo solicitando que el usuario escriba "Facturar"
            String input = JOptionPane.showInputDialog(null, "Por favor, escribe 'Facturar' para proceder:", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);

            // Verificar si el usuario ingresó correctamente la palabra "Facturar"
            if ("Facturar".equals(input)) {
                // Si la palabra es correcta, se procede a facturar y salir
                ExcelManager excelManager = new ExcelManager();
                excelManager.facturarYLimpiar();
                System.exit(0); // Salir del programa después de la facturación
            } else {
                // Si la palabra es incorrecta o el usuario cancela, mostrar un mensaje y regresar al menú principal
                JOptionPane.showMessageDialog(null, "Palabra incorrecta. Regresando al menú principal.", "Error", JOptionPane.ERROR_MESSAGE);
                // Aquí puedes volver al main o simplemente detener la ejecución del botón sin hacer nada
            }
        });
        //frame.add(mesasButton);
        frame.add(compraButton);
        frame.add(adminProductosButton);
        frame.add(salirButton);

        frame.setVisible(true);
    }



    private static void showAdminProductosDialog() {
        JDialog adminDialog = UIHelpers.createDialog("Administrar Productos", 300, 200, new GridLayout(2, 1));

        // Botones
        JButton addButton = UIHelpers.createButton("Agregar Producto", e -> showAddProductDialog());
        JButton listButton = UIHelpers.createButton("Listar Productos", e -> showListProductsDialog());

        // Añadir botones al diálogo
        adminDialog.add(addButton);
        adminDialog.add(listButton);

        adminDialog.setVisible(true);
    }

    private static void showAddProductDialog() {
        JDialog addProductDialog = UIHelpers.createDialog("Agregar Producto", 300, 200, new GridLayout(4, 2));

        // Campos de entrada para los productos
        addProductDialog.add(new JLabel("ID:"));
        JTextField idField = UIHelpers.createTextField();
        addProductDialog.add(idField);

        addProductDialog.add(new JLabel("Nombre:"));
        JTextField nameField = UIHelpers.createTextField();
        addProductDialog.add(nameField);

        addProductDialog.add(new JLabel("Cantidad:"));
        JTextField quantityField = UIHelpers.createTextField();
        addProductDialog.add(quantityField);


        addProductDialog.add(new JLabel("Precio:"));
        JTextField priceField = UIHelpers.createTextField();
        addProductDialog.add(priceField);

        JButton addButton = UIHelpers.createButton("Agregar", e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());

            Producto product = new Producto(id, name, quantity, price);
            productoManager.addProduct(product);

            JOptionPane.showMessageDialog(addProductDialog, "Producto agregado con éxito.");
            addProductDialog.dispose();
        });

        addProductDialog.add(addButton);
        addProductDialog.setVisible(true);
    }

    private static void showListProductsDialog() {
        // Crear el diálogo
        JDialog listProductsDialog = UIHelpers.createDialog("Listar Productos", 400, 300, new BorderLayout());

        // Crear el área de texto
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        // Obtener la lista de productos y formatearla
        List<Producto> products = productoManager.getProducts();
        String formattedProductList = productoManager.formatProductList(products);

        // Asignar la lista formateada al área de texto
        textArea.setText(formattedProductList);

        // Añadir el área de texto dentro de un JScrollPane
        listProductsDialog.add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Botón de cerrar
        JButton closeButton = UIHelpers.createButton("Cerrar", e -> listProductsDialog.dispose());
        listProductsDialog.add(closeButton, BorderLayout.SOUTH);

        // Mostrar el diálogo
        listProductsDialog.setVisible(true);
    }



    public static void showCompraDialog() {
        compraDialog = UIHelpers.createDialog("Realizar Compra", 500, 400, new BorderLayout());

        JPanel inputPanel = createInputPanel();
        compraDialog.add(inputPanel, BorderLayout.NORTH);

        JTable table = createProductTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        compraDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();
        compraDialog.add(totalPanel, BorderLayout.SOUTH);

        CompraManager compraManager = new CompraManager();

        JPanel buttonPanel = createButtonPanel(table, compraManager, compraDialog);
        compraDialog.add(buttonPanel, BorderLayout.SOUTH);

        compraDialog.setVisible(true);
    }

/*private static void showMesas() {
        // Lógica para el manejo de mesas
    }*/
}
