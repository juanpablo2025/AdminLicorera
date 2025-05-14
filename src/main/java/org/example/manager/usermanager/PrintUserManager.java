package org.example.manager.usermanager;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.example.manager.adminmanager.ConfigAdminManager;
import org.example.ui.uiadmin.UIAdminFacturas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.*;
import javax.swing.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;


public class PrintUserManager {

    private PrintUserManager() {}

    private static final Logger logger =  LoggerFactory.getLogger(PrintUserManager.class);


    public static void abrirPDF(String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFilePath);
            }
        } catch (IOException e) {
            logger.error("Error al abrir el PDF: ", e);
        }
    }

    public static void imprimirPDF(String pdfFilePath) {
        try {
            // Cargar el archivo PDF
            PDDocument document = Loader.loadPDF(new File(pdfFilePath));

            // Obtener la impresora guardada en config.properties
            String printerName = ConfigAdminManager.getPrinterName();
            PrintService selectedPrintService = null;

            // Buscar la impresora en el sistema
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            for (PrintService service : printServices) {
                if (service.getName().equalsIgnoreCase(printerName)) {
                    selectedPrintService = service;
                    break;
                }
            }

            // Si no se encuentra la impresora guardada, usar la predeterminada
            if (selectedPrintService == null) {
                selectedPrintService = PrintServiceLookup.lookupDefaultPrintService();
            }

            // Crear un trabajo de impresión sin mostrar cuadro de diálogo
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(selectedPrintService);
            printerJob.setPageable(new PDFPageable(document));

            // Realizar la impresión
            printerJob.print();
            // Cerrar el documento
            document.close();
        } catch (IOException | PrinterException e) {
            logger.error("Error al imprimir el PDF: ", e);
            JOptionPane.showMessageDialog(UIAdminFacturas.getAdminBillsPanel(), "Error al imprimir el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: ", e);
            JOptionPane.showMessageDialog(UIAdminFacturas.getAdminBillsPanel(), "Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
