package org.example.ui.uiUser;

import org.example.manager.userManager.ProductoUserManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;
import static org.example.utils.FormatterHelpers.ConfiguracionGlobal.TRM;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class UIUserProductList {

    private static ProductoUserManager productoUserManager = new ProductoUserManager();
    private static final NumberFormat FORMAT_USD = NumberFormat.getCurrencyInstance(Locale.US);
    private static Font titleFont;
    private static Font headerFont;

    static {
        try {
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            titleFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            headerFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.ITALIC, 26);
        } catch (Exception e) {
            titleFont = new Font("Serif", Font.BOLD, 50);
            headerFont = new Font("Serif", Font.ITALIC, 26);
        }
    }

    public static JPanel getProductListPanel() {
        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(new Color(250, 240, 230));
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventario", JLabel.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
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


        List<Producto> products = productoUserManager.getProducts();

        String[] columnNames = {
                "Nombre",
                "Cantidad",
                "<html><b>Pesos/USD</b><span style='font-size:14px; color:#28a748;'>(" + formatearMoneda(TRM) + " TRM)</span></html>"
        };

        Object[][] data = new Object[products.size()][3];
        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = formatProductName(p.getName());
            data[i][1] = p.getQuantity();

            double precioUSD = TRM != 0.0 ? p.getPrice() / TRM : 0.0;
            data[i][2] = String.format(
                    "<html><b>%s</b> <span style='font-size:14px; color:#000080;'>(%s)</span></html>",
                    "$ " + formatearMoneda(p.getPrice()),
                    FORMAT_USD.format(precioUSD)
            );
        }

        JTable productTable = new JTable(new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) { return false; }
        });

        productTable.setFont(new Font("Arial", Font.PLAIN, 18));
        productTable.setRowHeight(40);
        productTable.setBackground(new Color(250, 240, 230));
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setFillsViewportHeight(true);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(headerFont);
        header.setForeground(new Color(201, 41, 41));
        header.setBackground(new Color(28, 28, 28));
        try {
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = customFont.deriveFont(Font.ITALIC, 26); // Ajustar tamaño


            header = productTable.getTableHeader();
            header.setFont(customFont);
            header.setForeground(new Color(201, 41, 41));

        } catch (Exception e) {
            e.printStackTrace();
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fondo por cantidad
                if (!isSelected) {
                    try {
                        int cantidad = Integer.parseInt(table.getValueAt(row, 1).toString());
                        cell.setBackground(cantidad < 0 ? new Color(255, 150, 150) : new Color(250, 240, 230));
                    } catch (Exception e) {
                        cell.setBackground(Color.WHITE);
                    }
                } else {
                    cell.setBackground(table.getSelectionBackground());
                }

                // Alineación centrada para cantidad y precio
                if (column == 1 || column == 2) {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) cell).setHorizontalAlignment(SwingConstants.LEFT);
                }

                // Soporte para HTML en precios (columna 2)
                if (column == 2 && value instanceof String && ((String) value).contains("<html>")) {
                    ((JLabel) cell).setText((String) value);
                }

                return cell;
            }
        };

        // Asignar a todas las columnas el mismo renderer
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = new JButton("Volver") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, 7) : new Color(228, 185, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font("Arial", Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) productListPanel.getParent().getLayout();
            cl.show(productListPanel.getParent(), "mesas");
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonPanel.setBackground(new Color(250, 240, 230));
        buttonPanel.add(closeButton);

        productListPanel.add(titleLabel, BorderLayout.NORTH);
        productListPanel.add(scrollPane, BorderLayout.CENTER);
        productListPanel.add(buttonPanel, BorderLayout.SOUTH);

        return productListPanel;
    }

    private static String formatProductName(String name) {
        String formatted = name.replace("_", " ").toLowerCase();
        String[] words = formatted.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}







