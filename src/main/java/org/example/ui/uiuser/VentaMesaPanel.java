package org.example.ui.uiuser;

import org.example.manager.usermanager.ProductoUserManager;
import org.example.model.Producto;
import org.example.ui.UIHelpers;
import org.example.utils.FormatterHelpers;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.example.manager.usermanager.ExcelUserManager.cargarProductosMesaDesdeExcel;
import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiuser.UIUserVenta.createConfirmPurchaseMesaButton;
import static org.example.ui.uiuser.UIUserVenta.createSavePurchaseMesaButton;
import static org.example.utils.Constants.*;

public class VentaMesaPanel extends JPanel {

    public VentaMesaPanel(List<String[]> productos, String mesaID, JPanel mainPanel, JFrame frame) {
        AtomicReference<Double> sumaTotal = new AtomicReference<>(0.0);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1366, 720));

        JLabel titleLabel = createTitleLabel(mesaID);
        JTable table = createConfiguredTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        loadProductsToTable(productos, tableModel, sumaTotal);
        JTextField totalField = createTotalField(sumaTotal.get());
        JPanel totalPanel = createTotalPanel();
        totalPanel.add(totalField, BorderLayout.CENTER);

        setupTableListener(tableModel, totalField);

        JScrollPane tableScrollPane = new JScrollPane(table);
        JPanel inputPanel = UIHelpers.createInputPanel(table);

        add(titleLabel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.EAST);

        JPanel buttonPanel = createButtonPanel(table, (JDialog) compraDialog, mesaID, mainPanel, frame);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JLabel createTitleLabel(String mesaID) {
        JLabel titleLabel = new JLabel("Venta " + mesaID, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(28, 28, 28));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(250, 240, 230));
        try (InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return titleLabel;
    }

    private JTable createConfiguredTable() {
        JTable table = createProductTable();
        table.getColumnModel().getColumn(2).setCellRenderer(new UIHelpers.CurrencyRenderer());

        Font font = new Font(ARIAL_FONT, Font.PLAIN, 18);
        table.setFont(font);
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(ARIAL_FONT, Font.BOLD, 20));
        header.setBackground(new Color(28, 28, 28));
        header.setForeground(new Color(201, 41, 41));

        table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        table.setBackground(new Color(250, 240, 230));
        table.setSelectionBackground(new Color(173, 216, 255));
        table.setSelectionForeground(Color.BLACK);

        return table;
    }

    private void loadProductsToTable(List<String[]> productos, DefaultTableModel tableModel, AtomicReference<Double> sumaTotal) {
        tableModel.setRowCount(0);
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[0].trim();
                int cantidad = Integer.parseInt(productoDetalles[1].substring(1).trim());
                double precioUnitario = Double.parseDouble(productoDetalles[2].substring(1).trim());
                double total = cantidad * precioUnitario;

                tableModel.addRow(new Object[]{nombreProducto, cantidad, FormatterHelpers.formatearMoneda(precioUnitario), total});
                sumaTotal.updateAndGet(v -> v + total);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    private JTextField createTotalField(double total) {
        JTextField totalField = new JTextField(TOTAL_PRICE + FormatterHelpers.formatearMoneda(total) + PESOS);
        totalField.setFont(new Font(ARIAL_FONT, Font.BOLD, 26));
        totalField.setForeground(Color.RED);
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(SwingConstants.CENTER);
        totalField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 90));
        totalField.setVisible(total > 0);
        totalField.setBackground(new Color(250, 240, 230));

        try (InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
            if (fontStream == null) throw new IOException("No se encontró la fuente");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);
            totalField.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalField;
    }

    private void setupTableListener(DefaultTableModel tableModel, JTextField totalField) {
        tableModel.addTableModelListener(new TableModelListener() {
            private boolean updatingTable = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (updatingTable) return;

                updatingTable = true;
                try {
                    double nuevoTotalGeneral = 0;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String nombreProducto = (String) tableModel.getValueAt(i, 0);
                        int cantidad = (int) tableModel.getValueAt(i, 1);
                        Producto producto = ProductoUserManager.getProductByName(nombreProducto);

                        double precioUnitario = producto != null ? producto.getPrice() : 0.0;
                        double subtotal = cantidad * precioUnitario;
                        tableModel.setValueAt(subtotal, i, 3);
                        nuevoTotalGeneral += subtotal;
                    }

                    totalField.setText(TOTAL_PRICE + FormatterHelpers.formatearMoneda(nuevoTotalGeneral) + PESOS);
                    totalField.setVisible(nuevoTotalGeneral > 0);
                } finally {
                    updatingTable = false;
                }
            }
        });
    }


    private JPanel createButtonPanel(JTable table, JDialog compraDialog, String mesaID, JPanel mainPanel,JFrame frame) {
        JPanel buttonPanel = new JPanel(new BorderLayout()); // 🔹 Usamos BorderLayout
        JPanel topButtonsPanel = new JPanel(new GridLayout(1, 2, 0, 10)); // 1 fila, 2 columnas, separación de 20px
        JButton guardarCompra = createSavePurchaseMesaButton( mesaID, table);
        guardarCompra.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));

        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(compraDialog, mesaID, table,frame);
        confirmarCompraButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));

        List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
        boolean productosEnExcel = !productosPrevios.isEmpty();
        confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0);

        table.getModel().addTableModelListener(e -> confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0));

        guardarCompra.setPreferredSize(new Dimension(0, 50)); // Altura fija, ancho automático
        confirmarCompraButton.setPreferredSize(new Dimension(0, 50));

        topButtonsPanel.add(guardarCompra);
        topButtonsPanel.add(confirmarCompraButton);

        buttonPanel.add(topButtonsPanel, BorderLayout.CENTER); // 🔹 Agregar los botones de compra en el centro

        // 📌 Botón "Volver"
        JButton closeButton = new JButton("Volver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);
                g2.setColor(getModel().isPressed() ? new Color(255, 193, 7) : new Color(228, 185, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
            }
        };

        closeButton.setPreferredSize(new Dimension(150, 40));
        closeButton.setFont(new Font(ARIAL_FONT, Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(250, 240, 230));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, MESAS); // Vuelve a la vista de mesas
        });

        // 🔹 Panel para centrar el botón "Volver"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(closeButton);

        buttonPanel.add(bottomPanel, BorderLayout.SOUTH); // 🔹 Botón "Volver" centrado en la parte inferior
        bottomPanel.setBackground( new Color(250, 240, 230));
        return buttonPanel;
    }
}
