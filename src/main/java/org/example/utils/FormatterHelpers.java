package org.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

import static org.example.ui.uiadmin.MainAdminUi.obtenerTRM;

public class FormatterHelpers {

    public static String formatearMoneda(double valor) {

        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

        return formatCOP.format(valor);
    }

    public class ConfiguracionGlobal {

        public static final double TRM = obtenerTRM();

        private ConfiguracionGlobal() {}

    }
}
