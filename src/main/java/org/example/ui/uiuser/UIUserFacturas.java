package org.example.ui.uiuser;

import org.example.manager.usermanager.FacturasUserManager;
import org.example.model.Factura;
import org.example.ui.UIHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static org.example.utils.Constants.*;

public class UIUserFacturas {

    private static final Logger logger =  LoggerFactory.getLogger(UIUserFacturas.class);


    private UIUserFacturas() {}

    public static JPanel getFacturasPanel() {
        JPanel facturasPanel = new JPanel(new BorderLayout());
        facturasPanel.setBackground(FONDO_PRINCIPAL);
        facturasPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(FACTURAS, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(36, 36, 36));
        try {

            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT);

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: ", e);
            titleLabel.setFont(new Font(ARIAL_FONT, Font.BOLD, 50));
        }

        FacturasUserManager facturasUserManager = new FacturasUserManager();
        List<Factura> facturas = facturasUserManager.getFacturas();

        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora", "Tipo de pago"};
        Object[][] data = new Object[facturas.size()][FIVE];

        for (int i = ZERO; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][ZERO] = f.getId();

            List<String> productos = Collections.singletonList(f.getProductos());
            data[i][ONE] = String.join(", ", productos);

            data[i][TWO] = f.getTotal();
            data[i][THREE] = f.getFechaHora();
            data[i][FOUR] = f.getTipoPago();

        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable facturasTable = new JTable(tableModel);
        facturasTable.setFillsViewportHeight(true);
        facturasTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        Font font = new Font(ARIAL_FONT, Font.PLAIN, 18);
        facturasTable.setFont(font);
        facturasTable.setRowHeight(30);

        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        header.setBackground(new Color(28,28,28));
        header.setForeground(Color.BLACK);

        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, ONE));
        facturasTable.setBackground(FONDO_PRINCIPAL);
        facturasTable.setSelectionBackground(new Color(173, 216, 230));
        facturasTable.setSelectionForeground(Color.BLACK);

        facturasTable.getColumnModel().getColumn(TWO).setCellRenderer(new UIHelpers.CurrencyRenderer());
        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, 26);

            header = facturasTable.getTableHeader();
            header.setFont(customFont);
            header.setForeground(new Color(201, 41, 41));

        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
            header.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        }
        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasPanel.add(scrollPane, BorderLayout.CENTER);

        JButton reprintButton = getPrintButton(facturasTable, facturasPanel);

        JButton closeButton = getCloseButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(FONDO_PRINCIPAL);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);
        scrollPane = new JScrollPane(facturasTable);
                scrollPane.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) facturasPanel.getParent().getLayout();
            cl.show(facturasPanel.getParent(), "mesas");
        });

        buttonPanel.setBorder(new EmptyBorder(TEN, ZERO, TEN, ZERO));
        buttonPanel.add(reprintButton);

        facturasPanel.add(scrollPane, BorderLayout.CENTER);
        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);
        facturasPanel.add(titleLabel, BorderLayout.NORTH);
        return facturasPanel;
    }

    private static JButton getCloseButton() {
        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(ZERO, ZERO, ZERO, 30));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, SEVEN));
                } else {
                    g2.setColor(new Color(228, 185, 42));
                }
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);

        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        return closeButton;
    }

    private static JButton getPrintButton(JTable facturasTable, JPanel facturasPanel) {
        JButton reprintButton = new JButton("Imprimir") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                g2.setColor(new Color(ZERO, ZERO, ZERO, 30));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);


                if (getModel().isPressed()) {
                    g2.setColor(new Color(ZERO, 201, 87));
                } else {
                    g2.setColor(new Color(ZERO, 240, 100));
                }
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        reprintButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -1) {
                String facturaId = facturasTable.getValueAt(selectedRow, ZERO).toString();
                String productosStr = facturasTable.getValueAt(selectedRow, ONE).toString();
                double totalCompra = Double.parseDouble(facturasTable.getValueAt(selectedRow, TWO).toString());
                String fechaHoraStr = facturasTable.getValueAt(selectedRow, THREE).toString();

                List<String> productos = Arrays.asList(productosStr.split("\\n"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);

                if (productos.isEmpty()) {
                    JOptionPane.showMessageDialog(facturasPanel, "No hay productos en esta factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                generarFacturadeCompra(facturaId, productos, totalCompra, fechaHora, " ");

            } else {
                JOptionPane.showMessageDialog(facturasPanel, "Por favor selecciona una factura.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        reprintButton.setPreferredSize(new Dimension(150, 40));
        reprintButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        reprintButton.setForeground(Color.WHITE);
        reprintButton.setFocusPainted(false);
        reprintButton.setContentAreaFilled(false);
        reprintButton.setBorderPainted(false);
        reprintButton.setOpaque(false);
        return reprintButton;
    }
}
