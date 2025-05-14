package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Mesa {

    private String id; // Contador estático para el ID auto-incremental
    private boolean ocupada;
    private List<Producto> productos; // Lista de productos añadidos a la mesa

    public Mesa(String id) {
        this.id = id;
        this.ocupada = false;
        this.productos = new ArrayList<>();
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public String getId() {
        return id;
    }

    public void setID(String i) {
        this.id = i;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

}
