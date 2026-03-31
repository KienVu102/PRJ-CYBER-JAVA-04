package dao;

import model.PC;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PCDAO {

    private Connection getConn() { return DBConnection.getConnection(); }

    private PC mapRow(ResultSet rs) throws SQLException {
        PC pc = new PC();
        pc.setId(rs.getInt("id"));
        pc.setPcNumber(rs.getString("pc_number"));
        pc.setCategoryId(rs.getInt("category_id"));
        pc.setConfiguration(rs.getString("configuration"));
        pc.setPricePerHour(rs.getDouble("price_per_hour"));
        pc.setStatus(PC.Status.valueOf(rs.getString("status")));
        try { pc.setCategoryName(rs.getString("category_name")); } catch (SQLException ignored) {}
        return pc;
    }

    public List<PC> findAll() {
        List<PC> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM pcs p " +
                     "JOIN categories c ON p.category_id = c.id ORDER BY p.id";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll PCs: " + e.getMessage());
        }
        return list;
    }

    public List<PC> findAvailableByCategory(int categoryId) {
        List<PC> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM pcs p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.status='AVAILABLE' AND p.category_id=? ORDER BY p.pc_number";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAvailableByCategory: " + e.getMessage());
        }
        return list;
    }

    public List<PC> findAvailable() {
        List<PC> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM pcs p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.status='AVAILABLE' ORDER BY c.name, p.pc_number";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAvailable: " + e.getMessage());
        }
        return list;
    }

    public PC findById(int id) {
        String sql = "SELECT p.*, c.name AS category_name FROM pcs p " +
                     "JOIN categories c ON p.category_id = c.id WHERE p.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] findById PC: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(PC pc) {
        String sql = "INSERT INTO pcs (pc_number, category_id, configuration, price_per_hour, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pc.getPcNumber());
            ps.setInt(2, pc.getCategoryId());
            ps.setString(3, pc.getConfiguration());
            ps.setDouble(4, pc.getPricePerHour());
            ps.setString(5, pc.getStatus().name());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) pc.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert PC: " + e.getMessage());
        }
        return false;
    }

    public boolean update(PC pc) {
        String sql = "UPDATE pcs SET pc_number=?, category_id=?, configuration=?, price_per_hour=?, status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pc.getPcNumber());
            ps.setInt(2, pc.getCategoryId());
            ps.setString(3, pc.getConfiguration());
            ps.setDouble(4, pc.getPricePerHour());
            ps.setString(5, pc.getStatus().name());
            ps.setInt(6, pc.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] update PC: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int pcId, PC.Status status) {
        String sql = "UPDATE pcs SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, pcId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateStatus PC: " + e.getMessage());
        }
        return false;
    }

    // Tìm máy có sẵn và không được đặt trong khoảng thời gian cụ thể
    public List<PC> findAvailableForTimeSlot(int categoryId, LocalDateTime startTime, LocalDateTime endTime) {
        List<PC> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM pcs p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.status='AVAILABLE' " +
                     (categoryId > 0 ? "AND p.category_id=? " : "") +
                     "AND p.id NOT IN (" +
                     "  SELECT DISTINCT pc_id FROM bookings " +
                     "  WHERE status NOT IN ('CANCELLED','COMPLETED') " +
                     "  AND NOT (end_time <= ? OR start_time >= ?)" +
                     ") ORDER BY c.name, p.pc_number";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            int paramIndex = 1;
            if (categoryId > 0) {
                ps.setInt(paramIndex++, categoryId);
            }
            ps.setTimestamp(paramIndex++, Timestamp.valueOf(startTime));
            ps.setTimestamp(paramIndex++, Timestamp.valueOf(endTime));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAvailableForTimeSlot: " + e.getMessage());
        }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM pcs WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] delete PC: " + e.getMessage());
        }
        return false;
    }
}
