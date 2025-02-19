package org.example.manager.userManager;

import java.io.File;

public class MainUserManager {

    public static void crearDirectorios() {
        String documentosPath = System.getProperty("user.home") + "\\Calculadora del Administrador";
        String facturacionPath = documentosPath + "\\Facturación";
        String facturasPath = documentosPath + "\\Facturas";
        String realizadoPath = documentosPath + "\\Realizo";

        crearDirectorioSiNoExiste(facturasPath);
        crearDirectorioSiNoExiste(realizadoPath);
        crearDirectorioSiNoExiste(facturacionPath);
    }

    private static void crearDirectorioSiNoExiste(String path) {
        File directorio = new File(path);
        if (!directorio.exists()) {
            if (directorio.mkdirs()) {
                System.out.println("Directorio creado: " + path);
            } else {
                System.out.println("No se pudo crear el directorio: " + path);
            }
        } else {
            System.out.println("El directorio ya existe: " + path);
        }
    }
}
