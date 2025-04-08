package org.example.utils;

import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class Updater {



    private static final String VERSION_FILE = "version.txt";
    private static final String UPDATE_JSON_URL = "https://raw.githubusercontent.com/juanpablo2025/AdminLicorera/main/update.json"; // 👈 cambia esto
    private static final String TEMP_EXE_NAME = "update_temp.exe";
    private static final String APP_EXE_NAME = "Licorera CR.exe";

    public static void update(){
        try {
            String localVersion = readLocalVersion();
            JSONObject remoteInfo = fetchRemoteInfo();

            String remoteVersion = remoteInfo.getString("latest_version");
            String downloadUrl = remoteInfo.getString("download_url");

            if (isNewVersion(remoteVersion, localVersion)) {
                int opt = JOptionPane.showConfirmDialog(null,
                        "Hay una nueva versión disponible (" + remoteVersion + "). ¿Actualizar ahora?",
                        "Actualización disponible",
                        JOptionPane.YES_NO_OPTION);

                if (opt == JOptionPane.YES_OPTION) {
                    downloadFile(downloadUrl, TEMP_EXE_NAME);
                    createUpdateScript();
                    launchUpdateScript();
                    System.exit(0);
                }
            } else {
                System.out.println("La app está actualizada.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readLocalVersion() throws IOException {
        return Files.readString(Paths.get(VERSION_FILE)).trim();
    }

    private static JSONObject fetchRemoteInfo() throws IOException {
        URL url = new URL(UPDATE_JSON_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String json = new String(conn.getInputStream().readAllBytes());
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
                "taskkill /F /IM " + APP_EXE_NAME,
                "move /Y " + TEMP_EXE_NAME + " " + APP_EXE_NAME,
                "start \"\" \"" + APP_EXE_NAME + "\""
        ));
        Files.write(Paths.get("update_launcher.bat"), script.getBytes());
    }

    private static void launchUpdateScript() throws IOException {
        new ProcessBuilder("cmd", "/c", "start", "update_launcher.bat").start();
    }



}
