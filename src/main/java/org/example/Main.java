package org.example;

import org.apache.poi.ss.usermodel.*;
import org.example.manager.*;
import org.example.model.Mesa;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        JButton ventaButton = createButton(VENTA, e -> showVentaDialog());
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

        frame.add(ventaButton);
        frame.add(adminProductosButton);
        frame.add(gastosButton);
        frame.add(salirButton);

        // Centrar el frame en la pantalla
        frame.setLocationRelativeTo(null);

        // Mostrar la ventana principal
        frame.setVisible(true);
    }

    // Método para crear un panel que representa una mesa
    private static JPanel crearMesaPanel(boolean ocupada) {
        JPanel mesaPanel = new JPanel();
        mesaPanel.setBackground(ocupada ? Color.RED : Color.GREEN); // Rojo = Ocupada, Verde = Libre
        mesaPanel.setPreferredSize(new Dimension(80, 80)); // Tamaño del cuadro de la mesa
        mesaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return mesaPanel;
    }

    // Método para cambiar el estado de las mesas
    private static void cambiarEstadoMesas(ArrayList<JPanel> mesas) {
        for (JPanel mesa : mesas) {
            Color currentColor = mesa.getBackground();
            mesa.setBackground(currentColor.equals(Color.GREEN) ? Color.RED : Color.GREEN); // Alternar color
        }
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

   /* private static void showAddProductDialog() {
        ProductoManager productoManager = new ProductoManager();
        JDialog addProductDialog = UIHelpers.createDialog(AGREGAR_PRODUCTO, THREE_HUNDRED, TWO_HUNDRED, new GridLayout(3, 2));

        // Crear los campos de entrada
        JTextField nameField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_NOMBRE);
        JTextField priceField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_PRECIO);

        // Botón para agregar el producto
        JButton addButton = UIHelpers.createButton(AGREGAR_BTN, e -> {
            productoManager.addProductFromFields( nameField, quantityField, priceField, addProductDialog);
        });

        addProductDialog.add(addButton);
        addProductDialog.setVisible(true);
        addProductDialog.setLocationRelativeTo(null);
    }
*/

    public static void showVentaDialog() {
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

    }
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


    private static void finalizarMesa(Mesa mesa) {
        // Guardar productos y total de la cuenta en Excel
        if (mesa.getTotalCuenta() > 0) {
            String compraID = String.valueOf(System.currentTimeMillis() % 1000);
            LocalDateTime now = LocalDateTime.now();

            List<String> listaProductos = mesa.getProductos().stream()
                    .map(p -> p.getName() + " x" + p.getCantidad())
                    .collect(Collectors.toList());

            String productosEnLinea = String.join("\n", listaProductos);

            ExcelManager excelManager = new ExcelManager();
            excelManager.savePurchase(compraID, productosEnLinea, mesa.getTotalCuenta(), now);
        }

        // Finalizar la mesa y resetear su estado
        mesa.finalizarMesa();
    }

    private static void mostrarProductosMesa(Mesa mesa) {
        /*JDialog productosDialog = new JDialog();
        productosDialog.setTitle("Venta - Mesa " + mesa.getId()); // Asumimos que cada mesa tiene un ID o número
        productosDialog.setSize(400, 300);*/
        showVentaMesaDialog();
        /*/ Implementar lógica para seleccionar productos y añadirlos a la mesa
        JComboBox<String> productComboBox = new JComboBox<>(obtenerNombresProductos());
        JTextField cantidadField = new JTextField(5);
        JButton addButton = new JButton("Añadir Producto");

        addButton.addActionListener(e -> {
            Producto producto = productoManager.getProductByName((String) productComboBox.getSelectedItem());
            int cantidad = Integer.parseInt(cantidadField.getText());
            producto.setCantidad(cantidad);

            mesa.añadirProducto(producto); // Añadir producto a la mesa
        });*/

        // Añadir componentes al diálogo
       /* productosDialog.setLayout(new FlowLayout());
       /* productosDialog.add(new JLabel("Producto:"));
        productosDialog.add(productComboBox);
        productosDialog.add(new JLabel("Cantidad:"));
        productosDialog.add(cantidadField);
        productosDialog.add(addButton);

        productosDialog.setLocationRelativeTo(null);
        productosDialog.setVisible(true);*/
    }

    private static String[] obtenerNombresProductos() {
        List<Producto> productos = productoManager.getProducts();
        return productos.stream()
                .map(Producto::getName)
                .toArray(String[]::new);
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
            JPanel mesaPanel = crearMesaPanel(mesa); // Pasar el objeto Mesa
            mesasPanel.add(mesaPanel);
        }

        // Botón para añadir más mesas
        JButton addMesaButton = new JButton("Añadir Mesa");
        addMesaButton.addActionListener(e -> {
            int nuevoID = mesas.size() + 1; // Numerar correctamente las nuevas mesas
            Mesa nuevaMesa = new Mesa(nuevoID); // Crear la nueva mesa con el ID basado en el tamaño actual de la lista
            nuevaMesa.setID(String.valueOf(nuevoID)); // Asignar ID basado en la posición

            // Añadir la nueva mesa a la lista de mesas
            mesas.add(nuevaMesa);

            // Crear el panel para la nueva mesa
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa);
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

                Sheet mesasSheet = workbook.getSheet("mesas"); // Acceder a la hoja llamada "mesas"
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
                            Mesa mesa = new Mesa(id);
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

    // Método para crear un panel de mesa con botones "Editar" y "Finalizar"
    private static JPanel crearMesaPanel(Mesa mesa) {
        JPanel mesaPanel = new JPanel(new BorderLayout()); // Usar BorderLayout para organizar botones y etiquetas
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        // Bordes más estilizados para las mesas
        mesaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                "Mesa " + mesa.getId(), // Mostrar el número de la mesa
                TitledBorder.CENTER, TitledBorder.TOP));

        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? Color.RED : Color.GREEN);

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "Ocupada" : "Libre", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mesaLabel.setForeground(Color.WHITE);

        // Botón "Editar"
        JButton editarButton = new JButton("Editar");
        editarButton.addActionListener(e -> {
            mostrarProductosMesa(mesa); // Lógica para abrir el diálogo de edición de productos
        });

        // Botón "Finalizar"
        JButton finalizarButton = new JButton("Finalizar");
        finalizarButton.addActionListener(e -> {
            if (mesa.isOcupada()) {
                finalizarMesa(mesa);
                mesaPanel.setBackground(Color.GREEN); // Cambiar el color a libre
                mesaLabel.setText("Libre");
            }
        });

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editarButton);
        buttonPanel.add(finalizarButton);

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

            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet == null) {
                // Si no existe la hoja "mesas", crearla
                mesasSheet = workbook.createSheet("mesas");
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


    public static void showVentaMesaDialog() {
        ventaMesaDialog = createDialog(REALIZAR_VENTA, FIVE_HUNDRED, FOUR_HUNDRED, new BorderLayout());

        JPanel inputPanel = createInputPanel();
        ventaMesaDialog.add(inputPanel, BorderLayout.NORTH);

        JTable table = createProductTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        ventaMesaDialog.add(tableScrollPane, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();
        ventaMesaDialog.add(totalPanel, BorderLayout.SOUTH);

        VentaMesaManager ventaMesaManager = new VentaMesaManager();

        JPanel buttonPanel = createButtonPanelMesa(table, ventaMesaManager, ventaMesaDialog);
        ventaMesaDialog.add(buttonPanel, BorderLayout.SOUTH);



        ventaMesaDialog.setVisible(true);
        ventaMesaDialog.setLocationRelativeTo(null);

    }

    public static JPanel createButtonPanelMesa(JTable table, VentaMesaManager ventaMesaManager, JDialog compraDialog) {
        JPanel buttonPanel = new JPanel(new GridLayout(ONE, THREE));

        JButton agregarProductoButton = createAddProductMesaButton(table, ventaMesaManager);
        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaManager);
        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaManager, compraDialog);


        buttonPanel.add(agregarProductoButton);
        buttonPanel.add(guardarCompra);
        buttonPanel.add(confirmarCompraButton);

        return buttonPanel;
    }



    private static JButton createConfirmPurchaseMesaButton(VentaMesaManager ventaMesaManager, JDialog compraDialog) {
        JButton confirmarCompraButton = new JButton(CONFIRM_PURCHASE);
        confirmarCompraButton.addActionListener(e -> {
            try {
                double total = ventaMesaManager.getTotalCartAmount(); // Obtiene el total de la compra
                double dineroRecibido = 0;
                double devuelto = 0;



                // Generar un ID único para la venta
                String ventaID = String.valueOf(System.currentTimeMillis() % 1000);
                LocalDateTime dateTime = LocalDateTime.now();

                // Obtener la lista de productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities(); // Método que debes implementar

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
                    ventaMesaManager.generarFactura(ventaID, Collections.singletonList(String.valueOf(listaProductosEnLinea)), total, dateTime);
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


    private static JButton createSavePurchaseMesaButton(VentaMesaManager ventaMesaManager) {
        JButton saveCompraButton = new JButton("Guardar Compra");
        saveCompraButton.addActionListener(e -> {
            try {
                double total = ventaMesaManager.getTotalCartAmount(); // Obtener el total de la compra
                LocalDateTime dateTime = LocalDateTime.now();

                // Obtener los productos comprados y sus cantidades
                Map<String, Integer> productosComprados = ventaMesaManager.getProductListWithQuantities();
                StringBuilder listaProductos = new StringBuilder();

                // Construir la lista de productos con cantidad
                for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                    String nombreProducto = entry.getKey();
                    int cantidad = entry.getValue();
                    Producto producto = productoManager.getProductByName(nombreProducto);
                    double precioUnitario = producto.getPrice();
                    double precioTotal = precioUnitario * cantidad;

                    listaProductos.append(nombreProducto)
                            .append(" x")
                            .append(cantidad)
                            .append(" $")
                            .append(precioUnitario)
                            .append(" = ")
                            .append(precioTotal)
                            .append("\n");
                }

                // Guardar la compra en la pestaña "mesas"
                try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
                     Workbook workbook = WorkbookFactory.create(fis)) {

                    Sheet mesasSheet = workbook.getSheet("mesas");
                    if (mesasSheet != null) {
                        for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                            Row row = mesasSheet.getRow(i);
                            if (row != null && row.getCell(0).getStringCellValue().equalsIgnoreCase("1")) {
                                // Cambiar estado a "Ocupada" y guardar la compra
                                row.getCell(1).setCellValue("Ocupada");
                                row.createCell(2).setCellValue(listaProductos.toString());
                                row.createCell(3).setCellValue(total);
                                break;
                            }
                        }

                        try (FileOutputStream fos = new FileOutputStream(ExcelManager.FILE_PATH)) {
                            workbook.write(fos);
                        }

                        JOptionPane.showMessageDialog(null, "Compra guardada para la " + "1" + ".");
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

    public void reanudarCompraMesa(String mesaID) {
        try (FileInputStream fis = new FileInputStream(ExcelManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("mesas");
            if (mesasSheet != null) {
                for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null && row.getCell(0).getStringCellValue().equalsIgnoreCase(mesaID)) {
                        String estado = row.getCell(1).getStringCellValue();
                        if (estado.equals("Ocupada")) {
                            String productosGuardados = row.getCell(2).getStringCellValue();
                            double totalGuardado = row.getCell(3).getNumericCellValue();
                            // Restaurar productos y total en la interfaz
                            System.out.println("Productos: " + productosGuardados);
                            System.out.println("Total: " + totalGuardado);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
