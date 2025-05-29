package org.example.utils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.example.manager.usermanager.FacturacionUserManager.enviarMensaje;
import static org.example.utils.Constants.*;

public class Updater {

    private Updater() {}
    private static final Logger logger =  LoggerFactory.getLogger(Updater.class);

    private static final String CURRENT_VERSION = "v1.1.7";
    private static final String TEMP_EXE_NAME = "update_temp.exe";
    private static final String APP_EXE_NAME = "Licorera CR.exe";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/juanpablo2025/AdminLicorera/releases/latest";

    private static JFrame progressFrame;
    private static JProgressBar progressBar;

    public static void checkForUpdates() {
        if (!hayConexionInternet()) {
            return;
        }
        try {
            JSONObject release = fetchLatestRelease();
            String remoteVersion = release.getString("tag_name").trim();
            String downloadUrl = release.getJSONArray("assets").getJSONObject(ZERO).getString("browser_download_url");

            if (isNewVersion(remoteVersion)) {
                showProgressWindow();
                downloadFileWithProgress(downloadUrl);
                createUpdateScript();
                hideProgressWindow();
                launchUpdateScript();
                System.exit(ZERO);
            }
        } catch (Exception e) {
            logger.error("Error al verificar actualizaciones: ", e);
            hideProgressWindow();
        }
    }

    public static boolean hayConexionInternet() {
        try {
            URL url = new URL("https://github.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            return false;
        }
    }

    private static JSONObject fetchLatestRelease() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        String json = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new JSONObject(json);
    }

    private static boolean isNewVersion(String remote) {
        return remote.compareTo(Updater.CURRENT_VERSION) > ZERO;
    }

    private static void downloadFileWithProgress(String fileURL) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int contentLength = httpConn.getContentLength();

        try (InputStream in = httpConn.getInputStream();
             FileOutputStream out = new FileOutputStream(Updater.TEMP_EXE_NAME)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            int downloaded = ZERO;

            while ((bytesRead = in.read(buffer)) != -ONE) {
                out.write(buffer, ZERO, bytesRead);
                downloaded += bytesRead;
                final int progress = (int) ((downloaded / (float) contentLength) * ONE_HUNDRED);

                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }
        }
    }

    private static void createUpdateScript() throws IOException {
        String script = String.join("\r\n", Arrays.asList(
                "@echo off",
                "timeout /t 2 > nul",
                "taskkill /F /IM \"" + APP_EXE_NAME + "\" >nul 2>&1",
                "move /Y \"" + TEMP_EXE_NAME + "\" \"" + APP_EXE_NAME + "\"",
                "powershell -WindowStyle Hidden -Command \"Start-Process -FilePath 'Licorera CR.exe'\"",
                "exit"
        ));
        Files.writeString(Paths.get("update_launcher.bat"), script);
    }

    private static void launchUpdateScript() throws IOException {
        new ProcessBuilder("powershell", "-WindowStyle", "Hidden", "-Command", "Start-Process -FilePath update_launcher.bat -WindowStyle Hidden").start();
    }

    private static void showProgressWindow() {
        progressFrame = new JFrame("Actualizando");
        progressFrame.setUndecorated(true);
        progressBar = new JProgressBar(ZERO, ONE_HUNDRED);
        progressBar.setStringPainted(true);
        progressBar.setValue(ZERO);

        JLabel label = new JLabel("Descargando actualización...", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, SIXTEEN));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        progressFrame.setSize(350, FIFTY);
        progressFrame.setLocationRelativeTo(null);
        progressFrame.setAlwaysOnTop(true);
        progressFrame.setContentPane(panel);
        progressFrame.setVisible(true);

    }

    private static void hideProgressWindow() {
        if (progressFrame != null) {
            SwingUtilities.invokeLater(() -> progressFrame.dispose());
        }
    }
}

