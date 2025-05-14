package org.example.manager.adminmanager;

import org.example.model.Factura;
import java.util.List;

public class FacturasAdminManager {

    private final ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public List<Factura> getFacturas() {
        return excelAdminManager.getFacturas();
    }

    public boolean eliminarFacturaYActualizarProductos(String facturaID) {return excelAdminManager.eliminarFacturaYActualizarProductos(facturaID);}
}
