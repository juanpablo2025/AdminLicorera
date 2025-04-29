package org.example.ui.uiUser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.manager.userManager.ExcelUserManager;
import org.example.manager.userManager.ProductoUserManager;
import org.example.manager.userManager.VentaMesaUserManager;
import org.example.model.Mesa;
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
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
//import static org.example.manager.userManager.ExcelUserManager.actualizarCantidadStockExcel;
//import static org.example.manager.userManager.ExcelUserManager.cargarProductosMesaDesdeExcel;
import static org.example.manager.userDBManager.DatabaseUserManager.connect;
import static org.example.manager.userManager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.manager.userManager.ProductoUserManager.getProductListWithQuantities;
import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;
import static org.example.utils.Constants.ERROR_TITLE;
import static org.example.utils.FormatterHelpers.ConfiguracionGlobal.TRM;

public class UIUserVenta {
    private static ProductoUserManager productoUserManager = new ProductoUserManager();

    private static JDialog ventaMesaDialog;



    public static void showVentaMesaDialog(List<String[]> productos, String mesaID, JFrame frame) {
        ventaMesaDialog = createDialog("Realizar Venta "+"["+mesaID+"]", 1366, 720, new BorderLayout());
        ventaMesaDialog.setResizable(true);

        // Añadir un WindowListener para detectar el cierre de la ventana
        ventaMesaDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainUser(); // Si hay registro, abrir el panel de usuario
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
        table.setBackground(new Color(250, 240, 230)); // Fondo de la tabla
        table.setSelectionBackground(new Color(173, 216, 255)); // Color de selección
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
        ventaMesaDialog.add(inputPanel, BorderLayout.EAST);


        JPanel buttonPanel = createButtonPanelVentaMesa(table, new VentaMesaUserManager(), ventaMesaDialog, mesaID,frame);

        // Botón independiente para abrir el submenú de productos
        JButton openSubMenuButton = new JButton("Abrir Submenú de Productos");
        openSubMenuButton.setFont(new Font("Arial", Font.BOLD, 18));
        openSubMenuButton.addActionListener(e -> openProductSubMenu(table, productoUserManager.getProducts()));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
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
    public static JPanel createButtonPanelVentaMesa(JTable table, VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID, JFrame frame) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));


        // Crear el botón de guardar compra y asignar el ID de la mesa y la tabla
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaUserManager, mesaID, table,frame);
        guardarCompra.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18)); // Configuración de fuente
        buttonPanel.add(guardarCompra);


        return buttonPanel;
    }


    public static JButton createConfirmPurchaseMesaButtons(VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);

        confirmarCompraButton.setFont(new Font("Arial", Font.BOLD, 18));
        confirmarCompraButton.setForeground(Color.WHITE);
        confirmarCompraButton.setBackground(new Color(0, 204, 136));
        confirmarCompraButton.setOpaque(true);
        confirmarCompraButton.setBorderPainted(false);
        confirmarCompraButton.setFocusPainted(false);
        confirmarCompraButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmarCompraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));



        // Efecto hover: cambiar ligeramente el color al pasar el mouse
        confirmarCompraButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confirmarCompraButton.setBackground(new Color(0, 201, 87));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                confirmarCompraButton.setBackground(new Color(0, 170, 115));
            }
        });
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
              //  List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
                List<String[]> productosPrevios = DatabaseUserManager.cargarProductosMesaDesdeBD(mesaID);
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

                  /*  if (producto == null || producto.getQuantity() < cantidadAdicional) {
                        JOptionPane.showMessageDialog(compraDialog, "No hay suficiente stock para " + nombreProducto, "Error de stock", JOptionPane.ERROR_MESSAGE);
                        compraDialog.dispose();
                        return;
                    }*/

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
                panelPago.setLayout(new GridLayout(2, 2, 10, 10));

                // Crear botones de método de pago
                JButton botonEfectivo = new JButton("Efectivo", iconoEfectivo);
                JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
                JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
                JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
                JButton botonDatafono = new JButton("Datafono", iconoDatafono);





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
                //ExcelUserManager excelUserManager = new ExcelUserManager();
                //excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime, tipoPagoSeleccionado[0]);
                DatabaseUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime, tipoPagoSeleccionado[0]);

                //TODO: Guardar en BD
               /* try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
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
                }*/


                try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL)) {

                    // Actualizar estado de la mesa, productos y total en la base de datos
                    String sqlUpdateMesa = "UPDATE Mesas SET estado = ?, productos = ?, total = ? WHERE mesaID = ?";

                    try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdateMesa)) {

                        // Establecer valores para la actualización
                        pstmt.setString(1, "Libre");  // Cambiar el estado de la mesa a "Libre"
                        pstmt.setString(2, "");       // Limpiar productos
                        pstmt.setDouble(3, 0.0);      // Establecer total a 0.0
                        pstmt.setString(4, mesaID);   // Usar el ID de la mesa para la condición WHERE

                        // Ejecutar la actualización
                        int rowsUpdated = pstmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Mesa " + mesaID + " actualizada correctamente.");
                        } else {
                            System.out.println("No se encontró la mesa con ID " + mesaID);
                        }

                    } catch (SQLException es) {
                        es.printStackTrace();
                    }

                } catch (SQLException es) {
                    es.printStackTrace();
                }

                // Mostramos el dialogo de confirmación
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                if (respuesta == JOptionPane.YES_OPTION) {
                    // Corregimos aquí, enviamos la lista completa y no solo un String.
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime, tipoPagoSeleccionado[0]);
                }

                JOptionPane.showMessageDialog(compraDialog, PURCHASE_SUCCEDED + " " + "por un total de: $ " + formatCOP.format(total)+ " Pesos");

                // Actualizar las cantidades en el stock de Excel
               // actualizarCantidadStockExcel(productosComprados, mesaID);
                DatabaseUserManager.actualizarCantidadStockExcel(productosComprados, mesaID);




            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
            // Limpiar el carrito antes de iniciar el proceso de compra de la mesa actual
            productoUserManager.limpiarCarrito();
            compraDialog.dispose();
            mainUser();
        });

        return confirmarCompraButton;
    }

    public static List<String[]> cargarProductosPorMesa(String mesaID) {
        List<String[]> productos = new ArrayList<>();

        String query = """
        SELECT p.nombre, v.cantidad, p.precio
        FROM ventas v
        JOIN productos p ON v.productoID = p.productoID
        WHERE v.mesaID = ?
    """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, mesaID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                double precio = rs.getDouble("precio");

                productos.add(new String[]{
                        nombre,
                        "$" + cantidad,
                        "$" + precio
                });
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar productos de la mesa: " + e.getMessage());
        }

        return productos;
    }


    private static Map<String, Integer> actualizarProductosDesdeTabla(JTable productosTable) {
        Map<String, Integer> productosActualizados = new HashMap<>();
        DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nombreProducto = (String) tableModel.getValueAt(i, 0);
            int cantidad = (int) tableModel.getValueAt(i, 1);
            productosActualizados.put(nombreProducto, cantidad);
        }

        return productosActualizados;
    }
    public static JButton createConfirmPurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID,JTable productosTable,JFrame frame) {



        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);

        confirmarCompraButton.setFont(new Font("Arial", Font.BOLD, 22));
        confirmarCompraButton.setForeground(Color.WHITE);
        confirmarCompraButton.setBackground(new Color(168, 230, 207));
        confirmarCompraButton.setBorderPainted(false);
        confirmarCompraButton.setFocusPainted(false);
        confirmarCompraButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmarCompraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        // Efecto hover: cambiar color al pasar el mouse
        confirmarCompraButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmarCompraButton.setBackground(new Color(0, 201, 87)); // Cambia a color más oscuro
            }

            @Override
            public void mouseExited(MouseEvent e) {
                confirmarCompraButton.setBackground(new Color(168, 230, 207)); // Vuelve al color original
            }
        });

        confirmarCompraButton.addActionListener(e -> {
            try {

                // 🔄 Obtener los productos actualizados desde la tabla antes de confirmar la compra
                Map<String, Integer> productosComprados = actualizarProductosDesdeTabla(productosTable);
                
                double total = 0;
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000) + " " + mesaID;
                LocalDateTime dateTime = LocalDateTime.now();
                StringBuilder listaProductosEnLinea = new StringBuilder();

                // Inicializar mapas vacíos para la venta actual
                Map<String, Integer> cantidadTotalPorProducto = new HashMap<>();
                Map<String, Double> precioUnitarioPorProducto = new HashMap<>();



                // Procesar productos de la tabla actualizada
                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombre = entrada.getKey();
                    int cantidad = entrada.getValue();
                    Producto producto = productoUserManager.getProductByName(nombre);

                    cantidadTotalPorProducto.put(nombre, cantidad);
                    precioUnitarioPorProducto.put(nombre, producto.getPrice());
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



                // Selección de método de pago

                // Crear íconos redimensionados para los métodos de pago
                ImageIcon iconoBancolombia = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/bancolombia.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoNequi = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/nequi.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoEfectivo = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/dinero.png")).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                ImageIcon iconoDaviplata = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/Daviplata.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
                ImageIcon iconoDatafono = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/datafono.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
                ImageIcon iconoPaypal = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/Paypal.png")).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));

                JDialog dialogoPago = new JDialog(compraDialog, "Seleccione el método de pago", true);
                dialogoPago.setSize(1300, 400);
                dialogoPago.setLayout(new BorderLayout(20, 20));
                dialogoPago.setResizable(false);
                Double totalDolar = total/TRM;
                String textoTotal = String.format(
                        "<html><div style='text-align:center; font-size:28px; color:#2ecc71;'>" +
                                "<b>Total: $%,.0f Pesos</b><br>" +
                                "<span style='font-size:20pt; color:#000080;'><b>%.2f USD</b></span>" +
                                "</div></html>",
                        total, totalDolar
                );
                JLabel totalLabel = new JLabel(textoTotal, SwingConstants.CENTER);
                totalLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));

                // Crear panel para el menú de pago
                JPanel panelPago = new JPanel();
                panelPago.setLayout(new GridLayout(2, 3, 20, 20));
                panelPago.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
                panelPago.setBackground(Color.WHITE);

                // Crear botones de método de pago
                JButton botonEfectivo = new JButton("Efectivo", iconoEfectivo);
                JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
                JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
                JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
                JButton botonDatafono = new JButton("Datafono", iconoDatafono);
                JButton botonPaypal = new JButton("Paypal", iconoPaypal);

                // Ajuste visual común para los botones
                JButton[] botones = { botonEfectivo, botonBancolombia, botonNequi, botonDaviplata, botonDatafono, botonPaypal };
                for (JButton btn : botones) {
                    btn.setFont(new Font("Arial", Font.BOLD, 16));
                    btn.setBackground(new Color(245, 245, 245));
                    btn.setFocusPainted(false);
                    btn.setHorizontalAlignment(SwingConstants.LEFT);
                    btn.setIconTextGap(10);
                    panelPago.add(btn);
                }

                // Añadir todo al diálogo
                dialogoPago.add(totalLabel, BorderLayout.NORTH);
                dialogoPago.add(panelPago, BorderLayout.CENTER);
                dialogoPago.setLocationRelativeTo(null);
                final String[] tipoPagoSeleccionado = {null};
                final double finalTotal = total;

                botonBancolombia.addActionListener(event -> {

                    // Imagen QR
                    ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/procesoQR.png"))
                            .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
                    JLabel qrLabel = new JLabel(qrIcon);
                    qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Texto de valor total
                    JLabel montoLabel = new JLabel("Total: " + FormatterHelpers.formatearMoneda(finalTotal) + " Pesos");
                    montoLabel.setFont(new Font("Arial", Font.BOLD, 22));
                    montoLabel.setForeground(new Color(0, 153, 0));
                    montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Instrucción
                    JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                            + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                            + "</div></html>");
                    instruccion.setFont(new Font("Arial", Font.PLAIN, 18));
                    instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Botón continuar
                    JButton continuarBtn = new JButton("Continuar");
                    continuarBtn.setBackground(new Color(0, 153, 0));
                    continuarBtn.setForeground(Color.WHITE);
                    continuarBtn.setFont(new Font("Arial", Font.BOLD, 18));
                    continuarBtn.setFocusPainted(false);
                    continuarBtn.setPreferredSize(new Dimension(200, 50));
                    continuarBtn.setMaximumSize(new Dimension(200, 50)); // para que no se estire
                    continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Crear el diálogo antes de usarlo en el listener
                    JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago QR", true);
                    dialogoQR.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    // Acción del botón
                    continuarBtn.addActionListener(es -> {
                        dialogoQR.dispose(); // ahora sí cierra correctamente
                        tipoPagoSeleccionado[0] = "Bancolombia - Transferencia";
                        dialogoPago.dispose();
                        // Puedes agregar aquí cualquier otra lógica posterior
                    });

                    // Panel de contenido
                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                    content.setBackground(Color.WHITE);

                    content.add(montoLabel);
                    content.add(Box.createVerticalStrut(15));
                    content.add(qrLabel);
                    content.add(Box.createVerticalStrut(20));
                    content.add(instruccion);
                    content.add(Box.createVerticalStrut(25));
                    content.add(continuarBtn);

                    dialogoQR.getContentPane().add(content);
                    dialogoQR.pack();
                    dialogoQR.setResizable(false);
                    dialogoQR.setLocationRelativeTo(compraDialog);
                    dialogoQR.setVisible(true);
                });

                botonNequi.addActionListener(event -> {

                    // Imagen QR
                    ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/procesoQR.png"))
                            .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
                    JLabel qrLabel = new JLabel(qrIcon);
                    qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Texto de valor total
                    JLabel montoLabel = new JLabel("Total: " + FormatterHelpers.formatearMoneda(finalTotal) + " Pesos");
                    montoLabel.setFont(new Font("Arial", Font.BOLD, 22));
                    montoLabel.setForeground(new Color(0, 153, 0));
                    montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Instrucción
                    JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                            + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                            + "</div></html>");
                    instruccion.setFont(new Font("Arial", Font.PLAIN, 18));
                    instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Botón continuar
                    JButton continuarBtn = new JButton("Continuar");
                    continuarBtn.setBackground(new Color(0, 153, 0));
                    continuarBtn.setForeground(Color.WHITE);
                    continuarBtn.setFont(new Font("Arial", Font.BOLD, 18));
                    continuarBtn.setFocusPainted(false);
                    continuarBtn.setPreferredSize(new Dimension(200, 50));
                    continuarBtn.setMaximumSize(new Dimension(200, 50)); // para que no se estire
                    continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Crear el diálogo antes de usarlo en el listener
                    JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago QR", true);
                    dialogoQR.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    // Acción del botón
                    continuarBtn.addActionListener(es -> {
                        dialogoQR.dispose(); // ahora sí cierra correctamente
                        tipoPagoSeleccionado[0] = "Nequi - Transferencia";
                        dialogoPago.dispose();
                        // Puedes agregar aquí cualquier otra lógica posterior
                    });

                    // Panel de contenido
                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                    content.setBackground(Color.WHITE);

                    content.add(montoLabel);
                    content.add(Box.createVerticalStrut(15));
                    content.add(qrLabel);
                    content.add(Box.createVerticalStrut(20));
                    content.add(instruccion);
                    content.add(Box.createVerticalStrut(25));
                    content.add(continuarBtn);

                    dialogoQR.getContentPane().add(content);
                    dialogoQR.pack();
                    dialogoQR.setResizable(false);
                    dialogoQR.setLocationRelativeTo(compraDialog);
                    dialogoQR.setVisible(true);
                });

                botonDaviplata.addActionListener(event -> {

                    // Imagen QR
                    ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/procesoQR.png"))
                            .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
                    JLabel qrLabel = new JLabel(qrIcon);
                    qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Texto de valor total
                    JLabel montoLabel = new JLabel("Total: " + FormatterHelpers.formatearMoneda(finalTotal) + " Pesos");
                    montoLabel.setFont(new Font("Arial", Font.BOLD, 22));
                    montoLabel.setForeground(new Color(0, 153, 0));
                    montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Instrucción
                    JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                            + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                            + "</div></html>");
                    instruccion.setFont(new Font("Arial", Font.PLAIN, 18));
                    instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Botón continuar
                    JButton continuarBtn = new JButton("Continuar");
                    continuarBtn.setBackground(new Color(0, 153, 0));
                    continuarBtn.setForeground(Color.WHITE);
                    continuarBtn.setFont(new Font("Arial", Font.BOLD, 18));
                    continuarBtn.setFocusPainted(false);
                    continuarBtn.setPreferredSize(new Dimension(200, 50));
                    continuarBtn.setMaximumSize(new Dimension(200, 50)); // para que no se estire
                    continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Crear el diálogo antes de usarlo en el listener
                    JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago QR", true);
                    dialogoQR.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    // Acción del botón
                    continuarBtn.addActionListener(es -> {
                        dialogoQR.dispose(); // ahora sí cierra correctamente
                        tipoPagoSeleccionado[0] = "Daviplata - Transferencia";
                        dialogoPago.dispose();
                    });

                    // Panel de contenido
                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                    content.setBackground(Color.WHITE);

                    content.add(montoLabel);
                    content.add(Box.createVerticalStrut(15));
                    content.add(qrLabel);
                    content.add(Box.createVerticalStrut(20));
                    content.add(instruccion);
                    content.add(Box.createVerticalStrut(25));
                    content.add(continuarBtn);

                    dialogoQR.getContentPane().add(content);
                    dialogoQR.pack();
                    dialogoQR.setResizable(false);
                    dialogoQR.setLocationRelativeTo(compraDialog);
                    dialogoQR.setVisible(true);
                });

                botonPaypal.addActionListener(event -> {

                    // Imagen QR
                    ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/procesoQR.png"))
                            .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
                    JLabel qrLabel = new JLabel(qrIcon);
                    qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Texto de valor total
                    JLabel montoLabel = new JLabel("Total: " + FormatterHelpers.formatearMoneda(finalTotal) + " Pesos");
                    montoLabel.setFont(new Font("Arial", Font.BOLD, 22));
                    montoLabel.setForeground(new Color(0, 153, 0));
                    montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Instrucción
                    JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                            + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                            + "</div></html>");
                    instruccion.setFont(new Font("Arial", Font.PLAIN, 18));
                    instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Botón continuar
                    JButton continuarBtn = new JButton("Continuar");
                    continuarBtn.setBackground(new Color(0, 153, 0));
                    continuarBtn.setForeground(Color.WHITE);
                    continuarBtn.setFont(new Font("Arial", Font.BOLD, 18));
                    continuarBtn.setFocusPainted(false);
                    continuarBtn.setPreferredSize(new Dimension(200, 50));
                    continuarBtn.setMaximumSize(new Dimension(200, 50)); // para que no se estire
                    continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // Crear el diálogo antes de usarlo en el listener
                    JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago QR", true);
                    dialogoQR.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    // Acción del botón
                    continuarBtn.addActionListener(es -> {
                        dialogoQR.dispose(); // ahora sí cierra correctamente
                        tipoPagoSeleccionado[0] = "Paypal - Transferencia";
                        dialogoPago.dispose();
                        // Puedes agregar aquí cualquier otra lógica posterior
                    });

                    // Panel de contenido
                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                    content.setBackground(Color.WHITE);

                    content.add(montoLabel);
                    content.add(Box.createVerticalStrut(15));
                    content.add(qrLabel);
                    content.add(Box.createVerticalStrut(20));
                    content.add(instruccion);
                    content.add(Box.createVerticalStrut(25));
                    content.add(continuarBtn);

                    dialogoQR.getContentPane().add(content);
                    dialogoQR.pack();
                    dialogoQR.setResizable(false);
                    dialogoQR.setLocationRelativeTo(compraDialog);
                    dialogoQR.setVisible(true);
                });

                botonEfectivo.addActionListener(event -> {

                    JTextField inputField = new JTextField(12);
                    inputField.setFont(new Font("Arial", Font.PLAIN, 18));

                    JLabel title = new JLabel("Ingrese el dinero recibido:");
                    title.setFont(new Font("Arial", Font.BOLD, 20));
                    title.setHorizontalAlignment(SwingConstants.CENTER);

                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                    content.setBackground(Color.WHITE);

                    content.add(title);
                    content.add(Box.createVerticalStrut(15));
                    content.add(inputField);
                    content.add(Box.createVerticalStrut(25));

                    JButton continuarBtn = new JButton("Continuar");
                    continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                    continuarBtn.setBackground(new Color(0, 153, 0));
                    continuarBtn.setForeground(Color.WHITE);
                    continuarBtn.setFont(new Font("Arial", Font.BOLD, 16));
                    continuarBtn.setFocusPainted(false);
                    continuarBtn.setPreferredSize(new Dimension(150, 40));

                    // Container para botón con padding
                    JPanel btnPanel = new JPanel();
                    btnPanel.setBackground(Color.WHITE);
                    btnPanel.add(continuarBtn);

                    JDialog dialog = new JDialog(compraDialog, "Calcula la Devuelta", true);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.getContentPane().add(content, BorderLayout.CENTER);
                    dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
                    dialog.pack();
                    dialog.setLocationRelativeTo(compraDialog);
                    dialog.setResizable(false);

                    // Acción del botón
                    continuarBtn.addActionListener(es -> {
                        String input = inputField.getText().trim();

                        if (input.isEmpty()) {
                            dialog.dispose();
                            tipoPagoSeleccionado[0] = "Efectivo";
                            dialogoPago.dispose();
                            return;
                        }

                        try {
                            double dineroRecibido = Double.parseDouble(input);
                            if (dineroRecibido < finalTotal) {
                                JOptionPane.showMessageDialog(compraDialog,
                                        "El monto recibido es insuficiente.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                return;
                            }

                            double cambio = dineroRecibido - finalTotal;
                            String cambioFormateado = FormatterHelpers.formatearMoneda(cambio);
                            JLabel label = new JLabel("<html><div style='text-align: center;'>"
                                    + "<span style='font-size:16pt;font-weight:bold;'>Cambio devuelto:</span><br><br>"
                                    + "<span style='font-size:20pt;color:green;'>$" + cambioFormateado + " Pesos</span>"
                                    + "</div></html>");
                            label.setHorizontalAlignment(SwingConstants.CENTER);

                            JOptionPane.showMessageDialog(compraDialog, label, "Devuelta", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            tipoPagoSeleccionado[0] = "Efectivo";
                            dialogoPago.dispose();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(compraDialog,
                                    "Monto inválido.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                    // Si el usuario cierra la ventana con la X, continúa flujo
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            dialog.dispose();
                        }
                    });
                    dialog.setVisible(true);
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

                // Guardar la compra en Excel
                ExcelUserManager excelUserManager = new ExcelUserManager();
                //excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime ,tipoPagoSeleccionado[0]);

                DatabaseUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime ,tipoPagoSeleccionado[0]);
                /*try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
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
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }*/
                // Actualizar estado de la mesa, productos y total en la base de datos
                String sqlUpdateMesa = "UPDATE Mesas SET estado = ?, productos = ?, total = ? WHERE mesaID = ?";

                try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
                     PreparedStatement pstmt = connection.prepareStatement(sqlUpdateMesa)) {

                    // Establecer los valores a actualizar
                    pstmt.setString(1, "Libre");  // Cambiar estado de la mesa a "Libre"
                    pstmt.setString(2, "");       // Limpiar la lista de productos
                    pstmt.setDouble(3, 0.0);      // Establecer el total de la mesa a 0
                    pstmt.setString(4, mesaID);   // ID de la mesa a actualizar

                    // Ejecutar la actualización
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Mesa " + mesaID + " actualizada correctamente.");
                    } else {
                        System.out.println("No se encontró la mesa con ID " + mesaID);
                    }

                } catch (SQLException es) {
                    es.printStackTrace();
                }
                // Mostramos el dialogo de confirmación
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                if (respuesta == JOptionPane.YES_OPTION) {
                    // Corregimos aquí, enviamos la lista completa y no solo un String.
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime, tipoPagoSeleccionado[0]);
                }

                NumberFormat FORMAT_USD = NumberFormat.getCurrencyInstance(Locale.US);

                String mensaje = String.format(
                        "<html>" +
                                "<div style='font-size:16pt;'>" +
                                "✔ <b>%s</b><br><br>" +
                                "Por un total de:<br>" +
                                "<span style='font-size:28pt; color:#2ecc71;'><b>$%s Pesos</b></span><br>" +
                                "<span style='font-size:20pt; color:#000080;'><b>%s USD</b></span>" +
                                "</div></html>",
                        PURCHASE_SUCCEDED,
                        NumberFormat.getInstance(new Locale("es", "CO")).format(total),
                        FORMAT_USD.format(totalDolar)
                );

                JOptionPane.showMessageDialog(
                        compraDialog,
                        mensaje,
                        "Compra Exitosa",
                        JOptionPane.INFORMATION_MESSAGE
                );
                //actualizarCantidadStockExcel(cantidadTotalPorProducto, mesaID);
                DatabaseUserManager.actualizarCantidadStockBD(cantidadTotalPorProducto, mesaID);

                productoUserManager.limpiarCarrito();
                // Cerrar la ventana actual (si es un JDialog)
                SwingUtilities.invokeLater(() -> {
                    Window window = SwingUtilities.getWindowAncestor(confirmarCompraButton);
                    if (window != null) {
                        window.setVisible(false); // Ocultar antes de cerrar
                        window.dispose();
                    }
                    mainUser();
                });



            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return confirmarCompraButton;
    }


    public static JButton createSavePurchaseMesaButton(VentaMesaUserManager ventaMesaUserManager, String mesaID, JTable productosTable,JFrame frame) {
        JButton saveCompraButton = new JButton("Guardar Compra");


        saveCompraButton.setFont(new Font("Arial", Font.BOLD, 22));
        saveCompraButton.setForeground(Color.WHITE);
        saveCompraButton.setBackground(new Color(255, 111, 97));
        saveCompraButton.setOpaque(true);
        saveCompraButton.setBorderPainted(false);
        saveCompraButton.setFocusPainted(false);
        saveCompraButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveCompraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));



        // Efecto hover: cambiar ligeramente el color al pasar el mouse
        saveCompraButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveCompraButton.setBackground(new Color(201, 41, 41));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveCompraButton.setBackground(new Color(255, 111, 97));
            }
        });

        saveCompraButton.addActionListener(e -> {
            try {
                // Obtener los productos de la tabla
                DefaultTableModel tableModel = (DefaultTableModel) productosTable.getModel();
                int rowCount = tableModel.getRowCount();

                // Verificar si la tabla está vacía
                if (rowCount == 0) {
                    limpiarMesaEnBaseDeDatos(mesaID);  // Limpia la mesa en Excel si no hay productos
                    productoUserManager.limpiarCarrito();  // Limpia el carrito de esta mesa
                    JOptionPane.showMessageDialog(null, "La mesa " + mesaID + " ha sido limpiada.");
                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                        if (window != null) {
                            window.dispose();
                        }
                        mainUser();
                    });

                    return; // Salir después de limpiar la mesa
                }

                // Map para almacenar los productos comprados para esta mesa
                Map<String, Integer> productosComprados = new HashMap<>();
                for (int i = 0; i < rowCount; i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, 0); // Columna 0: nombre del producto
                    int cantidad = (int) tableModel.getValueAt(i, 1); // Columna 1: cantidad del producto
                    productosComprados.put(nombreProducto, cantidad);
                }

                // Obtener el total para esta mesa específica
                double total = productoUserManager.getTotalCartAmount();
                LocalDateTime dateTime = LocalDateTime.now();

                // Guardar la compra en la pestaña "mesas"
               /* try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
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
                        productoUserManager.limpiarCarrito();// Limpia el carrito de la mesa después de guardar la compra

                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                            if (window != null) {
                                window.dispose();
                            }
                            mainUser();
                        });

                    } else {
                        JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });*/
                // Conexión con la base de datos
                String sqlUpdateMesa = "UPDATE Mesas SET estado = ?, productos = ?, total = ? WHERE mesaID = ?";

                try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
                     PreparedStatement pstmt = connection.prepareStatement(sqlUpdateMesa)) {

                    // Cambiar el estado de la mesa a "Ocupada"
                    pstmt.setString(1, "Ocupada");

                    // Almacenar los productos comprados como una cadena de texto
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
                    pstmt.setString(2, listaProductos.toString());  // Productos comprados

                    // Poner el total en la columna de Total
                    pstmt.setDouble(3, total);

                    // Establecer el ID de la mesa
                    pstmt.setString(4, mesaID);

                    // Ejecutar la actualización
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Compra guardada correctamente para la mesa: " + mesaID);
                    } else {
                        JOptionPane.showMessageDialog(null, "Mesa " + mesaID + " no encontrada en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Mostrar mensaje de confirmación
                    JOptionPane.showMessageDialog(null, "Compra guardada para la mesa: " + mesaID + ".");

                    // Limpiar la tabla y el carrito de la mesa
                    tableModel.setRowCount(0);
                    productoUserManager.limpiarCarrito();

                    // Cerrar la ventana actual y volver al menú principal
                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                        if (window != null) {
                            window.dispose();
                        }
                        mainUser();
                    });

                } catch (SQLException es) {
                    es.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al guardar la compra en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }


            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return saveCompraButton;
    }

   /* // Método auxiliar para limpiar la mesa en el archivo Excel cuando no hay productos
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
    }*/

    // Método auxiliar para limpiar la mesa en la base de datos cuando no hay productos
    private static void limpiarMesaEnBaseDeDatos(String mesaID) {
        // Conexión con la base de datos
        String sqlUpdateMesa = "UPDATE Mesas SET estado = ?, productos = ?, total = ? WHERE mesaID = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseUserManager.URL);
             PreparedStatement pstmt = connection.prepareStatement(sqlUpdateMesa)) {

            // Establecer los valores a actualizar
            pstmt.setString(1, "Libre");   // Cambiar el estado de la mesa a "Libre"
            pstmt.setString(2, "");        // Limpiar productos (cadena vacía)
            pstmt.setDouble(3, 0.0);       // Establecer total a 0
            pstmt.setString(4, mesaID);    // Usar el ID de la mesa para la condición WHERE

            // Ejecutar la actualización
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Mesa " + mesaID + " actualizada correctamente.");
            } else {
                System.out.println("No se encontró la mesa con ID " + mesaID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
