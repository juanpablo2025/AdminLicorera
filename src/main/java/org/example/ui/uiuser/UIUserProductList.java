package org.example.ui.uiuser;

import org.example.manager.usermanager.ProductoUserManager;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static org.example.utils.Constants.*;
import static org.example.utils.FormatterHelpers.ConfigurationGlobal.TRM;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class UIUserProductList {

    private static final NumberFormat FORMAT_USD = NumberFormat.getCurrencyInstance(Locale.US);


    public static JPanel getProductListPanel() {
        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(FONDO_PRINCIPAL);
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventario", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        titleLabel.setFont(TITTLE_FONT);

         List<Producto>  products = ProductoUserManager.getProducts();

        String[] columnNames = {
                "Nombre",
                "Cantidad",
                "<html><b>Pesos/USD</b><span style='font-size:14px; color:#28a748;'>(" + formatearMoneda(TRM) + " TRM)</span></html>"
        };

        Object[][] data = new Object[products.size()][THREE];
        for (int i = ZERO; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][ZERO] = formatProductName(p.getName());
            data[i][ONE] = p.getQuantity();

            double precioUSD = TRM != 0.0 ? p.getPrice() / TRM : 0.0;
            data[i][TWO] = String.format(
                    "<html><b>%s</b> <span style='font-size:14px; color:#000080;'>(%s)</span></html>",
                    "$ " + formatearMoneda(p.getPrice()),
                    FORMAT_USD.format(precioUSD)
            );
        }

        JTable productTable = getJTable(data, columnNames);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(TITTLE_FONT.deriveFont(Font.PLAIN, 22f));
        header.setBackground(HEADER_COLOR);
        header.setForeground(HEADER_FONT_COLOR);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(ONE).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(TWO).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    try {
                        int cantidad = Integer.parseInt(table.getValueAt(row, ONE).toString());
                        cell.setBackground(cantidad < ZERO ? new Color(255, 150, 150) : FONDO_PRINCIPAL);
                    } catch (Exception e) {
                        cell.setBackground(Color.WHITE);
                    }
                } else {
                    cell.setBackground(table.getSelectionBackground());
                }

                if (column == ONE || column == TWO) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (column == TWO && value instanceof String s && s.contains("<html>")) {
                    ((JLabel) cell).setText(s);
                }
                return cell;
            }
        };

        for (int i = ZERO; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(TEN, TEN, TEN, TEN));

        JButton closeButton = getCloseButton(productListPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(TEN, ZERO, TEN, ZERO));
        buttonPanel.setBackground(FONDO_PRINCIPAL);
        buttonPanel.add(closeButton);

        productListPanel.add(titleLabel, BorderLayout.NORTH);
        productListPanel.add(scrollPane, BorderLayout.CENTER);
        productListPanel.add(buttonPanel, BorderLayout.SOUTH);

        return productListPanel;
    }

    private static JTable getJTable(Object[][] data, String[] columnNames) {
        JTable productTable = new JTable(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        productTable.setFont(new Font("Segoe UI Variable", Font.PLAIN, 18));
        productTable.setRowHeight(40);
        productTable.setBackground(FONDO_PRINCIPAL);
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setFillsViewportHeight(true);
        return productTable;
    }

    private static JButton getCloseButton(JPanel productListPanel) {
        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(TWO, FOUR, getWidth() - FOUR, getHeight() - FOUR, 40, 40);
                g2.setColor(getModel().isPressed() ? BTN_BACK_PRESSED : BTN_BACK);
                g2.fillRoundRect(ZERO, ZERO, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        closeButton.setForeground(BTN_BACK_FONT_COLOR);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) productListPanel.getParent().getLayout();
            cl.show(productListPanel.getParent(), "mesas");
        });
        return closeButton;
    }

    private static String formatProductName(String name) {
        String formatted = name.replace("_", " ").toLowerCase();
        String[] words = formatted.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(ZERO)))
                        .append(word.substring(ONE))
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}







