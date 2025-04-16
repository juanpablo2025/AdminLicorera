package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.ui.uiUser.UIUserMain;
import org.example.utils.FormatterHelpers;
import org.example.utils.Updater;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static java.awt.Component.CENTER_ALIGNMENT;
import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.manager.userManager.MainUserManager.crearDirectorios;
import static org.example.ui.uiAdmin.MainAdminUi.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;


public class Main {


    public static void main(String[] args) {
        crearDirectorios();

// Verificar si el archivo existe; si no, crear uno nuevo
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();  // Llama al método que crea el archivo si no existe
        }
        if (hayRegistroDeHoy()) {
            Updater.checkForUpdates();
            mainUser(); // Si hay registro, abrir el panel de usuario
        } else {
            Updater.checkForUpdates();
            mostrarLogin(); // Si no, mostrar el login

        }
    }



        public static void mostrarLogin() {


            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JFrame frame = new JFrame("Bienvenido - Licorera CR");
            // Cargar el icono desde el classpath
            ImageIcon icon = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));

            if (icon.getImage() != null) {
                // Redimensionar la imagen a 64x64 píxeles (ajusta según necesidad)
                Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                frame.setIconImage(scaledImage);
            } else {
                System.out.println("⚠ Error: No se encontró el icono. Verifica la ruta.");
            }
            frame.setSize(400, 650);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridBagLayout());
            frame.setResizable(false);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(250, 240, 230));
            panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Logo
            JLabel logoLabel = new JLabel();
            ImageIcon logo = new ImageIcon(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png"));
            Image img = logo.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));

            // Etiqueta y campo de usuario
            JLabel userLabel = new JLabel("Encargado de caja");
            userLabel.setFont(new Font("Arial", Font.BOLD, 20));
            userLabel.setForeground(Color.BLACK);

            JTextField userField = new JTextField(20);
            userField.setFont(new Font("Arial", Font.PLAIN, 18));
            userField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));




            JButton loginButton = new JButton("Iniciar Día");
            loginButton.setFont(new Font("Arial", Font.BOLD, 20));
            loginButton.setBackground(Color.RED);
            loginButton.setForeground(Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JButton adminButton = new JButton("Administrador");
            adminButton.setFont(new Font("Arial", Font.BOLD, 20));
            adminButton.setBackground(Color.BLACK);
            adminButton.setForeground(Color.WHITE);
            adminButton.setFocusPainted(false);
            adminButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Agregar componentes al panel
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            panel.add(logoLabel, gbc);

            gbc.gridy = 1;
            panel.add(userLabel, gbc);

            gbc.gridy = 2;
            panel.add(userField, gbc);

            gbc.gridy = 3;
            panel.add(loginButton, gbc);

            gbc.gridy = 4;
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

            adminButton.addActionListener(e -> {
                adminPassword(frame);

            });
        }
    }






