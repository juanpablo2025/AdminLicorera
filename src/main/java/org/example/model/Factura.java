package org.example.model;

public class Factura {
    private String id;
    private String productos;
    private double total;
    private String fechaHora;

    public Factura(String id, String productos, double total, String fechaHora) {
        this.id = id;
        this.productos = productos;
        this.total = total;
        this.fechaHora = fechaHora;
    }

    public String getId() {
        return id;
    }

    public String getProductos() {
        return productos;
    }

    public double getTotal() {
        return total;
    }

    public String getFechaHora() {
        return fechaHora;
    }
}
