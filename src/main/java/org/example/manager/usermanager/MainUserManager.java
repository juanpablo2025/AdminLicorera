package org.example.manager.usermanager;

import java.io.File;

public class MainUserManager {

    private MainUserManager() {}

    public static void crearDirectorios() {
        String documentosPath = System.getProperty("user.home") + "\\Calculadora del Administrador";
        String facturacionPath = documentosPath + "\\Facturacion";
        String facturasPath = documentosPath + "\\Facturas";
        String realizadoPath = documentosPath + "\\Realizo";

        crearDirectorioSiNoExiste(facturasPath);
        crearDirectorioSiNoExiste(realizadoPath);
        crearDirectorioSiNoExiste(facturacionPath);
    }

    private static void crearDirectorioSiNoExiste(String path) {
        new File(path);
    }
}
