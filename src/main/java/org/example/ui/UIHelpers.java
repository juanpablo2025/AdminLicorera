package org.example.ui;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.VentaManager;
import org.example.manager.ExcelManager;
import org.example.manager.ProductoManager;
import org.example.manager.VentaMesaManager;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

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

    public static JPanel createInputPanel(JTable table, VentaMesaManager ventaMesaManager) {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2)); // Tres filas, dos columnas

        // Primera fila: ComboBox de productos
        inputPanel.add(new JLabel(PRODUCT_FIELD));
        productComboBox = new JComboBox<>();
        java.util.List<Producto> productos = productoManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }
        inputPanel.add(productComboBox);

        // Segunda fila: Spinner de cantidad
        inputPanel.add(new JLabel(CANTIDAD_FIELD));
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // Incremento en 1
        inputPanel.add(cantidadSpinner);

        // Tercera fila: Espacio vacío para alineación, y el botón alineado a la derecha
        inputPanel.add(new JLabel("")); // Espacio vacío en la primera celda de la fila

        // Crear un panel para el botón con FlowLayout alineado a la derecha
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaManager);
        buttonPanel.add(agregarProductoButton);

        // Añadir el buttonPanel al inputPanel (segunda celda de la tercera fila)
        inputPanel.add(buttonPanel);

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
        /*JButton calcularDevueltoButton = createCalculateDevueltoButton(ventaManager, compraDialog);*/
        JButton confirmarCompraButton = createConfirmPurchaseButton(ventaManager, compraDialog);

        buttonPanel.add(agregarProductoButton);
        /*buttonPanel.add(calcularDevueltoButton);*/
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }

    public static JButton createAddProductButton(JTable table, VentaManager ventaManager) {
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

    public static JButton createAddProductMesaButton(JTable table, VentaMesaManager ventaManager) {
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
                /*dineroTotalField.setText(String.valueOf(total));*/

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_AMOUNT, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return agregarProductoButton;
    }

   /* private static JButton createCalculateDevueltoButton(VentaManager ventaManager, JDialog compraDialog) {
        JButton calcularDevueltoButton = new JButton(CALCULATED_CHANGE);
        calcularDevueltoButton.addActionListener(e -> ventaManager.calcularDineroDevuelto(dineroRecibidoField, devueltoLabel, tableModel, compraDialog));
        return calcularDevueltoButton;
    }*/

    public static JButton createConfirmPurchaseButton(VentaManager ventaManager, JDialog ventaDialog) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);
        confirmarCompraButton.addActionListener(e -> {
            try {
                double total = ventaManager.getTotalCartAmount(); // Obtiene el total de la compra
                double dineroRecibido = 0;
                double devuelto = 0;

                // Procesar el dinero recibido
                if (!dineroRecibidoField.getText().isEmpty()) {
                    dineroRecibido = Double.parseDouble(dineroRecibidoField.getText());
                    devuelto = dineroRecibido - total;
                    devueltoLabel.setText(CHANGE_PESO + devuelto);
                } else {
                    devueltoLabel.setText(CHANGE_GUION);
                }

                // Generar un ID único para la venta
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000);
                LocalDateTime dateTime = LocalDateTime.now();

                // Obtener la lista de productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaManager.getProductListWithQuantities(); // Método que debes implementar

// Crear un StringBuilder para construir la lista de productos con nombre y cantidad
                StringBuilder listaProductosEnLinea = new StringBuilder();

// Iterar sobre el mapa de productos y cantidades
                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombreProducto = entrada.getKey(); // Nombre del producto
                    int cantidad = entrada.getValue(); // Cantidad comprada
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    // Añadir la información del producto al StringBuilder
                    listaProductosEnLinea.append(nombreProducto).append(" x").append(cantidad).append(" $").append(precioUnitario).append(" = ").append(precioTotal+"\n");
                }


                //String listaProductosEnLinea = String.join(N, productosComprados.keySet()+" x"+productosComprados.values());

                // Guardar la compra en Excel
                ExcelManager excelManager = new ExcelManager();
                excelManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime);

                // Descontar la cantidad de los productos del stock en el archivo Excel
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    // Actualizar la cantidad del producto en la pestaña de productos
                    Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                        String nombreProducto = entry.getKey();
                        int cantidadComprada = entry.getValue();

                        boolean productoEncontrado = false;

                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                // Suponiendo que el nombre del producto está en la columna 1
                                if (row.getCell(1).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                                    // Asegurarse de que la celda de cantidad no sea nula y sea numérica
                                    Cell cantidadCell = row.getCell(2);
                                    if (cantidadCell != null && cantidadCell.getCellType() == CellType.NUMERIC) {
                                        int cantidadActual = (int) cantidadCell.getNumericCellValue(); // Suponiendo que la cantidad está en la columna 2
                                        int nuevaCantidad = cantidadActual - cantidadComprada;  // Restar la cantidad comprada

                                        // Verificar que no se intente establecer una cantidad negativa
                                        if (nuevaCantidad < 0) {
                                            JOptionPane.showMessageDialog(ventaDialog, "No hay suficiente cantidad del producto '" + nombreProducto + "' en stock.", "Error", JOptionPane.ERROR_MESSAGE);
                                            return; // Salir del método si no hay suficiente stock
                                        } else {
                                            cantidadCell.setCellValue(nuevaCantidad);  // Actualizar la cantidad
                                            productoEncontrado = true;
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(ventaDialog, "La cantidad actual no es válida para el producto '" + nombreProducto + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                                        return; // Salir si la cantidad no es válida
                                    }
                                    break; // Salir del bucle una vez que se actualiza el producto
                                }
                            }
                        }

                        // Verificar si el producto fue encontrado y actualizado
                        if (!productoEncontrado) {
                            JOptionPane.showMessageDialog(ventaDialog, "Producto '" + nombreProducto + "' no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Guardar la actualización de productos
                    try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                        workbook.write(fos);
                        System.out.println("Cantidad de productos actualizada exitosamente.");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Preguntar al usuario si quiere imprimir la factura
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario selecciona 'Sí', generar e imprimir la factura
                    ventaManager.generarFactura(ventaID, Collections.singletonList(String.valueOf(listaProductosEnLinea)), total, dateTime);
                    // Código para imprimir el recibo o mostrar un mensaje indicando que el recibo ha sido generado.
                }

                // Si hay cambio, mostrarlo en un diálogo
                if (devuelto > 0) {
                    JOptionPane.showMessageDialog(ventaDialog, CHANGE + devuelto + PESOS);
                }

                // Mostrar un mensaje de éxito de la compra
                JOptionPane.showMessageDialog(ventaDialog, PURCHASE_SUCCEDED + total);

                // Cerrar el diálogo de la venta
                ventaDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ventaDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });
        return confirmarCompraButton;
    }
}
