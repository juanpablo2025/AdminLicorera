package org.example.utils;

import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class Updater {

    private static final String CURRENT_VERSION = "v1.0.0"; // Cambiar en cada release
    private static final String TEMP_EXE_NAME = "update_temp.exe";
    private static final String APP_EXE_NAME = "Licorera CR.exe";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/juanpablo2025/AdminLicorera/releases/latest";

    public static void checkForUpdates() {
        try {
            JSONObject release = fetchLatestRelease();

            String remoteVersion = release.getString("tag_name").trim();
            String downloadUrl = release.getJSONArray("assets")
                    .getJSONObject(0)
                    .getString("browser_download_url");

            System.out.println("Versión actual: " + CURRENT_VERSION);
            System.out.println("Versión remota: " + remoteVersion);

            if (isNewVersion(remoteVersion, CURRENT_VERSION)) {
                System.out.println("Actualizando a nueva versión: " + remoteVersion);
                downloadFile(downloadUrl, TEMP_EXE_NAME);
                createUpdateScript();
                launchUpdateScript();
                System.exit(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private static void downloadFile(String fileURL, String saveAs) throws IOException {
        try (InputStream in = new URL(fileURL).openStream()) {
            Files.copy(in, Paths.get(saveAs), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void createUpdateScript() throws IOException {
        String script = String.join("\r\n", Arrays.asList(
                "@echo off",
                "timeout /t 2 > nul",
                "taskkill /F /IM \"" + APP_EXE_NAME + "\"",
                "move /Y \"" + TEMP_EXE_NAME + "\" \"" + APP_EXE_NAME + "\"",
                "start \"\" \"" + APP_EXE_NAME + "\""
        ));
        Files.write(Paths.get("update_launcher.bat"), script.getBytes(StandardCharsets.UTF_8));
    }

    private static void launchUpdateScript() throws IOException {
        new ProcessBuilder("cmd", "/c", "start", "update_launcher.bat").start();
    }
}
