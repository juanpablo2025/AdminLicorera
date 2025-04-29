package org.example.manager.adminManager;

import org.example.manager.adminDBManager.DatabaseAdminManager;
import org.example.model.Factura;
import java.util.List;

public class FacturasAdminManager {

    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    // Método para obtener todas las facturas desde el archivo Excel
    public List<Factura> getFacturas() {
       // return excelAdminManager.getFacturas();
        return DatabaseAdminManager.geFacturas();
    }

    // Método para eliminar una factura por su ID
    public void eliminarFactura(String facturaID) {
        //excelAdminManager.eliminarFactura(facturaID);
        DatabaseAdminManager.eliminarFactura(facturaID);
    }

    public boolean eliminarFacturaYActualizarProductos(String facturaID) {
        //return excelAdminManager.eliminarFacturaYActualizarProductos(facturaID);
        return DatabaseAdminManager.eliminarFacturaYActualizarProductos(facturaID);
    }
}
