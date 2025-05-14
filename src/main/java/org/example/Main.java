package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.ui.uiuser.UIUserMain;
import org.example.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import static org.example.manager.usermanager.ExcelUserManager.*;
import static org.example.manager.usermanager.MainUserManager.crearDirectorios;
import static org.example.ui.uiadmin.UIMainAdmin.adminPassword;
import static org.example.ui.uiuser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;


public class Main {

    private static final Logger logger =  LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        crearDirectorios();

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();
        }
        if (hayRegistroDeHoy()) {
            Updater.checkForUpdates();
            mainUser();
        } else {
            Updater.checkForUpdates();
            mostrarLogin();
        }
    }

    public static void mostrarLogin() {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            logger.error("Error al establecer el Look and Feel: ", ex);
        }

        JFrame frame = new JFrame("Bienvenido - Licorera CR");
        // Cargar el icono desde el classpath
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png")));

        if (icon.getImage() != null) {
            // Redimensionar la imagen a 64x64 píxeles (ajusta según necesidad)
            Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            frame.setIconImage(scaledImage);
        }

        frame.setSize(400, 650);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 240, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(THIRTY, THIRTY, THIRTY, THIRTY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(FIFTEEN, FIFTEEN, FIFTEEN, FIFTEEN);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel();
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png")));
        Image img = logo.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(img));

        // Etiqueta y campo de usuario
        JLabel userLabel = new JLabel("Encargado de caja");
        userLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, TWENTY));
        userLabel.setForeground(Color.BLACK);

        JTextField userField = new JTextField(TWENTY);
        userField.setFont(new Font(ARIAL_FONT, Font.PLAIN, EIGHTEEN));
        userField.setBorder(BorderFactory.createLineBorder(Color.RED, TWO));

        JButton loginButton = new JButton("Iniciar Día");
        loginButton.setFont(new Font(ARIAL_FONT, Font.BOLD, TWENTY));
        loginButton.setBackground(Color.RED);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        JButton adminButton = new JButton("Administrador");
        adminButton.setFont(new Font("Arial", Font.BOLD, TWENTY));
        adminButton.setBackground(Color.BLACK);
        adminButton.setForeground(Color.WHITE);
        adminButton.setFocusPainted(false);
        adminButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        // Agregar componentes al panel
        gbc.gridx = ZERO;
        gbc.gridy = ZERO;
        gbc.gridwidth = TWO;
        panel.add(logoLabel, gbc);

        gbc.gridy = ONE;
        panel.add(userLabel, gbc);

        gbc.gridy = TWO;
        panel.add(userField, gbc);

        gbc.gridy = THREE;
        panel.add(loginButton, gbc);

        gbc.gridy = FOUR;
        panel.add(adminButton, gbc);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            String nombreUsuario = userField.getText();
            if (!nombreUsuario.isEmpty()) {
                registrarDia(nombreUsuario);
                JOptionPane.showMessageDialog(frame, "¡Bienvenido!");
                frame.dispose();
                mainUser();
            } else {
                JOptionPane.showMessageDialog(frame, "Por favor ingresa un nombre de usuario.");
            }
        });

        adminButton.addActionListener(e -> adminPassword(frame));
    }
}






