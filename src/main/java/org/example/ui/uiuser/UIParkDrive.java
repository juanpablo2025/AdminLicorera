package org.example.ui.uiuser;

import org.example.model.Mesa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

import static org.example.manager.userDBManager.DatabaseUserManager.*;
import static org.example.utils.Constants.*;

public class UIParkDrive {

    public static JPanel crearEstacionamientoPanel(Mesa mesa, JFrame mainFrame, JPanel mainPanel) {
        JPanel parkingPanel = new JPanel(new BorderLayout());
        parkingPanel.setPreferredSize(new Dimension(ONE_HUNDRED, ONE_HUNDRED));

        String idMesa = mesa.getId();
        JLabel titleLabel = new JLabel("Espacio " + idMesa, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        titleLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 30));

        String tituloMesa = titleLabel.getText();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, TWO),
                tituloMesa,
                TitledBorder.CENTER, TitledBorder.TOP
        );

        titledBorder.setTitleFont(new Font("Segoe UI Variable", Font.BOLD, 13));
        parkingPanel.setBorder(titledBorder);
        parkingPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "VBA70C" : "LIBRE" , SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 15));
        mesaLabel.setForeground(Color.DARK_GRAY);
        parkingPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        parkingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                //List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);
                List<String[]> productosMesa = cargarProductosMesaDesdeBD(tituloMesa);
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
                parkingPanel.setBackground(mesa.isOcupada() ? new Color(255, 60, 60) : new Color(ZERO, 201, 87));

                newBorder.setTitleFont(new Font("Segoe UI Variable", Font.BOLD, 14));
                parkingPanel.setBorder(newBorder);
                mesaLabel.setForeground(Color.WHITE);
                newBorder.setTitleColor(Color.white);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                parkingPanel.setBorder(titledBorder);
                parkingPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));
                mesaLabel.setForeground(Color.DARK_GRAY);
            }
        });

        parkingPanel.add(mesaLabel, BorderLayout.CENTER);
        return parkingPanel;
    }


    public static JPanel showPanelParkDrive(JFrame mainFrame, JPanel contentPanel) {
        JPanel parkDrivePanel = new JPanel(new BorderLayout());
        parkDrivePanel.setBackground(FONDO_PRINCIPAL);

        parkDrivePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(ONE_HUNDRED, ONE_HUNDRED, ONE_HUNDRED), ZERO, true),
                new EmptyBorder(TEN, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Parqueadero", SwingConstants.CENTER);
        titleLabel.setForeground(new Color (28, 28, 28));
        titleLabel.setFont(TITTLE_FONT);


        JPanel gridParkDrivePanel = new JPanel(new GridLayout(ZERO, 12, FOUR, FOUR));
        gridParkDrivePanel.setBackground(FONDO_PRINCIPAL);
        List<Mesa> mesas = cargarParkingDesdeDB();

        for (int i = ZERO; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf(i + ONE));
            JPanel mesaPanel = crearEstacionamientoPanel(mesa, mainFrame, contentPanel);
            gridParkDrivePanel.add(mesaPanel);
        }

        JButton addparkDriveButton = getJButton();

        addparkDriveButton.addActionListener(e -> {
            String nuevoID = String.valueOf(mesas.size() + ONE);
            Mesa nuevaMesa = new Mesa(nuevoID);
            mesas.add(nuevaMesa);
            JPanel nuevaMesaPanel = crearEstacionamientoPanel(nuevaMesa, mainFrame, contentPanel);
            gridParkDrivePanel.add(nuevaMesaPanel);
            gridParkDrivePanel.revalidate();
            gridParkDrivePanel.repaint();
            //agregarMesaAExcel(nuevaMesa);
            try {
                agregarParkingABD(nuevaMesa);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addparkDriveButton);
        bottomPanel.setBackground(FONDO_PRINCIPAL);
        parkDrivePanel.add(titleLabel, BorderLayout.NORTH);
        parkDrivePanel.add(gridParkDrivePanel, BorderLayout.CENTER);
        parkDrivePanel.add(bottomPanel, BorderLayout.SOUTH);

        return parkDrivePanel;
    }


    private static JButton getJButton() {
        JButton addMesaButton = new JButton("Nuevo espacio") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? BTN_BACK_PRESSED : BTN_BACK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        addMesaButton.setPreferredSize(new Dimension(160, 40));
        addMesaButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        addMesaButton.setForeground(BTN_BACK_FONT_COLOR);
        addMesaButton.setFocusPainted(false);
        addMesaButton.setContentAreaFilled(false);
        addMesaButton.setBorderPainted(false);
        addMesaButton.setOpaque(false);
        return addMesaButton;
    }

}
