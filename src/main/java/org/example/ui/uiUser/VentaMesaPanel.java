package org.example.ui.uiUser;
import org.example.manager.userManager.ProductoUserManager;
import org.example.manager.userManager.VentaMesaUserManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


//import static org.example.manager.userManager.ExcelUserManager.cargarProductosMesaDesdeExcel;
import static org.example.manager.userDBManager.DatabaseUserManager.cargarProductosMesaDesdeBD;
import static org.example.ui.UIHelpers.*;
import static org.example.ui.uiUser.UIUserVenta.*;

public class VentaMesaPanel extends JPanel {


    public VentaMesaPanel(List<String[]> productos, String mesaID, JPanel mainPanel, JFrame frame) {

        AtomicReference<Double> sumaTotal = new AtomicReference<>(0.0);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1366, 720));

        JLabel titleLabel = new JLabel("Venta "+mesaID, JLabel.CENTER);
        titleLabel.setForeground(new Color(28,28,28));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(250, 240, 230));
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

        // Crear la tabla de productos usando createProductTable
        JTable table = createProductTable();
        table.getColumnModel().getColumn(2).setCellRenderer(new UIHelpers.CurrencyRenderer());


        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Establecer la fuente y el tamaño de la tabla
        Font font = new Font("Arial", Font.PLAIN, 18); // Cambiar el tipo y tamaño de fuente
        table.setFont(font);
        table.setRowHeight(30); // Aumentar la altura de las filas

        // Establecer la fuente para el encabezado
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente más grande para el encabezado
        header.setBackground(new Color(28,28,28)); // Fondo para el encabezado
        header.setForeground(new Color(201, 41, 41)); // Color del texto del encabezado

        // Configuración de borde y colores para la tabla
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        table.setBackground(new Color(250, 240, 230)); // Fondo de la tabla
        table.setSelectionBackground(new Color(173, 216, 255)); // Color de selección
        table.setSelectionForeground(Color.BLACK); // Color del texto seleccionado
        try (InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf")) {
            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente 'Lobster-Regular.ttf' en los recursos.");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.ITALIC, 26);

            header.setFont(customFont);
            header.setForeground(new Color(201, 41, 41));

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar la fuente personalizada: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        tableModel.setRowCount(0);

        // Cargar los productos en la tabla y calcular el total inicial
        for (String[] productoDetalles : productos) {
            try {
                String nombreProducto = productoDetalles[0].trim();
                int cantidad = Integer.parseInt(productoDetalles[1].substring(1).trim());
                double precioUnitario = Double.parseDouble(productoDetalles[2].substring(1).trim());
                double total = cantidad * precioUnitario;

                tableModel.addRow(new Object[]{nombreProducto, cantidad, FormatterHelpers.formatearMoneda(precioUnitario), total});
                sumaTotal.updateAndGet(v -> v + total);
            } catch (NumberFormatException ex) {
                System.err.println("Error al parsear datos: " + Arrays.toString(productoDetalles));
                ex.printStackTrace();
            }
        }

        // Campo para mostrar el total
        JTextField totalField = new JTextField("Total: $" + FormatterHelpers.formatearMoneda(sumaTotal.get()) + " Pesos");
        totalField.setFont(new Font("Arial", Font.BOLD, 26));
        totalField.setForeground(Color.RED);
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(JTextField.CENTER);
        totalField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 90));
        totalField.setVisible(sumaTotal.get() > 0);
        totalField.setBackground(new Color(250, 240, 230)); // Fondo del campo total
        try {
            // Cargar la fuente desde los recursos
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            // Crear la fuente y derivar un tamaño adecuado
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);

            // Aplicar el texto formateado
            totalField.setText("Total: $ " + FormatterHelpers.formatearMoneda(sumaTotal.get()) + " Pesos");

            // Aplicar la fuente al JTextField
            totalField.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace(); // Mostrar error en caso de fallo al cargar la fuente
        }

        // Panel del total
        JPanel totalPanel = createTotalPanel();
        totalPanel.add(totalField, BorderLayout.CENTER);

        // Listener para actualizar el total al cambiar la cantidad o agregar productos
        tableModel.addTableModelListener(new TableModelListener() {
            // Bandera para evitar recursión
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

                    totalField.setText("Total: $ " + FormatterHelpers.formatearMoneda(nuevoTotalGeneral) + " Pesos");
                    totalField.setVisible(nuevoTotalGeneral > 0);

                    try {
                        // Cargar la fuente desde los recursos
                        InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

                        if (fontStream == null) {
                            throw new IOException("No se pudo encontrar la fuente en los recursos.");
                        }

                        // Crear la fuente y derivar un tamaño adecuado
                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);

                        // Aplicar el texto formateado
                        totalField.setText("Total: $ " + FormatterHelpers.formatearMoneda(nuevoTotalGeneral) + " Pesos");

                        // Aplicar la fuente al JTextField
                        totalField.setFont(customFont);
                    } catch (FontFormatException | IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error al cargar la fuente personalizada: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    updatingTable = false;
                }
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(table);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = UIHelpers.createInputPanel(table, new VentaMesaUserManager());

        add(titleLabel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.EAST);

        // Botón independiente para abrir el submenú de productos
        JButton openSubMenuButton = new JButton("Abrir Submenú de Productos");
        openSubMenuButton.setFont(new Font("Arial", Font.BOLD, 18));
        openSubMenuButton.addActionListener(e -> createProductSubMenu(table, ProductoUserManager.getProducts(), mainPanel));

        totalPanel.setBackground((new Color(250, 240, 230)));
        JPanel buttonPanel = createButtonPanel(table, new VentaMesaUserManager(), (JDialog) compraDialog, mesaID, mainPanel, frame);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

    }
    private static JPanel createProductSubMenu(JTable table, List<Producto> productos, JPanel mainPanel) {
        JPanel subMenuPanel = new JPanel(new BorderLayout());

        // 📌 Panel principal para los botones
        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columnas, filas dinámicas

        for (Producto producto : productos) {
            JButton productoButton = new JButton(producto.getName());
            productoButton.setPreferredSize(new Dimension(120, 50)); // Tamaño de botón fijo
            productoButton.setFont(new Font("Arial", Font.BOLD, 16));

            productoButton.addActionListener(e -> {
                int cantidad = 1;
                double precioUnitario = producto.getPrice();
                double total = cantidad * precioUnitario;

                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                tableModel.addRow(new Object[]{
                        producto.getName(),
                        cantidad,
                        FormatterHelpers.formatearMoneda(precioUnitario),
                        total
                });

                updateTotalField(table, mainPanel); // Actualizar el campo total

                // 🔹 Volver a la vista principal
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.show(mainPanel, "mainView");
            });

            buttonPanel.add(productoButton);
        }

        // 📌 Añadir JScrollPane para hacer scroll si hay muchos productos
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 📌 Botón "Volver" para regresar a la vista principal
        JButton volverButton = new JButton("Volver");
        volverButton.setPreferredSize(new Dimension(150, 40));
        volverButton.setFont(new Font("Arial", Font.BOLD, 18));
        volverButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "mainView"); // 🔹 Regresar a la vista principal
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(volverButton);


        // 📌 Agregar todo al panel principal
        subMenuPanel.add(scrollPane, BorderLayout.CENTER);
        subMenuPanel.add(bottomPanel, BorderLayout.SOUTH);

        return subMenuPanel;
    }

    private static void updateTotalField(JTable table, JPanel mainPanel) {
        double nuevoTotal = 0;
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int cantidad = (int) tableModel.getValueAt(i, 1);
            double precioUnitario = Double.parseDouble(tableModel.getValueAt(i, 2).toString().replace(",", ""));
            nuevoTotal += cantidad * precioUnitario;
        }

        JTextField totalField = (JTextField) ((JPanel) mainPanel.getComponent(2)).getComponent(0);
        totalField.setText("Total: $ " + FormatterHelpers.formatearMoneda(nuevoTotal) + " Pesos");
        totalField.setVisible(nuevoTotal > 0);
        try {
            // Cargar la fuente desde los recursos
            InputStream fontStream = UIHelpers.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");

            if (fontStream == null) {
                throw new IOException("No se pudo encontrar la fuente en los recursos.");
            }

            // Crear la fuente y derivar un tamaño adecuado
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 26);

            // Aplicar el texto formateado
            totalField.setText("Total: $ " + FormatterHelpers.formatearMoneda(nuevoTotal) + " Pesos");

            // Aplicar la fuente al JTextField
            totalField.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace(); // Mostrar error en caso de fallo al cargar la fuente
        }
    }

    private JPanel createButtonPanel(JTable table, VentaMesaUserManager ventaMesaUserManager, JDialog compraDialog, String mesaID, JPanel mainPanel,JFrame frame) {
        JPanel buttonPanel = new JPanel(new BorderLayout()); // 🔹 Usamos BorderLayout

        // 📌 Panel con GridLayout para que los botones de compra ocupen todo el ancho
        JPanel topButtonsPanel = new JPanel(new GridLayout(1, 2, 0, 10)); // 1 fila, 2 columnas, separación de 20px

        JButton guardarCompra = createSavePurchaseMesaButton(ventaMesaUserManager, mesaID, table, frame);
        guardarCompra.setFont(new Font("Arial", Font.BOLD, 22));

        JButton confirmarCompraButton = createConfirmPurchaseMesaButton(ventaMesaUserManager, compraDialog, mesaID, table, frame);
        confirmarCompraButton.setFont(new Font("Arial", Font.BOLD, 22));


        //List<String[]> productosPrevios = cargarProductosMesaDesdeExcel(mesaID);
        List<String[]> productosPrevios = cargarProductosMesaDesdeBD(mesaID);

        boolean productosEnExcel = !productosPrevios.isEmpty();
        confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0);

        ((DefaultTableModel) table.getModel()).addTableModelListener(e -> {
            confirmarCompraButton.setEnabled(productosEnExcel || table.getRowCount() > 0);
        });

        // 🔹 Asegurar que los botones cubran todo el ancho
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
        closeButton.setFont(new Font("Arial", Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(250, 240, 230));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        closeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "mesas"); // Vuelve a la vista de mesas
        });

        // 🔹 Panel para centrar el botón "Volver"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(closeButton);

        buttonPanel.add(bottomPanel, BorderLayout.SOUTH); // 🔹 Botón "Volver" centrado en la parte inferior
        bottomPanel.setBackground( new Color(250, 240, 230));
        return buttonPanel;
    }
}
