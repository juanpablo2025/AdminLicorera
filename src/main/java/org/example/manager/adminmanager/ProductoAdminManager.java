package org.example.manager.adminmanager;

import org.example.manager.adminDBManager.DatabaseAdminManager;
import org.example.model.Producto;
import java.util.List;


public class ProductoAdminManager {

    //private final ExcelAdminManager excelAdminManager = new ExcelAdminManager();
    private final DatabaseAdminManager databaseAdminManager = new DatabaseAdminManager();

    public List<Producto> getProducts() {
        //return excelAdminManager.getProducts();
        return databaseAdminManager.getProducts();
    }

    public Producto getProductByName(String name) {

        //return excelAdminManager.getProductByName(name);
        return databaseAdminManager.getProductByName(name);
    }

}
