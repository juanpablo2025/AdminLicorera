package org.example.manager.adminManager;

import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.util.List;

import static org.example.utils.Constants.*;

public class ProductoAdminManager {
    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public void addProduct(Producto product) {
        excelAdminManager.addProduct(product);
    }

    public List<Producto> getProducts() {
        return excelAdminManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelAdminManager.getProductByName(name);
    }


    public JTextField createField(JDialog dialog, String label) {
        dialog.add(new JLabel(label));
        JTextField textField = UIHelpers.createTextField();
        dialog.add(textField);
        return textField;
    }

    /**
     * Agrega un producto utilizando los valores de los campos de entrada.
     */
    public void addProductFromFields( JTextField nameField, JTextField quantityField, JTextField priceField, JDialog dialog) {
        try {

            String name = nameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());

            // Formateamos el precio con el formato de moneda de Colombia
            double price = Double.parseDouble(priceField.getText());
            String foto = "foto" ;

            // Crear un nuevo producto (guarda el precio original pero puedes mostrar el formateado)
            Producto product = new Producto( name, quantity, price,foto);

            // Añadir el producto a la lista/gestión de productos
            addProduct(product);

            JOptionPane.showMessageDialog(dialog, PRODUCT_ADDED);
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, INVALID_DATA, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }
}
