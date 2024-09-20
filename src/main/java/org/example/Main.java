package org.example;

import org.example.manager.FacturacionManager;
import org.example.manager.VentaManager;
import org.example.manager.ProductoManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.example.ui.UIHelpers.*;
import static org.example.utils.Constants.*;

public class Main {
    private static ProductoManager productoManager = new ProductoManager();


    private static JDialog ventaDialog;
// eliminar botones

    public static void main(String[] args) {
        JFrame frame = new JFrame(CALCULADORA_ADMINISTRADOR);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(THREE, ONE));

        FacturacionManager facturacionManager = new FacturacionManager(); // Instancia de FacturacionManager

        // Botones principales
        JButton ventaButton = UIHelpers.createButton(VENTA, e -> showVentaDialog());
        JButton adminProductosButton = UIHelpers.createButton(ADMINISTRAR_PRODUCTOS, e -> showAdminProductosDialog());
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

        frame.add(ventaButton);
        frame.add(adminProductosButton);
        frame.add(salirButton);

        // Centrar el frame en la pantalla
        frame.setLocationRelativeTo(null);

        // Mostrar la ventana principal
        frame.setVisible(true);
    }

    private static void showListProductsDialog() {
        // Crear el diálogo
        JDialog listProductsDialog = UIHelpers.createDialog(LISTAR_PRODUCTO, 400, 300, new BorderLayout());

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
        JButton closeButton = UIHelpers.createButton(CERRAR_BUTTON, e -> listProductsDialog.dispose());
        listProductsDialog.add(closeButton, BorderLayout.SOUTH);

        // Mostrar el diálogo
        listProductsDialog.setVisible(true);
        listProductsDialog.setLocationRelativeTo(null);

    }

    private static void showAdminProductosDialog() {
        JDialog adminDialog = UIHelpers.createDialog( ADMINISTRAR_PRODUCTOS , 300, 200, new GridLayout(2, 1));

        // Botones
        JButton addButton = UIHelpers.createButton(AGREGAR_PRODUCTO, e -> showAddProductDialog());
        JButton listButton = UIHelpers.createButton(LISTAR_PRODUCTO, e -> showListProductsDialog());

        // Añadir botones al diálogo
        adminDialog.add(addButton);
        adminDialog.add(listButton);
        adminDialog.setLocationRelativeTo(null);
        adminDialog.setVisible(true);
    }

    private static void showAddProductDialog() {
        ProductoManager productoManager = new ProductoManager();
        JDialog addProductDialog = UIHelpers.createDialog(AGREGAR_PRODUCTO, 300, 200, new GridLayout(5, 2));

        // Crear los campos de entrada
        JTextField idField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_ID);
        JTextField nameField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_NOMBRE);
        JTextField quantityField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_CANTIDAD);
        JTextField priceField = productoManager.createField(addProductDialog, PRODUCTO_FIELD_PRECIO);

        // Botón para agregar el producto
        JButton addButton = UIHelpers.createButton(AGREGAR_BTN, e -> {
            productoManager.addProductFromFields(idField, nameField, quantityField, priceField, addProductDialog);
        });

        addProductDialog.add(addButton);
        addProductDialog.setVisible(true);
        addProductDialog.setLocationRelativeTo(null);
    }


    public static void showVentaDialog() {
        ventaDialog = UIHelpers.createDialog(REALIZAR_VENTA, 500, 400, new BorderLayout());

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

/*private static void showMesas() {
        // Lógica para el manejo de mesas
    }*/
}
