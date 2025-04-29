package org.example.manager.userManager;

import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.model.Factura;

import java.util.List;

public class FacturasUserManager {

    private ExcelUserManager excelUserManager = new ExcelUserManager();

    // Método para obtener todas las facturas desde el archivo Excel
    public List<Factura> getFacturas() {
        return DatabaseUserManager.getFacturas();
        //excelUserManager.getFacturas();
    }

}