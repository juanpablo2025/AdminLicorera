package org.example.ui.uiadmin;

import com.fazecast.jSerialComm.SerialPort;
import org.example.manager.adminmanager.ConfigAdminManager;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.*;
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
        String[] printerNames = new String[printServices.length];
        for (int i = ZERO; i < printServices.length; i++) {
            printerNames[i] = printServices[i].getName();
        }
        String currentPrinter = ConfigAdminManager.getPrinterName();

        SerialPort[] ports = SerialPort.getCommPorts();
        String[] dataphoneOptions = new String[ports.length + ONE];
        dataphoneOptions[ZERO] = "Ninguno";
        for (int i = ZERO; i < ports.length; i++) {
            dataphoneOptions[i + ONE] = ports[i].getSystemPortName() + " - " + ports[i].getDescriptivePortName();
        }
        String currentDataphone = ConfigAdminManager.getSelectedDataphone();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(TWENTY, FORTY, TWENTY, FORTY));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(TEN, TEN, TEN, TEN);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = ZERO;
        gbc.gridy = ZERO;

        JLabel paperLabel = new JLabel("Seleccione el tamaño de papel:");
        panel.add(paperLabel, gbc);

        gbc.gridy++;
        JComboBox<String> paperSizeComboBox = new JComboBox<>(paperSizeOptions);
        paperSizeComboBox.setSelectedItem(currentPaperSize);
        paperSizeComboBox.setPreferredSize(new Dimension(300, THIRTY));
        panel.add(paperSizeComboBox, gbc);

        gbc.gridy++;
        JLabel outputLabel = new JLabel("Seleccione el método de salida:");
        panel.add(outputLabel, gbc);

        gbc.gridy++;
        JComboBox<String> outputComboBox = new JComboBox<>(outputOptions);
        outputComboBox.setSelectedItem(currentOutput);
        outputComboBox.setPreferredSize(new Dimension(300, THIRTY));
        panel.add(outputComboBox, gbc);

        gbc.gridy++;
        JLabel printerLabel = new JLabel("Seleccione la impresora:");
        panel.add(printerLabel, gbc);

        gbc.gridy++;
        JComboBox<String> printerComboBox = new JComboBox<>(printerNames);
        printerComboBox.setSelectedItem(currentPrinter);
        printerComboBox.setEnabled(currentOutput.equals(IMPRESORA));
        printerComboBox.setPreferredSize(new Dimension(300, THIRTY));
        panel.add(printerComboBox, gbc);

        gbc.gridy++;
        JLabel dataphoneLabel = new JLabel("Seleccione el datáfono:");
        panel.add(dataphoneLabel, gbc);

        gbc.gridy++;
        JComboBox<String> dataphoneComboBox = new JComboBox<>(dataphoneOptions);
        dataphoneComboBox.setSelectedItem(currentDataphone);
        dataphoneComboBox.setPreferredSize(new Dimension(300, 30));
        panel.add(dataphoneComboBox, gbc);

        gbc.gridy++;
        JCheckBox enableMessageCheckBox = new JCheckBox("Activar envío de mensajes");
        enableMessageCheckBox.setFont(enableMessageCheckBox.getFont().deriveFont(Font.PLAIN, 14f));
        enableMessageCheckBox.setSelected(ConfigAdminManager.isMessageSendingEnabled());
        panel.add(enableMessageCheckBox, gbc);

        gbc.gridy++;
        JCheckBox trmCheckBox = new JCheckBox("Activar consulta TRM");
        trmCheckBox.setFont(trmCheckBox.getFont().deriveFont(Font.PLAIN, 14f));
        trmCheckBox.setSelected(ConfigAdminManager.isTrmEnabled());
        panel.add(trmCheckBox, gbc);

        outputComboBox.addActionListener(e -> printerComboBox.setEnabled(Objects.equals(outputComboBox.getSelectedItem(), IMPRESORA)));

        gbc.gridy++;
        JCheckBox feCheckBox = new JCheckBox("Activar facturación electrónica");
        feCheckBox.setFont(feCheckBox.getFont().deriveFont(Font.PLAIN, 14f));
        feCheckBox.setSelected(ConfigAdminManager.isElectronicBillingEnabled());
        panel.add(feCheckBox, gbc);

        gbc.gridy++;
        JLabel userLabel = new JLabel("Usuario Siigo:");
        panel.add(userLabel, gbc);
        gbc.gridy++;
        JTextField userField = new JTextField(ConfigAdminManager.getSiigoUsername(), TWENTY);
        panel.add(userField, gbc);

        gbc.gridy++;
        JLabel keyLabel = new JLabel("Clave API:");
        panel.add(keyLabel, gbc);
        gbc.gridy++;
        JTextField keyField = new JTextField(ConfigAdminManager.getSiigoAccessKey(), TWENTY);
        panel.add(keyField, gbc);

        gbc.gridy++;
        JLabel clientIdLabel = new JLabel("Client ID:");
        panel.add(clientIdLabel, gbc);
        gbc.gridy++;
        JTextField clientIdField = new JTextField(ConfigAdminManager.getSiigoClientId(), TWENTY);
        panel.add(clientIdField, gbc);

        gbc.gridy++;
        JLabel clientSecretLabel = new JLabel("Client Secret:");
        panel.add(clientSecretLabel, gbc);
        gbc.gridy++;
        JTextField clientSecretField = new JTextField(ConfigAdminManager.getSiigoClientSecret(), TWENTY);
        panel.add(clientSecretField, gbc);

        outputComboBox.addActionListener(e -> printerComboBox.setEnabled(Objects.equals(outputComboBox.getSelectedItem(), IMPRESORA)));

        gbc.gridy++;
        JButton saveButton = new JButton("Guardar configuración");
        saveButton.setPreferredSize(new Dimension(200, THIRTY_FIVE));
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD, 14f));
        saveButton.addActionListener(e -> {
            ConfigAdminManager.setPaperSize((String) paperSizeComboBox.getSelectedItem());
            ConfigAdminManager.setOutputType((String) outputComboBox.getSelectedItem());
            if (Objects.equals(outputComboBox.getSelectedItem(), IMPRESORA)) {
                ConfigAdminManager.setPrinterName((String) printerComboBox.getSelectedItem());
            }
            ConfigAdminManager.setSelectedDataphone((String) dataphoneComboBox.getSelectedItem());
            ConfigAdminManager.setMessageSendingEnabled(enableMessageCheckBox.isSelected());
            ConfigAdminManager.setTrmEnabled(trmCheckBox.isSelected());
            ConfigAdminManager.setElectronicBillingEnabled(feCheckBox.isSelected());
            ConfigAdminManager.setSiigoUsername(userField.getText().trim());
            ConfigAdminManager.setSiigoAccessKey(keyField.getText().trim());
            ConfigAdminManager.setSiigoClientId(clientIdField.getText().trim());
            ConfigAdminManager.setSiigoClientSecret(clientSecretField.getText().trim());
            JOptionPane.showMessageDialog(panel, "Configuración guardada con éxito.");
        });
        panel.add(saveButton, gbc);

        return panel;
    }


}

