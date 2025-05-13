package org.example.ui.uiadmin;

import org.example.manager.adminmanager.FacturasAdminManager;
import org.example.model.Factura;
import org.example.ui.UIHelpers;
import org.example.ui.uiuser.UIUserMesas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.example.manager.usermanager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.utils.Constants.ARIAL_FONT;

public class UIAdminFacturas {

    private UIAdminFacturas() {}

       public static JPanel getAdminBillsPanel() {
        JPanel facturasPanel = new JPanel(new BorderLayout());
        facturasPanel.setBackground(new Color(250, 240, 230));
        facturasPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margen de 20px en todos los lados

        JLabel titleLabel = new JLabel("Facturas", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(36, 36, 36));
        try {

            // Cargar la fuente desde los recursos dentro del JAR
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            // Crear la fuente desde el InputStream
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, 50); // Ajustar tamaño y peso
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FacturasAdminManager facturasAdminManager = new FacturasAdminManager();
        List<Factura> facturas = facturasAdminManager.getFacturas();

        // Columnas de la tabla
        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora","Tipo de pago"};
        Object[][] data = new Object[facturas.size()][5];

        // Llenar los datos en la tabla
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][0] = f.getId();
            data[i][1] = f.getProductos();
            data[i][2] = f.getTotal();
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
        Font font = new Font(ARIAL_FONT, Font.PLAIN, 18);
        facturasTable.setFont(font);
        facturasTable.setRowHeight(30);

        // Establecer la fuente para el encabezado
        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        header.setBackground(new Color(28,28,28));
        header.setForeground(Color.BLACK);

        // Configuración de borde y color de fondo
        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        facturasTable.setBackground(new Color(250, 240, 230));
        facturasTable.setSelectionBackground(new Color(173, 216, 230)); // Azul claro
        facturasTable.setSelectionForeground(Color.BLACK);
        // Aplicar renderer de moneda a la columna "Total"
        facturasTable.getColumnModel().getColumn(2).setCellRenderer(new UIHelpers.CurrencyRenderer());
        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, 26); // Ajustar tamaño

            header = facturasTable.getTableHeader();
            header.setFont(customFont);
            header.setForeground(new Color(201, 41, 41));

        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasPanel.add(scrollPane, BorderLayout.CENTER);
        // Crear el botón para eliminar
        JButton eliminarButton = new JButton("Eliminar"){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Color de fondo normal
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 111, 97)); // Amarillo oscuro al presionar
                } else {
                    g2.setColor(new Color(201, 41, 41)); // Amarillo Material Design
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        //eliminarButton.setEnabled(false); // Inicialmente deshabilitado

        eliminarButton.setPreferredSize(new Dimension(160, 40)); // Más grande
        eliminarButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        eliminarButton.setForeground(Color.WHITE); // Texto negro
        eliminarButton.setFocusPainted(false);
        eliminarButton.setContentAreaFilled(false);
        eliminarButton.setBorderPainted(false);
        eliminarButton.setOpaque(false);


        // Botón para eliminar una factura seleccionada
        eliminarButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -1) {
                String facturaID = facturasTable.getValueAt(selectedRow, 0).toString(); // Obtener el ID de la factura seleccionada
                int confirm = JOptionPane.showConfirmDialog(facturasPanel, "¿Seguro que deseas eliminar esta factura?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Llamar al método que elimina la factura y actualiza el inventario
                    boolean eliminado = facturasAdminManager.eliminarFacturaYActualizarProductos(facturaID);

                    if (eliminado) {
                        // Eliminar la fila de la tabla
                        ((DefaultTableModel) facturasTable.getModel()).removeRow(selectedRow);
                        JOptionPane.showMessageDialog(facturasPanel, "Factura eliminada exitosamente y productos actualizados.");
                    } else {
                        JOptionPane.showMessageDialog(facturasPanel, "Error al eliminar la factura y actualizar productos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(facturasPanel, "Por favor, selecciona una factura para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

           // **Botón ImprimirButton**
           JButton reprintButton = new JButton("Imprimir") {
               @Override
               protected void paintComponent(Graphics g) {
                   Graphics2D g2 = (Graphics2D) g;
                   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                   // Sombra del botón
                   g2.setColor(new Color(0, 0, 0, 30));
                   g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                   // Color de fondo amarillo
                   if (getModel().isPressed()) {
                       g2.setColor(new Color(0, 201, 87)); // Amarillo oscuro al presionar
                   } else {
                       g2.setColor(new Color(0, 240, 100)); // Amarillo brillante
                   }
                   g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                   super.paintComponent(g);
               }
           };

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
                       JOptionPane.showMessageDialog(facturasPanel, "No hay productos en esta factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                       return;
                   }

                   // Llamar a la función para generar el PDF con todos los productos de la factura seleccionada
                   generarFacturadeCompra(facturaId, productos, totalCompra, fechaHora, " ");

               } else {
                   JOptionPane.showMessageDialog(facturasPanel, "Por favor selecciona una factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
               }
           });
           // **Estilizar el botón**
           reprintButton.setPreferredSize(new Dimension(150, 40));
           reprintButton.setFont(new Font("Arial", Font.BOLD, 22));
           reprintButton.setForeground(Color.WHITE);
           reprintButton.setFocusPainted(false);
           reprintButton.setContentAreaFilled(false);
           reprintButton.setBorderPainted(false);
           reprintButton.setOpaque(false);

        // Añadir el botón de eliminación
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(reprintButton);
        buttonPanel.add(eliminarButton);

        facturasPanel.add(titleLabel, BorderLayout.NORTH);
        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);

        return facturasPanel;

    }
}