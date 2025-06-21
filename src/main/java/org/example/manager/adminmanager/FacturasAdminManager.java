package org.example.manager.adminmanager;

import org.example.manager.adminDBManager.DatabaseAdminManager;
import org.example.model.Factura;
import java.util.List;

public class FacturasAdminManager {

    //private final ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    private final DatabaseAdminManager databaseAdminManager = new DatabaseAdminManager();

    public List<Factura> getFacturas() {
        //return excelAdminManager.getFacturas();
        return databaseAdminManager.geFacturas();
    }

    public boolean eliminarFacturaYActualizarProductos(String facturaID) {//return excelAdminManager.eliminarFacturaYActualizarProductos(facturaID);
        return databaseAdminManager.eliminarFacturaYActualizarProductos(facturaID);
        }
}
