package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.UpdateMetaData;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static java.awt.Component.CENTER_ALIGNMENT;
import static javax.xml.transform.OutputKeys.VERSION;
import static org.example.manager.userManager.ExcelUserManager.hayRegistroDeHoy;
import static org.example.manager.userManager.ExcelUserManager.registrarDia;
import static org.example.manager.userManager.MainUserManager.crearDirectorios;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.ui.uiUser.UIUserMain.mainUser;


public class Main {

    private static final String METADATA_URL = "https://drive.google.com/uc?id=14dojyy1B4PEDNUYZLE9Y8-i9ENeBsHug";
    private static final String LOCAL_DIR = "C:\\Users\\DesktopPC\\Calculadora del Administrador"; // Directorio específico
    private static final String JAR_NAME = "inventario licorera la 70.jar"; // Nombre fijo del archivo JAR
    private static final String VERSION_FILE = "version.json"; // Archivo para manejar la versión local

    public static void main(String[] args) {

        verificarYActualizar();

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

    private static void verificarYActualizar() {
        try {
            System.out.println("Verificando actualizaciones...");
JOptionPane.showMessageDialog(null, "Verificando actualizaciones" + VERSION);
            // Leer la versión local
            String localVersion = leerVersionLocal();
            if (localVersion == null) {
                System.out.println("No se encontró versión local, asumiendo versión 0.0.");
                localVersion = "0.0";
            }
            System.out.println("Versión local detectada: " + localVersion);

            // Descargar y parsear el archivo de metadatos
            String metadataContent = descargarArchivoComoTexto(METADATA_URL);
            UpdateMetaData metadata = parsearMetadata(metadataContent);
            System.out.println("metadata version: "+metadata.getVersion());
            // Comparar versiones
            if (localVersion.compareTo(metadata.getVersion()) < 0) {
                System.out.println("Nueva versión disponible: " + metadata.getVersion());
                System.out.println("Descargando actualización...");
                JOptionPane.showMessageDialog(null, "Nueva versión disponible: " + metadata.getVersion());

                String tempJarPath = LOCAL_DIR + File.separator + JAR_NAME;
                descargarArchivo(metadata.getUrl(), tempJarPath);

                System.out.println("Actualización descargada: " + tempJarPath);
JOptionPane.showMessageDialog(null, "Actualización descargada: " + tempJarPath);
                // Actualizar la versión local
                guardarVersionLocal(metadata.getVersion());
                System.out.println("Versión actualizada a: " + metadata.getVersion());

                // Reiniciar la aplicación
                System.out.println("Reiniciando la aplicación...");
                Runtime.getRuntime().exec("java -jar " + tempJarPath);

            } else {
                System.out.println("La aplicación está actualizada.");
            }
        } catch (Exception e) {
            System.out.println("No se pudo verificar actualizaciones: " + e.getMessage());
        }
    }

    private static String leerVersionLocal() {
        Path versionFilePath = Paths.get(LOCAL_DIR, VERSION_FILE);
        if (!Files.exists(versionFilePath)) {
            return null;
        }
        try {
            return Files.readString(versionFilePath).trim();
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de versión local: " + e.getMessage());
            return null;
        }
    }

    private static void guardarVersionLocal(String version) {
        Path versionFilePath = Paths.get(LOCAL_DIR, VERSION_FILE);
        try {
            Files.writeString(versionFilePath, version, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Versión local guardada en " + VERSION_FILE);
        } catch (IOException e) {
            System.err.println("Error guardando el archivo de versión local: " + e.getMessage());
        }
    }

    private static String descargarArchivoComoTexto(String url) throws IOException {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private static void descargarArchivo(String url, String destino) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(destino)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static UpdateMetaData parsearMetadata(String jsonContent) {
        Gson gson = new Gson();
        return gson.fromJson(jsonContent, UpdateMetaData.class);
    }
}

