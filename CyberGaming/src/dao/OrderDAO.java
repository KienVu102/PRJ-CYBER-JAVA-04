package dao;

import model.Order;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private Connection getConn() { return DBConnection.getConnection(); }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setStatus(Order.Status.valueOf(rs.getString("status")));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try {
            int bId = rs.getInt("booking_id");
            if (!rs.wasNull()) o.setBookingId(bId);
        } catch (SQLException ignored) {}
        try { o.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        return o;
    }

    private Order.OrderItem mapItem(ResultSet rs) throws SQLException {
        Order.OrderItem item = new Order.OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setFoodId(rs.getInt("food_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        try { item.setFoodName(rs.getString("food_name")); } catch (SQLException ignored) {}
        return item;
    }

    public boolean insert(Order o) {
        String sql = "INSERT INTO orders (user_id, booking_id, status, total_amount) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getUserId());
            if (o.getBookingId() != null) ps.setInt(2, o.getBookingId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, o.getStatus().name());
            ps.setDouble(4, o.getTotalAmount());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) o.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert order: " + e.getMessage());
        }
        return false;
    }

    public boolean insertItem(Order.OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, food_id, quantity, unit_price) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getFoodId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) item.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insertItem: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int orderId, Order.Status status) {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateStatus order: " + e.getMessage());
        }
        return false;
    }

    public Order findById(int id) {
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id WHERE o.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order o = mapRow(rs);
                o.setItems(findItemsByOrder(id));
                return o;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] findById order: " + e.getMessage());
        }
        return null;
    }

    public List<Order> findByUser(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id " +
                     "WHERE o.user_id=? ORDER BY o.created_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = mapRow(rs);
                o.setItems(findItemsByOrder(o.getId()));
                list.add(o);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] findByUser order: " + e.getMessage());
        }
        return list;
    }

    public List<Order> findPending() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id " +
                     "WHERE o.status IN ('PENDING','CONFIRMED','IN_SERVICE') ORDER BY o.created_at";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = mapRow(rs);
                o.setItems(findItemsByOrder(o.getId()));
                list.add(o);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] findPending orders: " + e.getMessage());
        }
        return list;
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id ORDER BY o.created_at DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = mapRow(rs);
                o.setItems(findItemsByOrder(o.getId()));
                list.add(o);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] findAll orders: " + e.getMessage());
        }
        return list;
    }

    public List<Order.OrderItem> findItemsByOrder(int orderId) {
        List<Order.OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, f.name AS food_name FROM order_items oi " +
                     "JOIN foods f ON oi.food_id = f.id WHERE oi.order_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapItem(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findItemsByOrder: " + e.getMessage());
        }
        return items;
    }

    // Xóa tất cả đơn hàng của một người dùng cụ thể (bao gồm cả mục đơn hàng)
    public boolean deleteByUserId(int userId) {
        // First delete order items, then orders
        String deleteItemsSql = "DELETE oi FROM order_items oi " +
                               "JOIN orders o ON oi.order_id = o.id WHERE o.user_id=?";
        String deleteOrdersSql = "DELETE FROM orders WHERE user_id=?";
        
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps1 = conn.prepareStatement(deleteItemsSql);
                 PreparedStatement ps2 = conn.prepareStatement(deleteOrdersSql)) {
                
                ps1.setInt(1, userId);
                ps1.executeUpdate();
                
                ps2.setInt(1, userId);
                ps2.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[DAO] deleteByUserId orders: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] deleteByUserId orders connection: " + e.getMessage());
            return false;
        }
    }
}
