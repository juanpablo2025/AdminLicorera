package org.example.ui.uiuser;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.manager.usermanager.ExcelUserManager;
import org.example.manager.usermanager.FacturacionUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;

import static org.example.ui.uiadmin.UIMainAdmin.*;
import static org.example.ui.uiuser.UIUserFacturas.getFacturasPanel;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiuser.UIUserMesas.showPanelMesas;
import static org.example.utils.Constants.*;

public class UIUserMain {

    private static final Logger logger =  LoggerFactory.getLogger(UIUserMain.class);

    private UIUserMain() {}

    //private static final String EMPLOYEE_NAME = ExcelUserManager.obtenerUltimoEmpleado().toUpperCase();

    private static String EMPLOYEE_NAME;// ExcelUserManager.obtenerUltimoEmpleado().toUpperCase();

    static {
        try {
            EMPLOYEE_NAME = DatabaseUserManager.obtenerUltimoEmpleado();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void mainUser() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - "+EMPRESA_NAME);
            ImageIcon icon = LOGO_EMPRESA;
            if (icon.getImage() != null) {
                Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                frame.setIconImage(scaledImage);
            }

            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245));

            JPanel sidebarPanel = new JPanel();
            sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

            sidebarPanel.add(Box.createVerticalStrut(FIVE));

            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(showPanelMesas(frame,contentPanel), MESAS);
            contentPanel.add(UIUserProductList.getProductListPanel(), "productos");
            contentPanel.add(getFacturasPanel(), FACTURAS);
            contentPanel.add(UIUserGastos.createGastosPanel(contentPanel), "gastos");
            contentPanel.add(createFacturarPanel(contentPanel), "facturar");
            contentPanel.add(UIParkDrive.showPanelParkDrive(frame,contentPanel), "parqueadero");

            JPanel logoPanel = new JPanel();
            logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
            logoPanel.setBackground(FONDO_PRINCIPAL);

            ImageIcon logoIcon = LOGO_EMPRESA;
            Image imgLogo = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel logoLabel = getJLabel(imgLogo, contentPanel);

            JLabel employeeLabel = new JLabel(EMPLOYEE_NAME);
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeLabel.setFont(TITTLE_FONT.deriveFont(Font.BOLD, 38f));

            JLabel encargadoLabel = new JLabel("Gestión de ventas");
            encargadoLabel.setForeground(Color.BLACK);
            encargadoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            encargadoLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 15));


            logoPanel.add(Box.createVerticalStrut(ONE));
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(ONE));
            logoPanel.add(encargadoLabel);
            logoPanel.add(Box.createVerticalStrut(-2));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(3));

            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setBackground(FONDO_PRINCIPAL);

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

            Dimension buttonSize = new Dimension(10, 10);

            JButton listaProductosButton = createButton("Inventario", resizeIcon(PRODUCT_LIST_ICON), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "productos");
            });

            JButton gastosButton = createButton("Gastos", resizeIcon(GASTOS_ICON), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "gastos");
            });
            gastosButton.setMaximumSize(buttonSize);

            JButton salirButton = createButton("Finalizar Día", resizeIcon(FACTURAR_ICON), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "facturar");
            });
            salirButton.setMaximumSize(buttonSize);

            JButton moreOptionsButton =  createButton(FACTURAS, resizeIcon(FACTURAS_ICON), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, FACTURAS);
            });
            moreOptionsButton.setMaximumSize(buttonSize);

            JButton moreOptionsButtons = createButton("Administrador", resizeIcon(ADMIN_ICON), e -> adminPassword(frame));

            moreOptionsButtons.setMaximumSize(buttonSize);

            JButton moreParkDriveButtons = createButton("Parqueadero", resizeIcon(FACTURAR_ICON), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "parqueadero");
            });
            moreParkDriveButtons.setMaximumSize(buttonSize);

            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(listaProductosButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(gastosButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(salirButton);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));
            buttonsPanel.add(moreOptionsButtons);
            buttonsPanel.add(Box.createVerticalGlue());
            buttonsPanel.add(moreParkDriveButtons);
            buttonsPanel.add(Box.createVerticalStrut(FIVE));


            sidebarPanel.add(buttonsPanel, BorderLayout.CENTER);
            mainPanel.add(sidebarPanel, BorderLayout.WEST);
            mainPanel.add(contentPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            logger.error("Error al iniciar la interfaz de usuario: {}", ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error al iniciar la interfaz de usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JLabel getJLabel(Image imgLogo, JPanel contentPanel) {
        JLabel logoLabel = new JLabel(new ImageIcon(imgLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CardLayout layout = (CardLayout) contentPanel.getLayout();
                layout.show(contentPanel, MESAS);
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

    public static ImageIcon resizeIcon(String path) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource(path)));
        Image img = icon.getImage().getScaledInstance(65, 55, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static JPanel createFacturarPanel(JPanel contentPanel) {
        JPanel gastosPanel = new JPanel(new BorderLayout());
        gastosPanel.setBackground(FONDO_PRINCIPAL);
        gastosPanel.setPreferredSize(new Dimension(800, 600));

        Font customFont = TITTLE_FONT;

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(FONDO_PRINCIPAL);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        JLabel titleLabel = new JLabel("Finalizar Día", SwingConstants.CENTER);
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 50f));
        titleLabel.setForeground(new Color(36, 36, 36));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        gastosPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(FONDO_PRINCIPAL);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.CENTER;

        // Logo
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(FONDO_PRINCIPAL);
        imagePanel.setPreferredSize(new Dimension(230, 230));

        try {
            URL imageUrl = UIUserGastos.class.getResource("/icons/image.png");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                imagePanel.add(logoLabel, BorderLayout.CENTER);
            } else {
                imagePanel.add(new JLabel("Imagen no encontrada", SwingConstants.CENTER));
            }
        } catch (Exception e) {
            imagePanel.add(new JLabel("Error cargando imagen", SwingConstants.CENTER));
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        mainContentPanel.add(imagePanel, gbc);

        // Info Label
        JLabel infoLabel = new JLabel(
                "<html><div style='text-align:center;'>¿Terminaste la jornada?<br>Da clic en <span style='color:#00C957; font-weight:bold;'>[Finalizar]</span> y genera el realizo del día.</div></html>",
                SwingConstants.CENTER
        );
        infoLabel.setFont(new Font("Segoe UI Variable", Font.PLAIN, 18));
        infoLabel.setForeground(new Color(36, 36, 36));

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        mainContentPanel.add(infoLabel, gbc);

        // Botón Finalizar Día
        JButton finalizarBtn = new JButton("Finalizar");
        finalizarBtn.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        finalizarBtn.setPreferredSize(new Dimension(295, 40));
        finalizarBtn.setBackground(new Color(0, 201, 87));
        finalizarBtn.setForeground(Color.WHITE);

        finalizarBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    gastosPanel,
                    "¿Estás seguro de que deseas finalizar el día?",
                    "Confirmar Facturación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    FacturacionUserManager.facturarYSalir();
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, "main");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(gastosPanel,
                            "Ocurrió un error al finalizar el día.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    logger.error("Error al facturar y salir: {}", ex.getMessage());
                }
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 1;
        mainContentPanel.add(finalizarBtn, gbc);

        gastosPanel.add(mainContentPanel, BorderLayout.CENTER);

        JButton backButton = createBackButton(contentPanel);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(FONDO_PRINCIPAL);
        bottomPanel.add(backButton);
        gastosPanel.add(bottomPanel, BorderLayout.SOUTH);

        return gastosPanel;
    }


    private static JButton createBackButton(JPanel contentPanel) {
        JButton backButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? BTN_BACK_PRESSED : BTN_BACK);
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        backButton.setForeground(BTN_BACK_FONT_COLOR);
        backButton.setBackground(FONDO_PRINCIPAL);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setOpaque(false);
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, MESAS);
        });

        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, MESAS);
        });
        return backButton;
    }
}