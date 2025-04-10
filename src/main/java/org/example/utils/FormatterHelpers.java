package org.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

import static org.example.ui.uiAdmin.MainAdminUi.obtenerTRM;

public class FormatterHelpers {

    // Método auxiliar para formatear el valor de la moneda
    public static String formatearMoneda(double valor) {
        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));
        return formatCOP.format(valor);
    }

    public class ConfiguracionGlobal {
        public static double TRM = obtenerTRM();
    }
}
