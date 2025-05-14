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

        // Cargar fuente personalizada una sola vez
        Font customFont = loadCustomFont();
        if (customFont == null) {
            customFont = new Font(ARIAL_FONT, Font.BOLD, 36); // Fuente de respaldo
        }

        // Panel del título
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(FONDO_PRINCIPAL);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, ZERO, 30, ZERO));

        JLabel titleLabel = new JLabel("Registrar Gastos", SwingConstants.CENTER);
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
        JLabel descLabel = new JLabel("Descripción");
        descLabel.setFont(labelFont);
        descLabel.setForeground(labelColor);
        gbcForm.gridx = ZERO;
        gbcForm.gridy = ZERO;
        formPanel.add(descLabel, gbcForm);

        gbcForm.gridx = ONE;
        JTextField nombreGastoField = new JTextField(20);
        nombreGastoField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
        nombreGastoField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(nombreGastoField, gbcForm);

        // Campo Precio
        JLabel precioLabel = new JLabel("Total");
        precioLabel.setFont(labelFont);
        precioLabel.setForeground(labelColor);
        gbcForm.gridx = ZERO;
        gbcForm.gridy = ONE;
        formPanel.add(precioLabel, gbcForm);

        gbcForm.gridx = ONE;
        JTextField precioField = new JTextField(20);
        precioField.setFont(new Font(ARIAL_FONT, Font.PLAIN, 18));
        precioField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(precioField, gbcForm);

        // Botón Confirmar
        gbcForm.gridx = ZERO;
        gbcForm.gridy = TWO;
        gbcForm.gridwidth = TWO;
        gbcForm.fill = GridBagConstraints.CENTER;
        JButton addGastoButton = new JButton("Confirmar");
        addGastoButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        addGastoButton.setPreferredSize(new Dimension(405, 40));
        addGastoButton.setBackground(new Color(ZERO, 201, 87));
        addGastoButton.setForeground(Color.WHITE);
        formPanel.add(addGastoButton, gbcForm);

        // Acción del botón "Confirmar"
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
        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
        }
        return null;
    }

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
            cl.show(contentPanel, "mesas");
        });

        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "mesas");
        });
        return backButton;
    }

}
