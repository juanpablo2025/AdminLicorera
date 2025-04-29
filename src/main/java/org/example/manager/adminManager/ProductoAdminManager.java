package org.example.manager.adminManager;

import org.example.manager.adminDBManager.DatabaseAdminManager;
import org.example.model.Producto;
import java.util.List;


public class ProductoAdminManager {

    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public void addProduct(Producto product) {
       // excelAdminManager.addProduct(product);
        DatabaseAdminManager.addProduct(product);

    }

    public List<Producto> getProducts() {
        return DatabaseAdminManager.getProducts();
        //return excelAdminManager.getProducts();
    }
    public Producto getProductByName(String name) {
        return DatabaseAdminManager.getProductByName(name);
        //return excelAdminManager.getProductByName(name);
    }

}
