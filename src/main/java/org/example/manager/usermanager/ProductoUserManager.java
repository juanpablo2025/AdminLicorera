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

    // Obtener todos los productos
    public static List<Producto> getProducts() {
        return excelUserManager.getProducts();
    }

    // Obtener un producto por nombre
    public static Producto getProductByName(String name) {
        return excelUserManager.getProductByName(name);
    }

    // Obtener el monto total de la compra
    public double getTotalCartAmount() {
        double total = ZERO_DOUBLE;
        for (int i = ZERO; i < cartProducts.size(); i++) {
            Producto producto = cartProducts.get(i);
            int cantidad = cartQuantities.get(i);
            total += producto.getPrice() * cantidad;
        }
        return total;
    }

    // Eliminar un producto del carrito por índice
    public static void removeProductFromCart(int row) {
        if (row >= ZERO && row < cartProducts.size()) {
            cartProducts.remove(row);
            cartQuantities.remove(row);
        }
    }

    //  para limpiar el carrito después de cada venta
    public static void limpiarCarrito() {
        cartProducts.clear();
        cartQuantities.clear();
    }

    //  para actualizar la cantidad de un producto en el carrito
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


