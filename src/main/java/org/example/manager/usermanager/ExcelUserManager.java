package org.example.manager.usermanager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Factura;
import org.example.model.Mesa;
import org.example.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.example.manager.usermanager.FacturacionUserManager.*;
import static org.example.utils.Constants.*;

public class ExcelUserManager {

    public static final String FILE_NAME = "Inventario_Licorera_Cr_La_70.xlsx";
    public static final String DIRECTORY_PATH =System.getProperty(FOLDER_PATH) + "\\Calculadora del Administrador";
    public static final String FILE_PATH = DIRECTORY_PATH + '\\' + FILE_NAME;
    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);
    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion"+ fechaFormateada+".xlsx";

    private static final Logger logger =  LoggerFactory.getLogger(ExcelUserManager.class);

    public ExcelUserManager() {

        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {directory.mkdirs();}

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createExcelFile();
        }
    }


    public static void createExcelFile() {
        Workbook workbook = new XSSFWorkbook();
        Sheet productsSheet = workbook.createSheet(PRODUCTS_SHEET_NAME);
        Row header = productsSheet.createRow(ZERO);
        header.createCell(ZERO).setCellValue(ID);
        header.createCell(ONE).setCellValue(NOMBRE);
        header.createCell(TWO).setCellValue(CANTIDAD);
        header.createCell(THREE).setCellValue(PRECIO);
        header.createCell(FOUR).setCellValue(CANTIDAD_VENDIDA);
        header.createCell(FIVE ).setCellValue("Foto");

        Sheet purchasesSheet = workbook.createSheet(PURCHASES_SHEET_NAME);
        Row purchasesHeader = purchasesSheet.createRow(ZERO);
        purchasesHeader.createCell(ZERO).setCellValue(ID);
        purchasesHeader.createCell(ONE).setCellValue(PRODUCTOS);
        purchasesHeader.createCell(TWO).setCellValue(TOTAL);
        purchasesHeader.createCell(THREE).setCellValue(FECHA_HORA);
        purchasesHeader.createCell(FOUR).setCellValue("Forma de Pago");

        Sheet gastosSheet = workbook.createSheet("Gastos");
        Row gastosHeader = gastosSheet.createRow(ZERO);
        gastosHeader.createCell(ZERO).setCellValue("ID Producto");
        gastosHeader.createCell(ONE).setCellValue("Nombre Producto");
        gastosHeader.createCell(TWO).setCellValue("Cantidad");
        gastosHeader.createCell(THIRTY).setCellValue("Precio Compra");
        gastosHeader.createCell(FOUR).setCellValue("Fecha y Hora");

        Sheet mesasSheet = workbook.getSheet(MESAS_TITLE);
        if (mesasSheet == null) {
            mesasSheet = workbook.createSheet(MESAS_TITLE);
            Row headerRow = mesasSheet.createRow(ZERO);
            headerRow.createCell(ZERO).setCellValue("Mesa ID");
            headerRow.createCell(ONE).setCellValue("Estado");
            headerRow.createCell(TWO).setCellValue("Productos");
            headerRow.createCell(THREE).setCellValue("Total");

            for (int i = ONE; i <= TEN; i++) {
                Row row = mesasSheet.createRow(i);
                row.createCell(ZERO).setCellValue(MESA_TITLE + i);
                row.createCell(ONE).setCellValue("Libre");
            }
        }

        Sheet empleadosSheet = workbook.getSheet(EMPLOYEES_SHEET_NAME);
        if (empleadosSheet == null) {
            empleadosSheet = workbook.createSheet(EMPLOYEES_SHEET_NAME);
            Row empleadosHeader = empleadosSheet.createRow(ZERO);
            empleadosHeader.createCell(ZERO).setCellValue("Nombre Empleado");
            empleadosHeader.createCell(ONE).setCellValue("Hora Inicio");
            empleadosHeader.createCell(TWO).setCellValue("Fecha Inicio");
        }

        try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            logger.error("Error al crear el archivo Excel: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al crear el archivo Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error("Error al cerrar el archivo Excel: {}", e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al cerrar el archivo Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);
            for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int id = (int) row.getCell(ZERO).getNumericCellValue();
                    String name = getCellValueAsString(row.getCell(ONE));
                    int quantity = getCellValueAsInt(row.getCell(TWO));
                    double price = getCellValueAsDouble(row.getCell(THREE));
                    String foto = getCellValueAsString(row.getCell(FIVE));

                    products.add(new Producto(id,name, quantity, price, foto));
                }
            }
        } catch (IOException e) {
            logger.error("Error al leer los productos: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al leer los productos: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return products;
    }

    private String getCellValueAsString(Cell cell) {
        return (cell != null) ? cell.toString() : "Desconocido";
    }

    private int getCellValueAsInt(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? (int) cell.getNumericCellValue() : ZERO;
    }

    private double getCellValueAsDouble(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? cell.getNumericCellValue() : ZERO_DOUBLE;
    }

    public Producto getProductByName(String selectedProduct) {
        List<Producto> products = getProducts();
        for (Producto p : products) {
            if (p.getName().equals(selectedProduct)) {
                return p;
            }
        }
        return null;
    }

    public void savePurchase(String compraID, String productos, double total, String tipoCompra) {
        DateTimeFormatter formatteo = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String fechaFormateo = now.format(formatteo);
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
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
            row.createCell(ONE).setCellValue(productos);
            row.createCell(TWO).setCellValue(total);
            row.createCell(THREE).setCellValue(fechaFormateo);
            row.createCell(FOUR).setCellValue(tipoCompra);

            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            logger.error("Error al guardar la compra: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al guardar la compra: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static double restarTotalesGastos(Sheet gastosSheet) {
        double totalGastos = ZERO_DOUBLE;

        for (int i = ONE; i <= gastosSheet.getLastRowNum(); i++) {
            Row row = gastosSheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(THREE);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    totalGastos += cell.getNumericCellValue();
                }
            }
        }
        return totalGastos;
    }


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

    private void limpiarHojaCompras(Sheet purchasesSheet) {
        for (int i = purchasesSheet.getLastRowNum(); i >= ONE; i--) {
            Row row = purchasesSheet.getRow(i);
            if (row != null) {
                purchasesSheet.removeRow(row);
            }
        }
    }

    public void facturarYLimpiar() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet purchasesSheet = workbook.getSheet(PURCHASES_SHEET_NAME);
            Sheet gastosSheet = workbook.getSheet("Gastos");
            Sheet empleadosSheet = workbook.getSheet(EMPLOYEES_SHEET_NAME);
            Sheet reabastecimientoSheet = workbook.getSheet("Reabastecimiento");

            if (purchasesSheet != null) {
                Map<String, Integer> productosVendidos = new HashMap<>();

                Pattern pattern = Pattern.compile("(.+?)\\s+x(\\d+)", Pattern.CASE_INSENSITIVE);

                for (int i = ONE; i <= purchasesSheet.getLastRowNum(); i++) {
                    Row row = purchasesSheet.getRow(i);
                    if (row != null) {
                        Cell celdaProducto = row.getCell(ZERO);
                        if (celdaProducto != null && celdaProducto.getCellType() == CellType.STRING) {
                            String texto = celdaProducto.getStringCellValue().trim();

                            Matcher matcher = pattern.matcher(texto);
                            while (matcher.find()) {
                                String nombreProducto = matcher.group(ONE).trim();
                                int cantidadVendida = Integer.parseInt(matcher.group(TWO).trim());

                                productosVendidos.put(nombreProducto, productosVendidos.getOrDefault(nombreProducto, ZERO) + cantidadVendida);
                            }

                        }
                    }
                }

                Map<String, Double> totalPorFormaPago = new HashMap<>();
                for (int i = ONE; i <= purchasesSheet.getLastRowNum(); i++) {
                    Row row = purchasesSheet.getRow(i);
                    if (row != null) {
                        Cell pagoCell = row.getCell(FOUR);
                        Cell totalCell = row.getCell(TWO);

                        if (pagoCell != null && totalCell != null && totalCell.getCellType() == CellType.NUMERIC) {
                            String formaPago = pagoCell.getStringCellValue().trim();
                            double total = totalCell.getNumericCellValue();

                            totalPorFormaPago.put(formaPago, totalPorFormaPago.getOrDefault(formaPago, ZERO_DOUBLE) + total);
                        }
                    }
                }

                double totalCompra = sumarTotalesCompras(purchasesSheet);
                double totalGastos = restarTotalesGastos(gastosSheet);
                double totalReabastecimiento = restarTotalesGastos(reabastecimientoSheet);
                String nombreEmpleado = obtenerUltimoEmpleado();

                crearArchivoFacturacionYGastos(purchasesSheet, gastosSheet, empleadosSheet, totalCompra, totalGastos, reabastecimientoSheet, totalReabastecimiento);
                generarResumenDiarioEstilizadoPDF();
                limpiarCantidadVendida();

                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                    guardarTotalFacturadoEnArchivo(totalPorFormaPago, totalCompra);
                } catch (IOException e) {
                    logger.error("Error al guardar el archivo con datos: {}", e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error al guardar archivo con datos.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                limpiarHojaCompras(purchasesSheet);
                limpiarHojaCompras(gastosSheet);
                limpiarHojaCompras(empleadosSheet);
                limpiarHojaCompras(reabastecimientoSheet);

                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                } catch (IOException e) {
                    logger.error("Error al guardar el archivo limpio: {}", e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error al guardar archivo limpio.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
                limpiarCantidadVendida();
                limpiarFacturas();
                String nombreCapitalizado = nombreEmpleado.substring(ZERO, ONE).toUpperCase() + nombreEmpleado.substring(ONE).toLowerCase();

                JOptionPane.showMessageDialog(null, "Muchas gracias por tu ayuda "+nombreCapitalizado+ ".", "Día finalizado correctamente.", JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (IOException e) {
            logger.error("Error al abrir el archivo Excel: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al abrir el archivo Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void crearArchivoFacturacionYGastos(Sheet purchasesSheet, Sheet gastosSheet, Sheet empleadosSheet, double totalCompra, double totalGastos, Sheet reabastecimientoSheet,double totalRebastecimiento) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        LocalDateTime fechaHoraFacturacion = LocalDateTime.now();
        DateTimeFormatter formatterFac = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
        String formatterFacturacion = fechaHoraFacturacion.format(formatterFac);
        String facturacionHojaNombre = "Ventas_" + formatterFacturacion;
        Sheet facturacionSheet = workbook.createSheet(facturacionHojaNombre);
        copiarContenidoHoja(purchasesSheet, facturacionSheet);
        CellStyle redStyle = crearEstiloRojo(workbook);
        agregarTotal(facturacionSheet, totalCompra, "Total Compra", redStyle);

        String gastosHojaNombre = "Gastos_" + fechaFormateada;
        Sheet gastosSheetNueva = workbook.createSheet(gastosHojaNombre);

        copiarContenidoHoja(gastosSheet, gastosSheetNueva);

        agregarTotal(gastosSheetNueva, totalGastos, "Total Gastos", redStyle);

        String reabastecimientoHojaNombre = "Reabastecimientos_" + fechaFormateada;
        Sheet reabastecimientoSheetNueva = workbook.createSheet(reabastecimientoHojaNombre);

        copiarContenidoHoja(reabastecimientoSheet, reabastecimientoSheetNueva);

        agregarTotal(reabastecimientoSheetNueva, totalRebastecimiento, "Total Reabastecimiento", redStyle);

        String empleadosHojaNombre = "Empleados_" + fechaFormateada;
        Sheet empleadosSheetNueva = workbook.createSheet(empleadosHojaNombre);

        copiarContenidoHojaConHoraCerrada(empleadosSheet, empleadosSheetNueva);

        guardarArchivo(workbook);
    }

    private void copiarContenidoHojaConHoraCerrada(Sheet sourceSheet, Sheet targetSheet) {
        int rowCount = sourceSheet.getPhysicalNumberOfRows();
        LocalDateTime horaCerrada = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss /dd-MM-yyyy");

        for (int i = 0; i < rowCount; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = targetSheet.createRow(i);

            int cellCount = sourceRow.getPhysicalNumberOfCells();
            for (int j = ZERO; j < cellCount; j++) {
                Cell sourceCell = sourceRow.getCell(j);
                Cell targetCell = targetRow.createCell(j);
                copiarCelda(sourceCell, targetCell);
            }

            Cell horaCerradaCell = targetRow.createCell(cellCount);
            if (i == ZERO) {
                horaCerradaCell.setCellValue("Hora de Cierre");
            } else {
                horaCerradaCell.setCellValue(horaCerrada.format(timeFormatter));
            }
        }
    }

    private void copiarCelda(Cell sourceCell, Cell targetCell) {
        if (sourceCell == null) return;

        switch (sourceCell.getCellType()) {
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

    private void copiarContenidoHoja(Sheet oldSheet, Sheet newSheet) {
        for (int i = ZERO; i <= oldSheet.getLastRowNum(); i++) {
            Row oldRow = oldSheet.getRow(i);
            Row newRow = newSheet.createRow(i);
            if (oldRow != null) {
                for (int j = ZERO; j < oldRow.getLastCellNum(); j++) {
                    Cell oldCell = oldRow.getCell(j);
                    Cell newCell = newRow.createCell(j);

                    if (oldCell != null) {
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
                            default:
                                break;
                        }

                        newCell.setCellStyle(crearEstiloParaCelda(oldCell, newSheet.getWorkbook()));
                    }
                }
            }
        }
    }

    private CellStyle crearEstiloParaCelda(Cell oldCell, Workbook newWorkbook) {
        CellStyle newStyle = newWorkbook.createCellStyle();
        newStyle.cloneStyleFrom(oldCell.getCellStyle());

        return newStyle;
    }

    private CellStyle crearEstiloRojo(Workbook workbook) {
        CellStyle redStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        font.setBold(true);
        redStyle.setFont(font);
        return redStyle;
    }

    private void agregarTotal(Sheet sheet, double total, String label, CellStyle style) {
        int lastRow = sheet.getLastRowNum() + ONE;
        Row totalRow = sheet.createRow(lastRow);
        Cell totalLabelCell = totalRow.createCell(ZERO);
        totalLabelCell.setCellValue(label);

        Cell totalValueCell = totalRow.createCell(ONE);
        totalValueCell.setCellValue(total);

        totalValueCell.setCellStyle(style);
    }

    private void guardarArchivo(Workbook workbook) throws IOException {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, FACTURACION_FILENAME);
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    static List<Producto> obtenerProductosAgotados(Sheet productsSheet) {
        List<Producto> productosAgotados = new ArrayList<>();
        for (int i = ONE; i <= productsSheet.getLastRowNum(); i++) {
            Row row = productsSheet.getRow(i);
            if (row != null) {
                int id = (int) row.getCell(ZERO).getNumericCellValue();
                String name = row.getCell(ONE).getStringCellValue();
                int quantity = (int) row.getCell(TWO).getNumericCellValue();

                if (quantity <= -ONE) {
                    productosAgotados.add(new Producto(id,name, quantity, row.getCell(THREE).getNumericCellValue(),row.getCell(5).getStringCellValue()));
                }
            }
        }
        return productosAgotados;
    }

    public static List<Mesa> cargarMesasDesdeExcel() {
        final String DIRECTORY_PATH =System.getProperty(FOLDER_PATH) + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + '\\' + FILE_NAME;

        ArrayList<Mesa> mesas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet(MESAS_TITLE);
            if (mesasSheet != null) {
                for (int i = ONE; i <= mesasSheet.getLastRowNum(); i++) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(ZERO);
                        String idText = idCell.getStringCellValue();

                        int id = extraerNumeroDeTexto(idText);

                        String estado = row.getCell(ONE).getStringCellValue();
                        Mesa mesa = new Mesa(MESA_TITLE +id);
                        mesa.setOcupada(estado.equalsIgnoreCase("Ocupada"));
                        mesas.add(mesa);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            logger.error("Error al cargar las mesas desde Excel: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al cargar las mesas desde Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }

        return mesas;
    }

    private static int extraerNumeroDeTexto(String texto) {
        String numeroTexto = texto.replaceAll("[^0-9]", "");
        return Integer.parseInt(numeroTexto);
    }

    public static void actualizarCantidadStockExcel(Map<String, Integer> productosComprados) {
        try (FileInputStream fis = new FileInputStream(ExcelUserManager.FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(PRODUCTS_SHEET_NAME);

            Row headerRow = sheet.getRow(ZERO);
            int ventasColIndex = -ONE;

            for (int colIndex = ZERO; colIndex < headerRow.getLastCellNum(); colIndex++) {
                Cell headerCell = headerRow.getCell(colIndex);
                if (headerCell != null && "Cantidad Vendida".equalsIgnoreCase(headerCell.getStringCellValue())) {
                    ventasColIndex = colIndex;
                    break;
                }
            }

            if (ventasColIndex == -ONE) {
                ventasColIndex = headerRow.getLastCellNum();
                headerRow.createCell(ventasColIndex).setCellValue("Cantidad Vendida");
            }

            for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                String nombreProducto = entry.getKey();
                int cantidadComprada = entry.getValue();
                boolean productoEncontrado = false;

                for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        if (row.getCell(ONE).getStringCellValue().equalsIgnoreCase(nombreProducto)) {
                            Cell cantidadCell = row.getCell(TWO);
                            if (cantidadCell == null) {
                                cantidadCell = row.createCell(TWO);
                                cantidadCell.setCellValue(ZERO);
                            }

                            if (cantidadCell.getCellType() == CellType.NUMERIC) {
                                int cantidadActual = (int) cantidadCell.getNumericCellValue();
                                int nuevaCantidad = cantidadActual - cantidadComprada;

                                cantidadCell.setCellValue(nuevaCantidad);
                            }

                            Cell ventasCell = row.getCell(ventasColIndex);
                            if (ventasCell == null) {
                                ventasCell = row.createCell(ventasColIndex);
                                ventasCell.setCellValue(ZERO);
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
                    JOptionPane.showMessageDialog(null, "Producto '" + nombreProducto + "' no encontrado en stock.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(ExcelUserManager.FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException ex) {
            logger.error("Error al actualizar la cantidad de stock: {}", ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error al actualizar la cantidad de stock: " + ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<String[]> cargarProductosMesaDesdeExcel(String mesaID) {
        final String DIRECTORY_PATH = System.getProperty("user.home") + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + '\\' + FILE_NAME;

        List<String[]> productosMesa = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(MESAS_TITLE);
            if (sheet != null) {
                for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(ZERO);

                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String id = idCell.getStringCellValue();

                            if (mesaID.equals(id)) {

                                Cell productosCell = row.getCell(TWO);
                                if (productosCell != null && productosCell.getCellType() == CellType.STRING) {
                                    String productosTexto = productosCell.getStringCellValue();

                                    String[] productos = productosTexto.split("\n");
                                    for (String producto : productos) {
                                        String[] detallesProducto = producto.trim().split(" ");
                                        if (detallesProducto.length >= THREE) {
                                            productosMesa.add(detallesProducto);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error al cargar los productos de la mesa: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al cargar los productos de la mesa: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }

        return productosMesa;
    }


    public static void agregarMesaAExcel(Mesa nuevaMesa) {
        final String DIRECTORY_PATH =System.getProperty("user.home") + "\\Calculadora del Administrador";
        final String FILE_PATH = DIRECTORY_PATH + '\\' + FILE_NAME;
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet(MESAS_TITLE);
            if (mesasSheet == null) {
                mesasSheet = workbook.createSheet(MESAS_TITLE);
                Row headerRow = mesasSheet.createRow(ZERO);
                headerRow.createCell(ZERO).setCellValue("ID");
                headerRow.createCell(ONE).setCellValue("Estado");
            }

            int newRowNum = mesasSheet.getLastRowNum() + ONE;
            Row newRow = mesasSheet.createRow(newRowNum);
            newRow.createCell(ZERO).setCellValue(MESA_TITLE + nuevaMesa.getId());
            newRow.createCell(ONE).setCellValue(nuevaMesa.isOcupada() ? "Ocupada" : "Libre");

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            logger.error("Error al agregar la mesa: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al agregar la mesa: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void eliminarMesasConIdMayorA15() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet mesasSheet = workbook.getSheet(MESAS_TITLE);

            if (mesasSheet != null) {
                boolean mesaOcupada = false;
                for (int i = ONE; i <= mesasSheet.getLastRowNum(); i++) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(ZERO);
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String mesaID = idCell.getStringCellValue();
                            if (mesaID.startsWith("Mesa ")) {
                                int idNumero = Integer.parseInt(mesaID.split(" ")[ONE]);
                                if (idNumero > FIFTEEN) {
                                    Cell estadoCell = row.getCell(ONE);
                                    if (estadoCell != null && estadoCell.getCellType() == CellType.STRING) {
                                        String estado = estadoCell.getStringCellValue();
                                        if ("ocupada".equalsIgnoreCase(estado.trim())) {
                                            mesaOcupada = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (mesaOcupada) {
                    JOptionPane.showMessageDialog(null, "No se limpiaron las mesas, ya que al menos una mesa con ID mayor a 15 está ocupada.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                for (int i = mesasSheet.getLastRowNum(); i >= ONE; i--) {
                    Row row = mesasSheet.getRow(i);
                    if (row != null) {
                        Cell idCell = row.getCell(ZERO);
                        if (idCell != null && idCell.getCellType() == CellType.STRING) {
                            String mesaID = idCell.getStringCellValue();
                            if (mesaID.startsWith("Mesa ")) {
                                int idNumero = Integer.parseInt(mesaID.split(" ")[ONE]);
                                if (idNumero > FIFTEEN) {
                                    mesasSheet.removeRow(row);
                                    for (int j = i + ONE; j <= mesasSheet.getLastRowNum(); j++) {
                                        Row nextRow = mesasSheet.getRow(j);
                                        if (nextRow != null) {
                                            mesasSheet.shiftRows(j, j + ONE, -ONE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                    workbook.write(fos);
                }
            } else {
                JOptionPane.showMessageDialog(null, "La hoja de mesas no se encontró.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            logger.error("Error al abrir el archivo Excel: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al abrir el archivo Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<Factura> getFacturas() {
        List<Factura> facturas = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet ventasSheet = workbook.getSheet("Ventas");
            if (ventasSheet != null) {
                for (int i = ONE; i <= ventasSheet.getLastRowNum(); i++) {
                    Row row = ventasSheet.getRow(i);
                    if (row != null) {
                        String id = row.getCell(ZERO).getStringCellValue();
                        String productos = row.getCell(ONE).getStringCellValue();
                        double total = row.getCell(TWO).getNumericCellValue();
                        String fechaHora = row.getCell(THREE).getStringCellValue();
                        String formaPago = row.getCell(FOUR).getStringCellValue();

                        Factura factura = new Factura(id, productos, total, fechaHora, formaPago);
                        facturas.add(factura);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error al abrir el archivo Excel: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al abrir el archivo Excel: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return facturas;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public static LocalDate getFechaTurnoActivo() {
        LocalTime ahora = LocalTime.now();
        LocalDate hoy = LocalDate.now();
        if (ahora.isAfter(LocalTime.MIDNIGHT) && ahora.isBefore(LocalTime.of(SIX, ZERO))) {
            return hoy.minusDays(ONE);
        }

        return hoy;
    }
    public static boolean hayRegistroDeHoy() {
        LocalDate fechaTurno = getFechaTurnoActivo();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(EMPLOYEES_SHEET_NAME);
            if (sheet != null) {
                for (int i = ONE; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(TWO);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String fechaRegistro = cell.getStringCellValue();
                            LocalDate fecha = LocalDate.parse(fechaRegistro, DATE_FORMATTER);
                            if (fecha.isEqual(fechaTurno) ||
                                    (LocalTime.now().isBefore(LocalTime.of(SIX, ZERO)) && fecha.isEqual(fechaTurno.plusDays(1)))) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error al abrir el archivo Excel: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
        }
        return false;
    }

    public static void registrarDia(String nombreUsuario) {
        LocalDateTime now = LocalDateTime.now();
        String horaInicio = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String fechaInicio = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet empleadosSheet = workbook.getSheet(EMPLOYEES_SHEET_NAME);
            if (empleadosSheet == null) {
                empleadosSheet = workbook.createSheet(EMPLOYEES_SHEET_NAME);
                Row empleadosHeader = empleadosSheet.createRow(ZERO);
                empleadosHeader.createCell(ZERO).setCellValue("Nombre Empleado");
                empleadosHeader.createCell(ONE).setCellValue("Hora Inicio");
                empleadosHeader.createCell(TWO).setCellValue("Fecha Inicio");
            }

            int lastRow = empleadosSheet.getLastRowNum() + ONE;
            Row row = empleadosSheet.createRow(lastRow);
            row.createCell(ZERO).setCellValue(nombreUsuario);
            row.createCell(ONE).setCellValue(horaInicio);
            row.createCell(TWO).setCellValue(fechaInicio);

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            logger.error("Error al registrar el día: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al registrar el día: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String obtenerUltimoEmpleado() {
        String employeesheet = "Empleados";
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet empleadosSheet = workbook.getSheet(employeesheet);
            if (empleadosSheet == null) return "No hay empleados registrados";

            for (int i = empleadosSheet.getLastRowNum(); i >= ZERO; i--) {
                Row row = empleadosSheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(ZERO);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String nombre = cell.getStringCellValue().trim();
                        if (!nombre.isEmpty()) {
                            return nombre;
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error al abrir el archivo Excel: {}", e.getMessage());
        }
        return "No se encontró un nombre de empleado";
    }
}