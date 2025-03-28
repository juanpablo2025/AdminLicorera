package org.example.ui.uiUser;

import org.example.manager.userManager.ProductoUserManager;
import org.example.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.UIHelpers.createDialog;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.utils.Constants.CLOSE_BUTTON;
import static org.example.utils.Constants.LISTAR_PRODUCTO;

public class UIUserProductList {
    private static ProductoUserManager productoUserManager = new ProductoUserManager();


    public static void showListProductsDialog() {
        JDialog listProductsDialog = createDialog(LISTAR_PRODUCTO, 1280, 720, new BorderLayout());
        listProductsDialog.setResizable(true);
        listProductsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainUser();
            }
        });


        List<Producto> products = productoUserManager.getProducts();
        String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        Object[][] data = new Object[products.size()][3];

        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

        for (int i = 0; i < products.size(); i++) {
            Producto p = products.get(i);
            data[i][0] = p.getName();
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

        JScrollPane scrollPane = new JScrollPane(productTable);
        listProductsDialog.add(scrollPane, BorderLayout.CENTER);

        listProductsDialog.setVisible(true);
        listProductsDialog.setLocationRelativeTo(null);
    }}
