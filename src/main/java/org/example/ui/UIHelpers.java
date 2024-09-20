package org.example.ui;

import org.example.manager.VentaManager;
import org.example.manager.ExcelManager;
import org.example.manager.ProductoManager;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;

import static org.example.utils.Constants.CANTIDAD;
import static org.example.utils.Constants.PRODUCTO;


public class UIHelpers {

    private static ProductoManager productoManager = new ProductoManager();
    private static VentaManager ventaManager = new VentaManager();

    private static JComboBox<String> productComboBox;
    private static JSpinner cantidadSpinner;
    private static JTextField dineroRecibidoField;
    private static JTextField dineroTotalField;
    private static JLabel totalLabel;
    private static JLabel totalCompraLabel;
    private static JLabel devueltoLabel;
    private static DefaultTableModel tableModel;
    private static Component compraDialog;

    public static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
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

    public static JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Producto:"));
        productComboBox = new JComboBox<>();
        java.util.List<Producto> productos = productoManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }
        inputPanel.add(productComboBox);

        inputPanel.add(new JLabel("Cantidad:"));
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // Incremento en 1
        inputPanel.add(cantidadSpinner);

        inputPanel.add(new JLabel("Dinero recibido:"));
        dineroRecibidoField = UIHelpers.createTextField();
        inputPanel.add(dineroRecibidoField);

        inputPanel.add(new JLabel("Compra total:"));
        dineroTotalField = UIHelpers.createTextField();
        inputPanel.add(dineroTotalField);

        return inputPanel;
    }

    public static JTable createProductTable() {
        String[] columnNames = {PRODUCTO, CANTIDAD, "Precio Unitario", "Total", ""}; // Sin título en la columna de eliminar
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer(tableModel, ventaManager);

        table.getColumnModel().getColumn(1); // Spinner en la columna de cantidad

        table.getColumnModel().getColumn(4).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(4).setCellEditor(editorRenderer);
        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel(new GridLayout(3, 1));
        totalLabel = new JLabel("Total: $0.0");
        totalCompraLabel = new JLabel("Total Compra: $0.0");
        devueltoLabel = new JLabel("Devuelto: $0.0");

        totalPanel.add(totalLabel);
        totalPanel.add(totalCompraLabel);
        totalPanel.add(devueltoLabel);

        return totalPanel;
    }

    public static JPanel createButtonPanel(JTable table, VentaManager ventaManager, JDialog compraDialog) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

        JButton agregarProductoButton = createAddProductButton(table, ventaManager);
        JButton calcularDevueltoButton = createCalculateDevueltoButton(ventaManager, compraDialog);
        JButton confirmarCompraButton = createConfirmPurchaseButton(ventaManager, compraDialog);

        buttonPanel.add(agregarProductoButton);
        buttonPanel.add(calcularDevueltoButton);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }

    private static JButton createAddProductButton(JTable table, VentaManager ventaManager) {
        JButton agregarProductoButton = new JButton("Agregar Producto");
        agregarProductoButton.addActionListener(e -> {
            try {
                String selectedProduct = (String) productComboBox.getSelectedItem();
                int cantidad = (int) cantidadSpinner.getValue();

                Producto producto = productoManager.getProductByName(selectedProduct);
                double precioUnitario = producto.getPrice();
                double totalProducto = precioUnitario * cantidad;

                // Añadir producto a la tabla
                tableModel.addRow(new Object[]{selectedProduct, cantidad, precioUnitario, totalProducto, "X"});

                // Calcular el total general
                double total = 0.0;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    total += (double) tableModel.getValueAt(i, 3);
                }
                totalLabel.setText("Total: $" + total);

                totalCompraLabel.setText("Total Compra: $" + total);

                ventaManager.addProductToCart(producto, cantidad);
                dineroTotalField.setText(String.valueOf(total));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, "Cantidad debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return agregarProductoButton;
    }

    private static JButton createCalculateDevueltoButton(VentaManager ventaManager, JDialog compraDialog) {
        JButton calcularDevueltoButton = new JButton("Calcular Devuelto");
        calcularDevueltoButton.addActionListener(e -> ventaManager.calcularDineroDevuelto(dineroRecibidoField, devueltoLabel, tableModel, compraDialog));
        return calcularDevueltoButton;
    }

    private static JButton createConfirmPurchaseButton(VentaManager ventaManager, JDialog ventaDialog) {
        JButton confirmarCompraButton = new JButton("Confirmar Compra");
        confirmarCompraButton.addActionListener(e -> {
            try {
                double total = ventaManager.getTotalCartAmount();
                double dineroRecibido = 0;
                double devuelto = 0;

                if (!dineroRecibidoField.getText().isEmpty()) {
                    dineroRecibido = Double.parseDouble(dineroRecibidoField.getText());
                    devuelto = dineroRecibido - total;
                    devueltoLabel.setText("Devuelto: $" + devuelto);
                } else {
                    devueltoLabel.setText("Devuelto: -");
                }

                String ventaID = "Venta" + System.currentTimeMillis();
                LocalDateTime dateTime = LocalDateTime.now();

                List<String> listaDeProductos = ventaManager.getProductListForExcel();
                String listaProductosEnLinea = String.join("\n", listaDeProductos);

                ExcelManager excelManager = new ExcelManager();
                excelManager.savePurchase(ventaID, listaProductosEnLinea, ventaManager.getTotalCartAmount(), dateTime);



                //preguntar antes generar factura
                ventaManager.generarFactura(ventaID, listaDeProductos, ventaManager.getTotalCartAmount(), dineroRecibido, devuelto, dateTime);




                if (devuelto > 0) {
                    JOptionPane.showMessageDialog(ventaDialog, "devuelto $" + devuelto);
                }
                JOptionPane.showMessageDialog(ventaDialog, "Venta realizada con éxito. Total: $" + ventaManager.getTotalCartAmount());
                ventaDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ventaDialog, "Dinero recibido debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return confirmarCompraButton;
    }
}
