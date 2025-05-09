package org.example.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SiigoInvoice {



    private static final String AUTH_URL = "https://api.siigo.com/auth";
    private static final String INVOICE_URL = "https://api.siigo.com/v1/invoices";

    private final String clientId = "TU_CLIENT_ID";
    private final String clientSecret = "TU_CLIENT_SECRET";
    private final String username = "TU_CORREO_SIIGO";
    private final String accessKey = "TU_CLAVE_API";

    private String accessToken;

    public void authenticate() throws IOException {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpURLConnection conn = (HttpURLConnection) new URL(AUTH_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject authBody = new JSONObject();
        authBody.put("username", username);
        authBody.put("access_key", accessKey);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(authBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(response);
            accessToken = json.getString("access_token");
            System.out.println("✔ Autenticación exitosa: " + accessToken);
        } else {
            throw new IOException("Error de autenticación: " + conn.getResponseCode());
        }
    }

    public void crearFactura(JSONObject facturaJson) throws IOException {
        if (accessToken == null) authenticate();

        HttpURLConnection conn = (HttpURLConnection) new URL(INVOICE_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(facturaJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 201) {
            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("✔ Factura creada: " + response);
        } else {
            String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            System.err.println("❌ Error al crear factura: " + error);
        }
    }
}
