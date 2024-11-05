package org.example.ui.uiUser;

import org.example.manager.userManager.FacturasUserManager;
import org.example.model.Factura;
import org.example.ui.UIHelpers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.example.manager.userManager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.manager.userManager.PrintUserManager.abrirPDF;
import static org.example.manager.userManager.PrintUserManager.imprimirPDF;
import static org.example.ui.uiUser.UIUserMain.mainUser;

public class UIUserFacturas {
    public static void showFacturasDialog() {
        JDialog facturasDialog = new JDialog();
        facturasDialog.setTitle("Listado de Facturas");
        facturasDialog.setSize(1280, 720);
        facturasDialog.setLayout(new BorderLayout());
        facturasDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainUser(); // Regresa al menú principal al cerrar el diálogo
            }
        });

        // Obtener las facturas del gestor de facturas
        FacturasUserManager facturasUserManager = new FacturasUserManager();
        List<Factura> facturas = facturasUserManager.getFacturas();

        // Columnas de la tabla
        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora", "Tipo de pago"};
        Object[][] data = new Object[facturas.size()][5];

        // Llenar los datos en la tabla
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][0] = f.getId();

            // Convertir la lista de productos a una cadena separada por comas
            List<String> productos = Collections.singletonList(f.getProductos()); // Obtener directamente la lista de productos
            data[i][1] = productos != null ? String.join(", ", productos) : ""; // Mostrar todos los productos en una sola celda

            // Asegurarse de que el total sea correctamente representado
            data[i][2] = f.getTotal(); // Usar el total directamente sin manipulación adicional
            data[i][3] = f.getFechaHora();
            data[i][4] = f.getTipoPago();

        }

        // Crear la tabla con los datos de las facturas
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas no sean editables
            }
        };

        // Crear el JTable con el modelo y aplicar el estilo
        JTable facturasTable = new JTable(tableModel);
        facturasTable.setFillsViewportHeight(true);
        facturasTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Establecer la fuente y el tamaño
        Font font = new Font("Arial", Font.PLAIN, 18);
        facturasTable.setFont(font);
        facturasTable.setRowHeight(30);

        // Establecer la fuente para el encabezado
        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBackground(Color.LIGHT_GRAY);
        header.setForeground(Color.BLACK);

        // Configuración de borde y color de fondo
        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        facturasTable.setBackground(Color.WHITE);
        facturasTable.setSelectionBackground(Color.CYAN);
        facturasTable.setSelectionForeground(Color.BLACK);

        // Aplicar renderer de moneda a la columna "Total"
        facturasTable.getColumnModel().getColumn(2).setCellRenderer(new UIHelpers.CurrencyRenderer());

        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasDialog.add(scrollPane, BorderLayout.CENTER);

        // Crear el botón "Imprimir Factura"
        JButton reprintButton = new JButton("Imprimir Factura");
        reprintButton.setFont(new Font("Arial", Font.BOLD, 16));

        // Acción al presionar el botón de reimprimir
        reprintButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow(); // Obtener la fila seleccionada
            if (selectedRow != -1) {
                // Obtener los datos de la factura seleccionada
                String facturaId = facturasTable.getValueAt(selectedRow, 0).toString();
                String productosStr = facturasTable.getValueAt(selectedRow, 1).toString();
                double totalCompra = Double.parseDouble(facturasTable.getValueAt(selectedRow, 2).toString());
                String fechaHoraStr = facturasTable.getValueAt(selectedRow, 3).toString();

                // Convertir la cadena de productos a una lista usando saltos de línea como delimitador
                List<String> productos = Arrays.asList(productosStr.split("\\n"));

                // Convertir la fecha y hora a LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);



                // Verificar si la lista de productos no está vacía
                if (productos.isEmpty()) {
                    JOptionPane.showMessageDialog(facturasDialog, "No hay productos en esta factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Llamar a la función para generar el PDF con todos los productos de la factura seleccionada
                generarFacturadeCompra(facturaId, productos, totalCompra, fechaHora);

            } else {
                JOptionPane.showMessageDialog(facturasDialog, "Por favor selecciona una factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Crear un panel para el botón de reimprimir
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(reprintButton);

        // Añadir el panel del botón debajo de la tabla
        facturasDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el diálogo
        facturasDialog.setVisible(true);
        facturasDialog.setLocationRelativeTo(null);
    }


}
