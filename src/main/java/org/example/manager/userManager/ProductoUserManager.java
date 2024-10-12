package org.example.manager.userManager;

import org.example.model.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.utils.Constants.MINUS_ONE;
import static org.example.utils.Constants.ZERO;

public class ProductoUserManager {
    private ExcelUserManager excelUserManager = new ExcelUserManager();
    private static List<Producto> cartProducts = new ArrayList<>();  // Lista para almacenar los productos en el carrito
    private static List<Integer> cartQuantities = new ArrayList<>();
    public List<Producto> getProducts() {
        return excelUserManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelUserManager.getProductByName(name);
    }
    public static Map<String, Integer> getProductListWithQuantities() {
        Map<String, Integer> productsAndQuantities = new HashMap<>();
        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            productsAndQuantities.put(producto.getName(), cantidad);
        }
        return productsAndQuantities;
    }



    // Método para añadir un producto al carrito
    public void addProductToCart(Producto producto, int cantidad) {
        // Buscar si el producto ya está en el carrito
        int index = cartProducts.indexOf(producto);
        if (index != MINUS_ONE) {
            // Si el producto ya está en el carrito, actualizar la cantidad
            cartQuantities.set(index, cartQuantities.get(index) + cantidad);
        } else {
            // Si el producto no está en el carrito, añadirlo
            cartProducts.add(producto);
            cartQuantities.add(cantidad);
        }
    }

    // Método para obtener el monto total de la compra
    public double getTotalCartAmount() {
        double total = ZERO;
        for (int i = ZERO; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            total += producto.getPrice() * cantidad;
        }

        return total;
    }
    public static void removeProductFromCart(int row) {
    }


}
