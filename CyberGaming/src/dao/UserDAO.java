package dao;

import model.User;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection getConn() {
        return DBConnection.getConnection();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setPhone(rs.getString("phone"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setBalance(rs.getDouble("balance"));
        return u;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] findByUsername: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] findById: " + e.getMessage());
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, username";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll users: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(User u) {
        String sql = "INSERT INTO users (full_name, username, password, phone, role, balance) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getRole().name());
            ps.setDouble(6, u.getBalance());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) u.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert user: " + e.getMessage());
        }
        return false;
    }

    public boolean update(User u) {
        String sql = "UPDATE users SET full_name=?, phone=?, role=?, balance=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getRole().name());
            ps.setDouble(4, u.getBalance());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] update user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET balance=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateBalance: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        // Delete in correct order to respect foreign key constraints
        // 1. Delete transactions (references users)
        // 2. Delete order_items (references orders which reference users)
        // 3. Delete orders (references users)
        // 4. Delete bookings (references users)
        // 5. Finally delete the user
        
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            
            try {
                // Delete transactions
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.deleteByUserId(id);
                
                // Delete orders and order items
                OrderDAO orderDAO = new OrderDAO();
                orderDAO.deleteByUserId(id);
                
                // Delete bookings
                BookingDAO bookingDAO = new BookingDAO();
                bookingDAO.deleteByUserId(id);
                
                // Delete the user
                String sql = "DELETE FROM users WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    int rows = ps.executeUpdate();
                    conn.commit();
                    return rows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[DAO] delete user cascade: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] delete user connection: " + e.getMessage());
            return false;
        }
    }

    public List<User> findByRole(User.Role role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findByRole: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] existsByUsername: " + e.getMessage());
        }
        return false;
    }
}
