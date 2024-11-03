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

    private static final int MINUS_ONE = -1;
    private static final double ZERO = 0.0;

    // Obtener todos los productos
    public List<Producto> getProducts() {
        return excelUserManager.getProducts();
    }

    // Obtener un producto por nombre
    public Producto getProductByName(String name) {
        return excelUserManager.getProductByName(name);
    }

    // Obtener el mapa de productos y cantidades en el carrito
    public static Map<String, Integer> getProductListWithQuantities() {
        Map<String, Integer> productsAndQuantities = new HashMap<>();

        if (cartProducts.size() != cartQuantities.size()) {
            System.err.println("Error: Las listas cartProducts y cartQuantities tienen tamaños diferentes.");
            return productsAndQuantities; // Retorna un mapa vacío si hay un problema
        }

        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            productsAndQuantities.put(producto.getName(), cantidad);
        }
        return productsAndQuantities;
    }

    // Añadir un producto al carrito
    public void addProductToCart(Producto producto, int cantidad) {
        int index = cartProducts.indexOf(producto);
        if (index != MINUS_ONE) {
            cartQuantities.set(index, cartQuantities.get(index) + cantidad);
        } else {
            cartProducts.add(producto);
            cartQuantities.add(cantidad);
        }
    }

    // Obtener el monto total de la compra
    public double getTotalCartAmount() {
        double total = ZERO;
        for (int i = 0; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            total += producto.getPrice() * cantidad;
        }
        return total;
    }

    // Eliminar un producto del carrito por índice
    public static void removeProductFromCart(int row) {
        if (row >= 0 && row < cartProducts.size()) {
            cartProducts.remove(row);
            cartQuantities.remove(row);
        } else {
            System.err.println("Error: Índice fuera de rango al intentar eliminar producto del carrito.");
        }
    }

    // Método para limpiar el carrito después de cada venta
    public static void limpiarCarrito() {
        cartProducts.clear();
        cartQuantities.clear();
    }
}


