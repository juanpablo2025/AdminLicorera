package org.example.manager.usermanager;

import org.example.manager.userDBManager.DatabaseUserManager;
import org.example.model.Factura;

import java.util.List;

public class FacturasUserManager {

    public List<Factura> getFacturas() {//return ExcelUserManager.getFacturas();
        return DatabaseUserManager.getFacturas();
    }

}
