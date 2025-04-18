package org.example.manager.adminManager;

import org.example.model.Producto;
import java.util.List;


public class ProductoAdminManager {

    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public void addProduct(Producto product) {
        excelAdminManager.addProduct(product);
    }

    public List<Producto> getProducts() {
        return excelAdminManager.getProducts();
    }
    public Producto getProductByName(String name) {
        return excelAdminManager.getProductByName(name);
    }

}
