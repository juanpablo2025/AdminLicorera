package org.example.ui.uiUser;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.userManager.ExcelManager;
import org.example.manager.userManager.ProductoManager;
import org.example.manager.userManager.VentaMesaManager;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import static org.example.manager.userManager.ExcelManager.actualizarCantidadStockExcel;
import static org.example.manager.userManager.ExcelManager.cargarProductosMesaDesdeExcel;
import static org.example.ui.uiUser.UIHelpers.*;
import static org.example.ui.uiUser.UIHelpers.createTotalPanel;
import static org.example.utils.Constants.*;
import static org.example.utils.Constants.ERROR_TITLE;

public class UIVenta {
    private static ProductoManager productoManager = new ProductoManager();

    private static JDialog ventaMesaDialog;

    public static void showVentaMesaDialog(List<String[]> productos, String mesaID) {
        ventaMesaDialog = createDialog("Realizar Venta", 800, 600, new BorderLayout());
        ventaMesaDialog.setResizable(true);


        // Crear la tabla de productos y cargar los productos de la mesa
        JTable table = createProductTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Añadir los productos a la tabla, asegurando que los datos sean correctos
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[0].trim(); // Asegurarse de eliminar espacios innecesarios
                int cantidad = Integer.parseInt(productoDetalles[1].substring(1).trim()); // Extraer cantidad (x1, x2, etc.)
                double precioUnitario = Double.parseDouble(productoDetalles[2].substring(1).trim()); // Precio sin el símbolo $
                double total = Double.parseDouble(productoDetalles[4].trim()); // Total final del producto

                // Añadir la fila a la tabla
                tableModel.addRow(new Object[] { nombreProducto, cantidad, precioUnitario, total });
            } catch (NumberFormatException ex) {
                System.err.println("Error al parsear los datos del producto: " + Arrays.toString(productoDetalles));
                ex.printStackTrace();  // Para depuración
            }
        }

        VentaMesaManager ventaMesaManager = new VentaMesaManager();

        // Crear y añadir el panel del botón "Añadir" antes de la tabla
        /*JPanel addButtonPanel = addButtonPanelMesa(table, ventaMesaManager);

        ventaMesaDialog.add(addButtonPanel, BorderLayout.NORTH); */// Cambia a NORTH para que aparezca antes de la tabla
        // Estilizar la tabla
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajustar automáticamente el tamaño de las columnas

        // Establecer la fuente y el tamaño
        Font font = new Font("Arial", Font.PLAIN, 16); // Cambiar el tipo y tamaño de fuente
        table.setFont(font);
        table.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 18)); // Fuente más grande para el encabezado
        header.setBackground(Color.LIGHT_GRAY); // Fondo para el encabezado
        header.setForeground(Color.BLACK); // Color del texto del encabezado

        // Configuración de borde para mejorar la visibilidad
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        table.setBackground(Color.WHITE); // Fondo de la tabla
        table.setSelectionBackground(Color.CYAN); // Color de selección
        table.setSelectionForeground(Color.BLACK); // Color del texto seleccionado

        // Crear el JScrollPane para la tabla
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);


        JPanel inputPanel = createInputPanel(table, ventaMesaManager);
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);


        JPanel totalPanel = createTotalPanel();
        ventaMesaDialog.add(totalPanel, BorderLayout.SOUTH);

        // Pasar el ID de la mesa al crear el panel de botones
        JPanel buttonPanel = createButtonPanelVentaMesa(table, ventaMesaManager, ventaMesaDialog, mesaID);
        ventaMesaDialog.add(buttonPanel, BorderLayout.SOUTH);

        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);
    }

    public static JButton createConfirmPurchaseMesaButton(VentaMesaManager ventaMesaManager, JDialog compraDialog, String mesaID) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);
        confirmarCompraButton.addActionListener(e -> {
            try {
                // Inicializamos el total en 0
                double total = 0;
                // Generar un ID único para la venta
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000);
                LocalDateTime dateTime = LocalDateTime.now();

                // Crear un StringBuilder para construir la lista de productos con nombre y cantidad
                StringBuilder listaProductosEnLinea = new StringBuilder();

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

                // Obtener la lista de productos comprados y sus cantidades (nuevos productos)
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities();

                // Validar si hay productos agregados
                if (productosComprados.isEmpty() && productosPrevios.isEmpty()) {
                    JOptionPane.showMessageDialog(compraDialog, "No hay productos agregados a la mesa.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Salir del método si no hay productos
                }

                // Sumar el total de los productos nuevos agregados y verificar stock
                for (Map.Entry<String, Integer> entrada : productosComprados.entrySet()) {
                    String nombreProducto = entrada.getKey(); // Nombre del producto
                    int cantidad = entrada.getValue(); // Cantidad comprada
                    Producto producto = productoManager.getProductByName(nombreProducto);

                    // Validar stock
                    if (producto.getCantidad() < cantidad) {
                        JOptionPane.showMessageDialog(compraDialog,
                                "No hay suficiente stock para " + nombreProducto + ". Stock disponible: " + producto.getCantidad(),
                                "Error de stock",
                                JOptionPane.ERROR_MESSAGE);
                        ventaMesaDialog.dispose(); // Cerrar el diálogo de la venta
                        return; // Salir del método si no hay suficiente stock
                    }

                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    // Añadir la información del producto nuevo al StringBuilder
                    listaProductosEnLinea.append(nombreProducto)
                            .append(" x").append(cantidad)
                            .append(" $").append(precioUnitario)
                            .append(" = ").append(precioTotal).append("\n");

                    // Sumar al total general (solo productos nuevos)
                    total += precioTotal;
                }

                // Actualizar las cantidades en el stock de Excel
                actualizarCantidadStockExcel(productosComprados, productosPrevios);

                // Guardar la compra en Excel
                ExcelManager excelManager = new ExcelManager();
                excelManager.savePurchase(ventaID, listaProductosEnLinea.toString(), total, dateTime);

                // Limpiar la mesa (borrar productos y cambiar el estado a "Libre")
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
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

                                    // Cambiar el estado a "Libre"
                                    Cell estadoCell = row.getCell(1); // Columna B: Estado de la mesa
                                    if (estadoCell == null) {
                                        estadoCell = row.createCell(1);
                                    }
                                    estadoCell.setCellValue("Libre");

                                    // Borrar los productos de la mesa
                                    Cell productosCell = row.getCell(2); // Columna C: Productos
                                    if (productosCell != null) {
                                        productosCell.setCellValue("");  // Limpiar los productos
                                    }

                                    // Limpiar el total de la mesa
                                    Cell totalCell = row.getCell(3); // Columna D: Total de la compra
                                    if (totalCell != null) {
                                        totalCell.setCellValue(0.0);  // Restablecer el total a 0
                                    }

                                    // Salir del bucle una vez que la mesa fue actualizada
                                    break;
                                }
                            }
                        }

                        // Guardar los cambios en el archivo Excel
                        try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                            workbook.write(fos);
                        }

                        // Mensaje indicando que la mesa fue limpiada
                        //JOptionPane.showMessageDialog(compraDialog, "Mesa " + mesaID + " ha sido limpiada y marcada como libre.");
                    }
                }

                // Preguntar al usuario si quiere imprimir la factura
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);
                NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario selecciona 'Sí', generar e imprimir la factura
                    ventaMesaManager.generarFactura(ventaID, Collections.singletonList(listaProductosEnLinea.toString()), total, dateTime);
                }

                // Mostrar un mensaje de éxito de la compra
                JOptionPane.showMessageDialog(compraDialog, PURCHASE_SUCCEDED +"\n"+"Total: $ " + formatCOP.format(total));

                // Cerrar el diálogo de la venta
                compraDialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(compraDialog, INVALID_MONEY, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return confirmarCompraButton;
    }



    public static JButton createSavePurchaseMesaButton(VentaMesaManager ventaMesaManager, String mesaID) {
        JButton saveCompraButton = new JButton("Guardar Compra");
        saveCompraButton.addActionListener(e -> {
            try {
                // Obtener los productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities();

                // Validar que haya productos en la compra
                if (productosComprados.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hay productos agregados a la compra.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Salir si no hay productos
                }

                // Verificar stock para cada producto
                for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                    String nombreProducto = entry.getKey();
                    int cantidadComprada = entry.getValue();
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    if (producto.getCantidad() < cantidadComprada) {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto: " + nombreProducto, "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Salir si no hay suficiente stock
                    }
                }

                double total = ventaMesaManager.getTotalCartAmount(); // Obtener el total de la compra
                LocalDateTime dateTime = LocalDateTime.now(); // Fecha y hora actuales

                // Guardar la compra en la pestaña "mesas"
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
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

                                    // Leer productos existentes
                                    Cell productosCell = row.getCell(2); // Columna C: Productos
                                    Map<String, Integer> productosExistentes = new HashMap<>(); // Para guardar productos ya registrados
                                    Map<String, Double> preciosExistentes = new HashMap<>();   // Para guardar precios

// Verificar si productosCell es null y crearla si es necesario
                                    if (productosCell == null) {
                                        productosCell = row.createCell(2); // Crear celda si no existe
                                    }

// Ahora puedes proceder a leer los productos
                                    if (productosCell.getCellType() == CellType.STRING) {
                                        String[] lineasProductos = productosCell.getStringCellValue().split("\n");
                                        for (String linea : lineasProductos) {
                                            String[] partes = linea.split(" x| \\$| = "); // Separar por la estructura "producto x cantidad $precioUnitario = total"
                                            if (partes.length == 4) {
                                                String nombreProductoExistente = partes[0].trim();
                                                int cantidadExistente = Integer.parseInt(partes[1]);
                                                double precioTotalExistente = Double.parseDouble(partes[3]);

                                                productosExistentes.put(nombreProductoExistente, cantidadExistente);
                                                preciosExistentes.put(nombreProductoExistente, precioTotalExistente);
                                            }
                                        }
                                    }

                                    // Combinar productos nuevos con productos existentes
                                    for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                                        String nombreProducto = entry.getKey();
                                        int cantidadNueva = entry.getValue();
                                        Producto producto = productoManager.getProductByName(nombreProducto);
                                        double precioUnitario = producto.getPrice();
                                        double precioTotalNuevo = precioUnitario * cantidadNueva;

                                        // Si el producto ya existe, actualizar cantidad y precios
                                        if (productosExistentes.containsKey(nombreProducto)) {
                                            int cantidadTotal = productosExistentes.get(nombreProducto) + cantidadNueva;
                                            double precioTotal = preciosExistentes.get(nombreProducto) + precioTotalNuevo;

                                            productosExistentes.put(nombreProducto, cantidadTotal);
                                            preciosExistentes.put(nombreProducto, precioTotal);
                                        } else {
                                            // Si es nuevo, agregarlo
                                            productosExistentes.put(nombreProducto, cantidadNueva);
                                            preciosExistentes.put(nombreProducto, precioTotalNuevo);
                                        }
                                    }

                                    // Construir la lista actualizada de productos
                                    StringBuilder listaProductosActualizados = new StringBuilder();
                                    for (Map.Entry<String, Integer> productoEntry : productosExistentes.entrySet()) {
                                        String nombreProducto = productoEntry.getKey();
                                        int cantidadTotal = productoEntry.getValue();
                                        double precioTotal = preciosExistentes.get(nombreProducto);
                                        double precioUnitario = precioTotal / cantidadTotal;  // Precio unitario calculado

                                        listaProductosActualizados.append(nombreProducto)
                                                .append(" x")
                                                .append(cantidadTotal)
                                                .append(" $")
                                                .append(precioUnitario)
                                                .append(" = ")
                                                .append(precioTotal)
                                                .append("\n");
                                    }

                                    // Guardar la lista actualizada de productos en la celda
                                    productosCell.setCellValue(listaProductosActualizados.toString());

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
                        try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                            workbook.write(fos);
                        }

                        JOptionPane.showMessageDialog(null, "Compra guardada para la: " + mesaID + ".");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hoja 'mesas' no encontrada en el archivo Excel.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    // Cerrar el diálogo de la venta
                    ventaMesaDialog.dispose();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error al guardar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return saveCompraButton;
    }

    // Método modificado para crear el panel de botones de la mesa
    public static JPanel createButtonPanelVentaMesa(JTable table, VentaMesaManager ventaMesaManager, JDialog compraDialog, String mesaID) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaManager, mesaID); // Usar mesaID dinámicamente
        guardarCompra.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18)); // Fuente del botón
        buttonPanel.add(guardarCompra);
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaManager, compraDialog, mesaID); // Usar mesaID dinámicamente
        confirmarCompraButton.setFont(new java.awt.Font("Arial", Font.BOLD, 18)); // Fuente del botón
        buttonPanel.add(confirmarCompraButton);

        buttonPanel.add(guardarCompra);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }
}
