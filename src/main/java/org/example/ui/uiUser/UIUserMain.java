package org.example.ui.uiUser;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.manager.userManager.FacturacionUserManager;

import javax.swing.*;
import java.awt.*;

import static org.example.manager.userManager.ExcelUserManager.eliminarMesasConIdMayorA10;
import static org.example.ui.uiAdmin.MainAdminUi.*;
import static org.example.ui.uiUser.UIUserFacturas.showFacturasDialog;
import static org.example.ui.uiUser.UIUserGastos.showGastosGeneralesDialog;
import static org.example.ui.UIHelpers.createButton;
//import static org.example.ui.uiUser.UIUserMesas.showMesas;
import static org.example.ui.uiUser.UIUserMesas.showPanelMesas;
import static org.example.ui.uiUser.UIUserProductList.showListProductsDialog;

public class UIUserMain {
    // Estado para alternar entre los botones originales y las opciones adicionales
    private static boolean showingMoreOptions = false;

    private static String nombreEmpleado = "Empleado";




    public static void mainUser() {
        try {
            // Aplicar FlatLaf para un estilo moderno
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Crear ventana principal
            JFrame frame = new JFrame("Licorera LA 70");
            frame.setSize(1280, 720); // Tamaño más grande para incluir la barra lateral
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            // Crear un panel principal con un fondo gris claro
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245)); // Fondo gris claro

            /*// Crear el título estilizado para la parte superior
            JLabel titleLabel = new JLabel("Licorera LA 70", JLabel.RIGHT);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 40)); // Estilo y tamaño de la fuente
            titleLabel.setForeground(Color.RED); // Color del texto*/


            // Panel central con botones 2x2
            JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Grid 2x2
            buttonPanel.setBackground(new Color(245, 245, 245)); // Fondo igual al principal
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen

            // Crear y añadir los botones iniciales
            //addInitialButtons(buttonPanel, frame);

            // Crear barra lateral con el nombre del empleado y el botón "Opciones" al final
            JPanel sidebarPanel = new JPanel(new BorderLayout());
            sidebarPanel.setPreferredSize(new Dimension(250, 600)); // Ancho fijo para la barra lateral
            sidebarPanel.setBackground(new Color(220, 200, 180)); // Color gris claro para la barra lateral

// Nombre del empleado en la parte superior de la barra lateral
            JLabel employeeLabel = new JLabel(nombreEmpleado, JLabel.CENTER);
            employeeLabel.setFont(new Font("Arial", Font.BOLD, 40)); // Estilo y tamaño de la fuente
            employeeLabel.setForeground(Color.BLACK); // Color del texto
            sidebarPanel.add(employeeLabel, BorderLayout.BEFORE_LINE_BEGINS); // Colocar en la parte superior

// Panel para los botones con BoxLayout en Y_AXIS
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS)); // Alinear en columna
            buttonsPanel.setBackground(new Color(220, 200, 180));
            // Ancho fijo para los botones
// Crear los botones

            // Cargar la imagen desde la ruta especificada
            ImageIcon originalIcon = new ImageIcon((UIUserMain.class.getResource("/icons/mesa-redonda.png")));
            ImageIcon originalProductsIcon = new ImageIcon((UIUserMain.class.getResource("/icons/lista-de_productos.png")));
            // Redimensionar la imagen
            Image imgProduct = originalProductsIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon productsIcon = new ImageIcon(imgProduct);

            // Botón "Lista de Productos" - Abre la lista de productos
            JButton listaProductosButton = createButton("Lista de Productos", productsIcon, e -> {


                showListProductsDialog();
                frame.dispose();
            });

            ImageIcon originalGastosIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Gastos.png")));
            // Redimensionar la imagen
            Image imgGastos = originalGastosIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon GastosIcon = new ImageIcon(imgGastos);

            JButton gastosButton = createButton("Gastos",  GastosIcon, e -> {
                showGastosGeneralesDialog();
                frame.dispose();
            });



            ImageIcon originalFacturarIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Facturar.png")));
            // Redimensionar la imagen
            Image imgfacturar = originalFacturarIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites

            ImageIcon facturarIcon = new ImageIcon(imgfacturar);

            JButton salirButton = createButton("Finalizar Día", facturarIcon, e -> {
                String input = JOptionPane.showInputDialog(null, "Por favor escribe 'Facturar'", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);
                if (FacturacionUserManager.verificarFacturacion(input)) {
                    FacturacionUserManager.facturarYSalir();

                } else {
                    FacturacionUserManager.mostrarErrorFacturacion();
                }
            });


            ImageIcon originalCajaRegistradoraIcon = new ImageIcon((UIUserMain.class.getResource("/icons/admin/beneficios.png")));
             //Redimensionar la imagen
            Image imgCajaRegistradora = originalCajaRegistradoraIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon cajaRegistradoraIcon = new ImageIcon(imgCajaRegistradora);

            JButton moreOptionsButton =  createButton("Re-imprimir Facturas", cajaRegistradoraIcon, e -> {
                showFacturasDialog(); // Llamar a la función deseada
                frame.dispose();
            });

            ImageIcon originalAdminIcon = new ImageIcon((UIUserMain.class.getResource("/icons/obrero.png")));
            // Redimensionar la imagen
            Image imgAdmin = originalAdminIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon adminIcon = new ImageIcon(imgAdmin);

            JButton moreOptionsButtons =  createButton("Administrador",adminIcon, e -> {
                adminPassword(); // Llamar a la función deseada
                frame.dispose();
            });


// Configuración de fuente y tamaño uniforme para todos los botones
            Font buttonFont = new Font("Arial", Font.BOLD, 16);
            Dimension buttonSize = new Dimension(150, 50);

// Función auxiliar para aplicar estilo a los botones

            listaProductosButton.setFont(buttonFont);


// Aplicar estilos a todos los botones





// Agregar botones al panel


            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(listaProductosButton);
            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(gastosButton);
            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(moreOptionsButton);
            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(salirButton);
            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(moreOptionsButtons);
            buttonsPanel.add(Box.createVerticalGlue()); // Espacio flexible




        /*    // Botón "Más Opciones"
// Crear un nuevo botón "Opciones"
            JButton MesasButton = new JButton("Mesas");

// Agregar el ActionListener al botón
            MesasButton.addActionListener(e -> {
                toggleButtonOptions(buttonPanel, frame); // Llamar a la función deseada
            });            MesasButton.setFont(new Font("Arial", Font.BOLD, 16));
            MesasButton.setPreferredSize(new Dimension(150, 50));

            // Colocar el botón "Opciones" en la parte inferior de la barra lateral
            sidebarPanel.add(MesasButton, BorderLayout.NORTH);*/



            // Añadir el panel de botones y la barra lateral al panel principal
            /*mainPanel.add(titleLabel, BorderLayout.NORTH);*/
            mainPanel.add(showPanelMesas(frame), BorderLayout.CENTER); // Panel de botones al centro
            mainPanel.add(sidebarPanel, BorderLayout.WEST);  // Barra lateral a la izquierda
// Agregar el panel de botones al centro de la barra lateral


            sidebarPanel.add(buttonsPanel, BorderLayout.SOUTH);
            // Añadir el panel principal a la ventana
            frame.add(mainPanel);

            // Centrar la ventana
            frame.setLocationRelativeTo(null);



            // Mostrar la ventana principal
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   /* // Método para agregar los botones iniciales
    private static void addInitialButtons(JPanel buttonPanel, JFrame frame) {
        FacturacionUserManager facturacionUserManager = new FacturacionUserManager(); // Instancia de FacturacionManager
        // Cargar la imagen desde la ruta especificada
        ImageIcon originalIcon = new ImageIcon((UIUserMain.class.getResource("/icons/mesa-redonda.png")));
        ImageIcon originalProductsIcon = new ImageIcon((UIUserMain.class.getResource("/icons/lista-de_productos.png")));
        // Redimensionar la imagen
        Image imgProduct = originalProductsIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon productsIcon = new ImageIcon(imgProduct);

        JButton adminProductosButton = createButton("Lista de Productos", productsIcon, e -> { showListProductsDialog(); frame.dispose(); });



        ImageIcon originalGastosIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Gastos.png")));
        // Redimensionar la imagen
        Image imgGastos = originalGastosIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon GastosIcon = new ImageIcon(imgGastos);

        JButton gastosButton = createButton("Gastos", GastosIcon, e -> { showGastosGeneralesDialog(); frame.dispose(); });


        // Redimensionar la imagen
        Image img = originalIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon mesaIcon = new ImageIcon(img);
        JButton mesasButton = createButton("Mesas",mesaIcon , e -> { mainUser(); frame.dispose(); });

        ImageIcon originalFacturarIcon = new ImageIcon((UIUserMain.class.getResource("/icons/Facturar.png")));

        // Redimensionar la imagen
        Image imgFacturar = originalFacturarIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
        ImageIcon FacturarIcon = new ImageIcon(imgFacturar);


        JButton salirButton = createButton("Facturar", FacturarIcon, e -> {
            String input = JOptionPane.showInputDialog(null, "Por favor escribe 'Facturar'", "Confirmar Facturación", JOptionPane.QUESTION_MESSAGE);
            if (facturacionUserManager.verificarFacturacion(input)) {
                facturacionUserManager.facturarYSalir();

            } else {
                facturacionUserManager.mostrarErrorFacturacion();
            }
        });

        // Añadir botones al panel de botones
        buttonPanel.add(mesasButton);
        buttonPanel.add(adminProductosButton);
        buttonPanel.add(gastosButton);
        buttonPanel.add(salirButton);
    }

    // Método para alternar entre los botones iniciales y las opciones adicionales
    private static void toggleButtonOptions(JPanel buttonPanel, JFrame frame) {
        // Limpiar el panel de botones actual
        buttonPanel.removeAll();

        if (!showingMoreOptions) {


            ImageIcon originalfacturasIcon = new ImageIcon((UIUserMain.class.getResource("/icons/admin/beneficios.png")));
            // Redimensionar la imagen
            Image imgfacturas = originalfacturasIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon facturasIcon = new ImageIcon(imgfacturas);
            // Mostrar las nuevas opciones "Facturas" y "Administrador"
            // Crear los botones utilizando createButton y pasando null para el ícono
            JButton facturasButton = createButton("Re-Imprimir Facturas", facturasIcon, e -> {
                showFacturasDialog();
                frame.dispose();
            });


            ImageIcon originalAdminIcon = new ImageIcon(UIUserMain.class.getResource("/icons/obrero.png"));
            // Redimensionar la imagen
            Image imgAdmin = originalAdminIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon AdminIcon = new ImageIcon(imgAdmin);



            JButton adminButton = createButton("Administrador", AdminIcon, e -> {

                adminPassword();
                frame.dispose();  // Cierra la ventana actual
            });

            ImageIcon originalCajaRegistradoraIcon = new ImageIcon((UIUserMain.class.getResource("/icons/admin/cajero-automatico.png")));
            // Redimensionar la imagen
            Image imgCajaRegistradora = originalCajaRegistradoraIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH); // Cambia 80, 80 a lo que necesites
            ImageIcon CajaRegistradoraIcon = new ImageIcon(imgCajaRegistradora);


            JButton abrirCajaButton = createButton("Abrir Caja", CajaRegistradoraIcon, e -> abrirCajaRegistradora());

            // Agregar los botones al panel (si es necesario)
            buttonPanel.add(facturasButton);
            buttonPanel.add(adminButton);
            buttonPanel.add(abrirCajaButton);


        } else {
            // Restaurar los botones iniciales
            addInitialButtons(buttonPanel,frame);
        }

        // Cambiar el estado de los botones
        showingMoreOptions = !showingMoreOptions;

        // Refrescar el panel para mostrar los cambios
        buttonPanel.revalidate();
        buttonPanel.repaint();

        // Actualizar el frame para reflejar los cambios visuales
        frame.repaint();
    }*/

}
