package dao;

import model.Transaction;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private Connection getConn() { return DBConnection.getConnection(); }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setAmount(rs.getDouble("amount"));
        t.setType(Transaction.Type.valueOf(rs.getString("type")));
        t.setDescription(rs.getString("description"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    }

    public boolean insert(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, amount, type, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getDescription());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) t.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert transaction: " + e.getMessage());
        }
        return false;
    }

    public List<Transaction> findByUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findByUser transaction: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll transactions: " + e.getMessage());
        }
        return list;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='PAYMENT'";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getTotalRevenue: " + e.getMessage());
        }
        return 0;
    }

    // Xóa tất cả giao dịch của một người dùng cụ thể
    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM transactions WHERE user_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteByUserId transactions: " + e.getMessage());
        }
        return false;
    }
}
