package org.example.ui.uiUser;

import org.example.manager.userManager.GastosUserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.ui.uiUser.UIUserMesas.showMesas;

public class UIUserGastos {

    public static void showGastosGeneralesDialog() {
        JDialog gastosGeneralesDialog = createDialog("Registrar Gastos Generales", 500, 200, new GridLayout(3, 2));

        JTextField nombreGastoField = new JTextField(); // Campo para la descripción del gasto
        JTextField precioField = new JTextField();      // Campo para el precio del gasto
        // Añadir un WindowListener para detectar el cierre de la ventana
        gastosGeneralesDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainUser() ; // Llamada a showMesas cuando se cierra la ventana
            }
        });
        JButton confirmarGastoButton = createButton("CONFIRMAR", e -> {
            try {
                String nombreGasto = nombreGastoField.getText();
                String input = precioField.getText(); // Por ejemplo, "3.500" o "3,500"


                input = input.replace(".", "");

                // Reemplazar la coma (si existe) por un punto para manejar decimales correctamente
                input = input.replace(",", "");

                // Convertir el input limpio a un double
                double precio = Double.parseDouble(input);


                // Lógica para registrar el gasto en el Excel
                GastosUserManager.saveGasto(nombreGasto, 1, precio); // Implementar la lógica de guardado en el Excel, sin cantidad

                JOptionPane.showMessageDialog(null, "Gasto registrado correctamente.");
                gastosGeneralesDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el gasto.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        gastosGeneralesDialog.add(new JLabel("DESCRIPCIÓN O RAZÓN DEL GASTO:"));
        gastosGeneralesDialog.add(nombreGastoField);
        gastosGeneralesDialog.add(new JLabel("PRECIO:"));
        gastosGeneralesDialog.add(precioField);
        gastosGeneralesDialog.add(confirmarGastoButton);

        gastosGeneralesDialog.setLocationRelativeTo(null);
        gastosGeneralesDialog.setVisible(true);
    }
}
