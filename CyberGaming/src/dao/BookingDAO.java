package dao;

import model.Booking;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private Connection getConn() { return DBConnection.getConnection(); }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setPcId(rs.getInt("pc_id"));
        b.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        b.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        b.setStatus(Booking.Status.valueOf(rs.getString("status")));
        b.setTotalAmount(rs.getDouble("total_amount"));
        try { b.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        try { b.setPcNumber(rs.getString("pc_number")); } catch (SQLException ignored) {}
        return b;
    }

    public boolean insert(Booking b) {
        String sql = "INSERT INTO bookings (user_id, pc_id, start_time, end_time, status, total_amount) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getPcId());
            ps.setTimestamp(3, Timestamp.valueOf(b.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getEndTime()));
            ps.setString(5, b.getStatus().name());
            ps.setDouble(6, b.getTotalAmount());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) b.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert booking: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int bookingId, Booking.Status status) {
        String sql = "UPDATE bookings SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateStatus booking: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTotalAmount(int bookingId, double amount) {
        String sql = "UPDATE bookings SET total_amount=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateTotalAmount booking: " + e.getMessage());
        }
        return false;
    }

    public Booking findById(int id) {
        String sql = "SELECT b.*, u.username, p.pc_number FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN pcs p ON b.pc_id = p.id WHERE b.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] findById booking: " + e.getMessage());
        }
        return null;
    }

    public List<Booking> findByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.username, p.pc_number FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN pcs p ON b.pc_id = p.id " +
                     "WHERE b.user_id=? ORDER BY b.created_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findByUser booking: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.username, p.pc_number FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN pcs p ON b.pc_id = p.id ORDER BY b.created_at DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll bookings: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> findPending() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.username, p.pc_number FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN pcs p ON b.pc_id = p.id " +
                     "WHERE b.status IN ('PENDING','CONFIRMED','IN_SERVICE') ORDER BY b.start_time";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findPending bookings: " + e.getMessage());
        }
        return list;
    }

    // Kiểm tra xung đột thời gian: máy đã có người đặt trong khoảng thời gian này chưa?
    // Logic quan trọng: không cho phép 2 khách đặt cùng 1 máy trong cùng khung giờ.
    public boolean isConflict(int pcId, LocalDateTime start, LocalDateTime end, Integer excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                     "WHERE pc_id=? AND status NOT IN ('CANCELLED','COMPLETED') " +
                     "AND NOT (end_time <= ? OR start_time >= ?) " +
                     (excludeBookingId != null ? "AND id != ?" : "");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, pcId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            if (excludeBookingId != null) ps.setInt(4, excludeBookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] isConflict: " + e.getMessage());
        }
        return false;
    }

    // Xóa tất cả đặt máy của một người dùng cụ thể
    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM bookings WHERE user_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteByUserId bookings: " + e.getMessage());
        }
        return false;
    }
}
