package org.example.manager.adminManager;

import org.example.model.Factura;

import java.util.List;

public class FacturasAdminManager {
    private ExcelAdminManager excelAdminManager = new ExcelAdminManager();

    // Método para obtener todas las facturas desde el archivo Excel
    public List<Factura> getFacturas() {
        return excelAdminManager.getFacturas();
    }

    // Método para eliminar una factura por su ID
    public void eliminarFactura(String facturaID) {
        excelAdminManager.eliminarFactura(facturaID);
    }
}
