package org.example.utils;

import org.example.ui.uiuser.UIUserMain;
import org.example.ui.uiuser.UIUserMesas;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Objects;

public class Constants {

    public static final String EMPRESA_NAME = "Denuedo";
    //public static final String EMPRESA_NAME = "Licorera CR";
    ////public static final String "Segoe UI Variable" = "Arial";
    public static final String LOBSTER_FONT = "Lobster-Regular.ttf";
    public static final String COMBO_BOX_TEXT = "Busca un producto";
    public static final String CONTINUE = "continuar";
    public static final String MESAS = "mesas";
    public static final String QR = "/icons/procesoQR.png";
    public static final String GUIA_QR = "Guía de Pago QR";
    public static final String FOLDER = "Calculadora del Administrador";
    public static final String FOLDER_PATH = "user.home";
    public static final String EXPENSES_SHEET_NAME = "Gastos";
    public static final String NO_FOTO = "/icons/sinfoto.png";
    //VENTA
    public static final String PRODUCTO = "Producto";
    public static final String PRECIO = "Precio";
    public static final String CANTIDAD = "Cantidad";
    public static final String PRODUCTOS = "Productos";
    public static final String TOTAL = "TOTAL";
    public static final String FECHA_HORA = "Fecha y Hora";
    public static final String NOMBRE = "Nombre";
    public static final String ID = "Id";
    public static final String CONFIRM_PURCHASE = "Confirmar Venta";
    public static final String PURCHASE_SUCCEDED = "Venta realizada con éxito";
    public static final String EMPTY = " ";
    public static final String TOTAL_PRICE = "Total: $ ";
    public static final String EFECTIVO = "Efectivo";
    public static final String FOTOS = "Fotos";
    public static final String MESA_TITLE = "Mesa ";
    public static final String CANTIDAD_VENDIDA = "Cantidad Vendida";
    // ARCHIVOS   CAMBIAR RUTAS
    public static final String PRODUCTS_SHEET_NAME = "Productos";
    public static final String PURCHASES_SHEET_NAME = "Ventas";
    public static final String EMPLOYEES_SHEET_NAME = "Empleados";
    public static final String VENTAS = "Ventas";
    public static final String BILL_FILE = "Factura N°";
    public static final String MESAS_TITLE = "Mesas";
    // FActura config
    public static final float WIDE_DOTS = 2.83465f;
    public static final float HEIGHT_DOTS = 2.83465f;
    //ENCABEZADO DE LA FACTURA
    public static final String PDF_FORMAT = ".pdf";
    public static final String NIT = "NIT: 21468330-1";
    public static final String DIRECCION = "Dirección: CR 70 # 46 - 80";
    public static final String TELEFONO = "Teléfono: 411 19 00";
    //BILL DETAILS
    public static final String BILL_ID = "Factura ";
    public static final String TOTAL_BILL = "Total ";
    public static final String PESOS = " Pesos";
    public static final String PESO_SIGN = "$ ";
    public static final String THANKS_BILL = "Gracias por su compra!";
    public static final String SLASH_ZERO = "\0";
    public static final String COMFIRM_TITLE = "Confirmar Compra";
    public static final String PRINT_BILL = "¿Imprimir la factura?";
    public static final String FACTURAS = "Facturas";
    public static final String IMPRESORA = "IMPRESORA";
    //CALCULAR DEVUELTO
    public static final String ERROR_TITLE = "Error";
    public static final String ERROR_MENU = "Palabra incorrecta. Regresando al menú principal.";
    public static final double ZERO_DOUBLE = 0.0;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;
    public static final int SEVEN = 7;

    //NUMEROS
    public static final int EIGHT = 8;
    public static final int NINE = 9;
    public static final int TEN = 10;
    public static final int ELEVEN = 11;
    public static final int TWELVE = 12;
    public static final int THIRTEEN = 13;
    public static final int FOURTEEN = 14;
    public static final int FIFTEEN = 15;
    public static final int SIXTEEN = 16;
    public static final int SEVENTEEN = 17;
    public static final int EIGHTEEN = 18;
    public static final int NINETEEN = 19;
    public static final int TWENTY = 20;
    public static final int TWENTY_ONE = 21;
    public static final int TWENTY_TWO = 22;
    public static final int TWENTY_THREE = 23;
    public static final int TWENTY_FOUR = 24;
    public static final int TWENTY_FIVE = 25;
    public static final int TWENTY_SIX = 26;
    public static final int TWENTY_SEVEN = 27;
    public static final int TWENTY_EIGHT = 28;
    public static final int TWENTY_NINE = 29;
    public static final int THIRTY = 30;
    public static final int THIRTY_ONE = 31;
    public static final int THIRTY_TWO = 32;
    public static final int THIRTY_THREE = 33;
    public static final int THIRTY_FOUR = 34;
    public static final int THIRTY_FIVE = 35;
    public static final int THIRTY_SIX = 36;
    public static final int THIRTY_SEVEN = 37;
    public static final int THIRTY_EIGHT = 38;
    public static final int THIRTY_NINE = 39;
    public static final int FORTY = 40;
    public static final int FORTY_ONE = 41;
    public static final int FORTY_TWO = 42;
    public static final int FORTY_THREE = 43;
    public static final int FORTY_FOUR = 44;
    public static final int FORTY_FIVE = 45;
    public static final int FOURTY_SIX = 46;
    public static final int FORTY_SEVEN = 47;
    public static final int FORTY_EIGHT = 48;
    public static final int FORTY_NINE = 49;
    public static final int FIFTY = 50;
    public static final int ONE_HUNDRED = 100;
    public static final int TWO_HUNDRED = 200;
    public static final int THREE_HUNDRED = 300;
    public static final int FOUR_HUNDRED = 400;
    public static final int FIVE_HUNDRED = 500;
    public static final int MINUS_ONE = -ONE;
    public static Color FONDO_PRINCIPAL;
    public static Color FONTCOLOR_BUTTON_MENU;
    public static Color BTN_MOUSE_ENTERED;
    public static Color BTN_MOUSE_EXITED;
    public static Color FONT_BUTTON_MENU;
    public static Color SEPARATOR_COLOR;
    public static Font TITTLE_FONT = new Font("Segoe UI Variable", Font.BOLD, 50);
    public static Color PRODUCT_PANEL_COLOR;
    public static Color BTN_PRODUCT_DESCRIPTION;
    public static Color BTN_PRODUCT_IMAGE;
    public static Color BTN_BACK;
    public static Color BTN_BACK_PRESSED;
    public static Color HEADER_COLOR;
    public static Color HEADER_FONT_COLOR;
    public static Color CARD_BACKGROUND;
    public static Color CARD_BACKGROUND_PRESSED;
    public static Color CARD_BACKGROUND_SELECT;
    public static Color CARD_BACKGROUND_RELEASE;
    public static Color CANTIDAD_COLOR_FONT;
    public static ImageIcon LOGO_EMPRESA;
    public static String PRODUCT_LIST_ICON;
    public static String GASTOS_ICON;
    public static String FACTURAS_ICON;
    public static String FACTURAR_ICON;
    public static String ADMIN_ICON;
    public static Color BTN_BACK_FONT_COLOR;





    static {
        switch (EMPRESA_NAME.toLowerCase()) {
            case "denuedo":
                FONDO_PRINCIPAL = new Color(240, 240, 240);
                FONTCOLOR_BUTTON_MENU = Color.BLACK;
                BTN_MOUSE_ENTERED = Color.LIGHT_GRAY;
                BTN_MOUSE_EXITED = FONDO_PRINCIPAL;
                FONT_BUTTON_MENU = FONDO_PRINCIPAL;

                CARD_BACKGROUND = (new Color(230, 220, 210));
                CARD_BACKGROUND_PRESSED = (new Color(0, 150, 212));
                CARD_BACKGROUND_SELECT = (new Color(0, 120, 212));
                CARD_BACKGROUND_RELEASE = (new Color(0, 120, 212));

                CANTIDAD_COLOR_FONT= (new Color(28, 28, 28));

                BTN_BACK = (Color.lightGray);
                BTN_BACK_PRESSED = (Color.darkGray);
                BTN_BACK_FONT_COLOR = (new Color(28, 28, 28));

                LOGO_EMPRESA = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource("/icons/denuedo-icons/Denuedo_transparent.png")));
                PRODUCT_LIST_ICON = ("/icons/denuedo-icons/lista-de_productos.png");
                GASTOS_ICON = "/icons/denuedo-icons/Gastos.png";
                //FACTURAS_ICON = "/icons/denuedo-icons/beneficios.png";
                FACTURAS_ICON = "/icons/admin/beneficios.png";
                FACTURAR_ICON = "/icons/denuedo-icons/Facturar.png";
                ADMIN_ICON = "/icons/denuedo-icons/obrero.png";




                break;
            case "licorera cr":
                FONDO_PRINCIPAL = new Color(250, 240, 230);
                FONTCOLOR_BUTTON_MENU = Color.WHITE;
                BTN_MOUSE_ENTERED = new Color(220, 40, 40);
                BTN_MOUSE_EXITED = new Color(186, 27, 26);
                FONT_BUTTON_MENU = (new Color(186, 27, 26));
                SEPARATOR_COLOR = (new Color(200, 170, 100));
                PRODUCT_PANEL_COLOR = (new Color(28, 28, 28));

                BTN_PRODUCT_DESCRIPTION = (new Color(200, 170, 100));
                BTN_PRODUCT_IMAGE = (new Color(200, 170, 100));
                BTN_BACK = (new Color(228, 185, 42));
                BTN_BACK_PRESSED = (new Color(255, 193, 7));
                BTN_BACK_FONT_COLOR = (Color.WHITE);

                HEADER_COLOR = (new Color (28, 28, 28));
                HEADER_FONT_COLOR = (new Color(201, 41, 41));

                CARD_BACKGROUND = (new Color(230, 220, 210));
                CARD_BACKGROUND_PRESSED = (new Color(220, 20, 60));
                CARD_BACKGROUND_SELECT = (new Color(186, 27, 26));
                CARD_BACKGROUND_RELEASE = (new Color(186, 27, 26));

                CANTIDAD_COLOR_FONT= (new Color(230, 220, 210));

                LOGO_EMPRESA = new ImageIcon(Objects.requireNonNull(UIUserMain.class.getResource("/icons/Licorera_CR_transparent.png")));
                PRODUCT_LIST_ICON = ("/icons/lista-de_productos.png");
                GASTOS_ICON = "/icons/Gastos.png";
                FACTURAS_ICON = "/icons/admin/beneficios.png";
                FACTURAR_ICON = "/icons/Facturar.png";
                ADMIN_ICON = "/icons/obrero.png";






                try (InputStream fontStream = UIUserMesas.class.getClassLoader().getResourceAsStream(LOBSTER_FONT)) {
                    Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 50);

                 TITTLE_FONT = (customFont);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }


                break;

        }
    }

    private Constants() {
    }


}
