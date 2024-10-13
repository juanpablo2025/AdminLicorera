package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static java.awt.Component.CENTER_ALIGNMENT;
import static org.example.manager.userManager.ExcelUserManager.hayRegistroDeHoy;
import static org.example.manager.userManager.ExcelUserManager.registrarDia;
import static org.example.manager.userManager.MainUserManager.crearDirectorios;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.ui.uiUser.UIUserMain.mainUser;


public class Main {


    public static void main(String[] args) {
        crearDirectorios();


        if (hayRegistroDeHoy()) {
            mainUser(); // Si hay registro, abrir el panel de usuario
        } else {
            mostrarLogin(); // Si no, mostrar el login
        }
    }


    // Mostrar login estilizado
    public static void mostrarLogin() {
        try {
            // Aplicar FlatLaf para estilo moderno
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Login - Calculadora Administrador");
        frame.setSize(600, 300);  // Ampliar la ventana para más espacio
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout()); // Usamos GridBagLayout para mejor control

        // Panel principal para alineación
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245)); // Fondo gris claro
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Márgenes

        // Configuración de GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espaciado entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta y campo de usuario
        JLabel userLabel = new JLabel("Empleado");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        JTextField userField = new JTextField(20);  // Hacer el campo de texto más ancho

// Botón para iniciar sesión
        JButton loginButton = new JButton("Iniciar Día");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(new Color(100, 149, 237)); // Azul claro para el botón
        loginButton.setForeground(Color.WHITE);

        // Botón para acceder al administrador
        JButton adminButton = new JButton("Administrador");
        adminButton.setFont(new Font("Arial", Font.BOLD, 18));
        adminButton.setBackground(new Color(70, 130, 180)); // Azul oscuro
        adminButton.setForeground(Color.WHITE);
        adminButton.setPreferredSize(new Dimension(60, 20));  // Tamaño fijo

        // Añadir componentes al panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;  // Ocupa ambas columnas
        panel.add(userLabel, gbc);  // Etiqueta arriba

        gbc.gridy = 1;
        panel.add(userField, gbc);  // Campo de texto debajo de la etiqueta

        gbc.gridy = 2;
        gbc.gridwidth = 1;  // Resetear a una columna
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        gbc.gridy = 3;
        panel.add(adminButton, gbc);

        // Añadir panel al frame
        frame.add(panel);
        frame.setLocationRelativeTo(null); // Centrar ventana
        frame.setVisible(true);

        // Acción para iniciar sesión
        loginButton.addActionListener(e -> {
            String nombreUsuario = userField.getText();
            if (!nombreUsuario.isEmpty()) {
                registrarDia(nombreUsuario);
                JOptionPane.showMessageDialog(frame, "¡Día iniciado correctamente!");
                frame.dispose(); // Cerrar login
                mainUser(); // Abrir el menú de usuario
            } else {
                JOptionPane.showMessageDialog(frame, "Por favor ingresa un nombre de usuario.");
            }
        });

        // Acción para acceder al administrador
        adminButton.addActionListener(e -> {
            /*String password = JOptionPane.showInputDialog(frame, "Ingresa la contraseña de administrador:");
            if ("admin2024".equals(password)) {
                JOptionPane.showMessageDialog(frame, "Accediendo al administrador...");
                frame.dispose(); // Cerrar login*/
                mainAdmin(); // Lanzar el panel de administrador
            frame.dispose(); // Cerrar login
           /* } else {
                JOptionPane.showMessageDialog(frame, "Contraseña incorrecta.");
            }*/
        });
    }


}
