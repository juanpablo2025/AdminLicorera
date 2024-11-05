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
import javax.swing.table.JTableHeader;
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
        ventaMesaDialog = createDialog("Realizar Venta", 1280, 720, new BorderLayout());
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

        // Crear la tabla de productos usando createProductTable
        JTable table = createProductTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Establecer la fuente y el tamaño de la tabla
        Font font = new Font("Arial", Font.PLAIN, 18); // Cambiar el tipo y tamaño de fuente
        table.setFont(font);
        table.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente más grande para el encabezado
        header.setBackground(Color.LIGHT_GRAY); // Fondo para el encabezado
        header.setForeground(Color.BLACK); // Color del texto del encabezado

        // Configuración de borde y colores para la tabla
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        table.setBackground(Color.WHITE); // Fondo de la tabla
        table.setSelectionBackground(Color.CYAN); // Color de selección
        table.setSelectionForeground(Color.BLACK); // Color del texto seleccionado

        tableModel.setRowCount(0);

        // Cargar los productos en la tabla y calcular el total inicial
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[0].trim();
                int cantidad = Integer.parseInt(productoDetalles[1].substring(1).trim());
                double precioUnitario = Double.parseDouble(productoDetalles[2].substring(1).trim());
                double total = cantidad * precioUnitario;

                tableModel.addRow(new Object[]{nombreProducto, cantidad, FormatterHelpers.formatearMoneda(precioUnitario), total});
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
            // Bandera para evitar recursión
            private boolean updatingTable = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (updatingTable) return;

                updatingTable = true;
                try {
                    double nuevoTotalGeneral = 0;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String nombreProducto = (String) tableModel.getValueAt(i, 0);
                        int cantidad = (int) tableModel.getValueAt(i, 1);
                        Producto producto = ProductoUserManager.getProductByName(nombreProducto);

                        double precioUnitario = producto != null ? producto.getPrice() : 0.0;
                        double subtotal = cantidad * precioUnitario;
                        tableModel.setValueAt(subtotal, i, 3);
                        nuevoTotalGeneral += subtotal;
                    }

                    totalField.setText("Total: $" + FormatterHelpers.formatearMoneda(nuevoTotalGeneral) + " Pesos");
                    totalField.setVisible(nuevoTotalGeneral > 0);
                } finally {
                    updatingTable = false;
                }
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = createInputPanel(table, new VentaMesaUserManager());
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanelVentaMesa(table, new VentaMesaUserManager(), ventaMesaDialog, mesaID);

        // Botón independiente para abrir el submenú de productos
        JButton openSubMenuButton = new JButton("Abrir Submenú de Productos");
        openSubMenuButton.setFont(new Font("Arial", Font.BOLD, 18));
        openSubMenuButton.addActionListener(e -> openProductSubMenu(table, productoUserManager.getProducts()));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        //southPanel.add(openSubMenuButton, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        ventaMesaDialog.add(southPanel, BorderLayout.SOUTH);
        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);
    }

    private static void openProductSubMenu(JTable table, List<Producto> productos) {
        JDialog subMenuDialog = new JDialog(ventaMesaDialog, "Seleccionar Producto", true);
        subMenuDialog.setSize(800, 600);
        subMenuDialog.setLayout(new BorderLayout());

        // Panel principal para los botones
        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columnas, filas dinámicas

        for (Producto producto : productos) {
            JButton productoButton = new JButton(producto.getName());
            productoButton.setPreferredSize(new Dimension(120, 50)); // Tamaño de botón fijo
            productoButton.setFont(new Font("Arial", Font.BOLD, 16));

            productoButton.addActionListener(e -> {
                int cantidad = 1;
                double precioUnitario = producto.getPrice();
                double total = cantidad * precioUnitario;

                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                tableModel.addRow(new Object[]{
                        producto.getName(),
                        cantidad,
                        FormatterHelpers.formatearMoneda(precioUnitario),
                        total
                });

                updateTotalField(table);
                subMenuDialog.dispose();
            });

            buttonPanel.add(productoButton);
        }

        // Añadir JScrollPane para que se pueda hacer scroll en el panel de botones
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        subMenuDialog.add(scrollPane, BorderLayout.CENTER);
        subMenuDialog.setLocationRelativeTo(ventaMesaDialog);
        subMenuDialog.setVisible(true);
    }

    private static void updateTotalField(JTable table) {
        double nuevoTotal = 0;
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int cantidad = (int) tableModel.getValueAt(i, 1);
            double precioUnitario = Double.parseDouble(tableModel.getValueAt(i, 2).toString().replace(",", ""));
            nuevoTotal += cantidad * precioUnitario;
        }

        JTextField totalField = (JTextField) ((JPanel) ventaMesaDialog.getContentPane().getComponent(2)).getComponent(0);
        totalField.setText("Total: $" + FormatterHelpers.formatearMoneda(nuevoTotal) + " Pesos");
        totalField.setVisible(nuevoTotal > 0);
    }


    // Método modificado para crear el panel de botones de la mesa
    public static JPanel createButtonPanelVentaMesa(JTable table, VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // Crear el botón de guardar compra y asignar el ID de la mesa y la tabla
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaUserManager, mesaID, table);
        guardarCompra.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18)); // Configuración de fuente
        buttonPanel.add(guardarCompra);

        // Crear el botón de confirmar compra y asignar el ID de la mesa y el diálogo de compra
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaUserManager, compraDialog, mesaID);
        confirmarCompraButton.setFont(new java.awt.Font("Arial", Font.BOLD, 18)); // Configuración de fuente

        // Verificar si hay productos en Excel para la mesa
        List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
        boolean productosEnExcel = !productosPrevios.isEmpty();

        // Deshabilitar el botón si no hay productos en la tabla ni en Excel
        confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0);

        // Agregar un listener a la tabla para verificar el estado al cambiar su contenido
        ((DefaultTableModel) table.getModel()).addTableModelListener(e -> {
            confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0);
        });

        // Añadir el botón de confirmación al panel
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }


    public static JButton createConfirmPurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);


        confirmarCompraButton.addActionListener(e -> {
            try {


                double total = 0;
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000) + " " + mesaID;
                LocalDateTime dateTime = LocalDateTime.now();
                StringBuilder listaProductosEnLinea = new StringBuilder();

                // Inicializar mapas vacíos para la venta actual
                Map<String, Integer> cantidadTotalPorProducto = new HashMap<>();
                Map<String, Double> precioUnitarioPorProducto = new HashMap<>();



                // Cargar los productos previamente guardados en la mesa desde Excel
                List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);

                // Sumar el total de los productos previamente cargados
                if (!productosPrevios.isEmpty()) {
                    for (String[] productoPrevio : productosPrevios) {
                        String nombreProducto = productoPrevio[0];
                        int cantidadPrev = Integer.parseInt(productoPrevio[1].substring(1)); // xCantidad
                        double precioUnitarioPrev = Double.parseDouble(productoPrevio[2].substring(1)); // $PrecioUnitario
                        double precioTotalPrev = precioUnitarioPrev * cantidadPrev;

                        // Añadir producto a la lista de productos en línea
                        listaProductosEnLinea.append(nombreProducto)
                                .append(" x").append(cantidadPrev)
                                .append(" $").append(precioUnitarioPrev)
                                .append(" = ").append(precioTotalPrev).append("\n");

                        // Sumar al total general (solo productos previos)
                        total += precioTotalPrev;
                    }
                }


                // Procesar productos adicionales en el carrito
                Map<String, Integer> productosComprados = getProductListWithQuantities();
                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombreProducto = entrada.getKey();
                    int cantidadAdicional = entrada.getValue();
                    Producto producto = productoUserManager.getProductByName(nombreProducto);

                    if (producto == null || producto.getQuantity() < cantidadAdicional) {
                        JOptionPane.showMessageDialog(compraDialog, "No hay suficiente stock para " + nombreProducto, "Error de stock", JOptionPane.ERROR_MESSAGE);
                        compraDialog.dispose();
                        return;
                    }

                    // Actualizar la cantidad total del producto
                    int cantidadTotal = cantidadTotalPorProducto.getOrDefault(nombreProducto, 0) + cantidadAdicional;
                    cantidadTotalPorProducto.put(nombreProducto, cantidadTotal);
                    precioUnitarioPorProducto.put(nombreProducto, producto.getPrice());
                }

                // Generar resumen de productos y calcular el total
                for (Map.Entry<String, Integer> entrada : cantidadTotalPorProducto.entrySet()) {
                    String nombreProducto = entrada.getKey();
                    int cantidadTotal = entrada.getValue();
                    double precioUnitario = precioUnitarioPorProducto.get(nombreProducto);
                    double precioTotal = precioUnitario * cantidadTotal;

                    listaProductosEnLinea.append(nombreProducto)
                            .append(" x").append(cantidadTotal)
                            .append(" $").append(precioUnitario)
                            .append(" = ").append(precioTotal).append("\n");

                    total += precioTotal;
                }

                // Crear íconos redimensionados para los métodos de pago
                ImageIcon iconoBancolombia = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/bancolombia.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoNequi = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/nequi.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoEfectivo = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/dinero.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoDaviplata = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/Daviplata.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
                ImageIcon iconoDatafono = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/datafono.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));

                // Crear un diálogo modal personalizado
                JDialog dialogoPago = new JDialog(compraDialog, "Seleccione el método de pago", true);
                dialogoPago.setSize(1300, 150);
                dialogoPago.setLayout(new BorderLayout());
                dialogoPago.setResizable(false);

                // Crear panel para el menú de pago
                JPanel panelPago = new JPanel();
                panelPago.setLayout(new GridLayout(1, 3, 10, 10));

                // Crear botones de método de pago
                JButton botonEfectivo = new JButton("Efectivo", iconoEfectivo);
                JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
                JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
                JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
                JButton botonDatafono = new JButton("Datafono", iconoDatafono);



                // Añadir botones al panel
                panelPago.add(botonEfectivo);
                panelPago.add(botonBancolombia);
                panelPago.add(botonNequi);
                panelPago.add(botonDaviplata);
                panelPago.add(botonDatafono);

                dialogoPago.add(panelPago, BorderLayout.CENTER);

                // Variable para guardar el tipo de pago seleccionado
                final String[] tipoPagoSeleccionado = {null};
                final double finalTotal = total;

                // Listener para cada botón de pago y cerrar el diálogo al hacer una selección
                botonBancolombia.addActionListener(event -> {
                    tipoPagoSeleccionado[0] = "Bancolombia - Transferencia";
                    dialogoPago.dispose();
                });

                botonNequi.addActionListener(event -> {
                    tipoPagoSeleccionado[0] = "Nequi - Transferencia";
                    dialogoPago.dispose();
                });

                botonDaviplata.addActionListener(event -> {
                    tipoPagoSeleccionado[0] = "Daviplata - Transferencia";
                    dialogoPago.dispose();
                });

                botonEfectivo.addActionListener(event -> {
                    tipoPagoSeleccionado[0] = "Efectivo";
                    String input = JOptionPane.showInputDialog(compraDialog, "Ingrese el dinero recibido:");

                    // Si el usuario presiona "Cancelar" o cierra el diálogo
                    if (input == null) {
                        dialogoPago.dispose();  // Continuar el flujo sin calcular el cambio
                        return;
                    }

                    try {
                        double dineroRecibido = Double.parseDouble(input);
                        if (dineroRecibido < finalTotal) {
                            JOptionPane.showMessageDialog(compraDialog, "El monto recibido es insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        double cambio = dineroRecibido - finalTotal;
                        JOptionPane.showMessageDialog(compraDialog, "Devuelta: $" + FormatterHelpers.formatearMoneda(cambio)+ " Pesos", "Cambio", JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(compraDialog, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    dialogoPago.dispose();  // Cerrar el diálogo después de manejar el monto recibido o si el usuario cancela
                });

                botonDatafono.addActionListener(event -> {
                    tipoPagoSeleccionado[0] = "Datafono";
                    dialogoPago.dispose();
                });

                // Mostrar el diálogo modal y esperar la selección
                dialogoPago.setLocationRelativeTo(compraDialog);
                dialogoPago.setVisible(true);

                // Si no se seleccionó ningún tipo de pago, detener el flujo
                if (tipoPagoSeleccionado[0] == null) {
                    JOptionPane.showMessageDialog(compraDialog, "No se seleccionó un método de pago.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;

                }

                // Guardar la compra en Excel con el tipo de pago seleccionado
                ExcelUserManager excelUserManager = new ExcelUserManager();
                excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime, tipoPagoSeleccionado[0]);



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

                JOptionPane.showMessageDialog(compraDialog, PURCHASE_SUCCEDED + " " + "por un total de: $ " + formatCOP.format(total)+ " Pesos");

                // Actualizar las cantidades en el stock de Excel
                actualizarCantidadStockExcel(productosComprados);




            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Limpiar el carrito antes de iniciar el proceso de compra de la mesa actual
            productoUserManager.limpiarCarrito();
            compraDialog.dispose();
            showMesas();
        });

        return confirmarCompraButton;
    }
    public static void clearProductTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);  // Esto elimina todas las filas de la tabla
    }

    public static JButton createSavePurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, String mesaID, JTable productosTable) {
        JButton saveCompraButton = new JButton("Guardar Compra");

        saveCompraButton.addActionListener(e -> {
            try {
                // Obtener los productos de la tabla
                DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();
                int rowCount = tableModel.getRowCount();

                // Verificar si la tabla está vacía
                if (rowCount == 0) {
                    limpiarMesaEnExcel(mesaID);  // Limpia la mesa en Excel si no hay productos
                    productoUserManager.limpiarCarrito();  // Limpia el carrito de esta mesa
                    JOptionPane.showMessageDialog(null, "La mesa " + mesaID + " ha sido limpiada.");
                    ventaMesaDialog.dispose();
                    showMesas();
                    return; // Salir después de limpiar la mesa
                }

                // Map para almacenar los productos comprados para esta mesa
                Map<String, Integer> productosComprados = new HashMap<>();
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
                    if (producto.getQuantity() < cantidadComprada) {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto: " + nombreProducto, "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Salir si no hay suficiente stock
                    }
                }

                // Obtener el total para esta mesa específica
                double total = productoUserManager.getTotalCartAmount();
                LocalDateTime dateTime = LocalDateTime.now();

                // Guardar la compra en la pestaña "mesas"
                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        boolean mesaEncontrada = false;
                        for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null) {
                                Cell idCell = row.getCell(0);
                                if (idCell != null && idCell.getStringCellValue().trim().equalsIgnoreCase(mesaID.trim())) {
                                    mesaEncontrada = true;

                                    // Cambiar el estado de la mesa a "Ocupada"
                                    Cell estadoCell = row.getCell(1);
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(1);
                                    }
                                    estadoCell.setCellValue("Ocupada");

                                    // Almacenar productos comprados
                                    Cell productosCell = row.getCell(2);
                                    if (productosCell == null) {
                                        productosCell = row.createCell(2);
                                    }

                                    StringBuilder listaProductos = new StringBuilder();
                                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                                        String nombreProducto = entry.getKey();
                                        int cantidadComprada = entry.getValue();
                                        Producto producto = productoUserManager.getProductByName(nombreProducto);
                                        double precioUnitario = producto.getPrice();
                                        double precioTotal = precioUnitario * cantidadComprada;

                                        listaProductos.append(nombreProducto)
                                                .append(" x")
                                                .append(cantidadComprada)
                                                .append(" $")
                                                .append(precioUnitario)
                                                .append(" = ")
                                                .append(precioTotal)
                                                .append("\n");
                                    }

                                    productosCell.setCellValue(listaProductos.toString());

                                    // Poner el total en la columna de Total
                                    Cell totalCell = row.getCell(3);
                                    if (totalCell == null) {
                                        totalCell = row.createCell(3);
                                    }
                                    totalCell.setCellValue(total);

                                    break;
                                }
                            }
                        }

                        if (!mesaEncontrada) {
                            JOptionPane.showMessageDialog(null, "Mesa " + mesaID + " no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Guardar cambios en el archivo Excel
                        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                            workbook.write(fos);
                        }

                        // Mostrar mensaje de confirmación
                        JOptionPane.showMessageDialog(null, "Compra guardada para la mesa: " + mesaID + ".");
                        tableModel.setRowCount(0); // Limpiar la tabla
                        productoUserManager.limpiarCarrito(); // Limpia el carrito de la mesa después de guardar la compra
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

    // Método auxiliar para limpiar la mesa en el archivo Excel cuando no hay productos
    private static void limpiarMesaEnExcel(String mesaID) {
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
                            if (productosCell == null) {
                                productosCell = row.createCell(2);
                            }
                            productosCell.setCellValue("");

                            Cell totalCell = row.getCell(3);
                            if (totalCell == null) {
                                totalCell = row.createCell(3);
                            }
                            totalCell.setCellValue(0.0);

                            break;
                        }
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                    workbook.write(fos);
                }

            } else {
                JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
