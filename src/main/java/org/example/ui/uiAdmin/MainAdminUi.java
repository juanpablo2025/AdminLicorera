package org.example.ui.uiAdmin;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static org.example.Main.mostrarLogin;
import static org.example.manager.userManager.ExcelUserManager.hayRegistroDeHoy;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiAdmin.GastosAdminUI.showReabastecimientoDialog;
import static org.example.ui.uiAdmin.UIAdminProducts.showProductosDialog;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.ui.uiUser.UIUserMesas.showMesas;

public class MainAdminUi {




    public static void mainAdmin() {



        // Crear un campo de contraseña
        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar('*'); // Usar '*' como carácter para ocultar el texto

        // Mostrar el cuadro de diálogo personalizado con el campo de contraseña
        int option = JOptionPane.showConfirmDialog(null, passwordField, "Ingrese la contraseña del Administrador:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        // Obtener la contraseña ingresada
        if (option == JOptionPane.OK_OPTION) {
            String inputPassword = new String(passwordField.getPassword());

            // Verificar si la contraseña es correcta
            if (inputPassword.equals("admin2024")) {
                // Si la contraseña es correcta, mostrar el panel de administrador
                showAdminPanel();
            } else {
                // Mostrar un mensaje de error si la contraseña es incorrecta
                JOptionPane.showMessageDialog(null, "Contraseña incorrecta. Acceso denegado.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                if (hayRegistroDeHoy()) {
                    mainUser(); // Si hay registro, abrir el panel de usuario
                } else {
                    mostrarLogin(); // Si no, mostrar el login
                }
            }
        }

}

    private static void showAdminPanel() {
        try {
            // Aplicar FlatLaf para un estilo moderno
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Crear ventana principal
            JFrame frame = new JFrame("Administrador del Inventario");
            frame.setSize(1280, 720); // Tamaño más grande para incluir la barra lateral


            // Añadir un WindowListener para detectar el cierre de la ventana
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {


                    if (hayRegistroDeHoy()) {
                        mainUser(); // Si hay registro, abrir el panel de usuario
                    } else {
                        mostrarLogin(); // Si no, mostrar el login
                    }


                    // Volver a la ventana principal al cerrar
                }
            });

            // Crear un panel principal con un fondo gris claro
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245)); // Fondo gris claro

            // Crear el título estilizado a la izquierda, fuera de la barra lateral
            JLabel titleLabel = new JLabel("Panel de Administración", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Estilo y tamaño de la fuente
            titleLabel.setForeground(new Color(50, 50, 50)); // Color del texto




            // Panel central con botones 2x2
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Grid 2x2
            buttonPanel.setBackground(new Color(245, 245, 245)); // Fondo igual al principal
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Márgenes


            ImageIcon originalreabastecerIcon = new ImageIcon("src/main/java/org/example/utils/icons/admin/transaccion.png");
            // Redimensionar la imagen
            Image imgreabastecer = originalreabastecerIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon reabastecerIcon = new ImageIcon(imgreabastecer);



            // Crear botones estilizados
            JButton reabastecerButton = createButton("Reabastecer",reabastecerIcon, e -> {
                showReabastecimientoDialog();
                frame.dispose();
            });
            reabastecerButton.setFont(new Font("Arial", Font.BOLD, 18));


            ImageIcon originalProductosIcon = new ImageIcon("src/main/java/org/example/utils/icons/admin/menu.png");
            // Redimensionar la imagen
            Image imgProductos = originalProductosIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon productosIcon = new ImageIcon(imgProductos);


            JButton productosButton = createButton("Productos", productosIcon,e -> {
                showProductosDialog();
                frame.dispose();
            });
            productosButton.setFont(new Font("Arial", Font.BOLD, 18));


            ImageIcon originalfacturasIcon = new ImageIcon("src/main/java/org/example/utils/icons/admin/beneficios.png");
            // Redimensionar la imagen
            Image imgfacturas = originalfacturasIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon facturasIcon = new ImageIcon(imgfacturas);

            JButton facturasButton = createButton("Eliminar Facturas",facturasIcon, e -> {
                UIAdminFacturas.showFacturasDialog();
                frame.dispose();
            });
            facturasButton.setFont(new Font("Arial", Font.BOLD, 18));

            // Añadir botones al panel de botones
            buttonPanel.add(reabastecerButton);
            buttonPanel.add(productosButton);
            buttonPanel.add(facturasButton);

            // Crear barra lateral con botón "Ventas"
            JPanel sidebarPanel = new JPanel(new BorderLayout());
            sidebarPanel.setPreferredSize(new Dimension(250, 600)); // Ancho fijo para la barra lateral
            sidebarPanel.setBackground(new Color(235, 235, 235)); // Color gris claro para la barra lateral

// Nombre del empleado en la parte superior de la barra lateral
            JLabel employeeLabel = new JLabel("Administrador", JLabel.CENTER);
            employeeLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Estilo y tamaño de la fuente
            employeeLabel.setForeground(new Color(50, 50, 50)); // Color del texto
            sidebarPanel.add(employeeLabel, BorderLayout.NORTH); // Colocar en la parte superior

            JButton moreOptionsButton = new JButton("Ventas");

            // Botón para volver a la ventana de Ventas
            moreOptionsButton.addActionListener( e -> {
                if (hayRegistroDeHoy()) {
                    mainUser(); // Si hay registro, abrir el panel de usuario
                } else {
                    mostrarLogin(); // Si no, mostrar el login
                }
                frame.dispose(); // Cerrar ventana de Admin
            });
            moreOptionsButton.setFont(new Font("Arial", Font.BOLD, 16));
            moreOptionsButton.setPreferredSize(new Dimension(150, 50));

            // Añadir botón a la barra lateral
            sidebarPanel.add(moreOptionsButton, BorderLayout.SOUTH); // Ubicado en la parte superior

            // Añadir el título y el panel de botones al panel principal
            mainPanel.add(titleLabel, BorderLayout.NORTH); // Añadir el título en la parte superior
            mainPanel.add(buttonPanel, BorderLayout.CENTER); // Panel de botones al centro
            mainPanel.add(sidebarPanel, BorderLayout.WEST);  // Barra lateral a la izquierda

            // Añadir el panel principal a la ventana
            frame.add(mainPanel);

            // Centrar la ventana
            frame.setLocationRelativeTo(null);

            // Mostrar la ventana principal
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

