package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Main {
    private static ExcelManager excelManager = new ExcelManager();

    public static void main(String[] args) {
        // Crear ventana principal
        JFrame frame = new JFrame("Administrador de Licorera");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(4, 1));

        // Botones
        JButton mesasButton = new JButton("Mesas");
        JButton compraButton = new JButton("Compra");
        JButton adminProductosButton = new JButton("Administrar Productos");
        JButton salirButton = new JButton("Salir/Facturar");

        // Añadir botones a la ventana
        frame.add(mesasButton);
        frame.add(compraButton);
        frame.add(adminProductosButton);
        frame.add(salirButton);

        // Acción para "Administrar Productos"
        adminProductosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAdminProductosDialog();
            }
        });

        // Acción para "Compra"
        compraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCompraDialog();
            }
        });

        // Acción para "Salir/Facturar"
        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);  // Cierra la aplicación
            }
        });

        // Mostrar la ventana
        frame.setVisible(true);
    }

    // Método para mostrar el submenú "Administrar Productos"
    private static void showAdminProductosDialog() {
        JDialog adminProductosDialog = new JDialog();
        adminProductosDialog.setTitle("Administrar Productos");
        adminProductosDialog.setSize(300, 200);
        adminProductosDialog.setLayout(new GridLayout(2, 1));

        JButton addProductButton = new JButton("Agregar Producto");
        JButton listProductsButton = new JButton("Mostrar Lista");

        // Añadir botones al diálogo
        adminProductosDialog.add(addProductButton);
        adminProductosDialog.add(listProductsButton);

        // Acción para "Agregar Producto"
        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddProductDialog();
            }
        });

        // Acción para "Mostrar Lista"
        listProductsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showListProductsDialog();
            }
        });

        adminProductosDialog.setVisible(true);
    }

    // Método para agregar un producto (sin cambios)
    private static void showAddProductDialog() {
        JDialog addProductDialog = new JDialog();
        addProductDialog.setTitle("Agregar Producto");
        addProductDialog.setSize(300, 200);
        addProductDialog.setLayout(new GridLayout(4, 2));

        // Campos de entrada
        addProductDialog.add(new JLabel("ID:"));
        JTextField idField = new JTextField();
        addProductDialog.add(idField);

        addProductDialog.add(new JLabel("Nombre:"));
        JTextField nameField = new JTextField();
        addProductDialog.add(nameField);

        addProductDialog.add(new JLabel("Cantidad:"));
        JTextField quantityField = new JTextField();
        addProductDialog.add(quantityField);

        addProductDialog.add(new JLabel("Precio:"));
        JTextField priceField = new JTextField();
        addProductDialog.add(priceField);

        JButton addButton = new JButton("Agregar");
        addProductDialog.add(addButton);

        // Acción para agregar producto
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());

                Producto product = new Producto(id, name, quantity, price);
                excelManager.addProduct(product);

                JOptionPane.showMessageDialog(addProductDialog, "Producto agregado.");
                addProductDialog.dispose();
            }
        });

        addProductDialog.setVisible(true);
    }

    // Método para mostrar la lista de productos (sin cambios)
    private static void showListProductsDialog() {
        JDialog listProductsDialog = new JDialog();
        listProductsDialog.setTitle("Listar Productos");
        listProductsDialog.setSize(400, 300);
        listProductsDialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        List<Producto> products = excelManager.getProducts();
        StringBuilder productList = new StringBuilder("ID\tNombre\tCantidad\tPrecio\n");
        for (Producto p : products) {
            productList.append(p.getId()).append("\t")
                    .append(p.getName()).append("\t")
                    .append(p.getQuantity()).append("\t")
                    .append(p.getPrice()).append("\n");
        }

        textArea.setText(productList.toString());
        listProductsDialog.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar");
        listProductsDialog.add(closeButton, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> listProductsDialog.dispose());

        listProductsDialog.setVisible(true);
    }

    // Método para la pantalla de compra
    private static void showCompraDialog() {
        JDialog compraDialog = new JDialog();
        compraDialog.setTitle("Compra de Productos");
        compraDialog.setSize(400, 300);
        compraDialog.setLayout(new GridLayout(5, 2));

        // Campos para seleccionar producto y cantidad
        JComboBox<String> productComboBox = new JComboBox<>();
        List<Producto> products = excelManager.getProducts();
        for (Producto p : products) {
            productComboBox.addItem(p.getName());
        }

        JTextField quantityField = new JTextField();
        JTextField totalField = new JTextField();
        totalField.setEditable(false);

        compraDialog.add(new JLabel("Producto:"));
        compraDialog.add(productComboBox);
        compraDialog.add(new JLabel("Cantidad:"));
        compraDialog.add(quantityField);
        compraDialog.add(new JLabel("Total:"));
        compraDialog.add(totalField);

        JButton calculateButton = new JButton("Calcular Total");
        JButton checkoutButton = new JButton("Finalizar Compra");

        // Acción para calcular el total
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int quantity = Integer.parseInt(quantityField.getText());
                String selectedProduct = (String) productComboBox.getSelectedItem();
                Producto product = excelManager.getProductByName(selectedProduct);
                double total = quantity * product.getPrice();
                totalField.setText(String.valueOf(total));
            }
        });

        // Acción para finalizar la compra
        checkoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double total = Double.parseDouble(totalField.getText());
                String receivedAmountStr = JOptionPane.showInputDialog(compraDialog, "Dinero recibido:");
                double receivedAmount = Double.parseDouble(receivedAmountStr);
                double change = receivedAmount - total;

                // Guardar la compra en Excel
                excelManager.savePurchase(productComboBox.getSelectedItem().toString(), quantityField.getText(), total);

                // Mostrar cambio
                JOptionPane.showMessageDialog(compraDialog, "Total: " + total + "\nCambio: " + change);
                compraDialog.dispose();
            }
        });

        compraDialog.add(calculateButton);
        compraDialog.add(checkoutButton);

        compraDialog.setVisible(true);
    }
}
