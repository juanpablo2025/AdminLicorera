package org.example.manager.usermanager;

import org.example.model.Producto;

import java.util.ArrayList;
import java.util.List;

import static org.example.utils.Constants.*;


public class ProductoUserManager {

    private static final ExcelUserManager excelUserManager = new ExcelUserManager();
    private static final List<Producto> cartProducts = new ArrayList<>();
    private static final List<Integer> cartQuantities = new ArrayList<>();

    private static final int MINUS_ONE = -ONE;

    public static List<Producto> getProducts() {
        return excelUserManager.getProducts();
    }

    public static Producto getProductByName(String name) {
        return excelUserManager.getProductByName(name);
    }

    public double getTotalCartAmount() {
        double total = ZERO_DOUBLE;
        for (int i = ZERO; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            total += producto.getPrice() * cantidad;
        }
        return total;
    }

    public static void removeProductFromCart(int row) {
        if (row >= ZERO && row < cartProducts.size()) {
            cartProducts.remove(row);
            cartQuantities.remove(row);
        }
    }

    public static void limpiarCarrito() {
        cartProducts.clear();
        cartQuantities.clear();
    }

    public void updateProductQuantity(Producto producto, int nuevaCantidad) {
        int index = cartProducts.indexOf(producto);
        if (index != MINUS_ONE) {
            cartQuantities.set(index, nuevaCantidad);
        } else {
            cartProducts.add(producto);
            cartQuantities.add(nuevaCantidad);
        }
    }
}


