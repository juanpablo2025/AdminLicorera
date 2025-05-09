package org.example.ui.uiAdmin;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.adminManager.ConfigAdminManager;
import org.example.ui.uiUser.UIUserMain;
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
import static org.example.ui.uiAdmin.UIAdminFacturas.getAdminBillsPanel;
import static org.example.ui.uiAdmin.UIAdminProducts.getAdminProductListPanel;
import static org.example.ui.uiAdmin.UIConfigAdmin.createPrinterConfigPanel;
import static org.example.ui.uiUser.UIUserMain.*;

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

            JPanel sidebarPanel = new JPanel();
            sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
            sidebarPanel.add(Box.createVerticalStrut(5));

            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(getAdminProductListPanel(), "productos");
            contentPanel.add(getAdminBillsPanel(), "facturas");
            contentPanel.add(createPrinterConfigPanel(), "configuracion");

            JLabel titleLabel = new JLabel("Panel de Administración", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
            titleLabel.setForeground(new Color(50, 50, 50));

            // Panel de botones
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            buttonPanel.setBackground(new Color(245, 245, 245));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 50));

            // Panel superior con logo y nombre del empleado centrados
            JPanel logoPanel = new JPanel();
            logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
            logoPanel.setBackground(fondoPrincipal);

            ImageIcon logoIcon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            Image imgLogo = logoIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(imgLogo));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // **Añadir efectos al pasar el mouse**
            logoLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // **Obtener el CardLayout y mostrar el panel "mesas"**
                    CardLayout layout = (CardLayout) contentPanel.getLayout();
                    layout.show(contentPanel, "productos");
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el cursor a "mano"
                    logoLabel.setBorder(BorderFactory.createLineBorder(new Color(250, 240, 230), 1)); // Borde amarillo al pasar el mouse
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    logoLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Restaurar cursor normal
                    logoLabel.setBorder(null); // Eliminar borde al salir
                }
            });

            JLabel employeeLabel = new JLabel("Administrador");
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream("Pacifico-Regular.ttf");
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);
                employeeLabel.setFont(customFont);
            } catch (Exception e) {
                e.printStackTrace();
            }

            sidebarPanel.add(employeeLabel, BorderLayout.NORTH);

            logoPanel.add(Box.createVerticalStrut(1));
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(1));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(1));
            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
           // buttonsPanel.setBackground(new Color(200, 200, 200));

            // Agregar ComponentListener para cambiar tamaño dinámicamente
            sidebarPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = sidebarPanel.getWidth();
                    int panelHeight = sidebarPanel.getHeight();

                    int buttonWidth = (int) (panelWidth * 1.0);  // 90% del ancho del sidebar
                    int buttonHeight = (int) (panelHeight * 0.13);  // 12% del alto del sidebar

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

            Dimension buttonSize = new Dimension(100, 60);

            JButton listaProductosButton = createButton("Inventario", resizeIcon("/icons/lista-de_productos.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "productos");
            });
            listaProductosButton.setMaximumSize(buttonSize);
            JButton gastosButton = createButton("Facturas", resizeIcon("/icons/Facturar.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "facturas");
            });
            gastosButton.setMaximumSize(buttonSize);
            JButton configButton = createButton("Configuración", resizeIcon("/icons/obrero.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "configuracion");

            });
            configButton.setMaximumSize(buttonSize);

            JButton moreOptionsButton = createButton("Mesas", resizeIcon("/icons/mesa-redonda.png"), e -> {
                if (hayRegistroDeHoy()) {
                    mainUser();
                } else {
                    mostrarLogin();
                }
                frame.dispose();
            });
            moreOptionsButton.setMaximumSize(buttonSize);

            moreOptionsButton.setFont(new Font("Arial", Font.BOLD, 16));
            //moreOptionsButton.setPreferredSize(new Dimension(150, 50));

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
            }
        }
    }

    public static double obtenerTRM() {

        if (!ConfigAdminManager.isTrmEnabled()) return 0.0;
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

