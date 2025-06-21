package org.example.manager.userDBManager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Factura;
import org.example.model.Mesa;
import org.example.model.Producto;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static org.example.manager.userManager.FacturacionUserManager.generarResumenDiarioEstilizadoPDF;
//import static org.example.manager.userManager.FacturacionUserManager.guardarTotalFacturadoEnArchivo;

//import static org.example.manager.userManager.ExcelUserManager.FACTURACION_FILENAME;

//import static org.example.manager.userManager.ExcelUserManager.FACTURACION_FILENAME;

public class DatabaseUserManager {
    public static final String DB_NAME = "inventario_licorera.db";
    public static final String DIRECTORY_PATH = System.getProperty("user.home") + File.separator + "Calculadora del Administrador";
    public static final String DB_PATH = DIRECTORY_PATH + File.separator + DB_NAME;

    public static final String URL = "jdbc:sqlite:" + DB_PATH;


    static LocalDateTime fechaHora = LocalDateTime.now();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss");
    static String fechaFormateada = fechaHora.format(formatter);

    public static final String FACTURACION_FILENAME = "\\Facturacion\\Facturacion"+ fechaFormateada+".xlsx";

    public DatabaseUserManager() {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        crearEstructuraInicial();
    }

    public static ArrayList<Mesa> cargarMesasDesdeDB() {
        ArrayList<Mesa> mesas = new ArrayList<>();
        String query = "SELECT mesaID, estado FROM mesas";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreMesa = rs.getString("mesaID");
                String estado = rs.getString("estado");

                Mesa mesa = new Mesa(nombreMesa);
                mesa.setOcupada("Ocupada".equalsIgnoreCase(estado));
                mesas.add(mesa);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar mesas desde la base de datos: " + e.getMessage());
        }

        return mesas;
    }


    public static ArrayList<Mesa> cargarParkingDesdeDB() {
        ArrayList<Mesa> mesas = new ArrayList<>();
        String query = "SELECT parkingID, estado FROM parking";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreParking = rs.getString("parkingID");
                String estado = rs.getString("estado");

                Mesa parking = new Mesa(nombreParking);
                parking.setOcupada("Ocupada".equalsIgnoreCase(estado));
                mesas.add(parking);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar mesas desde la base de datos: " + e.getMessage());
        }

        return mesas;
    }





    public static void actualizarCantidadStockBD(Map<String, Integer> cantidadTotalPorProducto, String mesaID) {
        String update = "UPDATE productos SET cantidad = cantidad - ? WHERE nombre = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(update)) {
            for (Map.Entry<String, Integer> entry : cantidadTotalPorProducto.entrySet()) {
                String nombreProducto = entry.getKey();
                int cantidadRestar = entry.getValue();
                stmt.setInt(1, cantidadRestar);
                stmt.setString(2, nombreProducto);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar la cantidad de stock: " + e.getMessage());
        }
    }

    public static void actualizarCantidadStockExcel(Map<String, Integer> productosComprados, String mesaID) {
        String update = "UPDATE productos SET cantidad = cantidad - ? WHERE nombre = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(update)) {
            for (Map.Entry<String, Integer> entry : productosComprados.entrySet()) {
                String nombreProducto = entry.getKey();
                int cantidadRestar = entry.getValue();
                stmt.setInt(1, cantidadRestar);
                stmt.setString(2, nombreProducto);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar la cantidad de stock: " + e.getMessage());
        }
    }

    public static List<Producto> obtenerProductosAgotados(Sheet productsSheet) {
        List<Producto> productosAgotados = new ArrayList<>();
        for (Row row : productsSheet) {
            Cell cell = row.getCell(2); // Columna de cantidad
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                int cantidad = (int) cell.getNumericCellValue();
                if (cantidad < 0) {
                    int id = (int) row.getCell(0).getNumericCellValue(); // Columna de ID
                    String nombre = row.getCell(1).getStringCellValue(); // Columna de nombre
                    double precio = row.getCell(3).getNumericCellValue(); // Columna de precio
                    String foto = row.getCell(4).getStringCellValue(); // Columna de foto
                    productosAgotados.add(new Producto(id,nombre, cantidad, precio, foto));
                }
            }
        }
        return productosAgotados;
    }


    private void crearEstructuraInicial() {
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS productos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT,
                            cantidad INTEGER,
                            precio REAL,
                            cantidad_vendida INTEGER DEFAULT 0,
                            foto TEXT
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS compras (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            productos TEXT,
                            total REAL,
                            fecha_hora TEXT,
                            forma_pago TEXT
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS gastos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombreProducto INTEGER,
                            cantidad INTEGER,
                            precioCompra REAL,
                            fechaHora TEXT
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS reabastecimiento (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            producto_nombre TEXT,
                            cantidad_reabastecida INTEGER,
                            precio_compra REAL,
                            fecha_hora TEXT
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS mesas (                 
                            mesaID TEXT PRIMARY KEY,
                            estado TEXT,
                            productos TEXT,
                            total REAL
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS parking (                 
                            parkingID TEXT PRIMARY KEY,
                            estado TEXT,
                            productos TEXT,
                            total REAL
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS empleados (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT,
                            hora_inicio TEXT,
                            fecha_inicio TEXT
                        );
                    """);

            // Insertar 15 mesas si no existen
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM mesas");
            if (rs.next() && rs.getInt("total") == 0) {
                for (int i = 1; i <= 15; i++) {
                    String nombre = "Mesa " + i;
                    stmt.executeUpdate("INSERT INTO mesas(mesaID, estado) VALUES ('" + nombre + "', 'Libre')");
                }
            }


            // Insertar 15 estacionamiento si no existen
            ResultSet pk = stmt.executeQuery("SELECT COUNT(*) as total FROM parking");
            if (pk.next() && rs.getInt("total") == 0) {
                for (int i = 1; i <= 48; i++) {
                    String nombre = "Estacionamiento " + i;
                    stmt.executeUpdate("INSERT INTO parking(parkingID, estado) VALUES ('" + nombre + "', 'Libre')");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear estructura inicial: " + e.getMessage());
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }


    public Producto getProductByName(String selectedProduct) {
        String query = "SELECT id, nombre, cantidad, precio, foto FROM productos WHERE nombre = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, selectedProduct);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("nombre");
                    int quantity = rs.getInt("cantidad");
                    double price = rs.getDouble("precio");
                    String foto = rs.getString("foto");
                    return new Producto(id, name, quantity, price, foto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por nombre: " + e.getMessage());
        }
        return null;
    }

    public static void savePurchase(String compraID, String productos, double total, LocalDateTime now, String tipoCompra) {
        String insertSQL = "INSERT INTO compras(productos, total, fecha_hora, forma_pago) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, productos);
            stmt.setDouble(2, total);
            stmt.setString(3, now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            stmt.setString(4, tipoCompra);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar compra: " + e.getMessage());
        }
    }

    public static double restarTotalesGastos(Connection conn) {
        String query = "SELECT SUM(precio_compra) as total FROM gastos";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total de gastos: " + e.getMessage());
        }
        return 0.0;
    }

    public static double sumarTotalesCompras(Connection conn) {
        String query = "SELECT SUM(total) as total FROM compras";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total de compras: " + e.getMessage());
        }
        return 0.0;
    }

    private static void limpiarHojaCompras(Connection conn) {
        String delete = "DELETE FROM compras";
        try (PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al limpiar tabla compras: " + e.getMessage());
        }
    }


    public static void facturarYLimpiar() throws SQLException {
            Map<String, Integer> productosVendidos = new HashMap<>();
            Map<String, Double> totalPorFormaPago = new HashMap<>();
            double totalCompra = 0.0, totalGastos = 0.0, totalReabastecimiento = 0.0;
            String nombreEmpleado = "";

            try (Connection conn = connect()) {

                // 1️⃣ Obtener productos vendidos y totales
                String queryCompras = "SELECT productos, total, forma_pago FROM compras";
                try (PreparedStatement stmt = conn.prepareStatement(queryCompras);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        String producto = rs.getString("productos");
                        double total = rs.getDouble("total");
                        String formaPago = rs.getString("forma_pago");

                        productosVendidos.put(producto, productosVendidos.getOrDefault(producto, 0) /*+ cantidad*/);
                        totalCompra += total;
                        totalPorFormaPago.put(formaPago, totalPorFormaPago.getOrDefault(formaPago, 0.0) + total);
                    }
                }

                /* 2️⃣ Obtener gastos
                String queryGastos = "SELECT monto FROM gastos";
                try (PreparedStatement stmt = conn.prepareStatement(queryGastos);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        totalGastos += rs.getDouble("monto");
                    }
                }*/

                /*// 3️⃣ Obtener reabastecimiento
                String queryReabastecimiento = "SELECT costo FROM reabastecimiento";
                try (PreparedStatement stmt = conn.prepareStatement(queryReabastecimiento);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        totalReabastecimiento += rs.getDouble("costo");
                    }
                }*/

                // 4️⃣ Obtener último empleado
                String queryEmpleado = "SELECT nombre FROM empleados ORDER BY id DESC LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(queryEmpleado);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        nombreEmpleado = rs.getString("nombre");
                    }
                }

                // 5️⃣ Guardar resumen diario
               // guardarResumen(productosVendidos, totalCompra, totalGastos, totalReabastecimiento, totalPorFormaPago);
                //generarResumenDiarioEstilizadoPDF();
                //guardarTotalFacturadoEnArchivo(totalPorFormaPago, totalCompra);
                // 6️⃣ Limpiar tablas
                conn.setAutoCommit(false);
                //conn.prepareStatement("DELETE FROM compras").executeUpdate();
                //conn.prepareStatement("DELETE FROM gastos").executeUpdate();
                conn.prepareStatement("DELETE FROM empleados").executeUpdate();
                //conn.prepareStatement("DELETE FROM reabastecimiento").executeUpdate();
                conn.commit();

                // 7️⃣ Mensaje final
                String nombreCapitalizado = nombreEmpleado.isEmpty() ? "Usuario" :
                        nombreEmpleado.substring(0, 1).toUpperCase() + nombreEmpleado.substring(1).toLowerCase();

                JOptionPane.showMessageDialog(null,
                        "Muchas gracias por tu ayuda " + nombreCapitalizado + ".",
                        "Día finalizado correctamente.", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al facturar y limpiar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }/* catch (IOException e) {
                throw new RuntimeException(e);
            }*/
    }
    public static void limpiarFacturas(Connection conn) {
        String rutaFacturas = System.getProperty("user.home") + "\\Calculadora del Administrador\\Facturas";
        borrarContenidoCarpeta(rutaFacturas);
    }

    private static void borrarContenidoCarpeta(String ruta) {
        File carpeta = new File(ruta);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (!archivo.delete()) {
                        System.err.println("No se pudo borrar: " + archivo.getName());
                    }
                }
            }
        }
    }
    // Método genérico para limpiar una tabla
    private static void limpiarHojaGenerica(Connection conn, String tableName) {
        String delete = "DELETE FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al limpiar tabla " + tableName + ": " + e.getMessage());
        }
    }

    private static double restarTotalesGenerico(Connection conn, String table) {
        String query = "SELECT SUM(precio_compra) as total FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total de " + table + ": " + e.getMessage());
        }
        return 0.0;
    }

    public void crearArchivoFacturacionYGastosDesdeBD(Connection conn,
                                                      double totalCompra,
                                                      double totalGastos,
                                                      double totalReabastecimiento) throws IOException, SQLException {

        Workbook workbook = new XSSFWorkbook();
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH_mm_ss"));

        CellStyle redStyle = crearEstiloRojo(workbook);

        // FACTURACIÓN
        Sheet facturacionSheet = workbook.createSheet("Ventas_" + timestamp);
        copiarTablaASheet(conn, "compras", facturacionSheet);
        agregarTotal(facturacionSheet, totalCompra, "Total Compra", redStyle);

        // GASTOS
        Sheet gastosSheet = workbook.createSheet("Gastos_" + timestamp);
        copiarTablaASheet(conn, "gastos", gastosSheet);
        agregarTotal(gastosSheet, totalGastos, "Total Gastos", redStyle);

        // REABASTECIMIENTO
        Sheet reabSheet = workbook.createSheet("Reabastecimientos_" + timestamp);
        copiarTablaASheet(conn, "reabastecimiento", reabSheet);
        agregarTotal(reabSheet, totalReabastecimiento, "Total Reabastecimiento", redStyle);

        // EMPLEADOS
        Sheet empleadosSheet = workbook.createSheet("Empleados_" + timestamp);
        copiarEmpleadosConHoraCierre(conn, empleadosSheet);

        // Guardar archivo
        guardarArchivo(workbook);
    }

    private void copiarTablaASheet(Connection conn, String tabla, Sheet sheet) throws SQLException {
        String sql = "SELECT * FROM " + tabla;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= colCount; i++) {
                headerRow.createCell(i - 1).setCellValue(meta.getColumnName(i));
            }

            int rowIndex = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 1; i <= colCount; i++) {
                    Cell cell = row.createCell(i - 1);
                    Object value = rs.getObject(i);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(String.valueOf(value));
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


    private void copiarEmpleadosConHoraCierre(Connection conn, Sheet sheet) throws SQLException {
        String sql = "SELECT nombre, hora_inicio, fecha_inicio FROM empleados";
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss /dd-MM-yyyy");

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nombre Empleado");
            header.createCell(1).setCellValue("Hora Inicio");
            header.createCell(2).setCellValue("Fecha Inicio");
            header.createCell(3).setCellValue("Hora de Cierre");

            int rowIndex = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rs.getString("nombre"));
                row.createCell(1).setCellValue(rs.getString("hora_inicio"));
                row.createCell(2).setCellValue(rs.getString("fecha_inicio"));
                row.createCell(3).setCellValue(ahora.format(timeFormatter));
            }
        }
    }

    public static void agregarMesaABD( Mesa nuevaMesa) throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        String insert = "INSERT INTO mesas(mesaID, estado) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, "Mesa " + nuevaMesa.getId());
            stmt.setString(2, nuevaMesa.isOcupada() ? "Ocupada" : "Libre");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al agregar mesa: " + e.getMessage());
        }
    }

    public static void agregarParkingABD( Mesa nuevaMesa) throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        String insert = "INSERT INTO parking(parkingID, estado) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, "Estacionamiento " + nuevaMesa.getId());
            stmt.setString(2, nuevaMesa.isOcupada() ? "Ocupada" : "Disponible");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al agregar Estacionamiento: " + e.getMessage());
        }
    }

    public static void eliminarMesasConIdMayorA15() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        String delete = "DELETE FROM mesas WHERE CAST(SUBSTR(mesaID, 6) AS INTEGER) > 15";
        try (PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar mesas con ID mayor a 15: " + e.getMessage());
        }
    }

    public static List<Factura> getFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String query = "SELECT id, productos, total, fecha_hora, forma_pago FROM compras";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String productos = rs.getString("productos");
                double total = rs.getDouble("total");
                String fechaHora = rs.getString("fecha_hora");
                String formaPago = rs.getString("forma_pago");

                Factura factura = new Factura(id, productos, total, fechaHora, formaPago);
                facturas.add(factura);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener facturas: " + e.getMessage());
        }

        return facturas;
    }


    public static LocalDate getFechaTurnoActivo() {
        LocalTime ahora = LocalTime.now();
        LocalDate hoy = LocalDate.now();
        if (ahora.isAfter(LocalTime.MIDNIGHT) && ahora.isBefore(LocalTime.of(6, 0))) {
            return hoy.minusDays(1);
        }
        return hoy;
    }

    public static boolean hayRegistroDeHoy(Connection conn) {
        LocalDate fechaTurno = getFechaTurnoActivo();
        String query = "SELECT fecha_inicio FROM empleados";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fechaStr = rs.getString("fecha_inicio");
                LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (fecha.isEqual(fechaTurno) ||
                        (LocalTime.now().isBefore(LocalTime.of(6, 0)) && fecha.isEqual(fechaTurno.plusDays(1)))) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar registro de hoy: " + e.getMessage());
        }

        return false;
    }

    public static void registrarDia(Connection conn, String nombreUsuario) {
        LocalDateTime now = LocalDateTime.now();
        String horaInicio = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String fechaInicio = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String insert = "INSERT INTO empleados(nombre, hora_inicio, fecha_inicio) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, horaInicio);
            stmt.setString(3, fechaInicio);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar día: " + e.getMessage());
        }
    }

    public static String obtenerUltimoEmpleado() throws SQLException {
        Connection conn = DriverManager.getConnection(DatabaseUserManager.URL);


        String query = "SELECT nombre FROM empleados ORDER BY id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último empleado: " + e.getMessage());
        }
        return "No se encontró un nombre de empleado";
    }

    public List<Producto> getProducts() {
        List<Producto> productos = new ArrayList<>();
        String query = "SELECT id, nombre, cantidad, precio, foto FROM productos";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("nombre");
                int quantity = rs.getInt("cantidad");
                double price = rs.getDouble("precio");
                String foto = rs.getString("foto");
                productos.add(new Producto(id, name, quantity, price, foto));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    public static List<String[]> cargarProductosMesaDesdeBD(String mesaID) {
        List<String[]> productosMesa = new ArrayList<>();

        String query = "SELECT productos FROM mesas WHERE mesaID = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, mesaID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String productosTexto = rs.getString("productos");

                if (productosTexto != null && !productosTexto.isEmpty()) {
                    String[] productos = productosTexto.split("\n");

                    for (String producto : productos) {
                        String[] partes = producto.trim().split(" ");

                        if (partes.length >= 3) {
                            productosMesa.add(partes); // formato: {nombre, xCantidad, $Precio}
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productosMesa;
    }
}
