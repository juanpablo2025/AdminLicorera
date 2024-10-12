package org.example.manager.userManager;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

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

            // Crear un trabajo de impresión
            PrinterJob printerJob = PrinterJob.getPrinterJob();

            // Usar la impresora predeterminada del sistema
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

            if (defaultPrintService != null) {
                printerJob.setPrintService(defaultPrintService);
            } else {
                System.out.println("No hay una impresora predeterminada configurada.");
                return;
            }

            // Configurar el documento PDF para la impresión
            printerJob.setPageable(new PDFPageable(document));

            // Realizar la impresión
            if (printerJob.printDialog()) {  // Si deseas mostrar el diálogo de impresión, puedes cambiarlo a true
                printerJob.print();
            }

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
