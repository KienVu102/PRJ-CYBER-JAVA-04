package service;

import dao.BookingDAO;
import dao.PCDAO;
import dao.UserDAO;
import model.Booking;
import model.PC;
import model.User;
import util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;


public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PCDAO pcDAO = new PCDAO();
    private final UserDAO userDAO = new UserDAO();
    private final WalletService walletService = new WalletService();

    // Khách hàng đặt máy với validation toàn diện
    // @return thông báo lỗi, hoặc null nếu thành công
    public String createBooking(int userId, int pcId,
                                LocalDateTime startTime, LocalDateTime endTime) {
        
        // Validate user
        if (!ValidationUtil.isValidId(userId)) {
            return "ID người dùng không hợp lệ.";
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            return "Người dùng không tồn tại.";
        }

        // Validate PC
        if (!ValidationUtil.isValidId(pcId)) {
            return "ID máy không hợp lệ.";
        }
        PC pc = pcDAO.findById(pcId);
        if (pc == null) {
            return "Máy không tồn tại.";
        }
        if (pc.getStatus() == PC.Status.MAINTENANCE) {
            return "Máy đang bảo trì, không thể đặt.";
        }

        // Validate time range with comprehensive checks
        String timeValidationError = ValidationUtil.validateBookingCreation(pcId, startTime, endTime);
        if (timeValidationError != null) {
            return timeValidationError;
        }

        // Check for booking conflicts (same PC, overlapping times)
        if (bookingDAO.isConflict(pcId, startTime, endTime, null)) {
            return "Máy đã được đặt trong khung giờ này. Vui lòng chọn máy hoặc giờ khác.";
        }

        // Calculate total amount
        double hours = java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        double totalAmount = hours * pc.getPricePerHour();

        // Validate total amount is reasonable
        if (!ValidationUtil.isValidAmount(totalAmount)) {
            return "Chi phí không hợp lệ.";
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setPcId(pcId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.Status.PENDING);
        booking.setTotalAmount(totalAmount);

        return bookingDAO.insert(booking) ? null : "Đặt máy thất bại, vui lòng thử lại.";
    }

    public List<Booking> getBookingsByUser(int userId) {
        return bookingDAO.findByUser(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public List<Booking> getPendingBookings() {
        return bookingDAO.findPending();
    }

    public Booking getBookingById(int id) {
        return bookingDAO.findById(id);
    }

    // Nhân viên xác nhận đơn đặt máy: PENDING → CONFIRMED
    public String confirmBooking(int bookingId) {
        if (!ValidationUtil.isValidId(bookingId)) {
            return "ID đơn đặt máy không hợp lệ.";
        }
        
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) {
            return "Không tìm thấy đơn đặt máy.";
        }
        if (b.getStatus() != Booking.Status.PENDING) {
            return "Đơn đặt máy không ở trạng thái 'Chờ xác nhận'.";
        }
        
        boolean ok = bookingDAO.updateStatus(bookingId, Booking.Status.CONFIRMED);
        return ok ? null : "Xác nhận thất bại.";
    }

    // Nhân viên bắt đầu phục vụ: CONFIRMED → IN_SERVICE
    // Cập nhật trạng thái máy thành IN_USE
    public String startService(int bookingId) {
        if (!ValidationUtil.isValidId(bookingId)) {
            return "ID đơn đặt máy không hợp lệ.";
        }
        
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) {
            return "Không tìm thấy đơn đặt máy.";
        }
        if (b.getStatus() != Booking.Status.CONFIRMED) {
            return "Đơn đặt máy chưa được xác nhận.";
        }

        bookingDAO.updateStatus(bookingId, Booking.Status.IN_SERVICE);
        pcDAO.updateStatus(b.getPcId(), PC.Status.IN_USE);
        return null;
    }

    // Hoàn thành đơn đặt máy: IN_SERVICE → COMPLETED
    // Trừ tiền ví điện tử và trả máy về trạng thái AVAILABLE
    public String completeBooking(int bookingId) {
        if (!ValidationUtil.isValidId(bookingId)) {
            return "ID đơn đặt máy không hợp lệ.";
        }
        
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) {
            return "Không tìm thấy đơn đặt máy.";
        }
        if (b.getStatus() != Booking.Status.IN_SERVICE) {
            return "Đơn đặt máy không ở trạng thái 'Đang phục vụ'.";
        }

        User user = userDAO.findById(b.getUserId());
        if (user == null) {
            return "Người dùng không tồn tại.";
        }

        // Deduct from wallet
        String payErr = walletService.pay(b.getUserId(),
                b.getTotalAmount(),
                "Thanh toán đặt máy #" + bookingId);
        if (payErr != null) {
            return payErr;
        }

        bookingDAO.updateStatus(bookingId, Booking.Status.COMPLETED);
        pcDAO.updateStatus(b.getPcId(), PC.Status.AVAILABLE);
        return null;
    }

    // Hủy đơn đặt máy
    public String cancelBooking(int bookingId, int requestUserId, boolean isStaffOrAdmin) {
        if (!ValidationUtil.isValidId(bookingId)) {
            return "ID đơn đặt máy không hợp lệ.";
        }
        
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) {
            return "Không tìm thấy đơn đặt máy.";
        }
        if (!isStaffOrAdmin && b.getUserId() != requestUserId) {
            return "Bạn không có quyền hủy đơn này.";
        }
        if (b.getStatus() == Booking.Status.COMPLETED || b.getStatus() == Booking.Status.CANCELLED) {
            return "Đơn đặt máy đã kết thúc, không thể hủy.";
        }

        bookingDAO.updateStatus(bookingId, Booking.Status.CANCELLED);
        // If in service, return PC to AVAILABLE
        if (b.getStatus() == Booking.Status.IN_SERVICE) {
            pcDAO.updateStatus(b.getPcId(), PC.Status.AVAILABLE);
        }
        return null;
    }
}
