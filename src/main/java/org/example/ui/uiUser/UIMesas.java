package org.example.ui.uiUser;

import org.example.model.Mesa;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.manager.userManager.ExcelManager.*;
import static org.example.ui.uiUser.UIVenta.showVentaMesaDialog;

public class UIMesas {
    // Método para mostrar las mesas en la interfaz
    public static void showMesas() {
        JFrame mesasFrame = new JFrame("Administrar Mesas");
        mesasFrame.setSize(1200, 600);
        mesasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mesasPanel = new JPanel();
        mesasPanel.setLayout(new GridLayout(0, 5)); // Filas de 5 mesas

        // Cargar las mesas desde el archivo Excel
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        // Mostrar las mesas cargadas desde el archivo Excel
        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf((i + 1))); // Asignar ID basado en la posición
            JPanel mesaPanel = crearMesaPanel(mesa,mesasFrame); // Pasar el objeto Mesa
            mesasPanel.add(mesaPanel);
        }

        // Botón para añadir más mesas
        JButton addMesaButton = new JButton("Añadir Mesa");
        addMesaButton.addActionListener(e -> {
            // Generar un nuevo ID basado en la cantidad actual de mesas
            String nuevoID = String.valueOf(mesas.size() + 1); // Asegurarse de que el ID sea único
            Mesa nuevaMesa = new Mesa(nuevoID); // Crear la nueva mesa con el ID basado en el nuevo ID

            // Añadir la nueva mesa a la lista de mesas
            mesas.add(nuevaMesa);

            // Crear el panel para la nueva mesa
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa,mesasFrame);
            mesasPanel.add(nuevaMesaPanel);

            // Actualizar el panel de mesas
            mesasPanel.revalidate();
            mesasPanel.repaint();

            // Guardar la nueva mesa en el archivo Excel
            agregarMesaAExcel(nuevaMesa);
        });

        // Panel inferior con el botón para añadir mesas
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);

        mesasFrame.setLayout(new BorderLayout());
        mesasFrame.add(mesasPanel, BorderLayout.CENTER);
        mesasFrame.add(bottomPanel, BorderLayout.SOUTH);

        mesasFrame.setLocationRelativeTo(null);
        mesasFrame.setVisible(true);
    }
    // Método para crear un panel de mesa con botón "Atender Mesa"
    public static JPanel crearMesaPanel(Mesa mesa, JFrame mesasFrame) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        // Crear el borde con el título que incluye el número de la mesa
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                "Mesa " + mesa.getId(), // Mostrar el número de la mesa
                TitledBorder.CENTER, TitledBorder.TOP
        );

        // Establecer la fuente personalizada para el título
        border.setTitleFont(new Font("Arial", Font.BOLD, 13));


        // Asignar el borde al panel de la mesa
        mesaPanel.setBorder(border);
        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? Color.RED : Color.GREEN);

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mesaLabel.setForeground(Color.BLACK);

        // Crear el botón "Atender Mesa"
        JButton editarButton = new JButton("ATENDER MESA");
        editarButton.setFont(new Font("Arial", Font.BOLD, 13));
        // Fuente más grande para el encabezado


        // Añadir un ActionListener que capture el título del borde del panel (que contiene el ID de la mesa)
        editarButton.addActionListener(e -> {
            // Obtener el título del borde que tiene el nombre de la mesa
            TitledBorder panelBorder = (TitledBorder) mesaPanel.getBorder();
            String tituloMesa = panelBorder.getTitle();  // Esto devolverá algo como "Mesa 19"
            System.out.println("Atendiendo: " + tituloMesa); // Debug para verificar el título

            // Cargar los productos de la mesa desde Excel usando el título
            List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

            // Minimizar la ventana de las mesas
            mesasFrame.dispose();

            // Mostrar los productos de la mesa en un diálogo
            showVentaMesaDialog(productosMesa, tituloMesa);
        });

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editarButton);
        buttonPanel.setBackground(Color.GRAY);
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); /// Fondo blanco para los botones

        // Añadir componentes al panel de la mesa
        mesaPanel.add(mesaLabel, BorderLayout.CENTER); // Etiqueta en el centro
        mesaPanel.add(buttonPanel, BorderLayout.SOUTH); // Botones en la parte inferior

        return mesaPanel;
    }
}
