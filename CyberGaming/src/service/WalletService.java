package service;

import dao.TransactionDAO;
import dao.UserDAO;
import model.Transaction;
import model.User;

import java.util.List;

// Service xử lý ví điện tử nội bộ.
// Tính năng nâng cao: nạp tiền, thanh toán tự động, hoàn tiền.
public class WalletService {

    private final UserDAO userDAO = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // Nạp tiền vào tài khoản
    public String deposit(int userId, double amount, String description) {
        if (amount <= 0) return "Số tiền nạp phải lớn hơn 0.";
        User user = userDAO.findById(userId);
        if (user == null) return "Người dùng không tồn tại.";

        double newBalance = user.getBalance() + amount;
        userDAO.updateBalance(userId, newBalance);

        Transaction t = new Transaction(userId, amount, Transaction.Type.DEPOSIT,
                description != null ? description : "Nạp tiền vào tài khoản");
        transactionDAO.insert(t);
        return null;
    }

    // Trừ tiền thanh toán
    public String pay(int userId, double amount, String description) {
        if (amount <= 0) return "Số tiền thanh toán phải lớn hơn 0.";
        User user = userDAO.findById(userId);
        if (user == null) return "Người dùng không tồn tại.";
        if (user.getBalance() < amount)
            return String.format("Số dư không đủ. Cần %.0f VNĐ, hiện có %.0f VNĐ.",
                    amount, user.getBalance());

        double newBalance = user.getBalance() - amount;
        userDAO.updateBalance(userId, newBalance);

        Transaction t = new Transaction(userId, amount, Transaction.Type.PAYMENT,
                description != null ? description : "Thanh toán dịch vụ");
        transactionDAO.insert(t);
        return null;
    }

    // Hoàn tiền
    public String refund(int userId, double amount, String description) {
        if (amount <= 0) return "Số tiền hoàn phải lớn hơn 0.";
        User user = userDAO.findById(userId);
        if (user == null) return "Người dùng không tồn tại.";

        double newBalance = user.getBalance() + amount;
        userDAO.updateBalance(userId, newBalance);

        Transaction t = new Transaction(userId, amount, Transaction.Type.REFUND,
                description != null ? description : "Hoàn tiền");
        transactionDAO.insert(t);
        return null;
    }

    public double getBalance(int userId) {
        User user = userDAO.findById(userId);
        return user != null ? user.getBalance() : 0;
    }

    public List<Transaction> getHistory(int userId) {
        return transactionDAO.findByUser(userId);
    }

    public double getTotalRevenue() {
        return transactionDAO.getTotalRevenue();
    }
}
