package org.example.ui.uiUser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.userManager.ExcelUserManager;
import org.example.manager.userManager.ProductoUserManager;
import org.example.manager.userManager.VentaMesaUserManager;
import org.example.model.Producto;
import org.example.utils.FormatterHelpers;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.manager.userManager.ExcelUserManager.actualizarCantidadStockExcel;
import static org.example.manager.userManager.ExcelUserManager.cargarProductosMesaDesdeExcel;
import static org.example.manager.userManager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.manager.userManager.ProductoUserManager.getProductListWithQuantities;
import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserMesas.showMesas;
import static org.example.utils.Constants.*;
import static org.example.utils.Constants.ERROR_TITLE;

public class UIUserVenta {
    private static ProductoUserManager productoUserManager = new ProductoUserManager();

    private static JDialog ventaMesaDialog;



    public static void showVentaMesaDialog(List<String[]> productos, String mesaID) {
        ventaMesaDialog = createDialog("Realizar Venta", 800, 600, new BorderLayout());
        ventaMesaDialog.setResizable(true);

        // Añadir un WindowListener para detectar el cierre de la ventana
        ventaMesaDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showMesas();
            }
        });

        // Variable para el total acumulado
        AtomicReference<Double> sumaTotal = new AtomicReference<>(0.0);

        // Crear la tabla de productos y el modelo
        JTable table = createProductTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Cargar los productos en la tabla y calcular el total inicial
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[0].trim();
                int cantidad = Integer.parseInt(productoDetalles[1].substring(1).trim());
                double precioUnitario = Double.parseDouble(productoDetalles[2].substring(1).trim());
                double total = cantidad * precioUnitario;

                tableModel.addRow(new Object[]{nombreProducto, cantidad, precioUnitario, total});
                sumaTotal.updateAndGet(v -> v + total);
            } catch (NumberFormatException ex) {
                System.err.println("Error al parsear datos: " + Arrays.toString(productoDetalles));
                ex.printStackTrace();
            }
        }

        // Campo para mostrar el total
        JTextField totalField = new JTextField("Total: $" + FormatterHelpers.formatearMoneda(sumaTotal.get()) + " Pesos");
        totalField.setFont(new Font("Arial", Font.BOLD, 24));
        totalField.setForeground(Color.RED);
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(JTextField.RIGHT);
        totalField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalField.setVisible(sumaTotal.get() > 0);

// Panel del total
        JPanel totalPanel = createTotalPanel();
        totalPanel.add(totalField, BorderLayout.CENTER);

// Listener para actualizar el total al cambiar la cantidad o agregar productos
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // Escuchar tanto cambios en las cantidades (UPDATE) como nuevas filas agregadas (INSERT)
                if (e.getType() == TableModelEvent.UPDATE || e.getType() == TableModelEvent.INSERT) {
                    double nuevoTotal = 0;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        int cantidad = (int) tableModel.getValueAt(i, 1); // Cantidad en la columna 1
                        double precioUnitario = (double) tableModel.getValueAt(i, 2); // Precio unitario en la columna 2
                        double subtotal = cantidad * precioUnitario;

                        if ((double) tableModel.getValueAt(i, 3) != subtotal) {
                            tableModel.setValueAt(subtotal, i, 3); // Actualiza solo si cambia el subtotal
                        }

                        nuevoTotal += subtotal; // Suma cada subtotal al total general
                    }

                    sumaTotal.set(nuevoTotal); // Actualiza suma total acumulada
                    totalField.setText("Total: $" + FormatterHelpers.formatearMoneda(sumaTotal.get()) + " Pesos");
                    totalField.setVisible(sumaTotal.get() > 0); // Mostrar/ocultar según el total acumulado
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = createInputPanel(table, new VentaMesaUserManager());
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanelVentaMesa(table, new VentaMesaUserManager(), ventaMesaDialog, mesaID);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        ventaMesaDialog.add(southPanel, BorderLayout.SOUTH);
        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);
    }




    // Método modificado para crear el panel de botones de la mesa
    public static JPanel createButtonPanelVentaMesa(JTable table, VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // Crear el botón de guardar compra y asignar el ID de la mesa y la tabla
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaUserManager, mesaID, table); // Agregar el argumento `table`
        guardarCompra.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18)); // Configuración de fuente
        buttonPanel.add(guardarCompra);

        // Crear el botón de confirmar compra y asignar el ID de la mesa y el diálogo de compra
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaUserManager, compraDialog, mesaID);
        confirmarCompraButton.setFont(new java.awt.Font("Arial", Font.BOLD, 18)); // Configuración de fuente
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }


    public static JButton createConfirmPurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);

        confirmarCompraButton.addActionListener(e -> {
            try {
                // Continuar con la confirmación de compra
                double total = 0;
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000);
                LocalDateTime dateTime = LocalDateTime.now();
                StringBuilder listaProductosEnLinea = new StringBuilder();

                List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
                if (!productosPrevios.isEmpty()) {
                    for (String[] productoPrevio : productosPrevios) {
                        String nombreProducto = productoPrevio[0];
                        int cantidadPrev = Integer.parseInt(productoPrevio[1].substring(1)); // xCantidad
                        double precioUnitarioPrev = Double.parseDouble(productoPrevio[2].substring(1)); // $PrecioUnitario
                        double precioTotalPrev = precioUnitarioPrev * cantidadPrev;

                        listaProductosEnLinea.append(nombreProducto)
                                .append(" x").append(cantidadPrev)
                                .append(" $").append(precioUnitarioPrev)
                                .append(" = ").append(precioTotalPrev).append("\n");

                        total += precioTotalPrev;
                    }
                }

                Map<String, Integer> productosComprados = getProductListWithQuantities();
                if (productosComprados.isEmpty() && productosPrevios.isEmpty()) {
                    JOptionPane.showMessageDialog(compraDialog, "No hay productos agregados a la mesa.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombreProducto = entrada.getKey();
                    int cantidad = entrada.getValue();
                    Producto producto = productoUserManager.getProductByName(nombreProducto);

                    if (producto.getCantidad() < cantidad) {
                        JOptionPane.showMessageDialog(compraDialog, "No hay suficiente stock para " + nombreProducto, "Error de stock", JOptionPane.ERROR_MESSAGE);
                        compraDialog.dispose();
                        return;
                    }

                    boolean productoPrevioExiste = productosPrevios.stream()
                            .anyMatch(p -> p[0].equalsIgnoreCase(nombreProducto));

                    if (!productoPrevioExiste) {
                        double precioUnitario = producto.getPrice();
                        double precioTotal = precioUnitario * cantidad;

                        listaProductosEnLinea.append(nombreProducto)
                                .append(" x").append(cantidad)
                                .append(" $").append(precioUnitario)
                                .append(" = ").append(precioTotal).append("\n");

                        total += precioTotal;
                    }
                }

                // Guardar la compra en Excel
                ExcelUserManager excelUserManager = new ExcelUserManager();
                excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime);

                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null) {
                                Cell idCell = row.getCell(0);
                                if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(mesaID)) {
                                    Cell estadoCell = row.getCell(1);
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(1);
                                    }
                                    estadoCell.setCellValue("Libre");

                                    Cell productosCell = row.getCell(2);
                                    if (productosCell != null) {
                                        productosCell.setCellValue("");
                                    }

                                    Cell totalCell = row.getCell(3);
                                    if (totalCell != null) {
                                        totalCell.setCellValue(0.0);
                                    }
                                    break;
                                }
                            }
                        }

                        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                            workbook.write(fos);
                        }
                    }
                }

                // Mostramos el dialogo de confirmación
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                if (respuesta == JOptionPane.YES_OPTION) {
                    // Corregimos aquí, enviamos la lista completa y no solo un String.
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime);
                }

                JOptionPane.showMessageDialog(compraDialog, PURCHASE_SUCCEDED + "\n" + "Total: $ " + formatCOP.format(total));

                // Actualizar las cantidades en el stock de Excel
                actualizarCantidadStockExcel(productosComprados);




            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            compraDialog.dispose();
            showMesas();
        });

        return confirmarCompraButton;
    }



    public static JButton createSavePurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, String mesaID, JTable productosTable) {
        JButton saveCompraButton = new JButton("Guardar Compra");

        saveCompraButton.addActionListener(e -> {
            try {
                // Obtener los productos de la tabla
                DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();
                int rowCount = tableModel.getRowCount();

                // Validar que haya productos en la compra
                if (rowCount == 0) {
                    JOptionPane.showMessageDialog(null, "No hay productos agregados a la compra.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Salir si no hay productos
                }

                // Crear un Map para almacenar los productos y cantidades
                Map<String, Integer> productosComprados = new HashMap<>();

                // Iterar sobre la tabla y obtener los productos y cantidades
                for (int i = 0; i < rowCount; i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, 0); // Columna 0: nombre del producto
                    int cantidad = (int) tableModel.getValueAt(i, 1); // Columna 1: cantidad del producto

                    productosComprados.put(nombreProducto, cantidad);
                }

                // Verificar el stock para cada producto
                for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                    String nombreProducto = entry.getKey();
                    int cantidadComprada = entry.getValue();
                    Producto producto = productoUserManager.getProductByName(nombreProducto);
                    if (producto.getCantidad() < cantidadComprada) {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto: " + nombreProducto, "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Salir si no hay suficiente stock
                    }
                }

                double total = productoUserManager.getTotalCartAmount(); // Obtener el total de la compra
                LocalDateTime dateTime = LocalDateTime.now(); // Fecha y hora actuales

                // Guardar la compra en la pestaña "mesas"
                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    // Acceder a la hoja de "mesas"
                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        boolean mesaEncontrada = false;
                        for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null) {
                                Cell idCell = row.getCell(0); // Columna A: ID de la mesa
                                if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(mesaID)) {
                                    mesaEncontrada = true;

                                    // Cambiar el estado a "Ocupada"
                                    Cell estadoCell = row.getCell(1); // Columna B: Estado de la mesa
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(1);
                                    }
                                    estadoCell.setCellValue("Ocupada");

                                    // **Sobrescribir** los productos existentes en la columna C
                                    Cell productosCell = row.getCell(2); // Columna C: Productos
                                    if (productosCell == null) {
                                        productosCell = row.createCell(2);
                                    }

                                    // Crear una nueva lista de productos a partir de la compra actual (basada en la tabla)
                                    StringBuilder listaProductos = new StringBuilder();
                                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                                        String nombreProducto = entry.getKey();
                                        int cantidadComprada = entry.getValue();
                                        Producto producto = productoUserManager.getProductByName(nombreProducto);
                                        double precioUnitario = producto.getPrice();
                                        double precioTotal = precioUnitario * cantidadComprada;

                                        // Crear una línea para cada producto (sobreescribiendo la lista anterior)
                                        listaProductos.append(nombreProducto)
                                                .append(" x")
                                                .append(cantidadComprada)
                                                .append(" $")
                                                .append(precioUnitario)
                                                .append(" = ")
                                                .append(precioTotal)
                                                .append("\n");
                                    }

                                    // Sobrescribir la celda de productos con la nueva lista
                                    productosCell.setCellValue(listaProductos.toString());

                                    // Guardar el total en la columna D
                                    Cell totalCell = row.getCell(3); // Columna D: Total de la compra
                                    if (totalCell == null) {
                                        totalCell = row.createCell(3);
                                    }
                                    totalCell.setCellValue(total);

                                    // Terminar el bucle ya que la mesa fue encontrada
                                    break;
                                }
                            }
                        }

                        // Si la mesa no fue encontrada, mostrar un mensaje de error
                        if (!mesaEncontrada) {
                            JOptionPane.showMessageDialog(null, "Mesa " + mesaID + " no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Guardar los cambios en el archivo Excel
                        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                            workbook.write(fos); // Sobrescribir el archivo con los cambios
                        }

                        JOptionPane.showMessageDialog(null, "Compra guardada para la mesa: " + mesaID + ".");

                        // Cerrar el diálogo de la venta
                        ventaMesaDialog.dispose();
                        showMesas();

                    } else {
                        JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return saveCompraButton;
    }


}
