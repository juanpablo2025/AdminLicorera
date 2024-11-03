package org.example.model;

public class Producto {
    private static int nextId = 1; // Contador estático para el ID auto-incremental

    private int id;
    private String name;
    private int quantity ;
    private double price;

    public Producto(String name, int quantity, double price) {

        this.id = nextId++; // Asigna el ID actual y lo incrementa
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }


    public void setCantidad(int cantidad) {
        this.quantity = cantidad;
    }

    // Implementamos equals para comparar productos por nombre
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return name.equalsIgnoreCase(producto.name); // Comparación basada en nombre (ignorar mayúsculas/minúsculas)
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
