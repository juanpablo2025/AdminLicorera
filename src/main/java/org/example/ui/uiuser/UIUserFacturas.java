package org.example.ui.uiuser;

import org.example.manager.usermanager.FacturasUserManager;
import org.example.model.Factura;
import org.example.ui.UIHelpers;
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
import java.util.Collections;
import java.util.List;
import static org.example.manager.usermanager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.utils.Constants.ARIAL_FONT;

public class UIUserFacturas {

    private UIUserFacturas() {}

    public static JPanel getFacturasPanel() {
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
        reprintButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        reprintButton.setForeground(Color.WHITE);
        reprintButton.setFocusPainted(false);
        reprintButton.setContentAreaFilled(false);
        reprintButton.setBorderPainted(false);
        reprintButton.setOpaque(false);

        // **Botón Cerrar**
        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Color de fondo amarillo
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, 7)); // Amarillo oscuro al presionar
                } else {
                    g2.setColor(new Color(228, 185, 42)); // Amarillo brillante
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        // **Estilizar el botón**
        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);

        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        // Crear un panel para el botón de reimprimir
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 240, 230));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        // Añadir el panel del botón debajo de la tabla
        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);
        // **Panel con ScrollPane**
        scrollPane = new JScrollPane(facturasTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Espaciado interno

        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) facturasPanel.getParent().getLayout();
            cl.show(facturasPanel.getParent(), "mesas");
        });

        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonPanel.add(reprintButton);

        // **Añadir componentes al JPanel**
        facturasPanel.add(scrollPane, BorderLayout.CENTER);
        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);
        facturasPanel.add(titleLabel, BorderLayout.NORTH);
        return facturasPanel;
    }
}
