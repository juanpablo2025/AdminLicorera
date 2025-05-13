package org.example.ui.uiuser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.adminmanager.ConfigAdminManager;
import org.example.manager.usermanager.ExcelUserManager;
import org.example.manager.usermanager.ProductoUserManager;
import org.example.model.Producto;
import org.example.utils.FormatterHelpers;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

import static org.example.manager.usermanager.ExcelUserManager.actualizarCantidadStockExcel;
import static org.example.manager.usermanager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.ui.uiuser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;

import static org.example.utils.FormatterHelpers.ConfiguracionGlobal.TRM;

public class UIUserVenta {

    private UIUserVenta() {}

    private static ProductoUserManager productoUserManager = new ProductoUserManager();

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

    public static JButton createConfirmPurchaseMesaButton(JDialog compraDialog, String mesaID,JTable productosTable,JFrame frame) {

        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);

        confirmarCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
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
                    Producto producto = ProductoUserManager.getProductByName(nombre);

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

                String tipoPago = paySelection(compraDialog, total,frame);
                if (tipoPago == null) {
                    return; // el usuario canceló
                }

                // Guardar la compra en Excel
                ExcelUserManager excelUserManager = new ExcelUserManager();
                excelUserManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total ,tipoPago);
                try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet(MESAS);
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
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Mostramos el dialogo de confirmación
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    // Corregimos aquí, enviamos la lista completa y no solo un String.
                    generarFacturadeCompra(ventaID, Arrays.asList(listaProductosEnLinea.toString().split("\n")), total, dateTime, tipoPago);
                }

                NumberFormat formatusd = NumberFormat.getCurrencyInstance(Locale.US);
                Double totalDolar = total/TRM;
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
                        formatusd.format(totalDolar)
                );

                JOptionPane.showMessageDialog(
                        compraDialog,
                        mensaje,
                        "Compra Exitosa",
                        JOptionPane.INFORMATION_MESSAGE
                );
                actualizarCantidadStockExcel(cantidadTotalPorProducto);
                ProductoUserManager.limpiarCarrito();
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
                JOptionPane.showMessageDialog(compraDialog, "Monto inválido.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return confirmarCompraButton;
    }

    public static JButton createSavePurchaseMesaButton( String mesaID, JTable productosTable) {
        JButton saveCompraButton = new JButton("Guardar Compra");


        saveCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveCompraButton.setBackground(new Color(201, 41, 41));
            }
            @Override
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
                    limpiarMesaEnExcel(mesaID);  // Limpia la mesa en Excel si no hay productos
                    ProductoUserManager.limpiarCarrito();  // Limpia el carrito de esta mesa
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
                                        Producto producto = ProductoUserManager.getProductByName(nombreProducto);
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
                            JOptionPane.showMessageDialog(null, "Mesa " + mesaID + " no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                            return;
                        }


                        // Guardar los cambios en el archivo Excel
                        guardarCambiosWorkbook(workbook);

                        // Mostrar mensaje de confirmación
                        JOptionPane.showMessageDialog(null, "Compra guardada para la mesa: " + mesaID + ".");
                        tableModel.setRowCount(0); // Limpiar la tabla
                        ProductoUserManager.limpiarCarrito();// Limpia el carrito de la mesa después de guardar la compra

                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(saveCompraButton);
                            if (window != null) {
                                window.dispose();
                            }
                            mainUser();
                        });

                    } else {
                        JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        });

        return saveCompraButton;
    }

    private static void guardarCambiosWorkbook(Workbook workbook) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
            workbook.write(fos);
        }
    }

    public static JSONObject mostrarDialogoFactura(JFrame parentFrame) {
        JTextField nombreField = new JTextField(20);
        JTextField apellidoField = new JTextField(20);
        JTextField identificacionField = new JTextField(15);
        JTextField direccionField = new JTextField(30);
        JTextField ciudadField = new JTextField("11001"); // Bogotá
        JTextField estadoField = new JTextField("11");    // Cundinamarca
        JTextField paisField = new JTextField("CO");
        JTextField telefonoField = new JTextField("3001234567");
        JTextField correoField = new JTextField(25);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Apellido:"));
        panel.add(apellidoField);
        panel.add(new JLabel("Identificación:"));
        panel.add(identificacionField);
        panel.add(new JLabel("Dirección:"));
        panel.add(direccionField);
        panel.add(new JLabel("Ciudad (Código):"));
        panel.add(ciudadField);
        panel.add(new JLabel("Departamento (Código):"));
        panel.add(estadoField);
        panel.add(new JLabel("País (Código):"));
        panel.add(paisField);
        panel.add(new JLabel("Teléfono:"));
        panel.add(telefonoField);
        panel.add(new JLabel("Correo electrónico:"));
        panel.add(correoField);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Datos de Facturación Electrónica",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            JSONObject customer = new JSONObject()
                    .put("person_type", "Person")
                    .put("id_type", "13")
                    .put("identification", identificacionField.getText().trim())
                    .put("branch_office", 0)
                    .put("name", new JSONArray().put(nombreField.getText().trim()).put(apellidoField.getText().trim()))
                    .put("address", new JSONObject()
                            .put("address", direccionField.getText().trim())
                            .put("city", new JSONObject()
                                    .put("country_code", paisField.getText().trim())
                                    .put("state_code", estadoField.getText().trim())
                                    .put("city_code", ciudadField.getText().trim())
                            )
                    )
                    .put("phones", new JSONArray()
                            .put(new JSONObject().put("number", telefonoField.getText().trim()))
                    )
                    .put("contacts", new JSONArray()
                            .put(new JSONObject()
                                    .put("first_name", nombreField.getText().trim())
                                    .put("last_name", apellidoField.getText().trim())
                                    .put("email", correoField.getText().trim())
                            )
                    );
            return customer;
        } else {
            return null;
        }
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
                JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String paySelection(JDialog compraDialog, double total,JFrame frame) {
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
        JLabel totalLabel = getJLabel(total, totalDolar);

        // Crear panel para el menú de pago
        JPanel panelPago = new JPanel();
        panelPago.setLayout(new GridLayout(2, 3, 20, 20));
        panelPago.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panelPago.setBackground(Color.WHITE);

        // Crear botones de método de pago
        JButton botonEfectivo = new JButton(EFECTIVO, iconoEfectivo);
        JButton botonBancolombia = new JButton("Bancolombia - Transferencia", iconoBancolombia);
        JButton botonNequi = new JButton("Nequi - Transferencia", iconoNequi);
        JButton botonDaviplata = new JButton("Daviplata - Transferencia", iconoDaviplata);
        JButton botonDatafono = new JButton("Datafono", iconoDatafono);
        JButton botonPaypal = new JButton("Paypal", iconoPaypal);

        // Ajuste visual común para los botones
        JButton[] botones = { botonEfectivo, botonBancolombia, botonNequi, botonDaviplata, botonDatafono, botonPaypal };
        for (JButton btn : botones) {
            btn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            btn.setBackground(new Color(245, 245, 245));
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(10);
            panelPago.add(btn);
        }

        dialogoPago.add(totalLabel, BorderLayout.NORTH);
        dialogoPago.add(panelPago, BorderLayout.CENTER);
        dialogoPago.setLocationRelativeTo(null);
        final String[] tipoPagoSeleccionado = {null};
        final double finalTotal = total;

        configurarBotonConQR(dialogoPago, botonBancolombia, finalTotal, totalDolar, () -> {
            tipoPagoSeleccionado[0] = "Bancolombia - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonNequi, finalTotal, totalDolar, () -> {
            tipoPagoSeleccionado[0] = "Nequi - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonDaviplata, finalTotal, totalDolar, () -> {
            tipoPagoSeleccionado[0] = "Daviplata - Transferencia";
            dialogoPago.dispose();
        });

        configurarBotonConQR(dialogoPago, botonPaypal, finalTotal, totalDolar, () -> {
                tipoPagoSeleccionado[0] = "Paypal - Transferencia";
                dialogoPago.dispose();
        });

        botonEfectivo.addActionListener(event -> {

            JTextField inputField = new JTextField(12);
            inputField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));

            JLabel title = new JLabel("Ingrese el dinero recibido:");
            title.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
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
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(150, 40));


            JButton omitirBtn = new JButton("Omitir");
            omitirBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            omitirBtn.setBackground(Color.red);
            omitirBtn.setForeground(Color.WHITE);
            omitirBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            omitirBtn.setFocusPainted(false);
            omitirBtn.setPreferredSize(new Dimension(150, 40));

            // Container para botón con padding
            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(Color.WHITE);
            btnPanel.add(continuarBtn);
            btnPanel.add(omitirBtn);

            JDialog dialog = new JDialog(compraDialog, "Calcula la Devuelta", true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(content, BorderLayout.CENTER);
            dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(compraDialog);
            dialog.setResizable(false);
            // Acción del botón
            continuarBtn.addActionListener(es -> {
                String input = inputField.getText().trim();
                dialog.dispose();
                if (input.isEmpty()) {
                    tipoPagoSeleccionado[0] = EFECTIVO;
                    dialogoPago.dispose();
                    return;
                }

                try {
                    double dineroRecibido = Double.parseDouble(input);
                    if (dineroRecibido < finalTotal) {
                        JOptionPane.showMessageDialog(compraDialog,
                                "El monto recibido es insuficiente.",
                                ERROR_TITLE,
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    double cambio = dineroRecibido - finalTotal;
                    String cambioFormateado = FormatterHelpers.formatearMoneda(cambio);

                    JLabel label = new JLabel("<html><div style='text-align: center;'>"
                            + "<span style='font-size:16pt;font-weight:bold;'>¿Desea calcular el cambio?</span><br><br>"
                            + "<span style='font-size:20pt;color:green;'>$" + cambioFormateado + " Pesos</span>"
                            + "</div></html>");
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    // Opciones de botones
                    Object[] options = {"Calcular cambio", "Omitir"};

                    int option = JOptionPane.showOptionDialog(
                            compraDialog,
                            label,
                            "Devuelta",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        // El usuario eligió "Calcular cambio"
                        JOptionPane.showMessageDialog(compraDialog, label, "Cambio devuelto", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // En ambos casos se cierra el diálogo
                    dialog.dispose();
                    tipoPagoSeleccionado[0] = EFECTIVO;
                    dialogoPago.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(compraDialog,
                            "Monto inválido.",
                            ERROR_TITLE,
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            // Acción del botón "Omitir"
            omitirBtn.addActionListener(es -> {
                tipoPagoSeleccionado[0] = EFECTIVO;
                dialogoPago.dispose();
                dialog.dispose();
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

            // Imagen QR
            ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource("/icons/NoDatafono.png"))
                    .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Texto de valor total
            JLabel montoLabel = new JLabel(TOTAL_PRICE + FormatterHelpers.formatearMoneda(finalTotal) + PESOS);

            montoLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
            montoLabel.setForeground(new Color(0, 153, 0));
            montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel montoLabel2 = new JLabel(FormatterHelpers.formatearMoneda(totalDolar) + " USD");
            montoLabel2.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            montoLabel2.setForeground(new Color(0, 0, 128));
            montoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Instrucción
            JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                    + "Registra la compra en el Datafono.<br>Y verifica sí la transacción fue exitosa antes de continuar."
                    + "</div></html>");
            instruccion.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
            instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Botón continuar
            JButton continuarBtn = new JButton("Continuar");
            continuarBtn.setBackground(new Color(0, 153, 0));
            continuarBtn.setForeground(Color.WHITE);
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 18));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(200, 50));
            continuarBtn.setMaximumSize(new Dimension(200, 50)); // para que no se estire
            continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Crear el diálogo
            JDialog dialogoQR = new JDialog(compraDialog, "Guía de Pago Datafono", true); // Cambié el título a "Datafono"
            dialogoQR.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialogoQR.setLayout(new BoxLayout(dialogoQR.getContentPane(), BoxLayout.Y_AXIS)); // Usar BoxLayout para alinear verticalmente

            // Acción del botón "Continuar"
            continuarBtn.addActionListener(es -> {
                dialogoQR.dispose(); // Cierra el diálogo QR
                tipoPagoSeleccionado[0] = "Datafono";
                dialogoPago.dispose(); // Cierra el diálogo de selección de pago

            });

            // Panel de contenido
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            content.add(montoLabel);
            content.add(Box.createVerticalStrut(15));
            content.add(montoLabel2);
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

        // Mostrar el diálogo modal y esperar la selección
        dialogoPago.setLocationRelativeTo(compraDialog);
        dialogoPago.setVisible(true);

        if (tipoPagoSeleccionado[0] == null) {
            JOptionPane.showMessageDialog(compraDialog, "No se seleccionó un método de pago.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return null;
        }

         if(ConfigAdminManager.isElectronicBillingEnabled()) {

             JSONObject clienteSiigo = UIUserVenta.mostrarDialogoFactura(frame);
                   if (clienteSiigo != null) {
                       // Usar clienteSiigo en la creación de la factura
                        mostrarDialogoFactura(frame);
                    }
        }

        return tipoPagoSeleccionado[0];
    }

    private static JLabel getJLabel(double total, Double totalDolar) {
        String textoTotal = String.format(
                "<html><div style='text-align:center; font-size:28px; color:#2ecc71;'>" +
                        "<b>Total: $%,.0f Pesos</b><br>" +
                        "<span style='font-size:20pt; color:#000080;'><b>%.2f USD</b></span>" +
                        "</div></html>",
                total, totalDolar
        );
        JLabel totalLabel = new JLabel(textoTotal, SwingConstants.CENTER);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        return totalLabel;
    }

    private static void configurarBotonConQR(JDialog parentDialog, JButton boton, double total, double totalDolar, Runnable onSuccess) {
        boton.addActionListener(event -> {
            ImageIcon qrIcon = new ImageIcon(new ImageIcon(UIUserMain.class.getResource(QR))
                    .getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH));
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel montoLabel = new JLabel(TOTAL_PRICE + FormatterHelpers.formatearMoneda(total) + PESOS);
            montoLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
            montoLabel.setForeground(new Color(0, 153, 0));
            montoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel montoLabel2 = new JLabel(FormatterHelpers.formatearMoneda(totalDolar) + " USD");
            montoLabel2.setFont(new Font(ARIAL_FONT, Font.BOLD, 16));
            montoLabel2.setForeground(new Color(0, 0, 128));
            montoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel instruccion = new JLabel("<html><div style='text-align:center; color:red;'>"
                    + "Verifica en el teléfono del cliente<br>Sí la transacción fue exitosa antes de continuar."
                    + "</div></html>");
            instruccion.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
            instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton continuarBtn = new JButton(CONTINUE);
            continuarBtn.setBackground(new Color(0, 153, 0));
            continuarBtn.setForeground(Color.WHITE);
            continuarBtn.setFont(new Font(ARIAL_FONT, Font.BOLD, 18));
            continuarBtn.setFocusPainted(false);
            continuarBtn.setPreferredSize(new Dimension(200, 50));
            continuarBtn.setMaximumSize(new Dimension(200, 50));
            continuarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JDialog dialogoQR = new JDialog(parentDialog, GUIA_QR, true);
            dialogoQR.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            continuarBtn.addActionListener(e -> {
                dialogoQR.dispose();
                onSuccess.run(); // Acción al confirmar
            });

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            content.setBackground(Color.WHITE);

            content.add(montoLabel);
            content.add(Box.createVerticalStrut(15));
            content.add(montoLabel2);
            content.add(Box.createVerticalStrut(15));
            content.add(qrLabel);
            content.add(Box.createVerticalStrut(20));
            content.add(instruccion);
            content.add(Box.createVerticalStrut(25));
            content.add(continuarBtn);

            dialogoQR.getContentPane().add(content);
            dialogoQR.pack();
            dialogoQR.setResizable(false);
            dialogoQR.setLocationRelativeTo(parentDialog);
            dialogoQR.setVisible(true);
        });
    }
}
