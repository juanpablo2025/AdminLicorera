package org.example.ui.uiadmin;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.adminmanager.ConfigAdminManager;
import org.example.ui.uiuser.UIUserMain;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import static org.example.Main.mostrarLogin;
import static org.example.manager.usermanager.ExcelUserManager.hayRegistroDeHoy;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiadmin.UIAdminFacturas.getAdminBillsPanel;
import static org.example.ui.uiadmin.UIAdminProducts.getAdminProductListPanel;
import static org.example.ui.uiadmin.UIConfigAdmin.createPrinterConfigPanel;
import static org.example.ui.uiuser.UIUserMain.*;
import static org.example.utils.Constants.*;

public class UIMainAdmin {

    private UIMainAdmin() {}

    private static final Logger logger =  LoggerFactory.getLogger(UIMainAdmin.class);

    public static void showAdminPanel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - "+EMPRESA_NAME);
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);

            ImageIcon icon = LOGO_EMPRESA;
            if (icon.getImage() != null) {
                frame.setIconImage(icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            }

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
            sidebarPanel.add(Box.createVerticalStrut(FIVE));

            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(getAdminProductListPanel(), PRODUCTOS);
            contentPanel.add(getAdminBillsPanel(), "facturas");
            contentPanel.add(createPrinterConfigPanel(), "configuration");

            JLabel titleLabel = new JLabel("Panel de Administración", SwingConstants.CENTER);            titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
            titleLabel.setForeground(new Color(FIFTY, FIFTY, FIFTY));

            JPanel buttonPanel = new JPanel(new GridLayout(TWO, TWO, TEN, TEN));
            buttonPanel.setBackground(new Color(245, 245, 245));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(TWENTY, FIFTY, TEN, FIFTY));

            JPanel logoPanel = new JPanel();
            logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
            logoPanel.setBackground(FONDO_PRINCIPAL);

            ImageIcon logoIcon = LOGO_EMPRESA;
            Image imgLogo = logoIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            JLabel logoLabel = getJLabel(imgLogo, contentPanel);

            JLabel employeeLabel = new JLabel("Administrador");
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream("Pacifico-Regular.ttf");
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);
                employeeLabel.setFont(customFont);
            } catch (Exception e) {
                logger.error("Error al cargar la fuente personalizada: ", e);
                employeeLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 26));
            }

            sidebarPanel.add(employeeLabel, BorderLayout.NORTH);

            logoPanel.add(Box.createVerticalStrut(ONE));
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(ONE));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(ONE));
            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

            sidebarPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = sidebarPanel.getWidth();
                    int panelHeight = sidebarPanel.getHeight();

                    int buttonWidth = (int) (panelWidth * 1.0);
                    int buttonHeight = (int) (panelHeight * 0.13);

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

            Dimension buttonSize = new Dimension(ONE_HUNDRED, 60);

            JButton listaProductosButton = createButton("Inventario", resizeIcon("/icons/lista-de_productos.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, PRODUCTOS);
            });
            listaProductosButton.setMaximumSize(buttonSize);
            JButton gastosButton = createButton(FACTURAS, resizeIcon("/icons/Facturar.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "facturas");
            });
            gastosButton.setMaximumSize(buttonSize);
            JButton configButton = createButton("Configuración", resizeIcon("/icons/obrero.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "configuration");

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

            moreOptionsButton.setFont(new Font("Segoe UI Variable", Font.BOLD, SIXTEEN));

            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(listaProductosButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(gastosButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(configButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));

            sidebarPanel.add(buttonsPanel, BorderLayout.CENTER);
            mainPanel.add(sidebarPanel, BorderLayout.WEST);
            mainPanel.add(contentPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            logger.error("Error al mostrar el panel de administración", ex);
            JOptionPane.showMessageDialog(null, "Error al cargar la interfaz de administración: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JLabel getJLabel(Image imgLogo, JPanel contentPanel) {
        JLabel logoLabel = new JLabel(new ImageIcon(imgLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CardLayout layout = (CardLayout) contentPanel.getLayout();
                layout.show(contentPanel, PRODUCTOS);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                logoLabel.setBorder(BorderFactory.createLineBorder(FONDO_PRINCIPAL, ONE));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                logoLabel.setBorder(null);
            }
        });
        return logoLabel;
    }

    public static void adminPassword(Frame frame) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar('*');


        int option = JOptionPane.showConfirmDialog(null, passwordField,
                "Ingrese la contraseña del Administrador:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String inputPassword = new String(passwordField.getPassword());

            if (inputPassword.equals("admin2024")) {
                showAdminPanel();
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Contraseña incorrecta. Acceso denegado.",
                        "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static double obtenerTRM() {

        if (!ConfigAdminManager.isTrmEnabled()) return ZERO_DOUBLE;
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
                if (arr.isEmpty()) return ZERO_DOUBLE;
                return Double.parseDouble(arr.getJSONObject(ZERO).getString("valor"));
            }
        } catch (Exception e) {
            return 0.0;
        }
    }
}

