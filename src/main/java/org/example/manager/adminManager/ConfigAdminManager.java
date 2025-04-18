package org.example.manager.adminManager;

import java.io.*;
import java.util.Properties;

public class ConfigAdminManager {
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
            //System.out.println("Error al crear el archivo de configuración: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            messageSendingEnabled = Boolean.parseBoolean(properties.getProperty("message_sending_enabled", "true"));
        } catch (IOException e) {
            //System.out.println("No se pudo cargar el archivo de configuración: " + e.getMessage());
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
            //System.out.println("Error al guardar la configuración: " + e.getMessage());
        }
    }

    public static String getSelectedDataphone() {
        return properties.getProperty("selected_dataphone", "Dataphone 1");
    }

    public static void setSelectedDataphone(String selectedItem) {
        properties.setProperty("selected_dataphone", selectedItem);
        saveProperties();
    }
}