package org.example.ui.uiUser;

import org.example.model.Mesa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.example.manager.userManager.ExcelUserManager.*;
import static org.example.ui.uiUser.UIUserVenta.showVentaMesaDialog;

public class UIUserMesas {
    // Método para mostrar las mesas en la interfaz
   /* public static void showMesas() {
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
    }*/

    // Método para crear un panel de mesa sin botón, con acción en todo el panel
 /*   public static JPanel crearMesaPanel(Mesa mesa, JFrame mesasFrame) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        // Obtener el ID de la mesa correctamente
        String idMesa = mesa.getId();
        String tituloMesa = "Mesa " + idMesa;

        // Crear el borde con el título (sin modificar el ID)
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                tituloMesa, // Usar directamente el ID de la mesa
                TitledBorder.CENTER, TitledBorder.TOP
        );

        // Aplicar la fuente personalizada al título
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 13));
        mesaPanel.setBorder(titledBorder);

        // Cambiar color de fondo según estado de ocupación
        mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 102, 102) : new Color(144, 238, 144)); // Rojo si está ocupada, verde si está libre

        // Texto descriptivo dentro de la mesa
        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mesaLabel.setForeground(Color.BLACK);

        // Hacer que el panel sea interactivo como un botón
        mesaPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Atendiendo: " + tituloMesa); // Debug

                // Cargar productos de la mesa desde Excel
                List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

                // Cerrar la ventana de mesas
                mesasFrame.dispose();

                // Mostrar la ventana de venta de la mesa
                showVentaMesaDialog(productosMesa, tituloMesa);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Elevar el panel al pasar el mouse con un borde más grueso y oscuro
                mesaPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 3), // Borde más grueso y oscuro
                        tituloMesa,
                        TitledBorder.CENTER, TitledBorder.TOP
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Restaurar el borde original
                mesaPanel.setBorder(titledBorder);
            }
        });

        // Añadir la etiqueta de estado al panel
        mesaPanel.add(mesaLabel, BorderLayout.CENTER);

        return mesaPanel;
    }*/

    private static Color fondoPrincipal = new Color(250, 240, 230);


    public static JPanel crearMesaPanel(Mesa mesa, JFrame mainFrame, JPanel mainPanel) {
        JPanel mesaPanel = new JPanel(new BorderLayout());
        mesaPanel.setPreferredSize(new Dimension(100, 100));

        String idMesa = mesa.getId();



        JLabel titleLabel = new JLabel("Mesa " + idMesa, JLabel.CENTER);
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

        String tituloMesa = titleLabel.getText();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                tituloMesa,
                TitledBorder.CENTER, TitledBorder.TOP
        );

        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 18));
        mesaPanel.setBorder(titledBorder);

        mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));

        JLabel mesaLabel = new JLabel(mesa.isOcupada() ? "OCUPADA" : "LIBRE", SwingConstants.CENTER);
        mesaLabel.setFont(new Font("Arial", Font.BOLD, 28));
        mesaLabel.setForeground(Color.DARK_GRAY);

        mesaPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Atendiendo: " + tituloMesa);

                // Cargar productos de la mesa desde Excel
                List<String[]> productosMesa = cargarProductosMesaDesdeExcel(tituloMesa);

                // Asegurarse de que mainPanel tiene CardLayout
                if (!(mainPanel.getLayout() instanceof CardLayout)) {
                    mainPanel.setLayout(new CardLayout());
                }

// Obtener el CardLayout
                CardLayout cl = (CardLayout) mainPanel.getLayout();

// Agregar el nuevo panel de venta de mesa si no está agregado ya
                mainPanel.add(new VentaMesaPanel(productosMesa, tituloMesa, mainPanel,mainFrame), "VentaMesaPanel");

// Mostrar el panel de ventas
                cl.show(mainPanel, "VentaMesaPanel");
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                TitledBorder newBorder = BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 3),
                        tituloMesa,
                        TitledBorder.CENTER, TitledBorder.TOP
                );
                mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 60, 60) : new Color(0, 201, 87));

                newBorder.setTitleFont(new Font("Arial", Font.BOLD, 20)); // Cambiar fuente a 28
                mesaPanel.setBorder(newBorder);
                mesaLabel.setForeground(Color.WHITE);
                newBorder.setTitleColor(Color.white);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mesaPanel.setBorder(titledBorder);
                mesaPanel.setBackground(mesa.isOcupada() ? new Color(255, 111, 97) : new Color(168, 230, 207));
                mesaLabel.setForeground(Color.DARK_GRAY);
            }
        });

        mesaPanel.add(mesaLabel, BorderLayout.CENTER);
        return mesaPanel;
    }

    public static JPanel showPanelMesas(JFrame mainFrame, JPanel contentPanel) {
        JPanel mesasPanel = new JPanel(new BorderLayout());
        mesasPanel.setBackground(fondoPrincipal);

        mesasPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 100), 0, true),
                new EmptyBorder(10, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Mesas", JLabel.CENTER);
        titleLabel.setForeground(new Color (28, 28, 28));
        try {
            InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream("Lobster-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);
            titleLabel.setFont(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel gridMesasPanel = new JPanel(new GridLayout(0, 5, 4, 4));
        gridMesasPanel.setBackground(fondoPrincipal);
        ArrayList<Mesa> mesas = cargarMesasDesdeExcel();

        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            mesa.setID(String.valueOf(i + 1));
            JPanel mesaPanel = crearMesaPanel(mesa, mainFrame, contentPanel);
            gridMesasPanel.add(mesaPanel);
        }

        JButton addMesaButton = new JButton("Nueva Mesa") {
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
        addMesaButton.setPreferredSize(new Dimension(160, 40)); // Más grande
        addMesaButton.setFont(new Font("Arial", Font.BOLD, 22)); // Fuente grande
        addMesaButton.setForeground(Color.WHITE); // Texto negro
        addMesaButton.setFocusPainted(false);
        addMesaButton.setContentAreaFilled(false);
        addMesaButton.setBorderPainted(false);
        addMesaButton.setOpaque(false);

        addMesaButton.addActionListener(e -> {
            String nuevoID = String.valueOf(mesas.size() + 1);
            Mesa nuevaMesa = new Mesa(nuevoID);
            mesas.add(nuevaMesa);
            JPanel nuevaMesaPanel = crearMesaPanel(nuevaMesa, mainFrame, contentPanel);
            gridMesasPanel.add(nuevaMesaPanel);
            gridMesasPanel.revalidate();
            gridMesasPanel.repaint();
            agregarMesaAExcel(nuevaMesa);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addMesaButton);
        bottomPanel.setBackground(new Color(250, 240, 230));// Color de fondo amarillo
        mesasPanel.add(titleLabel, BorderLayout.NORTH);
        mesasPanel.add(gridMesasPanel, BorderLayout.CENTER);
        mesasPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mesasPanel;
    }






}
