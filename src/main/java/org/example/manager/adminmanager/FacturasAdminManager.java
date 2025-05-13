package org.example.manager.adminmanager;

import org.example.model.Factura;
import java.util.List;

public class FacturasAdminManager {

    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    public List<Factura> getFacturas() {
        return excelAdminManager.getFacturas();
    }

    public void eliminarFactura(String facturaID) {
        excelAdminManager.eliminarFactura(facturaID);
    }

    public boolean eliminarFacturaYActualizarProductos(String facturaID) {return excelAdminManager.eliminarFacturaYActualizarProductos(facturaID);}
}
