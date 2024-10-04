package org.example.manager;

import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.example.utils.Constants.*;

public class ProductoManager {
    private ExcelManager excelManager = new ExcelManager();

    public void addProduct(Producto product) {
        excelManager.addProduct(product);
    }

    public List<Producto> getProducts() {
        return excelManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelManager.getProductByName(name);
    }


    /**
     * Crea un campo de entrada de texto y añade una etiqueta asociada al diálogo.
     */
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

            // Crear un nuevo producto (guarda el precio original pero puedes mostrar el formateado)
            Producto product = new Producto( name, quantity, price);

            // Añadir el producto a la lista/gestión de productos
            addProduct(product);

            JOptionPane.showMessageDialog(dialog, PRODUCT_ADDED);
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, INVALID_DATA, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }




    // Nuevo método: Formatea la lista de productos para mostrarla
    public String formatProductList(List<Producto> products) {
        StringBuilder productList = new StringBuilder(STRINGBUILDER);
        for (Producto p : products) {
            productList.append(p.getName()).append(T)
                    .append(p.getQuantity()).append(T)
                    .append(p.getPrice()).append(N);
        }
        return productList.toString();
    }

}
