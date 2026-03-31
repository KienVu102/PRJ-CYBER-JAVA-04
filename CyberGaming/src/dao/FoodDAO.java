package dao;

import model.Food;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    private Connection getConn() { return DBConnection.getConnection(); }

    private Food mapRow(ResultSet rs) throws SQLException {
        return new Food(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getInt("stock"),
            rs.getBoolean("available")
        );
    }

    public List<Food> findAll() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT * FROM foods ORDER BY id";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll foods: " + e.getMessage());
        }
        return list;
    }

    public List<Food> findAvailable() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT * FROM foods WHERE available=true AND stock>0 ORDER BY id";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAvailable foods: " + e.getMessage());
        }
        return list;
    }

    public Food findById(int id) {
        String sql = "SELECT * FROM foods WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] findById food: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Food f) {
        String sql = "INSERT INTO foods (name, description, price, stock, available) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getName());
            ps.setString(2, f.getDescription());
            ps.setDouble(3, f.getPrice());
            ps.setInt(4, f.getStock());
            ps.setBoolean(5, f.isAvailable());
            if (ps.executeUpdate() > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) f.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] insert food: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Food f) {
        String sql = "UPDATE foods SET name=?, description=?, price=?, stock=?, available=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, f.getName());
            ps.setString(2, f.getDescription());
            ps.setDouble(3, f.getPrice());
            ps.setInt(4, f.getStock());
            ps.setBoolean(5, f.isAvailable());
            ps.setInt(6, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] update food: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStock(int foodId, int newStock) {
        String sql = "UPDATE foods SET stock=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, foodId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateStock food: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM foods WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] delete food: " + e.getMessage());
        }
        return false;
    }
}
