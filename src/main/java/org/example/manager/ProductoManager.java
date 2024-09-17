package org.example.manager;

import org.example.model.Producto;

import java.util.List;

public class ProductoManager {
    private ExcelManager excelManager = new ExcelManager();

    public void addProduct(Producto product) {
        excelManager.addProduct(product);
    }

    public List<Producto> getProducts() {
        return excelManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelManager.getProductByName(name);
    }
}
