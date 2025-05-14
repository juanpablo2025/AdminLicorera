package org.example.model;


public class Producto {

    private int id;
    private final String name;
    private int quantity ;
    private final double price;
    private final String foto;

    public Producto(int id,String name, int quantity, double price, String foto) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.foto = foto;
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

    public String getFoto() {
        return foto;
    }

    public void setId(int abs) {
        this.id = abs;
    }
}
