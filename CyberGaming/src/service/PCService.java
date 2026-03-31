package service;

import dao.CategoryDAO;
import dao.PCDAO;
import model.Category;
import model.PC;

import java.time.LocalDateTime;
import java.util.List;

// Service xử lý logic nghiệp vụ cho máy trạm và khu vực.
public class PCService {

    private final PCDAO pcDAO = new PCDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ============ CATEGORY ============

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Category getCategoryById(int id) {
        return categoryDAO.findById(id);
    }

    public String addCategory(String name, String description) {
        if (name == null || name.isBlank()) return "Tên khu vực không được để trống.";
        Category c = new Category(0, name.trim(), description != null ? description.trim() : "");
        return categoryDAO.insert(c) ? null : "Thêm khu vực thất bại.";
    }

    public String updateCategory(int id, String name, String description) {
        if (name == null || name.isBlank()) return "Tên khu vực không được để trống.";
        Category c = new Category(id, name.trim(), description != null ? description.trim() : "");
        return categoryDAO.update(c) ? null : "Cập nhật khu vực thất bại.";
    }

    public String deleteCategory(int id) {
        return categoryDAO.delete(id) ? null : "Xóa khu vực thất bại (có thể đang có máy liên kết).";
    }

    // ============ PC ============

    public List<PC> getAllPCs() {
        return pcDAO.findAll();
    }

    public List<PC> getAvailablePCs() {
        return pcDAO.findAvailable();
    }

    public List<PC> getAvailablePCsByCategory(int categoryId) {
        return pcDAO.findAvailableByCategory(categoryId);
    }

    // Lấy danh sách máy có sẵn và không được đặt trong khoảng thời gian cụ thể
    public List<PC> getAvailablePCsForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        return pcDAO.findAvailableForTimeSlot(0, startTime, endTime);
    }

    // Lấy danh sách máy có sẵn và không được đặt trong khoảng thời gian cụ thể theo khu vực
    public List<PC> getAvailablePCsForTimeSlot(int categoryId, LocalDateTime startTime, LocalDateTime endTime) {
        return pcDAO.findAvailableForTimeSlot(categoryId, startTime, endTime);
    }

    public PC getPCById(int id) {
        return pcDAO.findById(id);
    }

    public String addPC(String pcNumber, int categoryId, String config, double pricePerHour) {
        if (pcNumber == null || pcNumber.isBlank()) return "Số máy không được để trống.";
        if (categoryDAO.findById(categoryId) == null) return "Khu vực không tồn tại.";
        if (pricePerHour < 0) return "Giá tiền không được âm.";

        PC pc = new PC(0, pcNumber.trim(), categoryId, config, pricePerHour, PC.Status.AVAILABLE);
        return pcDAO.insert(pc) ? null : "Thêm máy thất bại (số máy đã tồn tại?).";
    }

    public String updatePC(int id, String pcNumber, int categoryId, String config,
                           double pricePerHour, PC.Status status) {
        if (pcNumber == null || pcNumber.isBlank()) return "Số máy không được để trống.";
        if (categoryDAO.findById(categoryId) == null) return "Khu vực không tồn tại.";
        if (pricePerHour < 0) return "Giá tiền không được âm.";

        PC pc = new PC(id, pcNumber.trim(), categoryId, config, pricePerHour, status);
        return pcDAO.update(pc) ? null : "Cập nhật máy thất bại.";
    }

    public String updateStatus(int pcId, PC.Status status) {
        return pcDAO.updateStatus(pcId, status) ? null : "Cập nhật trạng thái thất bại.";
    }

    public String deletePC(int id) {
        return pcDAO.delete(id) ? null : "Xóa máy thất bại (có thể đang có đơn đặt liên kết).";
    }
}
