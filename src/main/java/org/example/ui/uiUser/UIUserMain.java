package org.example.ui.uiUser;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userManager.FacturacionUserManager;

import javax.swing.*;
import java.awt.*;

import static org.example.manager.userManager.ExcelUserManager.eliminarMesasConIdMayorA10;
import static org.example.manager.userManager.PrintUserManager.abrirCajaRegistradora;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.ui.uiUser.UIUserFacturas.showFacturasDialog;
import static org.example.ui.uiUser.UIUserGastos.showGastosGeneralesDialog;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiUser.UIUserMesas.showMesas;
import static org.example.ui.uiUser.UIUserProductList.showListProductsDialog;

public class UIUserMain {
    // Estado para alternar entre los botones originales y las opciones adicionales
    private static boolean showingMoreOptions = false;

    private static String nombreEmpleado = "Empleado";


    public static void mainUser() {
        try {
            // Aplicar FlatLaf para un estilo moderno
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Crear ventana principal
            JFrame frame = new JFrame("Calculadora Administrador");
            frame.setSize(1280, 720); // Tamaño más grande para incluir la barra lateral
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Crear un panel principal con un fondo gris claro
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245)); // Fondo gris claro

            // Crear el título estilizado para la parte superior
            JLabel titleLabel = new JLabel("Ventas", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Estilo y tamaño de la fuente
            titleLabel.setForeground(new Color(50, 50, 50)); // Color del texto

            // Panel central con botones 2x2
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Grid 2x2
            buttonPanel.setBackground(new Color(245, 245, 245)); // Fondo igual al principal
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen

            // Crear y añadir los botones iniciales
            addInitialButtons(buttonPanel, frame);

            // Crear barra lateral con el nombre del empleado y el botón "Opciones" al final
            JPanel sidebarPanel = new JPanel(new BorderLayout());
            sidebarPanel.setPreferredSize(new Dimension(250, 600)); // Ancho fijo para la barra lateral
            sidebarPanel.setBackground(new Color(235, 235, 235)); // Color gris claro para la barra lateral

            // Nombre del empleado en la parte superior de la barra lateral
            JLabel employeeLabel = new JLabel(nombreEmpleado, JLabel.CENTER);
            employeeLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Estilo y tamaño de la fuente
            employeeLabel.setForeground(new Color(50, 50, 50)); // Color del texto
            sidebarPanel.add(employeeLabel, BorderLayout.NORTH); // Colocar en la parte superior

            // Botón "Más Opciones"
            JButton moreOptionsButton = createButton("Opciones", e -> toggleButtonOptions(buttonPanel, frame));
            moreOptionsButton.setFont(new Font("Arial", Font.BOLD, 16));
            moreOptionsButton.setPreferredSize(new Dimension(150, 50));

            // Colocar el botón "Opciones" en la parte inferior de la barra lateral
            sidebarPanel.add(moreOptionsButton, BorderLayout.SOUTH);

            // Añadir el panel de botones y la barra lateral al panel principal
            mainPanel.add(titleLabel, BorderLayout.NORTH);
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

    // Método para agregar los botones iniciales
    private static void addInitialButtons(JPanel buttonPanel, JFrame frame) {
        FacturacionUserManager facturacionUserManager = new FacturacionUserManager(); // Instancia de FacturacionManager

        JButton adminProductosButton = createButton("Listar Productos", e -> {showListProductsDialog();frame.dispose();});
        JButton gastosButton = createButton("Gastos", e ->{ showGastosGeneralesDialog(); frame.dispose();});
        JButton mesasButton = createButton("Mesas", e ->{ showMesas();frame.dispose();});
        JButton salirButton = createButton("Facturar", e -> {
            String input = JOptionPane.showInputDialog(null, "Por favor escribe 'Facturar'", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);
            if (facturacionUserManager.verificarFacturacion(input)) {
                facturacionUserManager.facturarYSalir();
                eliminarMesasConIdMayorA10();

            } else {
                facturacionUserManager.mostrarErrorFacturacion();
            }
        });

        // Añadir botones al panel de botones
        buttonPanel.add(mesasButton);
        buttonPanel.add(adminProductosButton);
        buttonPanel.add(gastosButton);
        buttonPanel.add(salirButton);
    }

    // Método para alternar entre los botones iniciales y las opciones adicionales
    private static void toggleButtonOptions(JPanel buttonPanel, JFrame frame) {
        // Limpiar el panel de botones actual
        buttonPanel.removeAll();

        if (!showingMoreOptions) {
            // Mostrar las nuevas opciones "Facturas" y "Administrador"
            JButton facturasButton = createButton("Re-Imprimir Facturas", e ->{ showFacturasDialog();frame.dispose();});
            JButton adminButton = createButton("Administrador", e -> {
                mainAdmin();  // Abre la nueva ventana de administración
                frame.dispose();  // Cierra la ventana actual
            });
            JButton abrirCajaButton = createButton("Abrir Caja", e -> abrirCajaRegistradora());

            buttonPanel.add(abrirCajaButton);
            buttonPanel.add(facturasButton);
            buttonPanel.add(adminButton);
        } else {
            // Restaurar los botones iniciales
            addInitialButtons(buttonPanel,frame);
        }

        // Cambiar el estado de los botones
        showingMoreOptions = !showingMoreOptions;

        // Refrescar el panel para mostrar los cambios
        buttonPanel.revalidate();
        buttonPanel.repaint();

        // Actualizar el frame para reflejar los cambios visuales
        frame.repaint();
    }

}
