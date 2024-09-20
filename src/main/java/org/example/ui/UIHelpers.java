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

import static org.example.utils.Constants.*;


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
        JPanel inputPanel = new JPanel(new GridLayout(FOUR, TWO));

        inputPanel.add(new JLabel(PRODUCT_FIELD));
        productComboBox = new JComboBox<>();
        java.util.List<Producto> productos = productoManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }
        inputPanel.add(productComboBox);

        inputPanel.add(new JLabel(CANTIDAD_FIELD));
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(ONE, ONE, ONE_HUNDRED, ONE)); // Incremento en 1
        inputPanel.add(cantidadSpinner);

        inputPanel.add(new JLabel(DINERO_RECIBIDO));
        dineroRecibidoField = UIHelpers.createTextField();
        inputPanel.add(dineroRecibidoField);

        inputPanel.add(new JLabel(COMPRA_TOTAL));
        dineroTotalField = UIHelpers.createTextField();
        inputPanel.add(dineroTotalField);

        return inputPanel;
    }

    public static JTable createProductTable() {
        String[] columnNames = {PRODUCTO, CANTIDAD, PRECIO_UNITARIO, TOTALP, SPACE}; // Sin título en la columna de eliminar
        tableModel = new DefaultTableModel(columnNames, ZERO);
        JTable table = new JTable(tableModel);
        UnifiedEditorRenderer editorRenderer = new UnifiedEditorRenderer(tableModel, ventaManager);

        table.getColumnModel().getColumn(ONE); // Spinner en la columna de cantidad

        table.getColumnModel().getColumn(FOUR).setCellRenderer(editorRenderer);
        table.getColumnModel().getColumn(FOUR).setCellEditor(editorRenderer);
        return table;
    }

    public static JPanel createTotalPanel() {
        JPanel totalPanel = new JPanel(new GridLayout(THREE, ONE));
        totalLabel = new JLabel(TOTAL_DOUBLE_ZERO_INIT);
        totalCompraLabel = new JLabel(TOTAL_PURCHASE_INIT);
        devueltoLabel = new JLabel(CHANGE_INIT);

        totalPanel.add(totalLabel);
        totalPanel.add(totalCompraLabel);
        totalPanel.add(devueltoLabel);

        return totalPanel;
    }

    public static JPanel createButtonPanel(JTable table, VentaManager ventaManager, JDialog compraDialog) {
        JPanel buttonPanel = new JPanel(new GridLayout(ONE, THREE));

        JButton agregarProductoButton = createAddProductButton(table, ventaManager);
        JButton calcularDevueltoButton = createCalculateDevueltoButton(ventaManager, compraDialog);
        JButton confirmarCompraButton = createConfirmPurchaseButton(ventaManager, compraDialog);

        buttonPanel.add(agregarProductoButton);
        buttonPanel.add(calcularDevueltoButton);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }

    private static JButton createAddProductButton(JTable table, VentaManager ventaManager) {
        JButton agregarProductoButton = new JButton(AGREGAR_PRODUCTO);
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
                double total = ZERO_DOUBLE;
                for (int i = ZERO; i < tableModel.getRowCount(); i++) {
                    total += (double) tableModel.getValueAt(i, THREE);
                }
                totalLabel.setText(TOTAL_PESO + total);

                totalCompraLabel.setText(TOTAL_COMPRA_PESO + total);

                ventaManager.addProductToCart(producto, cantidad);
                dineroTotalField.setText(String.valueOf(total));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_AMOUNT, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return agregarProductoButton;
    }

    private static JButton createCalculateDevueltoButton(VentaManager ventaManager, JDialog compraDialog) {
        JButton calcularDevueltoButton = new JButton(CALCULATED_CHANGE);
        calcularDevueltoButton.addActionListener(e -> ventaManager.calcularDineroDevuelto(dineroRecibidoField, devueltoLabel, tableModel, compraDialog));
        return calcularDevueltoButton;
    }

    private static JButton createConfirmPurchaseButton(VentaManager ventaManager, JDialog ventaDialog) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);
        confirmarCompraButton.addActionListener(e -> {
            try {
                double total = ventaManager.getTotalCartAmount();
                double dineroRecibido = ZERO;
                double devuelto = ZERO;

                if (!dineroRecibidoField.getText().isEmpty()) {
                    dineroRecibido = Double.parseDouble(dineroRecibidoField.getText());
                    devuelto = dineroRecibido - total;
                    devueltoLabel.setText(CHANGE_PESO + devuelto);
                } else {
                    devueltoLabel.setText(CHANGE_GUION);
                }

                String ventaID = VENTA + System.currentTimeMillis();
                LocalDateTime dateTime = LocalDateTime.now();

                List<String> listaDeProductos = ventaManager.getProductListForExcel();
                String listaProductosEnLinea = String.join(N, listaDeProductos);

                ExcelManager excelManager = new ExcelManager();
                excelManager.savePurchase(ventaID, listaProductosEnLinea, ventaManager.getTotalCartAmount(), dateTime);



                //preguntar antes generar factura
                ventaManager.generarFactura(ventaID, listaDeProductos, ventaManager.getTotalCartAmount(), dineroRecibido, devuelto, dateTime);




                if (devuelto > ZERO) {
                    JOptionPane.showMessageDialog(ventaDialog, CHANGE + devuelto+PESOS);
                }
                JOptionPane.showMessageDialog(ventaDialog, PURCHASE_SUCCEDED + ventaManager.getTotalCartAmount());
                ventaDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ventaDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });
        return confirmarCompraButton;
    }
}
