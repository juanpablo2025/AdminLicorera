package org.example.manager.usermanager;

import java.io.File;

public class MainUserManager {

    private MainUserManager() {}

    public static void crearDirectorios() {
        String documentosPath = System.getProperty("user.home") + File.separator + "Calculadora del Administrador";
        String facturacionPath = documentosPath + File.separator + "Facturacion";
        String facturasPath = documentosPath + File.separator + "Facturas";
        String realizadoPath = documentosPath + File.separator + "Realizo";

        crearDirectorioSiNoExiste(documentosPath); // Primero el raíz
        crearDirectorioSiNoExiste(facturacionPath);
        crearDirectorioSiNoExiste(facturasPath);
        crearDirectorioSiNoExiste(realizadoPath);
    }

    private static void crearDirectorioSiNoExiste(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean creado = dir.mkdirs(); // Crea todos los directorios necesarios
            if (!creado) {
                System.err.println("❌ No se pudo crear el directorio: " + path);
            }
        }
    }
}
