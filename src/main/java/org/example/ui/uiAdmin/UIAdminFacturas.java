package org.example.ui.uiAdmin;

import org.example.manager.adminManager.FacturasAdminManager;
import org.example.manager.adminManager.GastosAdminManager;
import org.example.manager.userManager.GastosUserManager;
import org.example.model.Factura;
import org.example.ui.uiUser.UIUserMesas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.List;

import static org.example.ui.UIHelpers.createButton;
import static org.example.ui.uiAdmin.MainAdminUi.mainAdmin;
import static org.example.ui.uiUser.UIUserMain.mainUser;

public class UIAdminFacturas {

    public static void showFacturasDialog() {
        JDialog facturasDialog = new JDialog();
        facturasDialog.setTitle("Listado de Facturas");
        facturasDialog.setSize(1280, 720);
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
        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora","Tipo de pago"};
        Object[][] data = new Object[facturas.size()][5];

        // Llenar los datos en la tabla
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][0] = f.getId();
            data[i][1] = f.getProductos();
            data[i][2] = f.getTotal();
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
        Font font = new Font("Arial", Font.PLAIN, 18); // Cambiar el tipo y tamaño de fuente
        facturasTable.setFont(font);
        facturasTable.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente más grande para el encabezado
        header.setBackground(Color.LIGHT_GRAY); // Fondo para el encabezado
        header.setForeground(Color.BLACK); // Color del texto del encabezado

        // Configuración de borde y color de fondo
        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        facturasTable.setBackground(Color.WHITE); // Fondo de la tabla
        facturasTable.setSelectionBackground(Color.CYAN); // Color de selección
        facturasTable.setSelectionForeground(Color.BLACK); // Color del texto seleccionado
        JScrollPane scrollPane = new JScrollPane(facturasTable);
        facturasDialog.add(scrollPane, BorderLayout.CENTER);
        // Crear el botón para eliminar
        JButton eliminarButton = new JButton("Eliminar");

// Botón para eliminar una factura seleccionada
        eliminarButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -1) {
                String facturaID = facturasTable.getValueAt(selectedRow, 0).toString(); // Obtener el ID de la factura seleccionada
                int confirm = JOptionPane.showConfirmDialog(facturasDialog, "¿Seguro que deseas eliminar esta factura?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Llamar al método que elimina la factura y actualiza el inventario
                    boolean eliminado = facturasAdminManager.eliminarFacturaYActualizarProductos(facturaID);

                    if (eliminado) {
                        // Eliminar la fila de la tabla
                        ((DefaultTableModel) facturasTable.getModel()).removeRow(selectedRow);
                        JOptionPane.showMessageDialog(facturasDialog, "Factura eliminada exitosamente y productos actualizados.");
                    } else {
                        JOptionPane.showMessageDialog(facturasDialog, "Error al eliminar la factura y actualizar productos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
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



    public static JPanel getAdminBillsPanel() {
        JPanel panel = new JPanel(new BorderLayout());


        JLabel titleLabel = new JLabel("Facturas", JLabel.CENTER);
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
        //panel.setTitle("Listado de Facturas");
        //panel.setSize(1280, 720);
        //panel.setLayout(new BorderLayout());
        /*panel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainAdmin() ; // Llamada a showMesas cuando se cierra la ventana
            }
        });*/
        FacturasAdminManager facturasAdminManager = new FacturasAdminManager();
        List<Factura> facturas = facturasAdminManager.getFacturas();

        // Columnas de la tabla
        String[] columnNames = {"ID", "Productos", "Total", "Fecha y Hora","Tipo de pago"};
        Object[][] data = new Object[facturas.size()][5];

        // Llenar los datos en la tabla
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            data[i][0] = f.getId();
            data[i][1] = f.getProductos();
            data[i][2] = f.getTotal();
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
        Font font = new Font("Arial", Font.PLAIN, 18); // Cambiar el tipo y tamaño de fuente
        facturasTable.setFont(font);
        facturasTable.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = facturasTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente más grande para el encabezado
        header.setBackground(Color.LIGHT_GRAY); // Fondo para el encabezado
        header.setForeground(Color.BLACK); // Color del texto del encabezado

        // Configuración de borde y color de fondo
        facturasTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        facturasTable.setBackground(Color.WHITE); // Fondo de la tabla
        facturasTable.setSelectionBackground(Color.CYAN); // Color de selección
        facturasTable.setSelectionForeground(Color.BLACK); // Color del texto seleccionado
        JScrollPane scrollPane = new JScrollPane(facturasTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        // Crear el botón para eliminar
        JButton eliminarButton = new JButton("Eliminar"){
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

        //eliminarButton.setEnabled(false); // Inicialmente deshabilitado

        eliminarButton.setPreferredSize(new Dimension(160, 40)); // Más grande
        eliminarButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        eliminarButton.setForeground(Color.WHITE); // Texto negro
        eliminarButton.setFocusPainted(false);
        eliminarButton.setContentAreaFilled(false);
        eliminarButton.setBorderPainted(false);
        eliminarButton.setOpaque(false);


// Botón para eliminar una factura seleccionada
        eliminarButton.addActionListener(e -> {
            int selectedRow = facturasTable.getSelectedRow();
            if (selectedRow != -1) {
                String facturaID = facturasTable.getValueAt(selectedRow, 0).toString(); // Obtener el ID de la factura seleccionada
                int confirm = JOptionPane.showConfirmDialog(panel, "¿Seguro que deseas eliminar esta factura?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Llamar al método que elimina la factura y actualiza el inventario
                    boolean eliminado = facturasAdminManager.eliminarFacturaYActualizarProductos(facturaID);

                    if (eliminado) {
                        // Eliminar la fila de la tabla
                        ((DefaultTableModel) facturasTable.getModel()).removeRow(selectedRow);
                        JOptionPane.showMessageDialog(panel, "Factura eliminada exitosamente y productos actualizados.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Error al eliminar la factura y actualizar productos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Por favor, selecciona una factura para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Añadir el botón de eliminación
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(eliminarButton);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;

    }
}