package org.example.manager.adminmanager;

import org.example.model.Producto;
import java.util.List;


public class ProductoAdminManager {

    private final ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public List<Producto> getProducts() {
        return excelAdminManager.getProducts();
    }

    public Producto getProductByName(String name) {
        return excelAdminManager.getProductByName(name);
    }

}
