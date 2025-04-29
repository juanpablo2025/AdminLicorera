package org.example.manager.adminDBManager;

import org.example.model.Factura;
import org.example.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.manager.userDBManager.DatabaseUserManager.URL;

public class DatabaseAdminManager {
    static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Factura> geFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT id, productos, total, fecha_hora, forma_pago FROM compras";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                facturas.add(new Factura(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("productos"),
                        rs.getDouble("total"),
                        rs.getString("fecha_hora"),
                        rs.getString("forma_pago")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener facturas: " + e.getMessage());
        }
        return facturas;
    }

    public static void updateProduct(Producto p) {
        String updateSQL = "UPDATE productos SET nombre = ?, cantidad = ?, precio = ?, foto = ? WHERE id = ?";
        String insertSQL = "INSERT INTO productos (id, nombre, cantidad, precio, foto) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM productos WHERE id = ?")) {
            check.setInt(1, p.getId());
            ResultSet rs = check.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;

            if (exists) {
                try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                    stmt.setString(1, p.getName());
                    stmt.setInt(2, p.getQuantity());
                    stmt.setDouble(3, p.getPrice());
                    stmt.setString(4, p.getFoto());
                    stmt.setInt(5, p.getId());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                    stmt.setInt(1, p.getId());
                    stmt.setString(2, p.getName());
                    stmt.setInt(3, p.getQuantity());
                    stmt.setDouble(4, p.getPrice());
                    stmt.setString(5, p.getFoto());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar/agregar producto: " + e.getMessage());
        }
    }

    public static void deleteProductById(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
        }
    }

    public static void addProduct(Producto p) {
        String sql = "INSERT INTO productos(id, nombre, cantidad, precio, foto) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getId());
            stmt.setString(2, p.getName());
            stmt.setInt(3, p.getQuantity());
            stmt.setDouble(4, p.getPrice());
            stmt.setString(5, p.getFoto());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
        }
    }

    public static List<Producto> getProducts() {
        List<Producto> products = new ArrayList<>();
        String sql = "SELECT id, nombre, cantidad, precio, foto FROM productos";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio"),
                        rs.getString("foto")
                );
                products.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return products;
    }

    public static Producto getProductByName(String name) {
        String sql = "SELECT id, nombre, cantidad, precio, foto FROM productos WHERE nombre = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio"),
                        rs.getString("foto")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    public List<Factura> getFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT id, productos, total, fecha_hora, forma_pago FROM compras";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                facturas.add(new Factura(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("productos"),
                        rs.getDouble("total"),
                        rs.getString("fecha_hora"),
                        rs.getString("forma_pago")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener facturas: " + e.getMessage());
        }
        return facturas;
    }

    public static void eliminarFactura(String idFactura) {
        String sql = "DELETE FROM compras WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(idFactura));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar factura: " + e.getMessage());
        }
    }

    public static boolean eliminarFacturaYActualizarProductos(String idFactura) {
        String sqlSelect = "SELECT productos FROM compras WHERE id = ?";
        String sqlDelete = "DELETE FROM compras WHERE id = ?";

        try (PreparedStatement select = conn.prepareStatement(sqlSelect)) {
            select.setInt(1, Integer.parseInt(idFactura));
            ResultSet rs = select.executeQuery();
            if (!rs.next()) return false;

            String productos = rs.getString("productos");
            String[] parts = productos.split(" ");
            String nombreProducto = null;
            int cantidad = 0;

            for (String part : parts) {
                if (part.startsWith("x")) {
                    cantidad = Integer.parseInt(part.substring(1).trim());

                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE productos SET cantidad = cantidad + ? WHERE nombre = ?")) {
                        update.setInt(1, cantidad);
                        update.setString(2, nombreProducto);
                        update.executeUpdate();
                    }

                    nombreProducto = null;
                    cantidad = 0;
                } else if (!part.startsWith("$") && !part.equals("=")) {
                    nombreProducto = part.trim();
                }
            }

            try (PreparedStatement delete = conn.prepareStatement(sqlDelete)) {
                delete.setInt(1, Integer.parseInt(idFactura));
                delete.executeUpdate();
            }

            return true;

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error al eliminar factura y actualizar productos: " + e.getMessage());
            return false;
        }
    }
}
