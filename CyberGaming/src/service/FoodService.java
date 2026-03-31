package service;

import dao.FoodDAO;
import model.Food;

import java.util.List;

// Service xử lý logic nghiệp vụ cho menu F&B.
public class FoodService {

    private final FoodDAO foodDAO = new FoodDAO();

    public List<Food> getAllFoods() {
        return foodDAO.findAll();
    }

    public List<Food> getAvailableFoods() {
        return foodDAO.findAvailable();
    }

    public Food getFoodById(int id) {
        return foodDAO.findById(id);
    }

    public String addFood(String name, String description, double price, int stock) {
        if (name == null || name.isBlank()) return "Tên món không được để trống.";
        if (price < 0) return "Giá không được âm.";
        if (stock < 0) return "Số lượng tồn kho không được âm.";

        Food f = new Food(0, name.trim(), description != null ? description.trim() : "", price, stock, true);
        return foodDAO.insert(f) ? null : "Thêm món thất bại.";
    }

    public String updateFood(int id, String name, String description,
                             double price, int stock, boolean available) {
        if (name == null || name.isBlank()) return "Tên món không được để trống.";
        if (price < 0) return "Giá không được âm.";
        if (stock < 0) return "Số lượng tồn kho không được âm.";

        Food f = new Food(id, name.trim(), description != null ? description.trim() : "",
                price, stock, available);
        return foodDAO.update(f) ? null : "Cập nhật món thất bại.";
    }

    public String deleteFood(int id) {
        return foodDAO.delete(id) ? null : "Xóa món thất bại.";
    }

    public String updateStock(int foodId, int newStock) {
        if (newStock < 0) return "Số lượng tồn kho không được âm.";
        return foodDAO.updateStock(foodId, newStock) ? null : "Cập nhật tồn kho thất bại.";
    }

    public boolean decreaseStock(int foodId, int quantity) {
        Food f = foodDAO.findById(foodId);
        if (f == null) return false;
        int newStock = f.getStock() - quantity;
        if (newStock < 0) newStock = 0;
        return foodDAO.updateStock(foodId, newStock);
    }
}
