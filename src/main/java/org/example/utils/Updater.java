package org.example.utils;

import org.json.JSONObject;

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

public class Updater {

    private Updater() {}

    private static final String CURRENT_VERSION = "v1.1.3";
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
            String downloadUrl = release.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");

            if (isNewVersion(remoteVersion, CURRENT_VERSION)) {
                showProgressWindow();
                downloadFileWithProgress(downloadUrl, TEMP_EXE_NAME);
                createUpdateScript();
                hideProgressWindow();
                launchUpdateScript();
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private static boolean isNewVersion(String remote, String local) {
        return remote.compareTo(local) > 0;
    }

    private static void downloadFileWithProgress(String fileURL, String saveAs) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int contentLength = httpConn.getContentLength();

        try (InputStream in = httpConn.getInputStream();
             FileOutputStream out = new FileOutputStream(saveAs)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            int downloaded = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                final int progress = (int) ((downloaded / (float) contentLength) * 100);

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
                "start \"\" /min powershell -WindowStyle Hidden -Command \"Start-Process -WindowStyle Minimized -FilePath 'Licorera CR.exe\"\n'\"",
                "exit"
        ));
        Files.write(Paths.get("update_launcher.bat"), script.getBytes(StandardCharsets.UTF_8));
    }

    private static void launchUpdateScript() throws IOException {
        new ProcessBuilder("powershell", "-WindowStyle", "Hidden", "-Command", "Start-Process -FilePath update_launcher.bat -WindowStyle Hidden").start();
    }

    private static void showProgressWindow() {
        progressFrame = new JFrame("Actualizando");
        progressFrame.setUndecorated(true);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        JLabel label = new JLabel("Descargando actualización...", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Fuente más grande

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        progressFrame.setSize(350, 50);
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

