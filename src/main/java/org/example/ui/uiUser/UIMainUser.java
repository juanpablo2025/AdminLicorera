package org.example.ui.uiUser;

import org.example.manager.userManager.FacturacionManager;
import org.example.manager.userManager.VentaMesaManager;

import javax.swing.*;
import java.awt.*;

import static org.example.manager.userManager.MainManager.crearDirectorios;
import static org.example.ui.uiUser.UIGastos.showGastosGeneralesDialog;
import static org.example.ui.uiUser.UIHelpers.createButton;
import static org.example.ui.uiUser.UIMesas.showMesas;
import static org.example.ui.uiUser.UIProductList.showListProductsDialog;
import static org.example.utils.Constants.*;

public class UIMainUser {
    public static void mainUser(){
        crearDirectorios();
        JFrame frame = new JFrame(CALCULADORA_ADMINISTRADOR);
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Evitar cerrar directamente
        //frame.setUndecorated(true); // Quitar bordes
        frame.setSize(600, 400); // Tamaño de la ventana

        // Crear un panel principal con un fondo azul oscuro y márgenes
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(45, 85, 135)); // Fondo azul oscuro para el panel principal

        // Panel central con botones
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Grid 2x2 con espacio entre botones
        buttonPanel.setBackground(new Color(45, 85, 135)); // Fondo azul oscuro para las líneas de los botones
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Añadir márgenes alrededor del panel

        // Inicializar la pestaña de mesas en el archivo Excel
        VentaMesaManager.initializeMesasSheet();

        FacturacionManager facturacionManager = new FacturacionManager(); // Instancia de FacturacionManager

        // Crear botones estilizados

        JButton adminProductosButton = createButton(LISTAR_PRODUCTOS, e -> showListProductsDialog());
        adminProductosButton.setFont(new Font("Arial", Font.BOLD, 18)); // Fuente y tamaño

        JButton gastosButton = createButton("GASTOS", e -> showGastosGeneralesDialog());
        gastosButton.setFont(new Font("Arial", Font.BOLD, 18)); // Fuente y tamaño

        JButton mesasButton = createButton("MESAS", e -> showMesas());
        mesasButton.setFont(new Font("Arial", Font.BOLD, 18)); // Fuente y tamaño

        JButton salirButton = createButton(SALIR_FACTURAR, e -> {
            String input = JOptionPane.showInputDialog(null, POR_FAVOR_ESCRIBE_FACTURAR, CONFIRMAR_FACTURACION, JOptionPane.QUESTION_MESSAGE);
            if (facturacionManager.verificarFacturacion(input)) {
                facturacionManager.facturarYSalir();
            } else {
                facturacionManager.mostrarErrorFacturacion();
            }
        });
        salirButton.setFont(new Font("Arial", Font.BOLD, 18)); // Fuente y tamaño


        // Añadir botones al panel de botones
        buttonPanel.add(mesasButton);
        buttonPanel.add(adminProductosButton);
        buttonPanel.add(gastosButton);
        buttonPanel.add(salirButton);

        // Añadir el panel de botones al panel principal
        mainPanel.add(buttonPanel, BorderLayout.CENTER); // Centrar el panel de botones

        // Añadir el panel principal a la ventana
        frame.add(mainPanel);

        // Centrar la ventana
        frame.setLocationRelativeTo(null);

        // Mostrar la ventana principal
        frame.setVisible(true);
    }


    private static void mainAdmin(){

    }
}
