package org.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

import static org.example.ui.uiadmin.UIMainAdmin.obtenerTRM;

public class FormatterHelpers {

    private FormatterHelpers() {}

    public static String formatearMoneda(double valor) {

        NumberFormat formatCOP = NumberFormat.getInstance(new Locale("es", "CO"));

        return formatCOP.format(valor);
    }

    public static class ConfigurationGlobal {

        public static final double TRM = obtenerTRM();

        private ConfigurationGlobal() {}

    }
}
