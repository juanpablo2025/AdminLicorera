package org.example.manager.userManager;

import org.example.model.Producto;

import java.util.List;

public class ProductoManager {
    private ExcelManager excelManager = new ExcelManager();

    public List<Producto> getProducts() {
        return excelManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelManager.getProductByName(name);
    }



}
