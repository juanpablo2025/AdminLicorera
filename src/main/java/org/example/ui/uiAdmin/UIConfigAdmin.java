package org.example.ui.uiAdmin;

import org.example.manager.adminManager.ConfigAdminManager;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;

public class UIConfigAdmin {
    public static void showConfigDialog() {
        // Opciones de tamaño de papel
        String[] paperSizeOptions = {"48mm","58mm", "80mm", "A4"};
        String currentPaperSize = ConfigAdminManager.getPaperSize();

        // Opciones de salida
        String[] outputOptions = {"PDF", "IMPRESORA"};
        String currentOutput = ConfigAdminManager.getOutputType();

        // Obtener lista de impresoras disponibles
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        String[] printerNames = new String[printServices.length];
        for (int i = 0; i < printServices.length; i++) {
            printerNames[i] = printServices[i].getName();
        }

        String currentPrinter = ConfigAdminManager.getPrinterName();

        // Crear un panel para mostrar las opciones en un solo diálogo
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Selector de tamaño de papel
        JComboBox<String> paperSizeComboBox = new JComboBox<>(paperSizeOptions);
        paperSizeComboBox.setSelectedItem(currentPaperSize);
        panel.add(new JLabel("Seleccione el tamaño de papel:"));
        panel.add(paperSizeComboBox);

        // Selector de tipo de salida
        JComboBox<String> outputComboBox = new JComboBox<>(outputOptions);
        outputComboBox.setSelectedItem(currentOutput);
        panel.add(new JLabel("Seleccione el método de salida:"));
        panel.add(outputComboBox);

        // Selector de impresora (inicialmente deshabilitado si la opción es "PDF")
        JComboBox<String> printerComboBox = new JComboBox<>(printerNames);
        printerComboBox.setSelectedItem(currentPrinter);
        printerComboBox.setEnabled(currentOutput.equals("IMPRESORA"));
        panel.add(new JLabel("Seleccione la impresora:"));
        panel.add(printerComboBox);

        // Agregar listener para habilitar o deshabilitar la selección de impresora
        outputComboBox.addActionListener(e -> {
            printerComboBox.setEnabled(outputComboBox.getSelectedItem().equals("IMPRESORA"));
        });

        // Mostrar cuadro de diálogo
        int result = JOptionPane.showConfirmDialog(null, panel, "Configuración de Impresora", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            // Guardar configuraciones
            String selectedPaperSize = (String) paperSizeComboBox.getSelectedItem();
            String selectedOutput = (String) outputComboBox.getSelectedItem();
            String selectedPrinter = (String) printerComboBox.getSelectedItem();

            ConfigAdminManager.setPaperSize(selectedPaperSize);
            ConfigAdminManager.setOutputType(selectedOutput);
            if (selectedOutput.equals("IMPRESORA")) {
                ConfigAdminManager.setPrinterName(selectedPrinter);
            }

            JOptionPane.showMessageDialog(null, "Configuración guardada con éxito.");

        }
    }
}

