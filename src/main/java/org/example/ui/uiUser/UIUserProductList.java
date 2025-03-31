package org.example.ui.uiUser;

import org.example.manager.userManager.ProductoUserManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;

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

    public static JPanel getProductListPanel() {
        JPanel productListPanel = new JPanel(new BorderLayout());
        productListPanel.setBackground(new Color(250, 240, 230));
        productListPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margen de 20px en todos los lados


        JLabel titleLabel = new JLabel("Inventario", JLabel.CENTER);
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


        List<Producto> products = productoUserManager.getProducts();
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};

        Object[][] data = new Object[products.size()][3];

        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = formatProductName(p.getName());
            data[i][1] = p.getQuantity();
            data[i][2] = formatCOP.format(p.getPrice());
        }

        // **Tabla con modelo de datos**
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


        // **Estilizar encabezado de tabla**
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBackground(new Color(28, 28, 28));


        // **Bordes y selección de tabla**
        productTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        productTable.setBackground(new Color(250, 240, 230));
        productTable.setSelectionBackground(new Color(173, 216, 230)); // Azul claro
        productTable.setSelectionForeground(Color.BLACK);

        // **Centrar texto en la columna "Cantidad"**
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // **Resaltar filas con cantidad <= 0 en rojo claro**
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int cantidad = (int) table.getValueAt(row, 1);

                if (cantidad < 0 && !isSelected) {
                    cell.setBackground(new Color(255, 150, 150)); // Rojo claro
                } else if (!isSelected) {
                    cell.setBackground(new Color(250, 240, 230));
                }

                return cell;
            }
        });
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
        // **ScrollPane con margen**
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Espaciado interno

        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Color de fondo normal
                if (getModel().isPressed()) {
                    g2.setColor(new Color(255, 193, 7)); // Amarillo oscuro al presionar
                } else {
                    g2.setColor(new Color(228, 185, 42)); // Amarillo Material Design
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        // Estilos del botón
        closeButton.setPreferredSize(new Dimension(150, 40)); // Más grande
        closeButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        closeButton.setForeground(Color.WHITE); // Texto negro
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        // **Agregar acción al botón**
        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) productListPanel.getParent().getLayout();
            cl.show(productListPanel.getParent(), "mesas");
        });



        // **Panel de botón con margen**
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Margen arriba
        buttonPanel.setBackground(new Color(250, 240, 230)); // Fondo igual al panel
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







