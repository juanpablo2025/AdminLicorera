package org.example;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.*;
import org.example.model.Mesa;
import org.example.model.Producto;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;


import static org.example.ui.UIHelpers.*;
import static org.example.utils.Constants.*;

public class Main {
    private static ProductoManager productoManager = new ProductoManager();


    private static JDialog ventaDialog;
    private static JDialog ventaMesaDialog;
// eliminar botones

    public static void main(String[] args) {
        crearDirectorios();
        JFrame frame = new JFrame(CALCULADORA_ADMINISTRADOR);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Cambiar a DO_NOTHING_ON_CLOSE
        frame.setUndecorated(true); // Quitar los decorados de la ventana
        frame.setSize(FOUR_HUNDRED, FOUR_HUNDRED);
        frame.setLayout(new GridLayout(TWO, ONE));
        VentaMesaManager.initializeMesasSheet(); // Inicializar la pestaña de mesas en el archivo Excel


        FacturacionManager facturacionManager = new FacturacionManager(); // Instancia de FacturacionManager

        // Botones principales
        //JButton ventaButton = createButton(VENTA, e -> showVentaDialog());
        JButton adminProductosButton = createButton(LISTAR_PRODUCTOS, e -> showListProductsDialog());
        JButton gastosButton = createButton("Gastos", e -> showGastosDialog());
        JButton mesasButton = createButton("Administrar Mesas", e -> showMesas()); // Nuevo botón para administrar mesas

        JButton salirButton = createButton(SALIR_FACTURAR, e -> {
            // Mostrar un cuadro de diálogo solicitando que el usuario escriba "Facturar"
            String input = JOptionPane.showInputDialog(null, POR_FAVOR_ESCRIBE_FACTURAR, CONFIRMAR_FACTURACION, JOptionPane.QUESTION_MESSAGE);

            // Verificar si el usuario ingresó correctamente la palabra "Facturar"
            if (facturacionManager.verificarFacturacion(input)) {
                // Si la palabra es correcta, se procede a facturar y salir
                facturacionManager.facturarYSalir();

            } else {
                // Si la palabra es incorrecta o el usuario cancela, mostrar un mensaje y regresar al menú principal
                facturacionManager.mostrarErrorFacturacion();
            }
        });
       frame.add(mesasButton); // Añadir el botón de mesas

        //frame.add(ventaButton);
        frame.add(adminProductosButton);
        frame.add(gastosButton);
        frame.add(salirButton);

        // Centrar el frame en la pantalla
        frame.setLocationRelativeTo(null);

        // Mostrar la ventana principal
        frame.setVisible(true);
    }

    private static void showListProductsDialog() {
        // Crear el diálogo
        JDialog listProductsDialog = createDialog(LISTAR_PRODUCTO, FOUR_HUNDRED, THREE_HUNDRED, new BorderLayout());

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
        JButton closeButton = createButton(CLOSE_BUTTON, e -> listProductsDialog.dispose());
        listProductsDialog.add(closeButton, BorderLayout.SOUTH);

        // Mostrar el diálogo
        listProductsDialog.setVisible(true);
        listProductsDialog.setLocationRelativeTo(null);

    }

    /*private static void showAdminProductosDialog() {
        JDialog adminDialog = UIHelpers.createDialog( ADMINISTRAR_PRODUCTOS , THREE_HUNDRED, TWO_HUNDRED, new GridLayout(2, 1));

        // Botones
        JButton listButton = UIHelpers.createButton(LISTAR_PRODUCTO, e -> showListProductsDialog());

        // Añadir botones al diálogo
        adminDialog.add(listButton);
        adminDialog.setLocationRelativeTo(null);
        adminDialog.setVisible(true);
    }*/



    /*public static void showVentaDialog() {
        ventaDialog = createDialog(REALIZAR_VENTA, FIVE_HUNDRED, FOUR_HUNDRED, new BorderLayout());

        JPanel inputPanel = createInputPanel();
        ventaDialog.add(inputPanel, BorderLayout.NORTH);

        JTable table = createProductTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();
        ventaDialog.add(totalPanel, BorderLayout.SOUTH);

        VentaManager ventaManager = new VentaManager();

        JPanel buttonPanel = createButtonPanel(table, ventaManager, ventaDialog);
        ventaDialog.add(buttonPanel, BorderLayout.SOUTH);

        ventaDialog.setVisible(true);
        ventaDialog.setLocationRelativeTo(null);

    }*/
    public static void crearDirectorios() {
        String documentosPath = System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
        String facturacionPath = documentosPath + "\\Facturacion";
        String facturasPath = documentosPath + "\\Facturas";
        String realizadoPath = documentosPath + "\\Realizo";

        crearDirectorioSiNoExiste(facturasPath);
        crearDirectorioSiNoExiste(realizadoPath);
        crearDirectorioSiNoExiste(facturacionPath);
    }

    private static void crearDirectorioSiNoExiste(String path) {
        File directorio = new File(path);
        if (!directorio.exists()) {
            if (directorio.mkdirs()) {
                System.out.println("Directorio creado: " + path);
            } else {
                System.out.println("No se pudo crear el directorio: " + path);
            }
        } else {
            System.out.println("El directorio ya existe: " + path);
        }
    }




    private static void showGastosDialog() {
        JDialog gastosDialog = createDialog("Registrar Gastos", 300, 200, new GridLayout(2, 1));

        JButton reabastecimientoButton = createButton("Reabastecimiento", e -> showReabastecimientoDialog());
        JButton gastosGeneralesButton = createButton("Gastos Generales", e -> showGastosGeneralesDialog());

        gastosDialog.add(reabastecimientoButton);
        gastosDialog.add(gastosGeneralesButton);

        gastosDialog.setLocationRelativeTo(null);
        gastosDialog.setVisible(true);
    }
    private static void showReabastecimientoDialog() {
        JDialog reabastecimientoDialog = createDialog("Reabastecimiento de Productos", 300, 250, new GridLayout(4, 2));

        JComboBox<String> productComboBox = new JComboBox<>();
        List<Producto> productos = productoManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }

        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Campo para ingresar el precio de la compra
        JTextField precioCompraField = new JTextField();

        JButton confirmarReabastecimientoButton = createButton("Confirmar", e -> {
            try {
                String selectedProduct = (String) productComboBox.getSelectedItem();
                int cantidad = (int) cantidadSpinner.getValue();
                double precioCompra = Double.parseDouble(precioCompraField.getText());

                Producto producto = productoManager.getProductByName(selectedProduct);

                // Guardar el reabastecimiento en Excel sin hacer operaciones adicionales
                GastosManager gastosManager = new GastosManager();
                gastosManager.reabastecerProducto(producto, cantidad, precioCompra);

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(null, "Producto reabastecido correctamente.");

                reabastecimientoDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(reabastecimientoDialog, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(reabastecimientoDialog, "Ocurrió un error durante el reabastecimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        reabastecimientoDialog.add(new JLabel("Producto:"));
        reabastecimientoDialog.add(productComboBox);
        reabastecimientoDialog.add(new JLabel("Cantidad:"));
        reabastecimientoDialog.add(cantidadSpinner);
        reabastecimientoDialog.add(new JLabel("Precio de la compra:"));
        reabastecimientoDialog.add(precioCompraField);  // Añadir el campo de texto para el precio
        reabastecimientoDialog.add(confirmarReabastecimientoButton);

        reabastecimientoDialog.setLocationRelativeTo(null);
        reabastecimientoDialog.setVisible(true);
    }


    private static void showGastosGeneralesDialog() {
        JDialog gastosGeneralesDialog = createDialog("Registrar Gastos Generales", 300, 150, new GridLayout(3, 2));

        JTextField nombreGastoField = new JTextField(); // Campo para la descripción del gasto
        JTextField precioField = new JTextField();      // Campo para el precio del gasto

        JButton confirmarGastoButton = createButton("Confirmar", e -> {
            try {
                String nombreGasto = nombreGastoField.getText();
                double precio = Double.parseDouble(precioField.getText());

                // Lógica para registrar el gasto en el Excel
                GastosManager.saveGasto(nombreGasto, 1, precio); // Implementar la lógica de guardado en el Excel, sin cantidad

                JOptionPane.showMessageDialog(null, "Gasto registrado correctamente.");
                gastosGeneralesDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el gasto.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        gastosGeneralesDialog.add(new JLabel("Descripción del Gasto:"));
        gastosGeneralesDialog.add(nombreGastoField);
        gastosGeneralesDialog.add(new JLabel("Precio:"));
        gastosGeneralesDialog.add(precioField);
        gastosGeneralesDialog.add(confirmarGastoButton);

        gastosGeneralesDialog.setLocationRelativeTo(null);
        gastosGeneralesDialog.setVisible(true);
    }



    // Método para mostrar las mesas en la interfaz
    private static void showMesas() {
        JFrame mesasFrame = new JFrame("Administrar Mesas");
        mesasFrame.setSize(1200, 600);
        mesasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mesasPanel = new JPanel();
        mesasPanel.setLayout(new GridLayout(0, 5)); // Filas de 5 mesas

        // Cargar las mesas desde el archivo Excel
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        // Mostrar las mesas cargadas desde el archivo Excel
        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf((i + 1))); // Asignar ID basado en la posición
            JPanel mesaPanel = crearMesaPanel(mesa,mesasFrame); // Pasar el objeto Mesa
            mesasPanel.add(mesaPanel);
        }

        // Botón para añadir más mesas
        JButton addMesaButton = new JButton("Añadir Mesa");
        addMesaButton.addActionListener(e -> {
            // Generar un nuevo ID basado en la cantidad actual de mesas
            String nuevoID = String.valueOf(mesas.size() + 1); // Asegurarse de que el ID sea único
            Mesa nuevaMesa = new Mesa(nuevoID); // Crear la nueva mesa con el ID basado en el nuevo ID

            // Añadir la nueva mesa a la lista de mesas
            mesas.add(nuevaMesa);

            // Crear el panel para la nueva mesa
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa,mesasFrame);
            mesasPanel.add(nuevaMesaPanel);

            // Actualizar el panel de mesas
            mesasPanel.revalidate();
            mesasPanel.repaint();

            // Guardar la nueva mesa en el archivo Excel
            agregarMesaAExcel(nuevaMesa);
        });

        // Panel inferior con el botón para añadir mesas
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);

        mesasFrame.setLayout(new BorderLayout());
        mesasFrame.add(mesasPanel, BorderLayout.CENTER);
        mesasFrame.add(bottomPanel, BorderLayout.SOUTH);

        mesasFrame.setLocationRelativeTo(null);
        mesasFrame.setVisible(true);
    }

    // Método para cargar las mesas desde el archivo Excel
    private static ArrayList<Mesa> cargarMesasDesdeExcel() {

         final String FILE_NAME = "productos.xlsx";
         final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
         final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

            ArrayList<Mesa> mesas = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(FILE_PATH);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet mesasSheet = workbook.getSheet("Mesas"); // Acceder a la hoja llamada "mesas"
                if (mesasSheet != null) {
                    for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) { // Empezamos en la fila 1 (saltamos el encabezado)
                        Row row = mesasSheet.getRow(i);
                        if (row != null) {
                            // Leer el ID de la mesa (columna 0)
                            Cell idCell = row.getCell(0);
                            String idText = idCell.getStringCellValue();

                            // Extraer el número de la mesa, por ejemplo, de "Mesa 1" extraer 1
                            int id = extraerNumeroDeTexto(idText);

                            // Leer el estado de la mesa (columna 1)
                            String estado = row.getCell(1).getStringCellValue();
                            Mesa mesa = new Mesa("Mesa "+id);
                            mesa.setOcupada(estado.equalsIgnoreCase("Ocupada"));
                            mesas.add(mesa);
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }

            return mesas;
        }

// Método auxiliar para extraer el número del ID de la mesa
        private static int extraerNumeroDeTexto(String texto) {
            // Remover cualquier cosa que no sea un número del texto
            String numeroTexto = texto.replaceAll("[^0-9]", "");
            return Integer.parseInt(numeroTexto);
        }

    // Método para crear un panel de mesa con botón "Atender Mesa"
    private static JPanel crearMesaPanel(Mesa mesa,JFrame mesasFrame) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        // Crear el borde con el título que incluye el número de la mesa
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                "Mesa " + mesa.getId(), // Mostrar el número de la mesa
                TitledBorder.CENTER, TitledBorder.TOP);

        // Asignar el borde al panel de la mesa
        mesaPanel.setBorder(border);

        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? Color.RED : Color.GREEN);

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "Ocupada" : "Libre", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mesaLabel.setForeground(Color.WHITE);

        // Crear el botón "Atender Mesa"
        JButton editarButton = new JButton("Atender Mesa");

        // Añadir un ActionListener que capture el título del borde del panel (que contiene el ID de la mesa)
        editarButton.addActionListener(e -> {
            // Obtener el título del borde que tiene el nombre de la mesa
            TitledBorder panelBorder = (TitledBorder) mesaPanel.getBorder();
            String tituloMesa = panelBorder.getTitle();  // Esto devolverá algo como "Mesa 19"
            System.out.println("Atendiendo: " + tituloMesa); // Debug para verificar el título

            // Cargar los productos de la mesa desde Excel usando el título
            List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

            // Minimizar la ventana de las mesas
            mesasFrame.dispose();

            // Mostrar los productos de la mesa en un diálogo
            showVentaMesaDialog(productosMesa, tituloMesa);
        });

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editarButton);

        // Añadir componentes al panel de la mesa
        mesaPanel.add(mesaLabel, BorderLayout.CENTER); // Etiqueta en el centro
        mesaPanel.add(buttonPanel, BorderLayout.SOUTH); // Botones en la parte inferior

        return mesaPanel;
    }

    private static void agregarMesaAExcel(Mesa nuevaMesa) {
        final String FILE_NAME = "productos.xlsx";
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
        String filePath = FILE_PATH; // Reemplaza con la ruta correcta
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("Mesas");
            if (mesasSheet == null) {
                // Si no existe la hoja "mesas", crearla
                mesasSheet = workbook.createSheet("Mesas");
                // Crear encabezado si es una hoja nueva
                Row headerRow = mesasSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Estado");
            }

            // Agregar nueva fila con los datos de la nueva mesa
            int newRowNum = mesasSheet.getLastRowNum() + 1; // La última fila más uno
            Row newRow = mesasSheet.createRow(newRowNum);
            newRow.createCell(0).setCellValue("Mesa " + nuevaMesa.getId()); // ID de la mesa
            newRow.createCell(1).setCellValue(nuevaMesa.isOcupada() ? "Ocupada" : "Libre"); // Estado de la mesa

            // Escribir los cambios en el archivo
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void showVentaMesaDialog(List<String[]> productos, String mesaID) {
        ventaMesaDialog = createDialog("Realizar Venta", 500, 400, new BorderLayout());



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
        JPanel inputPanel = createInputPanel(table, ventaMesaManager);
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();
        ventaMesaDialog.add(totalPanel, BorderLayout.SOUTH);

        // Pasar el ID de la mesa al crear el panel de botones
        JPanel buttonPanel = createButtonPanelMesa(table, ventaMesaManager, ventaMesaDialog, mesaID);
        ventaMesaDialog.add(buttonPanel, BorderLayout.SOUTH);

        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);
    }

    public static JPanel addButtonPanelMesa(JTable table, VentaMesaManager ventaMesaManager) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // Pasar el ID de la mesa correspondiente
        JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaManager);
        buttonPanel.add(agregarProductoButton);


        return buttonPanel;
    }
    // Método modificado para crear el panel de botones de la mesa
    public static JPanel createButtonPanelMesa(JTable table, VentaMesaManager ventaMesaManager, JDialog compraDialog, String mesaID) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // Pasar el ID de la mesa correspondiente
        /*JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaManager);
        buttonPanel.add(agregarProductoButton);*/
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaManager, mesaID); // Usar mesaID dinámicamente
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaManager, compraDialog, mesaID); // Usar mesaID dinámicamente


        buttonPanel.add(guardarCompra);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }



    private static JButton createConfirmPurchaseMesaButton(VentaMesaManager ventaMesaManager, JDialog compraDialog, String mesaID) {
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
                        JOptionPane.showMessageDialog(compraDialog, "Mesa " + mesaID + " ha sido limpiada y marcada como libre.");
                    }
                }

                // Preguntar al usuario si quiere imprimir la factura
                int respuesta = JOptionPane.showConfirmDialog(null, PRINT_BILL, COMFIRM_TITLE, JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario selecciona 'Sí', generar e imprimir la factura
                    ventaMesaManager.generarFactura(ventaID, Collections.singletonList(listaProductosEnLinea.toString()), total, dateTime);
                }

                // Mostrar un mensaje de éxito de la compra
                JOptionPane.showMessageDialog(compraDialog, PURCHASE_SUCCEDED + total);

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



    private static JButton createSavePurchaseMesaButton(VentaMesaManager ventaMesaManager, String mesaID) {
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
                StringBuilder listaProductosNuevos = new StringBuilder();

                // Construir la lista de productos con cantidad
                for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                    String nombreProducto = entry.getKey();
                    int cantidad = entry.getValue();
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    listaProductosNuevos.append(nombreProducto)
                            .append(" x")
                            .append(cantidad)
                            .append(" $")
                            .append(precioUnitario)
                            .append(" = ")
                            .append(precioTotal)
                            .append("\n"); // Asegura que cada producto termine con un salto de línea
                }

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

                                    // Leer productos existentes y agregar nuevos
                                    Cell productosCell = row.getCell(2); // Columna C: Productos
                                    if (productosCell == null) {
                                        productosCell = row.createCell(2); // Crear si no existe
                                    }

                                    String productosExistentes = "";
                                    if (productosCell.getCellType() == CellType.STRING) {
                                        productosExistentes = productosCell.getStringCellValue();
                                    }

                                    // Concatenar los nuevos productos con los existentes, asegurando un salto de línea
                                    String listaActualizada = productosExistentes.trim() + "\n" + listaProductosNuevos.toString();
                                    productosCell.setCellValue(listaActualizada);

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

                        JOptionPane.showMessageDialog(null, "Compra guardada para la mesa " + mesaID + ".");
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


    private static List<String[]> cargarProductosMesaDesdeExcel(String mesaID) {
        final String FILE_NAME = "productos.xlsx";
        final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Documents\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

        List<String[]> productosMesa = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("Mesas"); // Asegúrate de tener una hoja "mesas" en el archivo Excel
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Recorrer las filas de la hoja, empezando en la fila 1
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(0); // Columna A: ID de la mesa

                        // Asegúrate de que la celda no sea nula y de que contenga un valor de tipo String
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String id = idCell.getStringCellValue(); // Obtener el ID como String
                            System.out.println("ID de mesa en fila " + (i + 1) + ": " + id); // Log del ID leído

                            // Comparar el ID de la mesa con el valor esperado
                            if (mesaID.equals(id)) { // Si el ID coincide con el de la mesa
                                System.out.println("Mesa encontrada: " + id); // Log si se encuentra la mesa

                                // Leer los productos de la mesa (suponiendo que los productos están en la columna C)
                                Cell productosCell = row.getCell(2);
                                if (productosCell != null && productosCell.getCellType() == CellType.STRING) {
                                    String productosTexto = productosCell.getStringCellValue(); // Obtener los productos como String
                                    System.out.println("Productos encontrados: " + productosTexto); // Log de los productos encontrados

                                    // Suponiendo que cada producto está separado por un salto de línea
                                    String[] productos = productosTexto.split("\n");
                                    for (String producto : productos) {
                                        // Suponiendo que los productos tienen un formato "nombreProducto xCantidad $PrecioUnitario"
                                        String[] detallesProducto = producto.trim().split(" ");
                                        if (detallesProducto.length >= 3) { // Verifica que hay suficientes elementos
                                            productosMesa.add(detallesProducto); // Añadir el producto a la lista
                                        }
                                    }
                                } else {
                                    System.out.println("Celda de productos está vacía o no es de tipo String.");
                                }
                                break; // Una vez encontrados los productos de la mesa, no necesitamos seguir buscando
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return productosMesa;
    }
    // Método para actualizar las cantidades en el stock de Excel
    private static void actualizarCantidadStockExcel(Map<String, Integer> productosComprados, List<String[]> productosPrevios) {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            // Para tener en cuenta los productos previos, primero actualizamos las cantidades de estos
            for (String[] productoPrevio : productosPrevios) {
                String nombreProducto = productoPrevio[0]; // nombre del producto
                int cantidadPrev = Integer.parseInt(productoPrevio[1].substring(1)); // xCantidad

                // Descontar el stock del producto previo
                boolean productoEncontrado = false;
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        if (row.getCell(1).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                            Cell cantidadCell = row.getCell(2);
                            if (cantidadCell != null && cantidadCell.getCellType() == CellType.NUMERIC) {
                                int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                int nuevaCantidad = cantidadActual - cantidadPrev;

                                if (nuevaCantidad < 0) {
                                    JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto '" + nombreProducto + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                } else {
                                    cantidadCell.setCellValue(nuevaCantidad);
                                    productoEncontrado = true;
                                }
                            }
                            break;
                        }
                    }
                }

                if (!productoEncontrado) {
                    JOptionPane.showMessageDialog(null, "Producto '" + nombreProducto + "' no encontrado en stock.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Ahora actualizar las cantidades de los productos nuevos comprados
            for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                String nombreProducto = entry.getKey();
                int cantidadComprada = entry.getValue();

                boolean productoEncontrado = false;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        if (row.getCell(1).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                            Cell cantidadCell = row.getCell(2);
                            if (cantidadCell != null && cantidadCell.getCellType() == CellType.NUMERIC) {
                                int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                int nuevaCantidad = cantidadActual - cantidadComprada;

                                if (nuevaCantidad < 0) {
                                    JOptionPane.showMessageDialog(null, "No hay suficiente stock para el producto '" + nombreProducto + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                } else {
                                    cantidadCell.setCellValue(nuevaCantidad);
                                    productoEncontrado = true;
                                }
                            }
                            break;
                        }
                    }
                }

                if (!productoEncontrado) {
                    JOptionPane.showMessageDialog(null, "Producto '" + nombreProducto + "' no encontrado en stock.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
