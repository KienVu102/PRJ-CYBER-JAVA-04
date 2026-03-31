package model;

public class User {

    public enum Role { ADMIN, STAFF, CUSTOMER }

    private int id;
    private String fullName;
    private String username;
    private String password;
    private String phone;
    private Role role;
    private double balance;

    public User() {}

    public User(int id, String fullName, String username, String password,
                String phone, Role role, double balance) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.balance = balance;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s, balance=%.0f}",
                id, username, role, balance);
    }
}
