package org.example.ui.uiAdmin;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.ui.uiUser.UIUserGastos;
import org.example.ui.uiUser.UIUserMain;
import org.example.ui.uiUser.UIUserProductList;
import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.Main.mostrarLogin;
import static org.example.manager.userManager.ExcelUserManager.hayRegistroDeHoy;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.setMainFrame;
import static org.example.ui.uiAdmin.GastosAdminUI.showReabastecimientoDialog;
import static org.example.ui.uiAdmin.UIAdminFacturas.getAdminBillsPanel;
import static org.example.ui.uiAdmin.UIAdminProducts.getAdminProductListPanel;
import static org.example.ui.uiAdmin.UIConfigAdmin.showConfigDialog;
import static org.example.ui.uiUser.UIUserFacturas.getFacturasPanel;
import static org.example.ui.uiUser.UIUserMain.*;
import static org.example.ui.uiUser.UIUserMesas.showPanelMesas;
//import static org.example.ui.uiUser.UIUserMesas.showMesas;

public class MainAdminUi {




    public static void mainAdmin() {







        showAdminPanel();

        }

    public static void showAdminPanel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - Licorera CR");
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            // Cargar icono de la aplicación
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            if (icon.getImage() != null) {
                frame.setIconImage(icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            } else {
                System.out.println("⚠ Error: No se encontró el icono. Verifica la ruta.");
            }

            // Listener para manejar cierre de ventana
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (hayRegistroDeHoy()) {
                        mainUser();
                    } else {
                        mostrarLogin();
                    }
                }
            });

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245));

            JPanel sidebarPanel = new JPanel(new BorderLayout());
            sidebarPanel.setPreferredSize(new Dimension(240, frame.getHeight()));
            sidebarPanel.setBackground(new Color(200, 200, 200));

            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(getAdminProductListPanel(), "productos");
            contentPanel.add(getAdminBillsPanel(), "facturas");
            //contentPanel.add(getAdminProductListPanel(), "configuracion");

            JLabel titleLabel = new JLabel("Panel de Administración", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
            titleLabel.setForeground(new Color(50, 50, 50));

            // Panel de botones
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            buttonPanel.setBackground(new Color(245, 245, 245));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 50));

            // Panel superior con logo
            JPanel logoPanel = new JPanel();
            logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
            logoPanel.setBackground(new Color(200, 200, 200));

            ImageIcon logoIcon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            Image imgLogo = logoIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(imgLogo));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            logoLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    CardLayout layout = (CardLayout) contentPanel.getLayout();
                    layout.show(contentPanel, "mesas");
                }
            });

            JLabel employeeLabel = new JLabel("Administrador");
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream("Pacifico-Regular.ttf");
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 32);
                employeeLabel.setFont(customFont);
            } catch (Exception e) {
                e.printStackTrace();
            }




            sidebarPanel.add(employeeLabel, BorderLayout.NORTH);



            logoPanel.add(Box.createVerticalStrut(10));
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(10));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(10));
            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setBackground(new Color(200, 200, 200));

            sidebarPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = sidebarPanel.getWidth();
                    int panelHeight = sidebarPanel.getHeight();
                    int buttonWidth = (int) (panelWidth * 0.9);
                    int buttonHeight = (int) (panelHeight * 0.14);
                    Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);

                    for (Component comp : buttonsPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            comp.setPreferredSize(buttonSize);
                            comp.setMaximumSize(buttonSize);
                            comp.setMinimumSize(buttonSize);
                        }
                    }
                    buttonsPanel.revalidate();
                    buttonsPanel.repaint();
                }
            });

            Dimension buttonSize = new Dimension(200, 70);
            JButton listaProductosButton = createButton("Lista de Productos", resizeIcon("/icons/lista-de_productos.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "productos");
            });
            JButton gastosButton = createButton("Facturas", resizeIcon("/icons/Facturar.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "facturas");
            });
            JButton configButton = createButton("Configuracion", resizeIcon("/icons/obrero.png"), e -> {
                showConfigDialog();
            });

            JButton moreOptionsButton = createButton("Mesas", resizeIcon("/icons/mesa-redonda.png"), e -> {
                if (hayRegistroDeHoy()) {
                    mainUser();
                } else {
                    mostrarLogin();
                }
                frame.dispose();
            });

            moreOptionsButton.setFont(new Font("Arial", Font.BOLD, 16));
            moreOptionsButton.setPreferredSize(new Dimension(150, 50));

            buttonsPanel.add(moreOptionsButton);

            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(listaProductosButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(gastosButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            buttonsPanel.add(configButton);
            buttonsPanel.add(Box.createVerticalStrut(5));
            /*buttonsPanel.add(moreOptionsButtons);
            buttonsPanel.add(Box.createVerticalGlue());*/

            sidebarPanel.add(buttonsPanel, BorderLayout.CENTER);
            mainPanel.add(sidebarPanel, BorderLayout.WEST);
            mainPanel.add(contentPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void adminPassword(Frame frame) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar('*'); // Oculta la contraseña con '*'

        // Mostrar cuadro de diálogo con opciones OK y CANCEL
        int option = JOptionPane.showConfirmDialog(null, passwordField,
                "Ingrese la contraseña del Administrador:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        // Si el usuario presionó "OK"
        if (option == JOptionPane.OK_OPTION) {
            String inputPassword = new String(passwordField.getPassword());

            if (inputPassword.equals("admin2024")) {
                showAdminPanel(); // Abrir panel de administrador si la contraseña es correcta
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Contraseña incorrecta. Acceso denegado.",
                        "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                redirigirUsuario();
            }
        }
    }

    // Método para manejar la redirección después de un intento fallido o cancelación
    private static void redirigirUsuario() {
        if (hayRegistroDeHoy()) {
            mainUser(); // Si hay registro, abrir el panel de usuario
        } else {
            mostrarLogin(); // Si no hay registro, mostrar la pantalla de login

        }
    }
    public static double obtenerTRM() {
        try {
            URL url = new URL("https://www.datos.gov.co/resource/32sa-8pi3.json?$limit=1&$order=vigenciadesde%20DESC");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) throw new IOException("HTTP Error");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                JSONArray arr = new JSONArray(response.toString());
                if (arr.length() == 0) return 0.0;
                return Double.parseDouble(arr.getJSONObject(0).getString("valor"));
            }
        } catch (Exception e) {
            return 0.0;
        }
    }
}

