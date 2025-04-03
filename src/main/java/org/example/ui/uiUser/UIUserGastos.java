package org.example.ui.uiUser;

import org.example.manager.userManager.GastosUserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.URL;

import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiUser.UIUserMain.mainUser;
//import static org.example.ui.uiUser.UIUserMesas.showMesas;

public class UIUserGastos {

    public static void showGastosGeneralesDialog() {
        JDialog gastosGeneralesDialog = createDialog("Registrar Gastos Generales", 500, 200, new GridLayout(3, 2));

        JTextField nombreGastoField = new JTextField(); // Campo para la descripción del gasto
        JTextField precioField = new JTextField();      // Campo para el precio del gasto
        // Añadir un WindowListener para detectar el cierre de la ventana
        gastosGeneralesDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainUser(); // Llamada a showMesas cuando se cierra la ventana
            }
        });


        // Crear el botón "Confirmar"
        JButton addGastoButton = new JButton("Confirmar");

// Agregar el ActionListener al botón
        addGastoButton.addActionListener(e -> {
            try {
                String nombreGasto = nombreGastoField.getText();
                String input = precioField.getText(); // Por ejemplo, "3.500" o "3,500"

                // Limpiar el input
                input = input.replace(".", "");  // Eliminar puntos
                input = input.replace(",", ".");  // Reemplazar la coma por un punto para manejar decimales correctamente

                // Convertir el input limpio a un double
                double precio = Double.parseDouble(input);

                // Lógica para registrar el gasto en el Excel
                GastosUserManager.saveGasto(nombreGasto, 1, precio); // Implementar la lógica de guardado en el Excel, sin cantidad

                // Mensaje de éxito
                JOptionPane.showMessageDialog(null, "Gasto registrado correctamente.");
                gastosGeneralesDialog.dispose(); // Cerrar el diálogo
                UIUserMain.mainUser(); // Volver a la ventana principal de usuario
            } catch (NumberFormatException ex) {
                // Manejar error de formato de número
                JOptionPane.showMessageDialog(null, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Manejar cualquier otro error
                JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el gasto.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

// Si deseas agregar el botón a un panel o contenedor, hazlo aquí
// panel.add(addGastoButton);

        gastosGeneralesDialog.add(new JLabel("DESCRIPCIÓN O RAZÓN DEL GASTO:"));
        gastosGeneralesDialog.add(nombreGastoField);
        gastosGeneralesDialog.add(new JLabel("PRECIO:"));
        gastosGeneralesDialog.add(precioField);
        gastosGeneralesDialog.add(addGastoButton);

        gastosGeneralesDialog.setLocationRelativeTo(null);
        gastosGeneralesDialog.setVisible(true);
    }


    public static JPanel createGastosPanel(JPanel contentPanel) {
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

        JLabel titleLabel = new JLabel("Registrar Gastos", JLabel.CENTER);
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
        JLabel descLabel = new JLabel("Descripción");
        descLabel.setFont(labelFont);
        descLabel.setForeground(labelColor);
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        formPanel.add(descLabel, gbcForm);

        gbcForm.gridx = 1;
        JTextField nombreGastoField = new JTextField(20);
        nombreGastoField.setFont(new Font("Arial", Font.PLAIN, 18));
        nombreGastoField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(nombreGastoField, gbcForm);

        // Campo Precio
        JLabel precioLabel = new JLabel("Total");
        precioLabel.setFont(labelFont);
        precioLabel.setForeground(labelColor);
        gbcForm.gridx = 0;
        gbcForm.gridy = 1;
        formPanel.add(precioLabel, gbcForm);

        gbcForm.gridx = 1;
        JTextField precioField = new JTextField(20);
        precioField.setFont(new Font("Arial", Font.PLAIN, 18));
        precioField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(precioField, gbcForm);

        // Botón Confirmar
        gbcForm.gridx = 0;
        gbcForm.gridy = 2;
        gbcForm.gridwidth = 2;
        gbcForm.fill = GridBagConstraints.CENTER;
        JButton addGastoButton = new JButton("Confirmar");
        addGastoButton.setFont(new Font("Arial", Font.BOLD, 22));
        addGastoButton.setPreferredSize(new Dimension(405, 40));
        addGastoButton.setBackground(new Color(0, 201, 87));
        addGastoButton.setForeground(Color.WHITE);
        formPanel.add(addGastoButton, gbcForm);

        // Acción del botón "Confirmar"
        addGastoButton.addActionListener(e -> {
            try {
                String nombreGasto = nombreGastoField.getText();
                String input = precioField.getText().replace(".", "").replace(",", ".");
                double precio = Double.parseDouble(input);

                GastosUserManager.saveGasto(nombreGasto, 1, precio);
                JOptionPane.showMessageDialog(null, "Gasto registrado correctamente.");
                nombreGastoField.setText("");
                precioField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Por favor ingresa un precio válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
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
