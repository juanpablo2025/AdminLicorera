package org.example;

import org.example.manager.*;
import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.example.ui.UIHelpers.*;
import static org.example.utils.Constants.*;

public class Main {
    private static ProductoManager productoManager = new ProductoManager();


    private static JDialog ventaDialog;
// eliminar botones

    public static void main(String[] args) {
        crearDirectorios();
        JFrame frame = new JFrame(CALCULADORA_ADMINISTRADOR);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Cambiar a DO_NOTHING_ON_CLOSE
        frame.setUndecorated(true); // Quitar los decorados de la ventana
        frame.setSize(FOUR_HUNDRED, FOUR_HUNDRED);
        frame.setLayout(new GridLayout(TWO, ONE));


        FacturacionManager facturacionManager = new FacturacionManager(); // Instancia de FacturacionManager

        // Botones principales
        JButton ventaButton = UIHelpers.createButton(VENTA, e -> showVentaDialog());
        JButton adminProductosButton = UIHelpers.createButton(LISTAR_PRODUCTOS, e -> showListProductsDialog());
        JButton gastosButton = UIHelpers.createButton("Gastos", e -> showGastosDialog());
        JButton mesasButton = UIHelpers.createButton("Administrar Mesas", e -> showMesas()); // Nuevo botón para administrar mesas

        JButton salirButton = UIHelpers.createButton(SALIR_FACTURAR, e -> {
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
        //frame.add(mesasButton); // Añadir el botón de mesas

        frame.add(ventaButton);
        frame.add(adminProductosButton);
        frame.add(gastosButton);
        frame.add(salirButton);

        // Centrar el frame en la pantalla
        frame.setLocationRelativeTo(null);

        // Mostrar la ventana principal
        frame.setVisible(true);
    }
    private static void showMesas() {
        JFrame mesasFrame = new JFrame("Administrar Mesas");
        mesasFrame.setSize(500, 500);
        mesasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana

        // Panel para mostrar las mesas
        JPanel mesasPanel = new JPanel();
        mesasPanel.setLayout(new GridLayout(0, 5)); // Mostrar mesas en filas de 5

        ArrayList<JPanel> mesas = new ArrayList<>();

        // Inicializar 10 mesas por defecto
        for (int i = 0; i < 10; i++) {
            mesas.add(crearMesaPanel(false)); // false indica que la mesa está libre
        }

        // Agregar las mesas al panel
        for (JPanel mesa : mesas) {
            mesasPanel.add(mesa);
        }

        // Botón para añadir más mesas
        JButton addMesaButton = new JButton("Añadir Mesa");
        addMesaButton.addActionListener(e -> {
            JPanel nuevaMesa = crearMesaPanel(false); // Crear mesa libre
            mesas.add(nuevaMesa);
            mesasPanel.add(nuevaMesa);
            mesasPanel.revalidate(); // Refrescar el panel para mostrar la nueva mesa
        });

        // Botón para cambiar el estado de las mesas (Ocupar/Desocupar)
        JButton toggleMesaButton = new JButton("Cambiar Estado Mesas");
        toggleMesaButton.addActionListener(e -> cambiarEstadoMesas(mesas));

        // Panel inferior con los botones
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);
        bottomPanel.add(toggleMesaButton);

        // Agregar todo al frame de mesas
        mesasFrame.setLayout(new BorderLayout());
        mesasFrame.add(mesasPanel, BorderLayout.CENTER);
        mesasFrame.add(bottomPanel, BorderLayout.SOUTH);

        // Mostrar la ventana
        mesasFrame.setLocationRelativeTo(null);
        mesasFrame.setVisible(true);
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
        JDialog listProductsDialog = UIHelpers.createDialog(LISTAR_PRODUCTO, FOUR_HUNDRED, THREE_HUNDRED, new BorderLayout());

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
        JButton closeButton = UIHelpers.createButton(CLOSE_BUTTON, e -> listProductsDialog.dispose());
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
        ventaDialog = UIHelpers.createDialog(REALIZAR_VENTA, FIVE_HUNDRED, FOUR_HUNDRED, new BorderLayout());

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

        String facturasPath = documentosPath + "\\Facturas";
        String realizadoPath = documentosPath + "\\Realizo";

        crearDirectorioSiNoExiste(facturasPath);
        crearDirectorioSiNoExiste(realizadoPath);
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
        JDialog gastosDialog = UIHelpers.createDialog("Registrar Gastos", 300, 200, new GridLayout(2, 1));

        JButton reabastecimientoButton = UIHelpers.createButton("Reabastecimiento", e -> showReabastecimientoDialog());
        JButton gastosGeneralesButton = UIHelpers.createButton("Gastos Generales", e -> showGastosGeneralesDialog());

        gastosDialog.add(reabastecimientoButton);
        gastosDialog.add(gastosGeneralesButton);

        gastosDialog.setLocationRelativeTo(null);
        gastosDialog.setVisible(true);
    }
    private static void showReabastecimientoDialog() {
        JDialog reabastecimientoDialog = UIHelpers.createDialog("Reabastecimiento de Productos", 300, 250, new GridLayout(4, 2));

        JComboBox<String> productComboBox = new JComboBox<>();
        List<Producto> productos = productoManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }

        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Campo para ingresar el precio de la compra
        JTextField precioCompraField = new JTextField();

        JButton confirmarReabastecimientoButton = UIHelpers.createButton("Confirmar", e -> {
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
        JDialog gastosGeneralesDialog = UIHelpers.createDialog("Registrar Gastos Generales", 300, 150, new GridLayout(3, 2));

        JTextField nombreGastoField = new JTextField(); // Campo para la descripción del gasto
        JTextField precioField = new JTextField();      // Campo para el precio del gasto

        JButton confirmarGastoButton = UIHelpers.createButton("Confirmar", e -> {
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
}
