package org.example.ui.uiuser;

import org.example.manager.usermanager.GastosUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;

import static org.example.utils.Constants.*;

public class UIUserGastos {

    private static final Logger logger =  LoggerFactory.getLogger(UIUserGastos.class);

    private UIUserGastos() {}

    public static JPanel createGastosPanel(JPanel contentPanel) {
        JPanel gastosPanel = new JPanel(new BorderLayout());
        gastosPanel.setBackground(FONDO_PRINCIPAL);
        gastosPanel.setPreferredSize(new Dimension(800, 600));

        /*Font customFont = loadCustomFont();
        if (customFont == null) {
            customFont = new Font("Segoe UI Variable", Font.BOLD, 36);
            titleLabel.setFont(customFont.deriveFont(Font.BOLD, 50f));
        }*/

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(FONDO_PRINCIPAL);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, ZERO, 0, ZERO));

        JLabel titleLabel = new JLabel("Registrar Gastos", SwingConstants.CENTER);
        titleLabel.setFont(TITTLE_FONT);
        titleLabel.setForeground(new Color(36, 36, 36));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        gastosPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(FONDO_PRINCIPAL);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
                JLabel missingLabel = new JLabel("Imagen no encontrada", SwingConstants.CENTER);
                imagePanel.add(missingLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error cargando imagen", SwingConstants.CENTER);
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }

        gbc.gridx = ZERO;
        gbc.gridy = ZERO;
        gbc.gridheight = THREE;
        mainContentPanel.add(imagePanel, gbc);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(FONDO_PRINCIPAL);

        JLabel descripcionLabel = new JLabel("<html><div style='text-align:left;'>Registra gastos, servicios, compras o reparaciones de la tienda<br>en el realizo del día.</div></html>");
        descripcionLabel.setFont(new Font("Segoe UI Variable", Font.PLAIN, 18));
        descripcionLabel.setForeground(new Color(36, 36, 36));
        descripcionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("Descripción");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 16));
        descLabel.setForeground(Color.BLACK);
        JTextField nombreGastoField = new JTextField(20);
        nombreGastoField.setMaximumSize(new Dimension(400, 40));

        JLabel precioLabel = new JLabel("Valor");
        precioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        precioLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, 16));
        precioLabel.setForeground(Color.BLACK);
        JTextField precioField = new JTextField(20);
        precioField.setMaximumSize(new Dimension(400, 40));

        JButton addGastoButton = new JButton("Registrar");
        addGastoButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        addGastoButton.setPreferredSize(new Dimension(400, 40));
        addGastoButton.setBackground(new Color(ZERO, 201, 87));
        addGastoButton.setForeground(Color.WHITE);
        addGastoButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        addGastoButton.addActionListener(e -> {
            try {
                String nombreGasto = nombreGastoField.getText();
                String input = precioField.getText().replace(".", "").replace(",", ".");
                double precio = Double.parseDouble(input);

                GastosUserManager.saveGasto(nombreGasto, precio);
                JOptionPane.showMessageDialog(null, "Gasto registrado correctamente.");
                nombreGastoField.setText("");
                precioField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el gasto.", "Error", JOptionPane.ERROR_MESSAGE);
                logger.error("Error al registrar el gasto: {}", ex.getMessage());
            }
        });

        formPanel.add(descripcionLabel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(descLabel);
        formPanel.add(nombreGastoField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(precioLabel);
        formPanel.add(precioField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(addGastoButton);

        gbc.gridx = ONE;
        gbc.gridy = ZERO;
        gbc.gridheight = THREE;
        mainContentPanel.add(formPanel, gbc);

        gastosPanel.add(mainContentPanel, BorderLayout.CENTER);

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
        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
        }
        return null;
    }

    private static JButton createBackButton(JPanel contentPanel) {
        JButton backButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, SEVEN) : new Color(228, 185, 42));
                //g2.setColor(Color.LIGHT_GRAY);
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(FONDO_PRINCIPAL);
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
