package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.ui.uiuser.UIUserMain;
import org.example.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Objects;

import static org.example.manager.userDBManager.DatabaseUserManager.URL;
import static org.example.manager.userDBManager.DatabaseUserManager.registrarDia;
import static org.example.manager.usermanager.ExcelUserManager.*;
import static org.example.manager.usermanager.MainUserManager.crearDirectorios;
import static org.example.ui.uiadmin.UIMainAdmin.adminPassword;
import static org.example.ui.uiuser.UIUserMain.mainUser;
import static org.example.utils.Constants.*;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException {
        crearDirectorios();
        Connection conn = DriverManager.getConnection(URL);
        // Verificar si el archivo existe; si no, crear uno nuevo
       /* File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();  // Llama al método que crea el archivo si no existe
        }*/
        if (DatabaseUserManager.hayRegistroDeHoy(conn)) {
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
            logger.error("Error al establecer el Look and Feel: ", ex);
        }

        JFrame frame = new JFrame("Bienvenido - " +EMPRESA_NAME);
        ImageIcon icon = LOGO_EMPRESA;
        if (icon.getImage() != null) {
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

        JLabel logoLabel = new JLabel();
        ImageIcon logo = LOGO_EMPRESA;        Image img = logo.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(img));

        JLabel userLabel = new JLabel("Encargado de caja");
        userLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        userLabel.setForeground(Color.BLACK);

        JTextField userField = new JTextField(TWENTY);
        userField.setFont(new Font("Segoe UI Variable", Font.PLAIN, EIGHTEEN));
        userField.setBorder(BorderFactory.createLineBorder(Color.RED, TWO));

        JButton loginButton = new JButton("Iniciar Día");
        loginButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        loginButton.setBackground(Color.RED);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        JButton adminButton = new JButton("Administrador");
        adminButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        adminButton.setBackground(Color.BLACK);
        adminButton.setForeground(Color.WHITE);
        adminButton.setFocusPainted(false);
        adminButton.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));


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
            frame.dispose();
            String nombreUsuario = userField.getText();
            if (!nombreUsuario.isEmpty()) {
                Connection connection = null;
                try {
                    connection = DriverManager.getConnection(DatabaseUserManager.URL);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                registrarDia(connection, nombreUsuario);

                ImageIcon iconEmpleado = new ImageIcon(UIUserMain.class.getResource("/icons/assistant/Bienvenida.png"));
                if (iconEmpleado.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    iconEmpleado = null;
                }


                String saludo;
                LocalTime horaActual = LocalTime.now();
                if (horaActual.isBefore(LocalTime.of(12, 0))) {
                    saludo = "¡Buenos días, " + nombreUsuario + "!";
                } else if (horaActual.isBefore(LocalTime.of(18, 0))) {
                    saludo = "¡Buenas tardes, " + nombreUsuario + "!";
                } else {
                    saludo = "¡Buenas noches, " + nombreUsuario + "!";
                }

                JLabel textLabel = new JLabel(saludo);
                textLabel.setHorizontalAlignment(SwingConstants.CENTER);
                textLabel.setFont(ALERT_FONT);

                JPanel panelEmpleado = new JPanel();
                panelEmpleado.setLayout(new BoxLayout(panelEmpleado, BoxLayout.Y_AXIS));
                panelEmpleado.add(textLabel);
                panelEmpleado.add(Box.createVerticalStrut(10));
                panelEmpleado.add(new JLabel(iconEmpleado));

                JOptionPane pane = new JOptionPane(panelEmpleado,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        new Object[]{},
                        null);

                JDialog dialog = pane.createDialog(frame, "¡Ventana de Bienvenida!");
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                SwingUtilities.invokeLater(() -> dialog.setVisible(true));

                Timer timer = new Timer(2000, evt -> {
                    dialog.dispose();
                    mainUser();
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                JOptionPane.showMessageDialog(frame, "Por favor ingresa un nombre de usuario.");
            }
        });

        frame.getRootPane().setDefaultButton(loginButton);
        adminButton.addActionListener(e -> adminPassword(frame));
    }
}






