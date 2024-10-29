package org.example.ui.uiUser;

import org.example.model.Mesa;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.ui.uiUser.UIUserMain.mainUser;
import static org.example.ui.uiUser.UIUserVenta.showVentaMesaDialog;

public class UIUserMesas {
    // Método para mostrar las mesas en la interfaz
    public static void showMesas() {
        JFrame mesasFrame = new JFrame("Administrar Mesas");
        mesasFrame.setSize(1280, 720);
        // Añadir un WindowListener para detectar el cierre de la ventana
        mesasFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Cuando se cierra la ventana de venta, mostrar la ventana de mesas
                mainUser();  // Llamada a showMesas cuando se cierra la ventana
            }
        });

        JPanel mesasPanel = new JPanel();
        mesasPanel.setLayout(new GridLayout(0, 5)); // Filas de 5 mesas
        // Crear el título estilizado para la parte superior
        JLabel titleLabel = new JLabel("Mesas", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Estilo y tamaño de la fuente
        titleLabel.setForeground(new Color(50, 50, 50)); // Color del texto
        // Cargar las mesas desde el archivo Excel
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        // Mostrar las mesas cargadas desde el archivo Excel
        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf((i + 1))); // Asignar ID basado en la posición
            JPanel mesaPanel = crearMesaPanel(mesa, mesasFrame); // Pasar el objeto Mesa
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
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa, mesasFrame);
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
        mesasFrame.add(titleLabel, BorderLayout.NORTH);
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
                BorderFactory.createLineBorder(Color.GRAY, 2),
                "Mesa " + mesa.getId(), // Mostrar el número de la mesa
                TitledBorder.CENTER, TitledBorder.TOP
        );

        // Establecer la fuente personalizada para el título
        border.setTitleFont(new Font("Arial", Font.BOLD, 13));

        // Asignar el borde al panel de la mesa
        mesaPanel.setBorder(border);

        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 102, 102) : new Color(144, 238, 144)); // Rojo suave si está ocupada, verde claro si está libre

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mesaLabel.setForeground(Color.BLACK);

        // Crear el botón "Atender Mesa"
        JButton editarButton = new JButton("ATENDER MESA");
        editarButton.setFont(new Font("Arial", Font.BOLD, 13));
        editarButton.setBackground(new Color(220, 220, 220)); // Fondo gris claro
        editarButton.setForeground(Color.BLACK); // Texto en negro

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
        buttonPanel.setBackground(new Color(245, 245, 245)); // Fondo gris muy claro para los botones
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2)); // Borde gris claro

        // Añadir componentes al panel de la mesa
        mesaPanel.add(mesaLabel, BorderLayout.CENTER); // Etiqueta en el centro
        mesaPanel.add(buttonPanel, BorderLayout.SOUTH); // Botones en la parte inferior
        // Agregar efecto de "elevar" al pasar el mouse
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Cambiar el color de fondo
                mesaPanel.setBounds(mesaPanel.getX(), mesaPanel.getY() - 2, mesaPanel.getWidth(), mesaPanel.getHeight()); // Levantar efecto
            }

            @Override
            public void mouseExited(MouseEvent e) {
                 // Volver al color original
                mesaPanel.setBounds(mesaPanel.getX(), mesaPanel.getY() + 2, mesaPanel.getWidth(), mesaPanel.getHeight()); // Volver a la posición original
            }
        });
        return mesaPanel;
    }
}
