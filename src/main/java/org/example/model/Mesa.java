package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Mesa {

    private static int nextId = 1; // Contador estático para el ID auto-incremental
    private boolean ocupada;
    private List<Producto> productos; // Lista de productos añadidos a la mesa
    private double totalCuenta;

    public Mesa(int i) {
        this.nextId = nextId++;
        this.ocupada = false;
        this.productos = new ArrayList<>();
        this.totalCuenta = 0.0;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void añadirProducto(Producto producto) {
        this.productos.add(producto);
        this.totalCuenta += producto.getPrice(); // Sumar al total de la cuenta
    }

    public double getTotalCuenta() {
        return totalCuenta;
    }

    public void finalizarMesa() {
        this.ocupada = false;
        this.productos.clear();
        this.totalCuenta = 0.0;
    }

    public int getId() {
        return nextId;
    }
}
