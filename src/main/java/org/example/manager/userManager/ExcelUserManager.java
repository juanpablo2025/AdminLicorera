package org.example.manager.userManager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Factura;
import org.example.model.Mesa;
import org.example.model.Producto;


import javax.swing.*;
import java.io.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;


import static org.example.manager.userManager.FacturacionUserManager.*;

import static org.example.utils.Constants.*;

public class ExcelUserManager {

    public static final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
    public static final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);
    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion"+ fechaFormateada+".xlsx";

    public ExcelUserManager() {
        // Verificar si la carpeta existe, si no, crearla
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs(); // Crear la carpeta y las subcarpetas necesarias
        }

        // Verificar si el archivo existe
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();
        }
    }

    // Método para crear el archivo Excel si no existe
    public static void createExcelFile() {
        Workbook workbook = new XSSFWorkbook();

        // Crear hoja de productos
        Sheet productsSheet = workbook.createSheet(PRODUCTS_SHEET_NAME);
        Row header = productsSheet.createRow(0); // Utiliza constantes para los índices si las tienes
        header.createCell(0).setCellValue(ID);
        header.createCell(1).setCellValue(NOMBRE);
        header.createCell(2).setCellValue(CANTIDAD);
        header.createCell(3).setCellValue(PRECIO);
        header.createCell(4).setCellValue("Cantidad Vendida");
        header.createCell(5 ).setCellValue("Foto");

        // Crear hoja de compras
        Sheet purchasesSheet = workbook.createSheet(PURCHASES_SHEET_NAME);
        Row purchasesHeader = purchasesSheet.createRow(0);
        purchasesHeader.createCell(0).setCellValue(ID);
        purchasesHeader.createCell(1).setCellValue(PRODUCTOS);
        purchasesHeader.createCell(2).setCellValue(TOTAL);
        purchasesHeader.createCell(3).setCellValue(FECHA_HORA);
        purchasesHeader.createCell(4).setCellValue("Forma de Pago");

        // Crear hoja de gastos
        Sheet gastosSheet = workbook.createSheet("Gastos");
        Row gastosHeader = gastosSheet.createRow(0);
        gastosHeader.createCell(0).setCellValue("ID Producto");
        gastosHeader.createCell(1).setCellValue("Nombre Producto");
        gastosHeader.createCell(2).setCellValue("Cantidad");
        gastosHeader.createCell(3).setCellValue("Precio Compra");
        gastosHeader.createCell(4).setCellValue("Fecha y Hora");

        // crear hoja de mesas

        // Verificar si la pestaña "Mesas" ya existe
        Sheet mesasSheet = workbook.getSheet("Mesas");
        if (mesasSheet == null) {
            mesasSheet = workbook.createSheet("Mesas");
            Row headerRow = mesasSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mesa ID");
            headerRow.createCell(1).setCellValue("Estado");
            headerRow.createCell(2).setCellValue("Productos");
            headerRow.createCell(3).setCellValue("Total");

            // Crear 10 mesas por defecto
            for (int i = 1; i <= 10; i++) {
                Row row = mesasSheet.createRow(i);
                row.createCell(0).setCellValue("Mesa " + i);
                row.createCell(1).setCellValue("Libre");  // Estado inicial
            }
        }

        Sheet empleadosSheet = workbook.getSheet("Empleados");
        if (empleadosSheet == null) {
            empleadosSheet = workbook.createSheet("Empleados");
            Row empleadosHeader = empleadosSheet.createRow(0);
            empleadosHeader.createCell(0).setCellValue("Nombre Empleado");
            empleadosHeader.createCell(1).setCellValue("Hora Inicio");
            empleadosHeader.createCell(2).setCellValue("Fecha Inicio");
        }


        // Guarda el archivo en la ruta especificada
        try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
            workbook.write(fileOut);
           // System.out.println("Archivo Excel creado: " + FILE_PATH.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close(); // Cierra el workbook para liberar recursos
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para leer los productos del archivo Excel
    public List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int id = (int) row.getCell(ZERO).getNumericCellValue();
                    String name = getCellValueAsString(row.getCell(ONE));
                    int quantity = getCellValueAsInt(row.getCell(TWO));
                    double price = getCellValueAsDouble(row.getCell(THREE));
                    String foto = getCellValueAsString(row.getCell(5));

                    products.add(new Producto(id,name, quantity, price, foto));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Métodos auxiliares para manejar celdas nulas y tipos de datos
    private String getCellValueAsString(Cell cell) {
        return (cell != null) ? cell.toString() : "Desconocido";
    }

    private int getCellValueAsInt(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? (int) cell.getNumericCellValue() : 0;
    }

    private double getCellValueAsDouble(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? cell.getNumericCellValue() : 0.0;
    }

    // Método para obtener un producto por nombre
    public Producto getProductByName(String selectedProduct) {
        List<Producto> products = getProducts();
        for (Producto p : products) {
            if (p.getName().equals(selectedProduct)) {
                return p;
            }
        }
        return null;
    }

    // Método para guardar una compra en el archivo Excel
    public void savePurchase(String compraID, String productos, double total, LocalDateTime now, String tipoCompra) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaFormateada = now.format(formatter);
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.createSheet(PURCHASES_SHEET_NAME);
                Row header = sheet.createRow(ZERO);
                header.createCell(ZERO).setCellValue(ID);
                header.createCell(ONE).setCellValue(PRODUCTOS);
                header.createCell(TWO).setCellValue(TOTAL);
                header.createCell(THREE).setCellValue(FECHA_HORA);
                header.createCell(4).setCellValue("Forma de Pago");

            }
            int lastRow = sheet.getLastRowNum() + ONE;
            Row row = sheet.createRow(lastRow);

            row.createCell(ZERO).setCellValue(compraID);
            row.createCell(ONE).setCellValue(productos);  // Los productos se listan en líneas nuevas dentro de la misma celda
            row.createCell(TWO).setCellValue(total);
            row.createCell(THREE).setCellValue(fechaFormateada);
            row.createCell(FOUR).setCellValue(tipoCompra);

            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH.toString())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para restar los totales de la hoja "Gastos"
    public static double restarTotalesGastos(Sheet gastosSheet) {
        double totalGastos = ZERO_DOUBLE; // Usar la constante como en el ejemplo

        // Iterar a través de las filas de la hoja de gastos
        for (int i = ONE; i <= gastosSheet.getLastRowNum(); i++) {
            Row row = gastosSheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(3); // La columna D es el índice 3
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    totalGastos += cell.getNumericCellValue();
                }
            }
        }
        return totalGastos;
    }

    // Método para sumar los totales de la hoja "Compras"
    static double sumarTotalesCompras(Sheet purchasesSheet) {
        double totalSum = ZERO_DOUBLE;
        for (int i = ONE; i <= purchasesSheet.getLastRowNum(); i++) {
            Row row = purchasesSheet.getRow(i);
            if (row != null && row.getCell(TWO) != null) {
                totalSum += row.getCell(TWO).getNumericCellValue();
            }
        }
        return totalSum;
    }

    // Método para limpiar la hoja "Compras"
    private void limpiarHojaCompras(Sheet purchasesSheet) {
        for (int i = purchasesSheet.getLastRowNum(); i >= ONE; i--) {
            Row row = purchasesSheet.getRow(i);
            if (row != null) {
                purchasesSheet.removeRow(row);
            }
        }
    }

    public void facturarYLimpiar() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            Sheet gastosSheet = workbook.getSheet("Gastos"); // Asegúrate de que el nombre coincida
            Sheet empleadosSheet = workbook.getSheet("Empleados");
            Sheet reabastecimientoSheet = workbook.getSheet("Reabastecimiento");

            if (purchasesSheet != null) {
                // Mapa para almacenar las cantidades vendidas por producto
                Map<String, Integer> productosVendidos = new HashMap<>();

                // Sumar las cantidades vendidas por producto
                for (int i = 1; i <= purchasesSheet.getLastRowNum(); i++) { // Suponiendo que la fila 0 es el encabezado
                    Row row = purchasesSheet.getRow(i);
                    if (row != null) {
                        Cell nombreProductoCell = row.getCell(0); // Columna 0: Nombre del producto
                        Cell cantidadCell = row.getCell(1); // Columna 1: Cantidad vendida

                        if (nombreProductoCell != null && cantidadCell != null) {
                            String nombreProducto = nombreProductoCell.getStringCellValue().trim();

                            int cantidadVendida = 0;
                            if (cantidadCell.getCellType() == CellType.NUMERIC) {
                                cantidadVendida = (int) cantidadCell.getNumericCellValue();
                            } else if (cantidadCell.getCellType() == CellType.STRING) {
                                try {
                                    cantidadVendida = Integer.parseInt(cantidadCell.getStringCellValue());
                                } catch (NumberFormatException e) {
                                    System.err.println("Error al parsear cantidad de la fila: " + i);
                                }
                            }

                            if (cantidadVendida > 0) {
                                productosVendidos.put(nombreProducto, productosVendidos.getOrDefault(nombreProducto, 0) + cantidadVendida);
                            }
                        }
                    }
                }

               /* // Mostrar productos vendidos en consola para verificar
                for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                  //  System.out.println("Producto: " + entry.getKey() + ", Cantidad Vendida: " + entry.getValue());
                }*/

                // 3️⃣ Calcular el total por método de pago
                Map<String, Double> totalPorFormaPago = new HashMap<>();
                for (int i = 1; i <= purchasesSheet.getLastRowNum(); i++) {
                    Row row = purchasesSheet.getRow(i);
                    if (row != null) {
                        Cell pagoCell = row.getCell(4); // Forma de pago (columna 4)
                        Cell totalCell = row.getCell(2); // Total de la compra (columna 2)

                        if (pagoCell != null && totalCell != null && totalCell.getCellType() == CellType.NUMERIC) {
                            String formaPago = pagoCell.getStringCellValue().trim();
                            double total = totalCell.getNumericCellValue();

                            totalPorFormaPago.put(formaPago, totalPorFormaPago.getOrDefault(formaPago, 0.0) + total);
                        }
                    }
                }

                // Copiar la hoja "Compras" y renombrarla, pasando el total de la compra
                // 1️⃣ Calcular totales
                double totalCompra = sumarTotalesCompras(purchasesSheet);
                double totalGastos = restarTotalesGastos(gastosSheet);
                double totalReabastecimiento = restarTotalesGastos(reabastecimientoSheet);
                double totalFinal = totalCompra;
                String nombreEmpleado = obtenerUltimoEmpleado();

                crearArchivoFacturacionYGastos(purchasesSheet, gastosSheet, empleadosSheet, totalCompra, totalGastos, reabastecimientoSheet, totalReabastecimiento);
                generarResumenDiarioEstilizadoPDF();

                // 2️⃣ Guardar archivo con datos antes de limpiar
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                    guardarTotalFacturadoEnArchivo(totalPorFormaPago, totalFinal);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al guardar archivo con datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 3️⃣ Ahora limpiar hojas
                limpiarHojaCompras(purchasesSheet);
                limpiarHojaCompras(gastosSheet);
                limpiarHojaCompras(empleadosSheet);
                limpiarHojaCompras(reabastecimientoSheet);

                // 4️⃣ Guardar archivo limpio
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al guardar archivo limpio.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                limpiarFacturas();
                String nombreCapitalizado = nombreEmpleado.substring(0, 1).toUpperCase() + nombreEmpleado.substring(1).toLowerCase();

                JOptionPane.showMessageDialog(null, "Muchas gracias por tu ayuda "+nombreCapitalizado+ ".", "Día finalizado correctamente.", JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para crear el archivo Excel independiente con las hojas "Facturacion" y "Gastos"
    public void crearArchivoFacturacionYGastos(Sheet purchasesSheet, Sheet gastosSheet, Sheet empleadosSheet, double totalCompra, double totalGastos, Sheet reabastecimientoSheet,double totalRebastecimiento) throws IOException {
        // Crear un nuevo Workbook (archivo Excel)
        Workbook workbook = new XSSFWorkbook();
        LocalDateTime fechaHoraFacturacion = LocalDateTime.now();
        DateTimeFormatter formatterFac = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
        String formatterFacturacion = fechaHoraFacturacion.format(formatterFac);
        // Crear la hoja "Facturacion"
        String facturacionHojaNombre = "Ventas_" + formatterFacturacion;
        Sheet facturacionSheet = workbook.createSheet(facturacionHojaNombre);

        // Copiar el contenido de la hoja "Compras" a la hoja "Facturacion"
        copiarContenidoHoja(purchasesSheet, facturacionSheet);

        // Crear un estilo de celda para resaltar en rojo
        CellStyle redStyle = crearEstiloRojo(workbook);

        // Agregar una fila extra con el total al final de la hoja "Facturacion"
        agregarTotal(facturacionSheet, totalCompra, "Total Compra", redStyle);
        LocalDateTime fechaHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
        String fechaFormateada = fechaHora.format(formatter);

        // Crear la hoja "Gastos"
        String gastosHojaNombre = "Gastos_" + fechaFormateada;
        Sheet gastosSheetNueva = workbook.createSheet(gastosHojaNombre);

        // Copiar el contenido de la hoja "Gastos" a la nueva hoja "Gastos"
        copiarContenidoHoja(gastosSheet, gastosSheetNueva);

        // Agregar una fila extra con el total al final de la hoja "Gastos"
        agregarTotal(gastosSheetNueva, totalGastos, "Total Gastos", redStyle);

        // Crear la hoja "Gastos"
        String reabastecimientoHojaNombre = "Reabastecimientos_" + fechaFormateada;
        Sheet reabastecimientoSheetNueva = workbook.createSheet(reabastecimientoHojaNombre);

        // Copiar el contenido de la hoja "Gastos" a la nueva hoja "Gastos"
        copiarContenidoHoja(reabastecimientoSheet, reabastecimientoSheetNueva);

        // Agregar una fila extra con el total al final de la hoja "Gastos"
        agregarTotal(reabastecimientoSheetNueva, totalRebastecimiento, "Total Reabastecimiento", redStyle);

        // Crear la hoja "Empleados" y añadir la hora de cierre
        String empleadosHojaNombre = "Empleados_" + fechaFormateada;
        Sheet empleadosSheetNueva = workbook.createSheet(empleadosHojaNombre);

        // Copiar el contenido de la hoja "Empleados" y añadir la columna "Hora Cerrada"
        copiarContenidoHojaConHoraCerrada(empleadosSheet, empleadosSheetNueva);

        // Guardar el archivo Excel en el directorio especificado
        guardarArchivo(workbook);
    }

    // Método para copiar el contenido de una hoja y agregar la columna "Hora Cerrada"
    private void copiarContenidoHojaConHoraCerrada(Sheet sourceSheet, Sheet targetSheet) {
        int rowCount = sourceSheet.getPhysicalNumberOfRows();
        LocalDateTime horaCerrada = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss /dd-MM-yyyy");

        for (int i = 0; i < rowCount; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = targetSheet.createRow(i);

            int cellCount = sourceRow.getPhysicalNumberOfCells();
            for (int j = 0; j < cellCount; j++) {
                Cell sourceCell = sourceRow.getCell(j);
                Cell targetCell = targetRow.createCell(j);
                copiarCelda(sourceCell, targetCell);
            }

            // Añadir la columna "Hora Cerrada" al final de cada fila
            Cell horaCerradaCell = targetRow.createCell(cellCount);
            if (i == 0) {
                // Si es la primera fila (cabecera), escribir "Hora Cerrada"
                horaCerradaCell.setCellValue("Hora de Cierre");
            } else {
                // En las demás filas, escribir la hora actual
                horaCerradaCell.setCellValue(horaCerrada.format(timeFormatter));
            }
        }
    }

    // Método para copiar contenido de una celda a otra
    private void copiarCelda(Cell sourceCell, Cell targetCell) {
        if (sourceCell == null) return;

        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
        }
    }

    // Método auxiliar para copiar el contenido de una hoja a otra
    private void copiarContenidoHoja(Sheet oldSheet, Sheet newSheet) {
        for (int i = 0; i <= oldSheet.getLastRowNum(); i++) {
            Row oldRow = oldSheet.getRow(i);
            Row newRow = newSheet.createRow(i);
            if (oldRow != null) {
                for (int j = 0; j < oldRow.getLastCellNum(); j++) {
                    Cell oldCell = oldRow.getCell(j);
                    Cell newCell = newRow.createCell(j);

                    if (oldCell != null) {
                        // Copiar el tipo de celda
                        switch (oldCell.getCellType()) {
                            case STRING:
                                newCell.setCellValue(oldCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                newCell.setCellValue(oldCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                newCell.setCellValue(oldCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                newCell.setCellFormula(oldCell.getCellFormula());
                                break;
                            case BLANK:
                                newCell.setBlank();
                                break;
                            // Otros tipos de celda según sea necesario
                            default:
                                break;
                        }

                        // Aplicar el estilo a la nueva celda
                        newCell.setCellStyle(crearEstiloParaCelda(oldCell, newSheet.getWorkbook()));
                    }
                }
            }
        }
    }

    // Método para crear un nuevo estilo de celda basado en el estilo de otra celda
    private CellStyle crearEstiloParaCelda(Cell oldCell, Workbook newWorkbook) {
        CellStyle newStyle = newWorkbook.createCellStyle();

        // Copia las propiedades que consideres necesarias
        newStyle.cloneStyleFrom(oldCell.getCellStyle());

        return newStyle;
    }

    // Método auxiliar para crear un estilo de celda en rojo
    private CellStyle crearEstiloRojo(Workbook workbook) {
        CellStyle redStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());  // Establecer el color de la fuente en rojo
        font.setBold(true); // Poner en negrita
        redStyle.setFont(font);
        return redStyle;
    }

    // Método auxiliar para agregar el total al final de la hoja
    private void agregarTotal(Sheet sheet, double total, String label, CellStyle style) {
        int lastRow = sheet.getLastRowNum() + 1; // La siguiente fila vacía
        Row totalRow = sheet.createRow(lastRow);
        Cell totalLabelCell = totalRow.createCell(0); // Columna 0 para la etiqueta
        totalLabelCell.setCellValue(label);

        Cell totalValueCell = totalRow.createCell(1); // Columna 1 para el valor total
        totalValueCell.setCellValue(total);

        // Aplicar el estilo de color rojo a la celda del total
        totalValueCell.setCellStyle(style);
    }

    // Método auxiliar para guardar el archivo en el directorio
    private void guardarArchivo(Workbook workbook) throws IOException {
        // Crear el directorio si no existe
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Crear el archivo con el nombre Facturacion.xlsx
        File file = new File(directory, FACTURACION_FILENAME);
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }

        // Cerrar el Workbook para liberar recursos
        workbook.close();
    }

    // Método auxiliar para obtener productos con cantidad 0
    static List<Producto> obtenerProductosAgotados(Sheet productsSheet) {
        List<Producto> productosAgotados = new ArrayList<>();
        for (int i = ONE; i <= productsSheet.getLastRowNum(); i++) {
            Row row = productsSheet.getRow(i);
            if (row != null) {
                int id = (int) row.getCell(ZERO).getNumericCellValue();
                String name = row.getCell(ONE).getStringCellValue();
                int quantity = (int) row.getCell(TWO).getNumericCellValue();

                if (quantity <= -1) {
                    productosAgotados.add(new Producto(id,name, quantity, row.getCell(THREE).getNumericCellValue(),row.getCell(5).getStringCellValue()));
                }
            }
        }
        return productosAgotados;
    }

    // Método para cargar las mesas desde el archivo Excel
    public static ArrayList<Mesa> cargarMesasDesdeExcel() {

        final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

        ArrayList<Mesa> mesas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("Mesas"); // Acceder a la hoja llamada "mesas"
            if (mesasSheet != null) {
                for (int i = 1; i <= mesasSheet.getLastRowNum(); i++) { // Empezamos en la fila 1 (saltamos el encabezado)
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        // Leer el ID de la mesa (columna 0)
                        Cell idCell = row.getCell(0);
                        String idText = idCell.getStringCellValue();

                        // Extraer el número de la mesa, por ejemplo, de "Mesa 1" extraer 1
                        int id = extraerNumeroDeTexto(idText);

                        // Leer el estado de la mesa (columna 1)
                        String estado = row.getCell(1).getStringCellValue();
                        Mesa mesa = new Mesa("Mesa "+id);
                        mesa.setOcupada(estado.equalsIgnoreCase("Ocupada"));
                        mesas.add(mesa);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return mesas;
    }

    // Método auxiliar para extraer el número del ID de la mesa
    private static int extraerNumeroDeTexto(String texto) {
        // Remover cualquier cosa que no sea un número del texto
        String numeroTexto = texto.replaceAll("[^0-9]", "");
        return Integer.parseInt(numeroTexto);
    }

    public static void actualizarCantidadStockExcel(Map<String, Integer> productosComprados, String mesaID) {
        try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            // Verificar si existe la columna "Cantidad Vendida", si no, agregarla
            Row headerRow = sheet.getRow(0);
            int ventasColIndex = -1;

            for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
                Cell headerCell = headerRow.getCell(colIndex);
                if (headerCell != null && "Cantidad Vendida".equalsIgnoreCase(headerCell.getStringCellValue())) {
                    ventasColIndex = colIndex;
                    break;
                }
            }

            if (ventasColIndex == -1) {
                ventasColIndex = headerRow.getLastCellNum(); // Nueva columna al final
                headerRow.createCell(ventasColIndex).setCellValue("Cantidad Vendida");
            }

            // Ahora actualizar las cantidades de los productos y las ventas
            for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                String nombreProducto = entry.getKey();
                int cantidadComprada = entry.getValue();
                boolean productoEncontrado = false;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        if (row.getCell(1).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                            // Actualizar stock **permitiendo valores negativos**
                            Cell cantidadCell = row.getCell(2);
                            if (cantidadCell == null) {
                                cantidadCell = row.createCell(2);
                                cantidadCell.setCellValue(0);
                            }

                            if (cantidadCell.getCellType() == CellType.NUMERIC) {
                                int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                int nuevaCantidad = cantidadActual - cantidadComprada;

                                // **Aquí está el cambio: permite valores negativos**
                                cantidadCell.setCellValue(nuevaCantidad);
                            }

                            // Actualizar las ventas totales
                            Cell ventasCell = row.getCell(ventasColIndex);
                            if (ventasCell == null) {
                                ventasCell = row.createCell(ventasColIndex);
                                ventasCell.setCellValue(0); // Inicializar en 0 si no existe
                            }

                            if (ventasCell.getCellType() == CellType.NUMERIC) {
                                int ventasTotales = (int) ventasCell.getNumericCellValue();
                                ventasCell.setCellValue(ventasTotales + cantidadComprada);
                            }
                            productoEncontrado = true;
                            break;
                        }
                    }
                }

                if (!productoEncontrado) {
                    JOptionPane.showMessageDialog(null, "Producto '" + nombreProducto + "' no encontrado en stock.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Guardar los cambios en el archivo Excel
            try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<String[]> cargarProductosMesaDesdeExcel(String mesaID) {
        final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
        final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;

        List<String[]> productosMesa = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("Mesas"); // Asegúrate de tener una hoja "mesas" en el archivo Excel
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Recorrer las filas de la hoja, empezando en la fila 1
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(0); // Columna A: ID de la mesa

                        // Asegúrate de que la celda no sea nula y de que contenga un valor de tipo String
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String id = idCell.getStringCellValue(); // Obtener el ID como String
                          //  System.out.println("ID de mesa en fila " + (i + 1) + ": " + id); // Log del ID leído

                            // Comparar el ID de la mesa con el valor esperado
                            if (mesaID.equals(id)) { // Si el ID coincide con el de la mesa
                            //    System.out.println("Mesa encontrada: " + id); // Log si se encuentra la mesa

                                // Leer los productos de la mesa (suponiendo que los productos están en la columna C)
                                Cell productosCell = row.getCell(2);
                                if (productosCell != null && productosCell.getCellType() == CellType.STRING) {
                                    String productosTexto = productosCell.getStringCellValue(); // Obtener los productos como String
                                   // System.out.println("Productos encontrados: " + productosTexto); // Log de los productos encontrados

                                    // Suponiendo que cada producto está separado por un salto de línea
                                    String[] productos = productosTexto.split("\n");
                                    for (String producto : productos) {
                                        // Suponiendo que los productos tienen un formato "nombreProducto xCantidad $PrecioUnitario"
                                        String[] detallesProducto = producto.trim().split(" ");
                                        if (detallesProducto.length >= 3) { // Verifica que hay suficientes elementos
                                            productosMesa.add(detallesProducto); // Añadir el producto a la lista
                                        }
                                    }
                                } else {
                                  //  System.out.println("Celda de productos está vacía o no es de tipo String.");
                                }
                                break; // Una vez encontrados los productos de la mesa, no necesitamos seguir buscando
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return productosMesa;
    }


    public static void agregarMesaAExcel(Mesa nuevaMesa) {
        final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + "\\" + FILE_NAME;
        String filePath = FILE_PATH; // Reemplaza con la ruta correcta
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("Mesas");
            if (mesasSheet == null) {
                // Si no existe la hoja "mesas", crearla
                mesasSheet = workbook.createSheet("Mesas");
                // Crear encabezado si es una hoja nueva
                Row headerRow = mesasSheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Estado");
            }

            // Agregar nueva fila con los datos de la nueva mesa
            int newRowNum = mesasSheet.getLastRowNum() + 1; // La última fila más uno
            Row newRow = mesasSheet.createRow(newRowNum);
            newRow.createCell(0).setCellValue("Mesa " + nuevaMesa.getId()); // ID de la mesa
            newRow.createCell(1).setCellValue(nuevaMesa.isOcupada() ? "Ocupada" : "Libre"); // Estado de la mesa

            // Escribir los cambios en el archivo
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Método para eliminar mesas con ID mayor a 10
    public static void eliminarMesasConIdMayorA15() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet("Mesas"); // Nombre de la hoja donde se encuentran las mesas

            if (mesasSheet != null) {
                // Iterar a través de las filas de la hoja de mesas
                for (int i = mesasSheet.getLastRowNum(); i >= 1; i--) { // Comenzar desde la última fila
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(0); // Suponiendo que el ID de la mesa está en la columna A (índice 0)
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String mesaID = idCell.getStringCellValue(); // Obtener el ID de la mesa como String
                            if (mesaID.startsWith("Mesa ")) {
                                // Extraer el número después de "Mesa "
                                int idNumero = Integer.parseInt(mesaID.split(" ")[1]);
                                if (idNumero > 15) {
                                    mesasSheet.removeRow(row); // Eliminar la fila de la mesa
                                    // Mover el resto de las filas hacia arriba
                                    for (int j = i + 1; j <= mesasSheet.getLastRowNum(); j++) {
                                        Row nextRow = mesasSheet.getRow(j);
                                        if (nextRow != null) {
                                            mesasSheet.shiftRows(j, j + 1, -1); // Desplazar filas hacia arriba
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Guardar los cambios en el archivo Excel
                try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toString())) {
                    workbook.write(fos);
                }

                // JOptionPane.showMessageDialog(null, "Mesas con ID mayor a 10 han sido eliminadas.");

            } else {
                JOptionPane.showMessageDialog(null, "La hoja de mesas no se encontró.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener todas las facturas desde la hoja de "compras"
    public List<Factura> getFacturas() {
        List<Factura> facturas = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet ventasSheet = workbook.getSheet("Ventas");
            if (ventasSheet != null) {
                for (int i = 1; i <= ventasSheet.getLastRowNum(); i++) {
                    Row row = ventasSheet.getRow(i);
                    if (row != null) {
                        String id = row.getCell(0).getStringCellValue();
                        String productos = row.getCell(1).getStringCellValue();
                        double total = row.getCell(2).getNumericCellValue();
                        String fechaHora = row.getCell(3).getStringCellValue();
                        String formaPago = row.getCell(4).getStringCellValue();

                        Factura factura = new Factura(id, productos, total, fechaHora, formaPago);
                        facturas.add(factura);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return facturas;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public static LocalDate getFechaTurnoActivo() {
        LocalTime ahora = LocalTime.now();
        LocalDate hoy = LocalDate.now();
        if (ahora.isAfter(LocalTime.MIDNIGHT) && ahora.isBefore(LocalTime.of(6, 0))) {
            return hoy.minusDays(1); // entre 00:00 y 06:00, aún es el turno de ayer
        }

        return hoy;
    }
    public static boolean hayRegistroDeHoy() {
        LocalDate fechaTurno = getFechaTurnoActivo();

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toString());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("Empleados");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(2);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String fechaRegistro = cell.getStringCellValue();
                            LocalDate fecha = LocalDate.parse(fechaRegistro, DATE_FORMATTER);
                            if (fecha.isEqual(fechaTurno) ||
                                    (LocalTime.now().isBefore(LocalTime.of(6, 0)) && fecha.isEqual(fechaTurno.plusDays(1)))) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void registrarDia(String nombreUsuario) {
        LocalDateTime now = LocalDateTime.now();
        String horaInicio = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String fechaInicio = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Obtener o crear la hoja de empleados
            Sheet empleadosSheet = workbook.getSheet("Empleados");
            if (empleadosSheet == null) {
                empleadosSheet = workbook.createSheet("Empleados");
                // Crear fila de encabezado
                Row empleadosHeader = empleadosSheet.createRow(0);
                empleadosHeader.createCell(0).setCellValue("Nombre Empleado");
                empleadosHeader.createCell(1).setCellValue("Hora Inicio");
                empleadosHeader.createCell(2).setCellValue("Fecha Inicio");
            }

            // Agregar la nueva entrada
            int lastRow = empleadosSheet.getLastRowNum() + 1;
            Row row = empleadosSheet.createRow(lastRow);
            row.createCell(0).setCellValue(nombreUsuario);
            row.createCell(1).setCellValue(horaInicio);
            row.createCell(2).setCellValue(fechaInicio);

            // Guardar cambios en el archivo
            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
               // enviarMensaje("+573226094632","El empleado " + nombreUsuario + " ha comenzado su jornada laboral a las " + horaInicio + " del día " + fechaInicio);
            } /*catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String obtenerUltimoEmpleado() {
        String EMPLEADOS_SHEET_NAME = "Empleados";
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet empleadosSheet = workbook.getSheet(EMPLEADOS_SHEET_NAME);
            if (empleadosSheet == null) return "No hay empleados registrados";

            for (int i = empleadosSheet.getLastRowNum(); i >= 0; i--) {
                Row row = empleadosSheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0); // Columna A
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String nombre = cell.getStringCellValue().trim();
                        if (!nombre.isEmpty()) {
                            return nombre;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "No se encontró un nombre de empleado";
    }
}