package org.example.ui.uiAdmin;

import org.example.manager.adminManager.GastosAdminManager;
import org.example.manager.adminManager.ProductoAdminManager;
import org.example.model.Producto;
import org.example.ui.uiUser.UIUserMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;

public class GastosAdminUI {
    static ProductoAdminManager productoAdminManager = new ProductoAdminManager();

    public static void showReabastecimientoDialog() {
        JDialog reabastecimientoDialog = createDialog("Reabastecimiento de Productos", 500, 300, new GridLayout(4, 2));
        reabastecimientoDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainAdmin() ; // Llamada a showMesas cuando se cierra la ventana
            }
        });
        JComboBox<String> productComboBox = new JComboBox<>();
        List<Producto> productos = productoAdminManager.getProducts();
        for (Producto producto : productos) {
            productComboBox.addItem(producto.getName());
        }

        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Campo para ingresar el precio de la compra
        JTextField precioCompraField = new JTextField();


        // Crear el botón "Confirmar"
        JButton confirmarReabastecimientoButton = new JButton("Confirmar");

         confirmarReabastecimientoButton.addActionListener( e -> {
            try {
                String selectedProduct = (String) productComboBox.getSelectedItem();
                int cantidad = (int) cantidadSpinner.getValue();
                //double precioCompra = Double.parseDouble(precioCompraField.getText());
                String input = precioCompraField.getText(); // Por ejemplo, "3.500" o "3,500"


                input = input.replace(".", "");

                // Reemplazar la coma (si existe) por un punto para manejar decimales correctamente
                input = input.replace(",", "");

                // Convertir el input limpio a un double
                double precio = Double.parseDouble(input);





                Producto producto = productoAdminManager.getProductByName(selectedProduct);

                // Guardar el reabastecimiento en Excel sin hacer operaciones adicionales
                GastosAdminManager gastosAdminManager = new GastosAdminManager();
                gastosAdminManager.reabastecerProducto(producto, cantidad, precio);

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(null, "Producto reabastecido correctamente.");

                reabastecimientoDialog.dispose();
                MainAdminUi.mainAdmin(); // Volver a la ventana principal de usuario

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(reabastecimientoDialog, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(reabastecimientoDialog, "Ocurrió un error durante el reabastecimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        reabastecimientoDialog.add(new JLabel("PRODUCTO:"));
        productComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
        reabastecimientoDialog.add(productComboBox);
        reabastecimientoDialog.add(new JLabel("CANTIDAD:"));
        cantidadSpinner.setFont(new Font("Arial", Font.PLAIN, 18));
        reabastecimientoDialog.add(cantidadSpinner);
        reabastecimientoDialog.add(new JLabel("PRECIO TOTAL DE LA COMPRA:"));
        precioCompraField.setFont(new Font("Arial", Font.PLAIN, 18));
        reabastecimientoDialog.add(precioCompraField);  // Añadir el campo de texto para el precio
        reabastecimientoDialog.add(confirmarReabastecimientoButton);
        confirmarReabastecimientoButton.setFont(new Font("Arial", Font.BOLD, 18));

        reabastecimientoDialog.setLocationRelativeTo(null);
        reabastecimientoDialog.setVisible(true);
    }


}
