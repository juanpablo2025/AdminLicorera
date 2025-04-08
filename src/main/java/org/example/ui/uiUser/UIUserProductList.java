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
import java.util.List;
import java.util.Locale;

import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;
import static org.example.utils.FormatterHelpers.formatearMoneda;

public class UIUserProductList {
    private static ProductoUserManager productoUserManager = new ProductoUserManager();


    public static void showListProductsDialog() {
        JDialog listProductsDialog = createDialog("Inventario - Licorera CR", 1280, 720, new BorderLayout());
        listProductsDialog.setResizable(true);
        listProductsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainUser();
            }
        });


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
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][3];

        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);

            // **Transformar el nombre del producto**
            String formattedName = p.getName().replace("_", " ").toLowerCase();
            String[] words = formattedName.split(" ");
            StringBuilder capitalizedName = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    capitalizedName.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            formattedName = capitalizedName.toString().trim(); // Elimina espacio extra al final

            data[i][0] = formattedName; // Nombre formateado
            data[i][1] = p.getQuantity();
            data[i][2] = formatCOP.format(p.getPrice());
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        productTable.setFont(new Font("Arial", Font.PLAIN, 18));
        productTable.setRowHeight(30);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBackground(Color.LIGHT_GRAY);
        header.setForeground(Color.BLACK);

        productTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        productTable.setBackground(Color.WHITE);
        productTable.setSelectionBackground(Color.CYAN);
        productTable.setSelectionForeground(Color.BLACK);

        // **Centrar cantidad en la celda**
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // **Resaltar toda la fila si cantidad <= -1**
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Object cantidadValue = table.getValueAt(row, 1);
                if (cantidadValue instanceof Integer && (Integer) cantidadValue <= -1) {
                    cell.setBackground(new Color(255, 200, 200)); // Rojo sutil para toda la fila
                } else if (!isSelected) {
                    cell.setBackground(Color.WHITE);
                }

                return cell;
            }
        });
        listProductsDialog.add(titleLabel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(productTable);
        listProductsDialog.add(scrollPane, BorderLayout.CENTER);

        listProductsDialog.setVisible(true);
        listProductsDialog.setLocationRelativeTo(null);
    }


    private static final NumberFormat FORMAT_COP = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
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

    public static double obtenerTRM() {
        try {
            URL url = new URL("https://www.datos.gov.co/resource/32sa-8pi3.json?$limit=1&$order=vigenciadesde%20DESC");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) throw new IOException("HTTP Error");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                JSONArray arr = new JSONArray(response.toString());
                if (arr.length() == 0) return 0.0;
                return Double.parseDouble(arr.getJSONObject(0).getString("valor"));
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static JPanel getProductListPanel() {
        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(new Color(250, 240, 230));
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventario", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(36, 36, 36));

        double trm = obtenerTRM();
        List<Producto> products = productoUserManager.getProducts();

        String[] columnNames = {"Nombre", "Cantidad", "Precio PESOS/USD "+formatearMoneda((obtenerTRM()))+" TRM"};


        Object[][] data = new Object[products.size()][3];
        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = formatProductName(p.getName());
            data[i][1] = p.getQuantity();

            double precioUSD = trm != 0.0 ? p.getPrice() / trm : 0.0;
            data[i][2] = String.format("%s (%s)", "$ "+ formatearMoneda(p.getPrice()), FORMAT_USD.format(precioUSD));
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    int cantidad = (int) table.getValueAt(row, 1);
                    cell.setBackground(cantidad < 0 ? new Color(255, 150, 150) : new Color(250, 240, 230));
                }
                return cell;
            }
        });

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







