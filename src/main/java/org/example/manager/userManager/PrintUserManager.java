package org.example.manager.userManager;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.example.manager.adminManager.ConfigAdminManager;

import javax.print.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;


public class PrintUserManager {

    public static void abrirPDF(String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void imprimirPDF(String pdfFilePath) {
        try {
            // Cargar el archivo PDF
            PDDocument document = PDDocument.load(new File(pdfFilePath));

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

            // Si no hay impresoras disponibles, mostrar mensaje de error
            if (selectedPrintService == null) {
                System.out.println("No hay impresoras disponibles.");
                return;
            }

            // Crear un trabajo de impresión sin mostrar cuadro de diálogo
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(selectedPrintService);
            printerJob.setPageable(new PDFPageable(document));

            // 🚨 IMPRIMIR DIRECTAMENTE (sin diálogo)
            printerJob.print();

            // Cerrar el documento
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar el archivo PDF.");
        } catch (PrinterException e) {
            e.printStackTrace();
            System.out.println("Error al imprimir el archivo PDF.");
        }
    }

    public static void abrirCajaRegistradora() {
        try {
            // Buscar la impresora predeterminada del sistema
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

            if (printService != null) {
                // Crear un trabajo de impresión
                DocPrintJob printJob = printService.createPrintJob();

                // Comando típico para abrir la caja registradora (código ESC/POS)
                byte[] abrirCajaComando = new byte[]{27, 112, 0, 50, (byte) 250};  // Comando ESC/POS para abrir la caja

                // Crear un documento que envía el comando a la impresora
                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                Doc doc = new SimpleDoc(abrirCajaComando, flavor, null);

                // Enviar el comando a la impresora
                printJob.print(doc, null);

                System.out.println("Caja registradora abierta.");
            } else {
                System.out.println("No se encontró una impresora predeterminada.");
            }
        } catch (PrintException e) {
            e.printStackTrace();
            System.out.println("Error al intentar abrir la caja registradora.");
        }
    }

}
