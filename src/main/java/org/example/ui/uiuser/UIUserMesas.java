package org.example.ui.uiuser;

import org.example.model.Mesa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;
import static org.example.manager.usermanager.ExcelUserManager.*;
import static org.example.utils.Constants.*;

public class UIUserMesas {

    private static final Logger logger =  LoggerFactory.getLogger(UIUserMesas.class);

    private UIUserMesas() {}


    public static JPanel crearMesaPanel(Mesa mesa, JFrame mainFrame, JPanel mainPanel) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(ONE_HUNDRED, ONE_HUNDRED));

        String idMesa = mesa.getId();

        JLabel titleLabel = new JLabel("Mesa " + idMesa, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        titleLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 50));
        /*try {

            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: ", e);
            titleLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 50));
        }*/

        String tituloMesa = titleLabel.getText();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, TWO),
                tituloMesa,
                TitledBorder.CENTER, TitledBorder.TOP
        );

        titledBorder.setTitleFont(new Font("Segoe UI Variable", Font.BOLD, 18));
        mesaPanel.setBorder(titledBorder);
        mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 28));
        mesaLabel.setForeground(Color.DARK_GRAY);
        mesaPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

                if (!(mainPanel.getLayout() instanceof CardLayout)) {
                    mainPanel.setLayout(new CardLayout());
                }

                CardLayout cl = (CardLayout) mainPanel.getLayout();
                mainPanel.add(new UIUserVenta(productosMesa, tituloMesa, mainPanel,mainFrame), "VentaMesaPanel");
                mainPanel.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(100, 100, 100), ZERO, true),
                        new EmptyBorder(TEN, 20, 20, 20)
                ));
                mainPanel.setBackground(FONDO_PRINCIPAL);
                cl.show(mainPanel, "VentaMesaPanel");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                TitledBorder newBorder = BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, THREE),
                        tituloMesa,
                        TitledBorder.CENTER, TitledBorder.TOP
                );
                mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 60, 60) : new Color(ZERO, 201, 87));

                newBorder.setTitleFont(new Font("Segoe UI Variable", Font.BOLD, 20));
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
        mesasPanel.setBackground(FONDO_PRINCIPAL);

        mesasPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(ONE_HUNDRED, ONE_HUNDRED, ONE_HUNDRED), ZERO, true),
                new EmptyBorder(TEN, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Mesas", SwingConstants.CENTER);
        titleLabel.setForeground(new Color (28, 28, 28));
        titleLabel.setFont(TITTLE_FONT);


        JPanel gridMesasPanel = new JPanel(new GridLayout(ZERO, FIVE, FOUR, FOUR));
        gridMesasPanel.setBackground(FONDO_PRINCIPAL);
        List<Mesa> mesas = cargarMesasDesdeExcel();

        for (int i = ZERO; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf(i + ONE));
            JPanel mesaPanel = crearMesaPanel(mesa, mainFrame, contentPanel);
            gridMesasPanel.add(mesaPanel);
        }

        JButton addMesaButton = getJButton();

        addMesaButton.addActionListener(e -> {
            String nuevoID = String.valueOf(mesas.size() + ONE);
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
        bottomPanel.setBackground(FONDO_PRINCIPAL);
        mesasPanel.add(titleLabel, BorderLayout.NORTH);
        mesasPanel.add(gridMesasPanel, BorderLayout.CENTER);
        mesasPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mesasPanel;
    }

    private static JButton getJButton() {
        JButton addMesaButton = new JButton("Nueva Mesa") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ZERO, ZERO, ZERO, 30));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);


                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, SEVEN));
                    //g2.setColor(Color.DARK_GRAY);
                } else {
                    g2.setColor(new Color(228, 185, 42));
                    //g2.setColor(Color.LIGHT_GRAY);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        addMesaButton.setPreferredSize(new Dimension(160, 40));
        addMesaButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        addMesaButton.setForeground(Color.WHITE);
        addMesaButton.setFocusPainted(false);
        addMesaButton.setContentAreaFilled(false);
        addMesaButton.setBorderPainted(false);
        addMesaButton.setOpaque(false);
        return addMesaButton;
    }
}
