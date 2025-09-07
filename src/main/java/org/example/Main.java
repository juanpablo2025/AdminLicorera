package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.ui.uiuser.UIUserMain;
import org.example.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

import java.sql.Connection;

import java.sql.SQLException;
import java.time.LocalTime;


import static org.example.manager.userDBManager.DatabaseUserManager.*;

import static org.example.manager.usermanager.MainUserManager.crearDirectorios;
import static org.example.ui.uiadmin.UIMainAdmin.adminPassword;
import static org.example.ui.uiuser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException {
        crearDirectorios();
        crearEstructuraInicial();
        try (Connection conn = connect()) {
            if (hayRegistroDeHoy(conn)) {
                //Updater.checkForUpdates();
                mainUser(); // Si hay registro, abrir el panel de usuario
            } else {
                //Updater.checkForUpdates();
                //DatabaseUserManager.crearEstructuraInicial();// Crear la estructura de la base de datos si no existe
                mostrarLogin(); // Si no, mostrar el login
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos MySQL: " + e.getMessage());
        }
    }

    public static void mostrarLogin() {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            logger.error("Error al establecer el Look and Feel: ", ex);
        }

        JFrame frame = new JFrame("Bienvenido - " +EMPRESA_NAME);
        ImageIcon icon = LOGO_EMPRESA;
        if (icon.getImage() != null) {
            Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            frame.setIconImage(scaledImage);
        }

        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO_PRINCIPAL);
        panel.setBorder(BorderFactory.createEmptyBorder(THIRTY, THIRTY, THIRTY, THIRTY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(FIFTEEN, FIFTEEN, FIFTEEN, FIFTEEN);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        /*JLabel logoLabel = new JLabel();
        ImageIcon logo = LOGO_EMPRESA;        Image img = logo.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(img));*/

        JLabel userLabel = new JLabel("Encargado de caja");
        userLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        userLabel.setForeground(Color.BLACK);

        JTextField userField = new JTextField(TWENTY);
        userField.setFont(new Font("Segoe UI Variable", Font.PLAIN, EIGHTEEN));
        userField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, TWO));

        JButton loginButton = new JButton("Iniciar Día");
        loginButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        JButton adminButton = new JButton("Administrador");
        adminButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        adminButton.setBackground(new Color(0, 120, 212));
        adminButton.setForeground(Color.WHITE);
        adminButton.setFocusPainted(false);
        adminButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));


        /*gbc.gridx = ZERO;
        gbc.gridy = ZERO;
        gbc.gridwidth = TWO;
        panel.add(logoLabel, gbc);*/

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
            frame.dispose();
            String nombreUsuario = userField.getText();
            if (!nombreUsuario.isEmpty()) {
                Connection connection = null;
                try {
                    connection = DatabaseUserManager.connect();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                registrarDia(connection, nombreUsuario);


                mainUser();
            } else {
                JOptionPane.showMessageDialog(frame, "Por favor ingresa un nombre de usuario.");
            }
        });

        frame.getRootPane().setDefaultButton(loginButton);
        adminButton.addActionListener(e -> adminPassword(frame));
    }
}






