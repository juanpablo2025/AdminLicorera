package org.example.manager;

import org.example.model.Producto;
import org.example.ui.UIHelpers;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    // Nuevo método: Formatea la lista de productos para mostrarla
    public String formatProductList(List<Producto> products) {
        StringBuilder productList = new StringBuilder("ID\tNombre\tCantidad\tPrecio\n");
        for (Producto p : products) {
            productList.append(p.getId()).append("\t")
                    .append(p.getName()).append("\t")
                    .append(p.getQuantity()).append("\t")
                    .append(p.getPrice()).append("\n");
        }
        return productList.toString();
    }
    private static void showListProductsDialog(ProductoManager productoManager) {
        // Crear el diálogo
        JDialog listProductsDialog = UIHelpers.createDialog("Listar Productos", 400, 300, new BorderLayout());

        // Crear el área de texto
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        // Obtener la lista de productos y formatearla
        List<Producto> products = productoManager.getProducts();
        String formattedProductList = productoManager.formatProductList(products);

        // Asignar la lista formateada al área de texto
        textArea.setText(formattedProductList);

        // Añadir el área de texto dentro de un JScrollPane
        listProductsDialog.add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Botón de cerrar
        JButton closeButton = UIHelpers.createButton("Cerrar", e -> listProductsDialog.dispose());
        listProductsDialog.add(closeButton, BorderLayout.SOUTH);

        // Mostrar el diálogo
        listProductsDialog.setVisible(true);
    }
}
