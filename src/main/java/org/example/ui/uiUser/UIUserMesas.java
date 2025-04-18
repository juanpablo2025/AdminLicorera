package org.example.ui.uiUser;

import org.example.model.Mesa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.ui.uiUser.UIUserVenta.showVentaMesaDialog;

public class UIUserMesas {



    private static Color fondoPrincipal = new Color(250, 240, 230);

    // Método para mostrar las mesas en la interfaz
    public static JPanel crearMesaPanel(Mesa mesa, JFrame mainFrame, JPanel mainPanel) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        String idMesa = mesa.getId();

        JLabel titleLabel = new JLabel("Mesa " + idMesa, JLabel.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        try {

            // Cargar la fuente desde los recursos dentro del JAR
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            // Crear la fuente desde el InputStream
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, 50); // Ajustar tamaño y peso
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String tituloMesa = titleLabel.getText();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                tituloMesa,
                TitledBorder.CENTER, TitledBorder.TOP
        );

        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 18));
        mesaPanel.setBorder(titledBorder);

        mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));

        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 28));
        mesaLabel.setForeground(Color.DARK_GRAY);

        mesaPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              //  System.out.println("Atendiendo: " + tituloMesa);

                // Cargar productos de la mesa desde Excel
                List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

                // Asegurarse de que mainPanel tiene CardLayout
                if (!(mainPanel.getLayout() instanceof CardLayout)) {
                    mainPanel.setLayout(new CardLayout());
                }

                // Obtener el CardLayout
                CardLayout cl = (CardLayout) mainPanel.getLayout();

                // Agregar el nuevo panel de venta de mesa si no está agregado ya
                mainPanel.add(new VentaMesaPanel(productosMesa, tituloMesa, mainPanel,mainFrame), "VentaMesaPanel");

                // Mostrar el panel de ventas
                cl.show(mainPanel, "VentaMesaPanel");
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                TitledBorder newBorder = BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 3),
                        tituloMesa,
                        TitledBorder.CENTER, TitledBorder.TOP
                );
                mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 60, 60) : new Color(0, 201, 87));

                newBorder.setTitleFont(new Font("Arial", Font.BOLD, 20)); // Cambiar fuente a 28
                mesaPanel.setBorder(newBorder);
                mesaLabel.setForeground(Color.WHITE);
                newBorder.setTitleColor(Color.white);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mesaPanel.setBorder(titledBorder);
                mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));
                mesaLabel.setForeground(Color.DARK_GRAY);
            }
        });

        mesaPanel.add(mesaLabel, BorderLayout.CENTER);
        return mesaPanel;
    }

    public static JPanel showPanelMesas(JFrame mainFrame, JPanel contentPanel) {
        JPanel mesasPanel = new JPanel(new BorderLayout());
        mesasPanel.setBackground(fondoPrincipal);

        mesasPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 100), 0, true),
                new EmptyBorder(10, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Mesas", JLabel.CENTER);
        titleLabel.setForeground(new Color (28, 28, 28));
        try {
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel gridMesasPanel = new JPanel(new GridLayout(0, 5, 4, 4));
        gridMesasPanel.setBackground(fondoPrincipal);
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf(i + 1));
            JPanel mesaPanel = crearMesaPanel(mesa, mainFrame, contentPanel);
            gridMesasPanel.add(mesaPanel);
        }

        JButton addMesaButton = new JButton("Nueva Mesa") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Color de fondo normal
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, 7)); // Amarillo oscuro al presionar
                } else {
                    g2.setColor(new Color(228, 185, 42)); // Amarillo Material Design
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        // Estilos del botón
        addMesaButton.setPreferredSize(new Dimension(160, 40)); // Más grande
        addMesaButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        addMesaButton.setForeground(Color.WHITE); // Texto negro
        addMesaButton.setFocusPainted(false);
        addMesaButton.setContentAreaFilled(false);
        addMesaButton.setBorderPainted(false);
        addMesaButton.setOpaque(false);

        addMesaButton.addActionListener(e -> {
            String nuevoID = String.valueOf(mesas.size() + 1);
            Mesa nuevaMesa = new Mesa(nuevoID);
            mesas.add(nuevaMesa);
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa, mainFrame, contentPanel);
            gridMesasPanel.add(nuevaMesaPanel);
            gridMesasPanel.revalidate();
            gridMesasPanel.repaint();
            agregarMesaAExcel(nuevaMesa);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);
        bottomPanel.setBackground(new Color(250, 240, 230));// Color de fondo amarillo
        mesasPanel.add(titleLabel, BorderLayout.NORTH);
        mesasPanel.add(gridMesasPanel, BorderLayout.CENTER);
        mesasPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mesasPanel;
    }
}
