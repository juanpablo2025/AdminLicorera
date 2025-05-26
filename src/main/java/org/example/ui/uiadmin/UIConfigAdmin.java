package org.example.ui.uiadmin;

import com.fazecast.jSerialComm.SerialPort;
import org.example.manager.adminmanager.ConfigAdminManager;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

import static org.example.utils.Constants.*;

public class UIConfigAdmin {

    private UIConfigAdmin() {}

    public static JPanel createPrinterConfigPanel() {
        String[] paperSizeOptions = {"48mm", "58mm", "80mm", "A4"};
        String currentPaperSize = ConfigAdminManager.getPaperSize();

        String[] outputOptions = {"PDF", IMPRESORA};
        String currentOutput = ConfigAdminManager.getOutputType();

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        String[] printerNames = Arrays.stream(printServices).map(PrintService::getName).toArray(String[]::new);
        String currentPrinter = ConfigAdminManager.getPrinterName();

        SerialPort[] ports = SerialPort.getCommPorts();
        String[] dataphoneOptions = new String[ports.length + 1];
        dataphoneOptions[0] = "Ninguno";
        for (int i = 0; i < ports.length; i++) {
            dataphoneOptions[i + 1] = ports[i].getSystemPortName() + " - " + ports[i].getDescriptivePortName();
        }
        String currentDataphone = ConfigAdminManager.getSelectedDataphone();

        // Panel de dos columnas
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel(new GridBagLayout());
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Columna Izquierda
        leftPanel.add(new JLabel("Tamaño de papel:"), gbc);
        gbc.gridy++;
        JComboBox<String> paperSizeComboBox = new JComboBox<>(paperSizeOptions);
        paperSizeComboBox.setSelectedItem(currentPaperSize);
        leftPanel.add(paperSizeComboBox, gbc);

        gbc.gridy++;
        leftPanel.add(new JLabel("Método de salida:"), gbc);
        gbc.gridy++;
        JComboBox<String> outputComboBox = new JComboBox<>(outputOptions);
        outputComboBox.setSelectedItem(currentOutput);
        leftPanel.add(outputComboBox, gbc);

        gbc.gridy++;
        leftPanel.add(new JLabel("Impresora:"), gbc);
        gbc.gridy++;
        JComboBox<String> printerComboBox = new JComboBox<>(printerNames);
        printerComboBox.setSelectedItem(currentPrinter);
        printerComboBox.setEnabled(currentOutput.equals(IMPRESORA));
        leftPanel.add(printerComboBox, gbc);

        gbc.gridy++;
        leftPanel.add(new JLabel("Datáfono:"), gbc);
        gbc.gridy++;
        JComboBox<String> dataphoneComboBox = new JComboBox<>(dataphoneOptions);
        dataphoneComboBox.setSelectedItem(currentDataphone);
        leftPanel.add(dataphoneComboBox, gbc);

        // Columna Derecha
        gbc.gridy = 0;
        rightPanel.add(new JCheckBox("Activar envío de mensajes") {{
            setSelected(ConfigAdminManager.isMessageSendingEnabled());
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
        }}, gbc);

        gbc.gridy++;
        rightPanel.add(new JCheckBox("Activar consulta TRM") {{
            setSelected(ConfigAdminManager.isTrmEnabled());
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
        }}, gbc);

        gbc.gridy++;
        JCheckBox feCheckBox = new JCheckBox("Activar facturación electrónica");
        feCheckBox.setFont(feCheckBox.getFont().deriveFont(Font.PLAIN, 14f));
        feCheckBox.setSelected(ConfigAdminManager.isElectronicBillingEnabled());
        rightPanel.add(feCheckBox, gbc);

        gbc.gridy++;
        rightPanel.add(new JLabel("Usuario Siigo:"), gbc);
        gbc.gridy++;
        JTextField userField = new JTextField(ConfigAdminManager.getSiigoUsername(), 20);
        rightPanel.add(userField, gbc);

        gbc.gridy++;
        rightPanel.add(new JLabel("Clave API:"), gbc);
        gbc.gridy++;
        JTextField keyField = new JTextField(ConfigAdminManager.getSiigoAccessKey(), 20);
        rightPanel.add(keyField, gbc);

        gbc.gridy++;
        rightPanel.add(new JLabel("Client ID:"), gbc);
        gbc.gridy++;
        JTextField clientIdField = new JTextField(ConfigAdminManager.getSiigoClientId(), 20);
        rightPanel.add(clientIdField, gbc);

        gbc.gridy++;
        rightPanel.add(new JLabel("Client Secret:"), gbc);
        gbc.gridy++;
        JTextField clientSecretField = new JTextField(ConfigAdminManager.getSiigoClientSecret(), 20);
        rightPanel.add(clientSecretField, gbc);

        gbc.gridy++;
        JButton saveButton = new JButton("Guardar configuración");
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD, 14f));
        saveButton.addActionListener(e -> {
            ConfigAdminManager.setPaperSize((String) paperSizeComboBox.getSelectedItem());
            ConfigAdminManager.setOutputType((String) outputComboBox.getSelectedItem());
            if (Objects.equals(outputComboBox.getSelectedItem(), IMPRESORA)) {
                ConfigAdminManager.setPrinterName((String) printerComboBox.getSelectedItem());
            }
            ConfigAdminManager.setSelectedDataphone((String) dataphoneComboBox.getSelectedItem());
            ConfigAdminManager.setMessageSendingEnabled(((JCheckBox) rightPanel.getComponent(0)).isSelected());
            ConfigAdminManager.setTrmEnabled(((JCheckBox) rightPanel.getComponent(1)).isSelected());
            ConfigAdminManager.setElectronicBillingEnabled(feCheckBox.isSelected());
            ConfigAdminManager.setSiigoUsername(userField.getText().trim());
            ConfigAdminManager.setSiigoAccessKey(keyField.getText().trim());
            ConfigAdminManager.setSiigoClientId(clientIdField.getText().trim());
            ConfigAdminManager.setSiigoClientSecret(clientSecretField.getText().trim());
            JOptionPane.showMessageDialog(panel, "Configuración guardada con éxito.");
        });
        rightPanel.add(saveButton, gbc);

        panel.add(leftPanel);
        panel.add(rightPanel);

        return panel;
    }

}

