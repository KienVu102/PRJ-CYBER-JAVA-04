-- Xóa database nếu nó đã tồn tại
DROP DATABASE IF EXISTS cyber_gaming;

-- Tạo mới database
CREATE DATABASE cyber_gaming;

-- Bắt đầu sử dụng
USE cyber_gaming;

-- Bảng người dùng
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng khu vực máy
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- Bảng máy trạm
CREATE TABLE IF NOT EXISTS pcs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pc_number VARCHAR(20) NOT NULL UNIQUE,
    category_id INT NOT NULL,
    configuration VARCHAR(255),
    price_per_hour DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('AVAILABLE', 'IN_USE', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Bảng đồ ăn/thức uống
CREATE TABLE IF NOT EXISTS foods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- Bảng đặt máy
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    pc_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'IN_SERVICE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (pc_id) REFERENCES pcs(id)
);

-- Bảng đơn hàng F&B
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    booking_id INT,
    status ENUM('PENDING', 'CONFIRMED', 'IN_SERVICE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Chi tiết đơn hàng
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (food_id) REFERENCES foods(id)
);

-- Bảng lịch sử giao dịch (ví điện tử)
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('DEPOSIT', 'PAYMENT', 'REFUND') NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);


-- Tài khoản demo
INSERT INTO users (full_name, username, password, phone, role, balance) VALUES
('Administrator', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '0900000001', 'ADMIN', 0.00),
('Nhân Viên 1', 'staff1', '7c12772809b1687b9bb2f7540bbbe2e0cf6d36ccc47f2c91c5c29e9f985cac11', '0900000002', 'STAFF', 0.00),
('Nguyễn Văn A', 'customer1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '0900000003', 'CUSTOMER', 500000.00);
-- Password: admin=admin, staff1=staff123, customer1=password123

-- Khu vực
INSERT INTO categories (name, description) VALUES
('Standard', 'Khu vực máy tiêu chuẩn'),
('VIP', 'Khu vực VIP cao cấp'),
('Stream Room', 'Phòng streaming chuyên nghiệp');

-- Máy trạm
INSERT INTO pcs (pc_number, category_id, configuration, price_per_hour, status) VALUES
('PC-S01', 1, 'i5-12400F / GTX1660 / 16GB RAM / 240Hz', 15000, 'AVAILABLE'),
('PC-S02', 1, 'i5-12400F / GTX1660 / 16GB RAM / 240Hz', 15000, 'AVAILABLE'),
('PC-S03', 1, 'i5-12400F / GTX1660 / 16GB RAM / 240Hz', 15000, 'MAINTENANCE'),
('PC-V01', 2, 'i9-13900K / RTX4080 / 32GB RAM / 360Hz', 35000, 'AVAILABLE'),
('PC-V02', 2, 'i9-13900K / RTX4080 / 32GB RAM / 360Hz', 35000, 'AVAILABLE'),
('PC-X01', 3, 'i7-13700K / RTX4070 / 64GB RAM / 360Hz', 50000, 'AVAILABLE');

-- Menu F&B
INSERT INTO foods (name, description, price, stock) VALUES
('Mì tôm trứng', 'Mì tôm nấu với trứng', 25000, 50),
('Bánh mì thịt', 'Bánh mì kẹp thịt nướng', 30000, 30),
('Cơm gà', 'Cơm trắng với gà xối mỡ', 45000, 20),
('Sting dâu', 'Nước tăng lực vị dâu', 15000, 100),
('Pepsi lon', 'Nước ngọt Pepsi 330ml', 12000, 100),
('Nước suối', 'Nước suối lạnh 500ml', 8000, 200),
('Snack Oishi', 'Bim bim Oishi vị tôm', 10000, 80),
('Cà phê sữa đá', 'Cà phê pha phin sữa đá', 20000, 60);