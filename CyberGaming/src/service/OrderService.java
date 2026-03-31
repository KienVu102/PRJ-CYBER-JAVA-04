package service;

import dao.FoodDAO;
import dao.OrderDAO;
import model.Food;
import model.Order;
import util.ValidationUtil;

import java.util.List;
import java.util.Map;

// Service xử lý nghiệp vụ đơn hàng F&B - Buổi 5: Kiểm thử & Làm sạch Code
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final WalletService walletService = new WalletService();

    // Tạo đơn hàng mới từ danh sách món (foodId → quantity)
    // Validation toàn diện cho tất cả input
    // @return thông báo lỗi hoặc null nếu thành công
    public String createOrder(int userId, Integer bookingId, Map<Integer, Integer> cartItems) {
        // Validate cart is not empty
        if (cartItems == null || cartItems.isEmpty()) {
            return "Giỏ hàng trống.";
        }

        double total = 0;

        // Validate each item before creating order
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int foodId = entry.getKey();
            int qty = entry.getValue();

            // Validate food ID
            if (!ValidationUtil.isValidId(foodId)) {
                return "ID món ăn không hợp lệ.";
            }

            // Validate quantity
            String qtyError = ValidationUtil.validateOrderItem(foodId, qty);
            if (qtyError != null) {
                return qtyError;
            }

            // Check food exists
            Food f = foodDAO.findById(foodId);
            if (f == null) {
                return "Món ăn ID=" + foodId + " không tồn tại.";
            }

            // Check food is available
            if (!f.isAvailable()) {
                return "Món '" + f.getName() + "' hiện không có sẵn.";
            }

            // Check stock is sufficient
            if (f.getStock() < qty) {
                return String.format("Món '%s' không đủ số lượng (còn %d).", f.getName(), f.getStock());
            }

            // Add to total
            total += f.getPrice() * qty;
        }

        // Validate total amount
        if (!ValidationUtil.isValidAmount(total)) {
            return "Tổng tiền không hợp lệ.";
        }

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setBookingId(bookingId);
        order.setStatus(Order.Status.PENDING);
        order.setTotalAmount(total);

        if (!orderDAO.insert(order)) {
            return "Tạo đơn hàng thất bại.";
        }

        // Save order items and update stock
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int foodId = entry.getKey();
            int qty = entry.getValue();
            Food f = foodDAO.findById(foodId);

            Order.OrderItem item = new Order.OrderItem(foodId, f.getName(), qty, f.getPrice());
            item.setOrderId(order.getId());
            orderDAO.insertItem(item);

            // Reduce stock
            foodDAO.updateStock(foodId, f.getStock() - qty);
        }

        return null; // Success
    }

    public List<Order> getOrdersByUser(int userId) {
        return orderDAO.findByUser(userId);
    }

    public List<Order> getPendingOrders() {
        return orderDAO.findPending();
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public Order getOrderById(int id) {
        return orderDAO.findById(id);
    }

    // Nhân viên xác nhận đơn hàng: PENDING → CONFIRMED
    public String confirmOrder(int orderId) {
        if (!ValidationUtil.isValidId(orderId)) {
            return "ID đơn hàng không hợp lệ.";
        }

        Order o = orderDAO.findById(orderId);
        if (o == null) {
            return "Không tìm thấy đơn hàng.";
        }
        if (o.getStatus() != Order.Status.PENDING) {
            return "Đơn hàng không ở trạng thái 'Chờ xác nhận'.";
        }

        return orderDAO.updateStatus(orderId, Order.Status.CONFIRMED) ? null : "Xác nhận thất bại.";
    }

    // Nhân viên bắt đầu chuẩn bị: CONFIRMED → IN_SERVICE
    public String startPreparing(int orderId) {
        if (!ValidationUtil.isValidId(orderId)) {
            return "ID đơn hàng không hợp lệ.";
        }

        Order o = orderDAO.findById(orderId);
        if (o == null) {
            return "Không tìm thấy đơn hàng.";
        }
        if (o.getStatus() != Order.Status.CONFIRMED) {
            return "Đơn hàng chưa được xác nhận.";
        }

        return orderDAO.updateStatus(orderId, Order.Status.IN_SERVICE) ? null : "Cập nhật thất bại.";
    }

    // Hoàn thành đơn hàng: IN_SERVICE → COMPLETED + trừ tiền ví
    public String completeOrder(int orderId) {
        if (!ValidationUtil.isValidId(orderId)) {
            return "ID đơn hàng không hợp lệ.";
        }

        Order o = orderDAO.findById(orderId);
        if (o == null) {
            return "Không tìm thấy đơn hàng.";
        }
        if (o.getStatus() != Order.Status.IN_SERVICE) {
            return "Đơn hàng không ở trạng thái 'Đang phục vụ'.";
        }

        // Deduct from wallet
        String payErr = walletService.pay(o.getUserId(),
                o.getTotalAmount(),
                "Thanh toán đơn F&B #" + orderId);
        if (payErr != null) {
            return payErr;
        }

        return orderDAO.updateStatus(orderId, Order.Status.COMPLETED) ? null : "Hoàn thành thất bại.";
    }

    // Hủy đơn hàng
    public String cancelOrder(int orderId, int requestUserId, boolean isStaffOrAdmin) {
        if (!ValidationUtil.isValidId(orderId)) {
            return "ID đơn hàng không hợp lệ.";
        }

        Order o = orderDAO.findById(orderId);
        if (o == null) {
            return "Không tìm thấy đơn hàng.";
        }
        if (!isStaffOrAdmin && o.getUserId() != requestUserId) {
            return "Bạn không có quyền hủy đơn này.";
        }
        if (o.getStatus() == Order.Status.COMPLETED || o.getStatus() == Order.Status.CANCELLED) {
            return "Đơn hàng đã kết thúc, không thể hủy.";
        }

        // Restore stock
        for (Order.OrderItem item : o.getItems()) {
            Food f = foodDAO.findById(item.getFoodId());
            if (f != null) {
                foodDAO.updateStock(item.getFoodId(), f.getStock() + item.getQuantity());
            }
        }

        return orderDAO.updateStatus(orderId, Order.Status.CANCELLED) ? null : "Hủy đơn thất bại.";
    }
}
