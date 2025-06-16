package org.example.ui.uiadmin;

import org.example.manager.adminmanager.FacturasAdminManager;
import org.example.model.Factura;
import org.example.ui.UIHelpers;
import org.example.ui.uiuser.UIUserMesas;
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
import java.util.List;

import static org.example.manager.usermanager.FacturacionUserManager.generarFacturadeCompra;
import static org.example.utils.Constants.*;

public class UIAdminFacturas {

    private UIAdminFacturas() {}

    private static final Logger logger =  LoggerFactory.getLogger(UIAdminFacturas.class);


    public static JPanel getAdminBillsPanel() {
        JPanel facturasPanel = new JPanel(new BorderLayout());
        facturasPanel.setBackground(FONDO_PRINCIPAL);
        facturasPanel.setBorder(new EmptyBorder(TWENTY, TWENTY, TWENTY, TWENTY));

        JLabel titleLabel = new JLabel(FACTURAS, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(THIRTY_SIX, THIRTY_SIX, THIRTY_SIX));
        try {

            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT);

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.BOLD, FIFTY);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
            titleLabel.setFont(new Font("Segoe UI Variable", Font.BOLD, FIFTY));
        }
        FacturasAdminManager facturasAdminManager = new FacturasAdminManager();
        List<Factura> facturas = facturasAdminManager.getFacturas();

        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora","Tipo de pago"};
        Object[][] data = new Object[facturas.size()][FIVE];

        for (int i = ZERO; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][ZERO] = f.getId();
            data[i][ONE] = f.getProductos();
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

        Font font = new Font("Segoe UI Variable", Font.PLAIN, EIGHTEEN);
        facturasTable.setFont(font);
        facturasTable.setRowHeight(THIRTY);

        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        header.setBackground(new Color(TWENTY_EIGHT,TWENTY_EIGHT,TWENTY_EIGHT));
        header.setForeground(Color.BLACK);

        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, ONE));
        facturasTable.setBackground(FONDO_PRINCIPAL);
        facturasTable.setSelectionBackground(new Color(173, 216, 230));
        facturasTable.setSelectionForeground(Color.BLACK);
        facturasTable.getColumnModel().getColumn(TWO).setCellRenderer(new UIHelpers.CurrencyRenderer());
        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream(LOBSTER_FONT);

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, TWENTY_SIX);

            header = facturasTable.getTableHeader();
            header.setFont(customFont);
            header.setForeground(new Color(201, 41, 41));

        } catch (Exception e) {
            logger.error("Error al cargar la fuente personalizada: {}", e.getMessage());
            header.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY));
        }

        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasPanel.add(scrollPane, BorderLayout.CENTER);

           JButton eliminarButton = getEliminarButton(facturasTable, facturasPanel, facturasAdminManager);
           JButton reprintButton = getReprintButton(facturasTable, facturasPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(reprintButton);
        buttonPanel.add(eliminarButton);

        facturasPanel.add(titleLabel, BorderLayout.NORTH);
        facturasPanel.add(buttonPanel, BorderLayout.SOUTH);

        return facturasPanel;

    }

    private static JButton getReprintButton(JTable facturasTable, JPanel facturasPanel) {
        JButton reprintButton = new JButton("Imprimir") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ZERO, ZERO, ZERO, THIRTY));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, FORTY, FORTY);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(ZERO, 201, 87));
                } else {
                    g2.setColor(new Color(ZERO, 240, 100));
                }
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), FORTY, FORTY);
                super.paintComponent(g);
            }
        };

        reprintButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -ONE) {

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

        reprintButton.setPreferredSize(new Dimension(150, FORTY));
        reprintButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY_TWO));
        reprintButton.setForeground(Color.WHITE);
        reprintButton.setFocusPainted(false);
        reprintButton.setContentAreaFilled(false);
        reprintButton.setBorderPainted(false);
        reprintButton.setOpaque(false);
        return reprintButton;
    }

    private static JButton getEliminarButton(JTable facturasTable, JPanel facturasPanel, FacturasAdminManager facturasAdminManager) {
        JButton eliminarButton = getJButton();

        eliminarButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -ONE) {
                String facturaID = facturasTable.getValueAt(selectedRow, ZERO).toString(); // Obtener el ID de la factura seleccionada
                int confirm = JOptionPane.showConfirmDialog(facturasPanel, "¿Seguro que deseas eliminar esta factura?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {

                    boolean eliminado = facturasAdminManager.eliminarFacturaYActualizarProductos(facturaID);

                    if (eliminado) {
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
        return eliminarButton;
    }

    private static JButton getJButton() {
        JButton eliminarButton = new JButton("Eliminar"){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ZERO, ZERO, ZERO, THIRTY));
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, FORTY, FORTY);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 111, 97));
                } else {
                    g2.setColor(new Color(201, 41, 41));
                }

                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), FORTY, FORTY);
                super.paintComponent(g);
            }
        };

        eliminarButton.setPreferredSize(new Dimension(160, FORTY));
        eliminarButton.setFont(new Font("Segoe UI Variable", Font.BOLD, TWENTY_TWO));
        eliminarButton.setForeground(Color.WHITE);
        eliminarButton.setContentAreaFilled(false);
        eliminarButton.setBorderPainted(false);
        eliminarButton.setOpaque(false);
        return eliminarButton;
    }
}