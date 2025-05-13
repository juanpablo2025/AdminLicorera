package org.example.manager.adminmanager;

import java.io.*;
import java.util.Properties;

public class ConfigAdminManager {

    private ConfigAdminManager() {}

    public static final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Calculadora del Administrador";
    private static final String CONFIG_FILE = DIRECTORY_PATH + "\\config.properties";
    private static Properties properties = new Properties();
    private static boolean messageSendingEnabled;

    static {
        ensureConfigFileExists();
        loadProperties();
    }

    public static boolean isMessageSendingEnabled() {
        return messageSendingEnabled;
    }

    public static void setMessageSendingEnabled(boolean enabled) {
        messageSendingEnabled = enabled;
        properties.setProperty("message_sending_enabled", String.valueOf(enabled));
        saveProperties();
    }

    public static boolean isTrmEnabled() {
        return Boolean.parseBoolean(properties.getProperty("trm_enabled", "true"));
    }

    public static void setTrmEnabled(boolean enabled) {
        properties.setProperty("trm_enabled", String.valueOf(enabled));
        saveProperties();
    }

    private static void ensureConfigFileExists() {
        try {
            File directory = new File(DIRECTORY_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File configFile = new File(CONFIG_FILE);
            if (!configFile.exists()) {
                configFile.createNewFile();
                try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
                    properties.setProperty("paper_size", "80mm");
                    properties.setProperty("output_type", "PDF");
                    properties.setProperty("printer_name", "Default Printer");
                    properties.setProperty("message_sending_enabled", "true");
                    properties.setProperty("trm_enabled", "true"); // ✅ ahora se guarda
                    properties.store(output, "Configuración de la Aplicación");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            messageSendingEnabled = Boolean.parseBoolean(properties.getProperty("message_sending_enabled", "true"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPaperSize() {
        return properties.getProperty("paper_size", "80mm");
    }

    public static void setPaperSize(String paperSize) {
        properties.setProperty("paper_size", paperSize);
        saveProperties();
    }

    public static String getOutputType() {
        return properties.getProperty("output_type", "PDF");
    }

    public static void setOutputType(String outputType) {
        properties.setProperty("output_type", outputType);
        saveProperties();
    }

    public static String getPrinterName() {
        return properties.getProperty("printer_name", "Default Printer");
    }

    public static void setPrinterName(String printerName) {
        properties.setProperty("printer_name", printerName);
        saveProperties();
    }

    private static void saveProperties() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Configuración de la Aplicación");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSelectedDataphone() {
        return properties.getProperty("selected_dataphone", "Dataphone 1");
    }

    public static void setSelectedDataphone(String selectedItem) {
        properties.setProperty("selected_dataphone", selectedItem);
        saveProperties();
    }

    public static void setSiigoAccessKey(String trim) {
        properties.setProperty("siigo_access_key", trim);
        saveProperties();
    }

    public static void setElectronicBillingEnabled(boolean selected) {
        properties.setProperty("electronic_billing_enabled", String.valueOf(selected));
        saveProperties();
    }

    public static void setSiigoUsername(String trim) {
        properties.setProperty("siigo_username", trim);
        saveProperties();
    }

    public static void setSiigoClientId(String trim) {
        properties.setProperty("siigo_client_id", trim);
        saveProperties();
    }

    public static void setSiigoClientSecret(String trim) {
        properties.setProperty("siigo_client_secret", trim);
        saveProperties();
    }

    public static boolean isElectronicBillingEnabled() {
        return Boolean.parseBoolean(properties.getProperty("electronic_billing_enabled", "false"));
     }

    public static String getSiigoUsername() {
        return properties.getProperty("siigo_username", "default_username");
    }

    public static String getSiigoAccessKey() {
        return properties.getProperty("siigo_access_key", "default_access_key");
    }

    public static String getSiigoClientId() {
        return properties.getProperty("siigo_client_id", "default_client_id");
    }

    public static String getSiigoClientSecret() {
        return properties.getProperty("siigo_client_secret", "default_client_secret");
    }
}