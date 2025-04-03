package org.example.ui.uiUser;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userManager.ExcelUserManager;
import org.example.manager.userManager.FacturacionUserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;

import static org.example.manager.userManager.ExcelUserManager.obtenerUltimoEmpleado;
import static org.example.ui.uiAdmin.MainAdminUi.*;
import static org.example.ui.uiUser.UIUserFacturas.getFacturasPanel;
import static org.example.ui.UIHelpers.createButton;
//import static org.example.ui.uiUser.UIUserMesas.showMesas;
import static org.example.ui.uiUser.UIUserMesas.showPanelMesas;

public class UIUserMain {
    private static boolean showingMoreOptions = false;
    private static String nombreEmpleado = ExcelUserManager.obtenerUltimoEmpleado().toUpperCase();
        public static Color fondoPrincipal = new Color(250, 240, 230);

    public static void mainUser() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            JFrame frame = new JFrame("Ventas - Licorera CR");
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            if (icon.getImage() != null) {
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
            sidebarPanel.setPreferredSize(new Dimension(240, frame.getHeight()));
            sidebarPanel.setBackground(fondoPrincipal);
// Panel dinámico para cambiar vistas
            JPanel contentPanel = new JPanel(new CardLayout());
            contentPanel.add(showPanelMesas(frame,contentPanel), "mesas");
            contentPanel.add(UIUserProductList.getProductListPanel(), "productos");
            contentPanel.add(getFacturasPanel(), "Facturas");
            contentPanel.add(UIUserGastos.createGastosPanel(contentPanel), "gastos");
            contentPanel.add(createFacturarPanel(contentPanel), "facturar");

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
                    layout.show(contentPanel, "mesas");
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


            JLabel employeeLabel = new JLabel(nombreEmpleado);
            employeeLabel.setForeground(Color.DARK_GRAY);
            employeeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            try {
                InputStream fontStream = UIUserMain.class.getClassLoader().getResourceAsStream("Pacifico-Regular.ttf");
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 32);
                employeeLabel.setFont(customFont);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logoPanel.add(Box.createVerticalStrut(1)); // Espaciado superior
            logoPanel.add(logoLabel);
            logoPanel.add(Box.createVerticalStrut(1));
            logoPanel.add(employeeLabel);
            logoPanel.add(Box.createVerticalStrut(1));

            sidebarPanel.add(logoPanel, BorderLayout.NORTH);

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
                    int buttonHeight = (int) (panelHeight * 0.14); // 12% del alto del sidebar

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

            JButton moreOptionsButton =  createButton("Ventas Realizadas", resizeIcon("/icons/admin/beneficios.png"), e -> {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, "Facturas");
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
            mainPanel.add(contentPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static ImageIcon resizeIcon(String path) {
        ImageIcon icon = new ImageIcon(UIUserMain.class.getResource(path));
        Image img = icon.getImage().getScaledInstance(85, 55, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }












    public static JPanel createFacturarPanel(JPanel contentPanel) {
        JPanel gastosPanel = new JPanel(new BorderLayout());
        gastosPanel.setBackground(new Color(250, 240, 230));
        gastosPanel.setPreferredSize(new Dimension(800, 600));

        // Cargar fuente personalizada una sola vez
        Font customFont = loadCustomFont("Lobster-Regular.ttf", 36f);
        if (customFont == null) {
            customFont = new Font("Arial", Font.BOLD, 36); // Fuente de respaldo
        }

        // Panel del título
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(250, 240, 230));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JLabel titleLabel = new JLabel("Finalizar Día", JLabel.CENTER);
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 50f)); // Aplicar negrita y tamaño 50
        titleLabel.setForeground(new Color(36, 36, 36));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        gastosPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel principal para imagen y formulario
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(new Color(250, 240, 230));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Panel para la imagen (izquierda)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(250, 240, 230));
        imagePanel.setPreferredSize(new Dimension(200, 200));

        try {
            URL imageUrl = UIUserGastos.class.getResource("/icons/image.png");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage), JLabel.CENTER);
                imagePanel.add(logoLabel, BorderLayout.CENTER);
            } else {
                JLabel missingLabel = new JLabel("Imagen no encontrada", JLabel.CENTER);
                missingLabel.setFont(customFont.deriveFont(14f));
                imagePanel.add(missingLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error cargando imagen", JLabel.CENTER);
            errorLabel.setFont(customFont.deriveFont(14f));
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        mainContentPanel.add(imagePanel, gbc);

        // Panel para los campos de entrada (derecha)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 240, 230));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(10, 10, 10, 10);
        gbcForm.anchor = GridBagConstraints.WEST;

        // Configurar fuente para los labels
        Font labelFont = customFont.deriveFont(20f);
        Color labelColor = new Color(36, 36, 36);

        // Campo Descripción
        JLabel descLabel = new JLabel("Por favor escribe 'Facturar'");
        descLabel.setFont(labelFont);
        descLabel.setForeground(labelColor);
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        formPanel.add(descLabel, gbcForm);

        gbcForm.gridx = 0;
        gbcForm.gridy = 1;
        JTextField nombreGastoField = new JTextField(20);
        nombreGastoField.setFont(labelFont.deriveFont(Font.PLAIN, 18f));
        nombreGastoField.setPreferredSize(new Dimension(100, 35));
        formPanel.add(nombreGastoField, gbcForm);

       /* // Campo Precio
        JLabel precioLabel = new JLabel("Precio:");
        precioLabel.setFont(labelFont);
        precioLabel.setForeground(labelColor);
        gbcForm.gridx = 0;
        gbcForm.gridy = 1;
        formPanel.add(precioLabel, gbcForm);

        gbcForm.gridx = 1;
        JTextField precioField = new JTextField(20);
        precioField.setFont(labelFont.deriveFont(Font.PLAIN, 18f));
        precioField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(precioField, gbcForm);*/

        // Botón Confirmar
        gbcForm.gridx = 0;
        gbcForm.gridy = 2;
        gbcForm.gridwidth = 2;
        gbcForm.fill = GridBagConstraints.CENTER;
        JButton addGastoButton = new JButton("Confirmar");
        addGastoButton.setFont(new Font("Arial", Font.BOLD, 22));
        addGastoButton.setPreferredSize(new Dimension(295, 40));
        addGastoButton.setBackground(new Color(0, 201, 87));
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
                ex.printStackTrace();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        mainContentPanel.add(formPanel, gbc);

        gastosPanel.add(mainContentPanel, BorderLayout.CENTER);

        // Botón Volver
        JButton backButton = createBackButton(contentPanel, customFont);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(250, 240, 230));
        bottomPanel.add(backButton);
        gastosPanel.add(bottomPanel, BorderLayout.SOUTH);

        return gastosPanel;
    }

    private static Font loadCustomFont(String fontPath, float size) {
        try {
            InputStream fontStream = UIUserGastos.class.getClassLoader().getResourceAsStream(fontPath);
            if (fontStream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                return font.deriveFont(size);
            }
        } catch (Exception e) {
            System.err.println("Error cargando fuente: " + e.getMessage());
        }
        return null;
    }



    // Método auxiliar para crear el botón Volver (opcional)
    private static JButton createBackButton(JPanel contentPanel, Font customFont) {
        // Botón "Volver"
        JButton backButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, 7) : new Color(228, 185, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        backButton.setPreferredSize(new Dimension(150, 40)); // Aumenta tamaño del botón
        backButton.setFont(new Font("Arial", Font.BOLD, 22));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(250, 240, 230));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setOpaque(false);
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "mesas");
        });

        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "mesas");
        });
        return backButton;
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


