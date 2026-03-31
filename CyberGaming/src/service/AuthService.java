package service;

import dao.UserDAO;
import model.User;
import util.HashUtil;
import util.ValidationUtil;

// Service xử lý đăng nhập và đăng ký - Buổi 5: Kiểm thử & Làm sạch Code
public class AuthService {

    public final UserDAO userDAO = new UserDAO();

    // Đăng nhập với validation input
    public User login(String username, String password) {
        // Validate input
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        
        // Trim and normalize username
        String normalizedUsername = username.trim().toLowerCase();
        
        // Find user by username
        User user = userDAO.findByUsername(normalizedUsername);
        if (user == null) return null;
        
        // Verify password
        if (!HashUtil.verify(password, user.getPassword())) return null;
        
        return user;
    }

    // Đăng ký với validation toàn diện
    // @return null nếu thành công, hoặc thông báo lỗi
    public String register(String fullName, String username, String password,
                           String phone, User.Role role) {
        // Trim inputs
        fullName = fullName != null ? fullName.trim() : "";
        username = username != null ? username.trim().toLowerCase() : "";
        phone = phone != null ? phone.trim() : "";
        
        // Validation using ValidationUtil
        String validationError = ValidationUtil.validateRegistration(fullName, username, password, phone);
        if (validationError != null) {
            return validationError;
        }

        // Check if username already exists
        if (userDAO.existsByUsername(username)) {
            return "Tên đăng nhập '" + username + "' đã tồn tại.";
        }

        // Create new user
        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setPassword(HashUtil.sha256(password));
        u.setPhone(phone);
        u.setRole(role != null ? role : User.Role.CUSTOMER);
        u.setBalance(0.0);

        // Insert into database
        boolean ok = userDAO.insert(u);
        return ok ? null : "Đăng ký thất bại, vui lòng thử lại.";
    }
}
