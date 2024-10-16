package org.example.ui.uiAdmin;

import org.example.manager.adminManager.FacturasAdminManager;
import org.example.model.Factura;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.ui.uiUser.UIUserMain.mainUser;

public class UIAdminFacturas {

    public static void showFacturasDialog() {
        JDialog facturasDialog = new JDialog();
        facturasDialog.setTitle("Listado de Facturas");
        facturasDialog.setSize(1000, 600);
        facturasDialog.setLayout(new BorderLayout());
        facturasDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainAdmin() ; // Llamada a showMesas cuando se cierra la ventana
            }
        });
        FacturasAdminManager facturasAdminManager = new FacturasAdminManager();
        List<Factura> facturas = facturasAdminManager.getFacturas();

        // Columnas de la tabla
        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora"};
        Object[][] data = new Object[facturas.size()][4];

        // Llenar los datos en la tabla
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][0] = f.getId();
            data[i][1] = f.getProductos();
            data[i][2] = f.getTotal();
            data[i][3] = f.getFechaHora();
        }

        // Crear la tabla con los datos de las facturas
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas no sean editables
            }
        };
        JTable facturasTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasDialog.add(scrollPane, BorderLayout.CENTER);

        // Botón para eliminar una factura seleccionada
        JButton eliminarButton = createButton("Eliminar", e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -1) {
                String facturaID = facturasTable.getValueAt(selectedRow, 0).toString(); // Obtener el ID de la factura seleccionada
                int confirm = JOptionPane.showConfirmDialog(facturasDialog, "¿Seguro que deseas eliminar esta factura?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    facturasAdminManager.eliminarFactura(facturaID); // Eliminar la factura del Excel
                    ((DefaultTableModel) facturasTable.getModel()).removeRow(selectedRow); // Eliminar la fila de la tabla
                    JOptionPane.showMessageDialog(facturasDialog, "Factura eliminada exitosamente.");
                }
            } else {
                JOptionPane.showMessageDialog(facturasDialog, "Por favor, selecciona una factura para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Añadir el botón de eliminación
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(eliminarButton);
        facturasDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el diálogo
        facturasDialog.setVisible(true);
        facturasDialog.setLocationRelativeTo(null);
    }
}