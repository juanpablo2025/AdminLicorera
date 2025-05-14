package org.example.ui.uiuser;

import com.formdev.flatlaf.FlatLightLaf;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static org.example.ui.uiadmin.UIMainAdmin.*;
import static org.example.ui.uiuser.UIUserFacturas.getFacturasPanel;
import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiuser.UIUserMesas.showPanelMesas;
import static org.example.utils.Constants.*;

public class UIUserMain {

    private static final Logger logger =  LoggerFactory.getLogger(UIUserMain.class);

    private UIUserMain() {}

    private static final String EMPLOYEE_NAME = ExcelUserManager.obtenerUltimoEmpleado().toUpperCase();


    public static void mainUser() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - Licorera CR");
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
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

            // Panel dinámico para cambiar vistas
            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(showPanelMesas(frame,contentPanel), MESAS);
            contentPanel.add(UIUserProductList.getProductListPanel(), "productos");
            contentPanel.add(getFacturasPanel(), FACTURAS);
            contentPanel.add(UIUserGastos.createGastosPanel(contentPanel), "gastos");
            contentPanel.add(createFacturarPanel(contentPanel), "facturar");

            // Panel superior con logo y nombre del empleado centrados
            JPanel logoPanel = new JPanel();
            logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
            logoPanel.setBackground(FONDO_PRINCIPAL);

            ImageIcon logoIcon = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png")));
            Image imgLogo = logoIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            JLabel logoLabel = getJLabel(imgLogo, contentPanel);

            JLabel employeeLabel = new JLabel(EMPLOYEE_NAME);
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream("Pacifico-Regular.ttf");
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 32);
                employeeLabel.setFont(customFont);
            } catch (Exception e) {
                logger.error("Error al cargar la fuente personalizada: ", e);
                employeeLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 32));
            }

            logoPanel.add(Box.createVerticalStrut(ONE)); // Espaciado superior
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(ONE));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(ONE));

            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setBackground(FONDO_PRINCIPAL);

            // Agregar ComponentListener para cambiar tamaño dinámicamente
            sidebarPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = sidebarPanel.getWidth();
                    int panelHeight = sidebarPanel.getHeight();

                    int buttonWidth = (int) (panelWidth * 1.0);  // 90% del ancho del sidebar
                    int buttonHeight = (int) (panelHeight * 0.13); // 12% del alto del sidebar

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

            Dimension buttonSize = new Dimension(ONE_HUNDRED, 60);

            JButton listaProductosButton = createButton("Inventario", resizeIcon("/icons/lista-de_productos.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "productos");
            });

            JButton gastosButton = createButton("Gastos", resizeIcon("/icons/Gastos.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "gastos");
            });
            gastosButton.setMaximumSize(buttonSize);

            JButton salirButton = createButton("Finalizar Día", resizeIcon("/icons/Facturar.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "facturar");
            });
            salirButton.setMaximumSize(buttonSize);

            JButton moreOptionsButton =  createButton(FACTURAS, resizeIcon("/icons/admin/beneficios.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, FACTURAS);
            });
            moreOptionsButton.setMaximumSize(buttonSize);

            JButton moreOptionsButtons = createButton("Administrador", resizeIcon("/icons/obrero.png"), e -> adminPassword(frame));

            moreOptionsButtons.setMaximumSize(buttonSize);

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

        // **Añadir efectos al pasar el mouse**
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // **Obtener el CardLayout y mostrar el panel "mesas"**
                CardLayout layout = (CardLayout) contentPanel.getLayout();
                layout.show(contentPanel, MESAS);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el cursor a "mano"
                logoLabel.setBorder(BorderFactory.createLineBorder(FONDO_PRINCIPAL, ONE)); // Borde amarillo al pasar el mouse
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Restaurar cursor normal
                logoLabel.setBorder(null); // Eliminar borde al salir
            }
        });
        return logoLabel;
    }

    public static ImageIcon resizeIcon(String path) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource(path)));
        Image img = icon.getImage().getScaledInstance(85, 55, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static JPanel createFacturarPanel(JPanel contentPanel) {
        JPanel gastosPanel = new JPanel(new BorderLayout());
        gastosPanel.setBackground(FONDO_PRINCIPAL);
        gastosPanel.setPreferredSize(new Dimension(800, 600));

        // Cargar fuente personalizada una sola vez
        Font customFont = loadCustomFont();
        if (customFont == null) {
            customFont = new Font(ARIAL_FONT, Font.BOLD, 36); // Fuente de respaldo
        }

        // Panel del título
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(FONDO_PRINCIPAL);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, ZERO, 30, ZERO));

        JLabel titleLabel = new JLabel("Finalizar Día", SwingConstants.CENTER);
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 50f)); // Aplicar negrita y tamaño 50
        titleLabel.setForeground(new Color(36, 36, 36));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        gastosPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel principal para imagen y formulario
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(FONDO_PRINCIPAL);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Panel para la imagen (izquierda)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(FONDO_PRINCIPAL);
        imagePanel.setPreferredSize(new Dimension(200, 200));

        try {
            URL imageUrl = UIUserGastos.class.getResource("/icons/image.png");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                imagePanel.add(logoLabel, BorderLayout.CENTER);
            } else {
                JLabel missingLabel = new JLabel("Imagen no encontrada", SwingConstants.CENTER);
                missingLabel.setFont(customFont.deriveFont(14f));
                imagePanel.add(missingLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error cargando imagen", SwingConstants.CENTER);
            errorLabel.setFont(customFont.deriveFont(14f));
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }

        gbc.gridx = ZERO;
        gbc.gridy = ZERO;
        gbc.gridheight = TWO;
        mainContentPanel.add(imagePanel, gbc);

        // Panel para los campos de entrada (derecha)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(FONDO_PRINCIPAL);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(TEN, TEN, TEN, TEN);
        gbcForm.anchor = GridBagConstraints.WEST;

        // Configurar fuente para los labels
        Font labelFont = customFont.deriveFont(20f);
        Color labelColor = new Color(36, 36, 36);

        // Campo Descripción
        JLabel descLabel = new JLabel("Por favor escribe 'Facturar'");
        descLabel.setFont(labelFont);
        descLabel.setForeground(labelColor);
        gbcForm.gridx = ZERO;
        gbcForm.gridy = ZERO;
        formPanel.add(descLabel, gbcForm);

        gbcForm.gridx = ZERO;
        gbcForm.gridy = ONE;
        JTextField nombreGastoField = new JTextField(20);
        nombreGastoField.setFont(labelFont.deriveFont(Font.PLAIN, 18f));
        nombreGastoField.setPreferredSize(new Dimension(100, 35));
        formPanel.add(nombreGastoField, gbcForm);

        // Botón Confirmar
        gbcForm.gridx = ZERO;
        gbcForm.gridy = TWO;
        gbcForm.gridwidth = TWO;
        gbcForm.fill = GridBagConstraints.CENTER;
        JButton addGastoButton = new JButton("Confirmar");
        addGastoButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        addGastoButton.setPreferredSize(new Dimension(295, 40));
        addGastoButton.setBackground(new Color(ZERO, 201, 87));
        addGastoButton.setForeground(Color.WHITE);
        formPanel.add(addGastoButton, gbcForm);

        // Acción del botón "Confirmar"
        addGastoButton.addActionListener(e -> {
            try {

                if (FacturacionUserManager.verificarFacturacion(nombreGastoField.getText())) {
                    FacturacionUserManager.facturarYSalir();

                } else {
                    FacturacionUserManager.mostrarErrorFacturacion();
                }

                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "main");
            }  catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el gasto.", "Error", JOptionPane.ERROR_MESSAGE);
                logger.error("Error al registrar el gasto: {}", ex.getMessage());
            }
        });

        gbc.gridx = ONE;
        gbc.gridy = ZERO;
        gbc.gridheight = ONE;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        mainContentPanel.add(formPanel, gbc);

        gastosPanel.add(mainContentPanel, BorderLayout.CENTER);

        // Botón Volver
        JButton backButton = createBackButton(contentPanel);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(FONDO_PRINCIPAL);
        bottomPanel.add(backButton);
        gastosPanel.add(bottomPanel, BorderLayout.SOUTH);

        return gastosPanel;
    }

    private static Font loadCustomFont() {
        try {
            InputStream fontStream = UIUserGastos.class.getClassLoader().getResourceAsStream(LOBSTER_FONT);
            if (fontStream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                return font.deriveFont((float) 36.0);
            }
        } catch (IOException | FontFormatException e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
        }
        return null;
    }

    //  auxiliar para crear el botón Volver (opcional)
    private static JButton createBackButton(JPanel contentPanel) {
        // Botón "Volver"
        JButton backButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, SEVEN) : new Color(228, 185, 42));
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        backButton.setPreferredSize(new Dimension(150, 40)); // Aumenta tamaño del botón
        backButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        backButton.setForeground(Color.WHITE);
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