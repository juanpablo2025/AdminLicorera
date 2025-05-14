package org.example.model;

public class Factura {
    private final String id;
    private String productos;
    private final double total;
    private final String fechaHora;
    private final String tipoPago;

    public Factura(String id, String productos, double total, String fechaHora, String tipoPago) {
        this.id = id;
        this.productos = productos;
        this.total = total;
        this.fechaHora = fechaHora;
        this.tipoPago = tipoPago;

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

    public String getTipoPago() {
        return tipoPago;
    }

    public void setProductos(String productos) {
        this.productos = productos;
    }
}
