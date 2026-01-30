# Personal Expense Manager (Java Desktop)

Một ứng dụng desktop hiện đại, trực quan giúp quản lý chi tiêu cá nhân hiệu quả, được xây dựng bằng **Java** và **MySQL**. Dự án áp dụng mô hình kiến trúc **MVC**, giao diện **Swing** kết hợp với **FlatLaf** cho trải nghiệm người dùng mượt mà và thẩm mỹ.

## 🚀 Các Tính Năng Chính

### 📊 Dashboard (Tổng quan)
- **Biểu đồ trực quan**:
    - **Cash Flow**: Theo dõi dòng tiền (Thu/Chi) qua biểu đồ đường.
    - **Thu/Chi theo danh mục**: Biểu đồ tròn thể hiện tỷ lệ chi tiêu cho từng nhóm (Ăn uống, Di chuyển, v.v.).
- **Chỉ số tài chính (KPIs)**: Tổng thu, Tổng chi, Số dư ví, và Ngân sách còn lại trong tháng.
- **Cảnh báo ngân sách**: Hiển thị trạng thái ngân sách của các danh mục (An toàn, Cảnh báo, Vượt mức).

### 💸 Quản lý Giao dịch (Transactions)
- **Thêm mới**: Giao diện dialog tiện lợi để nhập liệu nhanh chóng (Số tiền, Danh mục, Ví, Ghi chú, Thời gian).
- **Lịch sử & Tìm kiếm**:
    - Xem danh sách giao dịch chi tiết.
    - Bộ lọc mạnh mẽ: Theo khoảng thời gian (Hôm nay, Tuần này, Tháng này...), Ví, Danh mục.
    - Tìm kiếm theo từ khóa trong ghi chú.

### 💰 Quản lý Ngân sách (Budget)
- **Thiết lập mục tiêu**: Đặt ngân sách tối đa cho từng danh mục trong tháng.
- **Theo dõi tiến độ**: Dashboard và Transaction View giúp bạn biết mình đã chi bao nhiêu so với ngân sách.

### 🛠️ Quản lý Danh mục & Ví
- Hệ thống danh mục đa dạng (Ăn uống, Nhà cửa, Lương, Thưởng...).
- Hỗ trợ nhiều loại ví (Tiền mặt, Thẻ, Ví điện tử, Chuyển khoản).

---

## 🛠 Tech Stack

Dự án sử dụng các công nghệ và thư viện Java tiêu chuẩn:

- **Ngôn ngữ**: Java **17** (LTS)
- **Giao diện (UI)**:
    - **Swing**: Framework UI cốt lõi.
    - **FlatLaf**: Look and Feel hiện đại (Flat UI).
    - **MigLayout**: Quản lý layout mạnh mẽ và linh hoạt.
    - **JCalendar**: Component chọn ngày tháng.
- **Biểu đồ**: **JFreeChart**
- **Cơ sở dữ liệu**: **MySQL** (kết nối qua JDBC / `mysql-connector-j`)
- **Kiến trúc**: MVC (Model - View - Controller) + DAO Pattern + Service Layer.
- **Build Tool**: Maven

---

## ⚙️ Yêu cầu hệ thống

- **Java JDK**: phiên bản **17** trở lên.
- **Maven**: phiên bản **3.6+**.
- **MySQL Server**: phiên bản **8.0+**.

---

## 📥 Hướng dẫn Cài đặt & Chạy ứng dụng

### 1. Chuẩn bị Cơ sở dữ liệu (Database)

Trước khi chạy ứng dụng, bạn cần tạo database và các bảng.

1.  Mở MySQL Client hoặc Workbench.
2.  Tạo database:
    ```sql
    CREATE DATABASE IF NOT EXISTS expense_manager;
    ```
3.  Chạy script khởi tạo (tạo bảng và dữ liệu mẫu):
    
    *Cách 1: Dùng command line*
    ```bash
    mysql -u root -p expense_manager < src/main/java/com/expensemanager/db/init.sql
    ```
    
    *Cách 2: Dùng MySQL Workbench/DBeaver*
    - Mở file `src/main/java/com/expensemanager/db/init.sql`.
    - Execute toàn bộ script vào database `expense_manager`.

### 2. Cấu hình Kết nối

Mặc định ứng dụng kết nối tới `localhost:3306`, user `root`, không mật khẩu. Nếu cấu hình của bạn khác, hãy set biến môi trường hoặc sửa code (hoặc chạy với tham số hệ thống - chưa implement config file ngoài).

*Hiện tại cấu hình cứng trong `DatabaseHelper.java` hoặc biến môi trường:*
- `DB_HOST` (default: localhost)
- `DB_PORT` (default: 3306)
- `DB_NAME` (default: expense_manager)
- `DB_USER` (default: root)
- `DB_PASSWORD` (default: *empty*)

### 3. Build và Chạy

Mở terminal tại thư mục gốc của dự án (`project_java_desk_app`).

**Bước 1: Cài đặt dependencies và compile**
```bash
mvn clean compile
```

**Bước 2: Chạy ứng dụng**
```bash
mvn exec:java
```

Hoặc đóng gói thành file JAR và chạy:
```bash
mvn package
java -jar target/personal-expense-manager-1.0.0-SNAPSHOT.jar
```

---

## 📁 Cấu trúc Thư mục

```text
src/main/java/com/expensemanager/
├── controller/       # Xử lý logic điều hướng và tương tác (Controller trong MVC)
├── model/            # Các lớp thực thể (Entity) đại diện dữ liệu
├── view/             # Giao diện người dùng (JFrame, JPanel, Dialog)
│   ├── DashboardView/
│   ├── TransactionView/
│   ├── BudgetView/
│   └── CommonComponents/
├── dao/              # Data Access Object - Tương tác trực tiếp với CSDL
├── service/          # Business Logic Layer - Xử lý nghiệp vụ
├── db/               # Cấu hình kết nối DB và script init.sql
└── util/             # Các tiện ích (Formatter, Constants...)
```
