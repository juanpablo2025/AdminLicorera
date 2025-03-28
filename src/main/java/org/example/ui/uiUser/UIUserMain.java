package org.example.ui.uiUser;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userManager.ExcelUserManager;
import org.example.manager.userManager.FacturacionUserManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static org.example.ui.uiAdmin.MainAdminUi.*;
import static org.example.ui.uiUser.UIUserFacturas.showFacturasDialog;
import static org.example.ui.uiUser.UIUserGastos.showGastosGeneralesDialog;
import static org.example.ui.UIHelpers.createButton;
//import static org.example.ui.uiUser.UIUserMesas.showMesas;
import static org.example.ui.uiUser.UIUserMesas.showPanelMesas;
import static org.example.ui.uiUser.UIUserProductList.showListProductsDialog;

public class UIUserMain {
    private static boolean showingMoreOptions = false;
    private static String nombreEmpleado = ExcelUserManager.obtenerUltimoEmpleado().toUpperCase();
    private static Color fondoPrincipal = new Color(250, 240, 230);
    private static JFrame mainFrame; // Almacenar el frame principal

    public static void mainUser() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - Licorera CR");
            // Cargar el icono desde un archivo (Asegúrate de que la ruta es correcta)
// Cargar el icono desde el classpath (Asegúrate de que el archivo está en /resources/icons/)
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            if (icon.getImage() != null) {
                // Redimensionar la imagen a 64x64 píxeles (ajusta según necesidad)
                Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                frame.setIconImage(scaledImage);
            } else {
                System.out.println("⚠ Error: No se encontró el icono. Verifica la ruta.");
            }

            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245));

            JPanel sidebarPanel = new JPanel(new BorderLayout());
            sidebarPanel.setPreferredSize(new Dimension(260, frame.getHeight()));
            sidebarPanel.setBackground(fondoPrincipal);

            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.setBackground(fondoPrincipal);

            ImageIcon logoIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png")));
            Image imgLogo = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(imgLogo));

            JLabel employeeLabel = new JLabel(nombreEmpleado);
            employeeLabel.setFont(new Font("Arial", Font.BOLD, 30));
            employeeLabel.setForeground(Color.DARK_GRAY);

            headerPanel.add(logoLabel);
            headerPanel.add(employeeLabel);
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
            separator.setForeground(new Color(200, 170, 100));
            sidebarPanel.add(headerPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setBackground(fondoPrincipal);

            // Agregar ComponentListener para cambiar tamaño dinámicamente
            sidebarPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = sidebarPanel.getWidth();
                    int panelHeight = sidebarPanel.getHeight();

                    int buttonWidth = (int) (panelWidth * 0.9);  // 90% del ancho del sidebar
                    int buttonHeight = (int) (panelHeight * 0.15); // 12% del alto del sidebar

                    Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);

                    // Ajustar tamaño de todos los botones dentro de buttonsPanel
                    for (Component comp : buttonsPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            comp.setPreferredSize(buttonSize);
                            comp.setMaximumSize(buttonSize);
                            comp.setMinimumSize(buttonSize);
                        }
                    }

                    // Forzar revalidación y repintado
                    buttonsPanel.revalidate();
                    buttonsPanel.repaint();
                }
            });

            Dimension buttonSize = new Dimension(200, 70);

            JButton listaProductosButton = createButton("Lista de Productos", resizeIcon("/icons/lista-de_productos.png"), e -> {
                showListProductsDialog();
                frame.dispose();
            });
            listaProductosButton.setMaximumSize(buttonSize);

            JButton gastosButton = createButton("Gastos", resizeIcon("/icons/Gastos.png"), e -> {
                showGastosGeneralesDialog();
                frame.dispose();
            });
            gastosButton.setMaximumSize(buttonSize);

            JButton salirButton = createButton("Finalizar Día", resizeIcon("/icons/Facturar.png"), e -> {
                String input = JOptionPane.showInputDialog(null, "Por favor escribe 'Facturar'", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);
                if (FacturacionUserManager.verificarFacturacion(input)) {
                    FacturacionUserManager.facturarYSalir();
                } else {
                    FacturacionUserManager.mostrarErrorFacturacion();
                }
            });
            salirButton.setMaximumSize(buttonSize);

            JButton moreOptionsButton = createButton("Ventas Realizadas", resizeIcon("/icons/admin/beneficios.png"), e -> {
                showFacturasDialog();
                frame.dispose();
            });
            moreOptionsButton.setMaximumSize(buttonSize);

            JButton moreOptionsButtons = createButton("Administrador", resizeIcon("/icons/obrero.png"), e -> {
                adminPassword();
                frame.dispose();
            });
            moreOptionsButtons.setMaximumSize(buttonSize);

            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(listaProductosButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(gastosButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(salirButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(moreOptionsButtons);
            buttonsPanel.add(Box.createVerticalGlue());

            sidebarPanel.add(buttonsPanel, BorderLayout.CENTER);
            mainPanel.add(sidebarPanel, BorderLayout.WEST);
            mainPanel.add(showPanelMesas(frame), BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static ImageIcon resizeIcon(String path) {
        ImageIcon icon = new ImageIcon(UIUserMain.class.getResource(path));
        Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}

   /* // Método para agregar los botones iniciales
    private static void addInitialButtons(JPanel buttonPanel, JFrame frame) {
        FacturacionUserManager facturacionUserManager = new FacturacionUserManager(); // Instancia de FacturacionManager
        // Cargar la imagen desde la ruta especificada
        ImageIcon originalIcon = new ImageIcon((UIUserMain.class.getResource("/icons/mesa-redonda.png")));
        ImageIcon originalProductsIcon = new ImageIcon((UIUserMain.class.getResource("/icons/lista-de_productos.png")));
        // Redimensionar la imagen
        Image imgProduct = originalProductsIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon productsIcon = new ImageIcon(imgProduct);

        JButton adminProductosButton = createButton("Lista de Productos", productsIcon, e -> { showListProductsDialog(); frame.dispose(); });



        ImageIcon originalGastosIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Gastos.png")));
        // Redimensionar la imagen
        Image imgGastos = originalGastosIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon GastosIcon = new ImageIcon(imgGastos);

        JButton gastosButton = createButton("Gastos", GastosIcon, e -> { showGastosGeneralesDialog(); frame.dispose(); });


        // Redimensionar la imagen
        Image img = originalIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon mesaIcon = new ImageIcon(img);
        JButton mesasButton = createButton("Mesas",mesaIcon , e -> { mainUser(); frame.dispose(); });

        ImageIcon originalFacturarIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Facturar.png")));

        // Redimensionar la imagen
        Image imgFacturar = originalFacturarIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon FacturarIcon = new ImageIcon(imgFacturar);


        JButton salirButton = createButton("Facturar", FacturarIcon, e -> {
            String input = JOptionPane.showInputDialog(null, "Por favor escribe 'Facturar'", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);
            if (facturacionUserManager.verificarFacturacion(input)) {
                facturacionUserManager.facturarYSalir();

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


            ImageIcon originalfacturasIcon = new ImageIcon((UIUserMain.class.getResource("/icons/admin/beneficios.png")));
            // Redimensionar la imagen
            Image imgfacturas = originalfacturasIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon facturasIcon = new ImageIcon(imgfacturas);
            // Mostrar las nuevas opciones "Facturas" y "Administrador"
            // Crear los botones utilizando createButton y pasando null para el ícono
            JButton facturasButton = createButton("Re-Imprimir Facturas", facturasIcon, e -> {
                showFacturasDialog();
                frame.dispose();
            });


            ImageIcon originalAdminIcon = new ImageIcon(UIUserMain.class.getResource("/icons/obrero.png"));
            // Redimensionar la imagen
            Image imgAdmin = originalAdminIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon AdminIcon = new ImageIcon(imgAdmin);



            JButton adminButton = createButton("Administrador", AdminIcon, e -> {

                adminPassword();
                frame.dispose();  // Cierra la ventana actual
            });

            ImageIcon originalCajaRegistradoraIcon = new ImageIcon((UIUserMain.class.getResource("/icons/admin/cajero-automatico.png")));
            // Redimensionar la imagen
            Image imgCajaRegistradora = originalCajaRegistradoraIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon CajaRegistradoraIcon = new ImageIcon(imgCajaRegistradora);


            JButton abrirCajaButton = createButton("Abrir Caja", CajaRegistradoraIcon, e -> abrirCajaRegistradora());

            // Agregar los botones al panel (si es necesario)
            buttonPanel.add(facturasButton);
            buttonPanel.add(adminButton);
            buttonPanel.add(abrirCajaButton);


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
    }*/


