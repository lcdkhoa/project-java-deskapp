# Personal Expense Manager (Java Desktop)

Ứng dụng desktop quản lý chi tiêu cá nhân (Java Swing) theo Functional Specification v1.0.

## Tech stack

- **Ngôn ngữ**: Java **17** (LTS)
- **UI**: Swing + FlatLaf
- **Biểu đồ**: JFreeChart
- **Layout**: MigLayout
- **Database**: MySQL + JDBC (mysql-connector-j)
- **Kiến trúc**: MVC (`model` / `view` / `controller`) + `dao`

## Links (tải & tài liệu)

- **Java 17 (JDK)**: `https://learn.microsoft.com/vi-vn/java/openjdk/download` (Microsoft Build of OpenJDK) / `https://adoptium.net/temurin/releases/?version=17`
- **Maven**: `https://maven.apache.org/download.cgi`
- **FlatLaf**: `https://www.formdev.com/flatlaf/` / Maven Central: `https://central.sonatype.com/artifact/com.formdev/flatlaf`
- **MySQL Connector/J**: Maven Central: `https://central.sonatype.com/artifact/com.mysql/mysql-connector-j`
- **MySQL Server**: `https://dev.mysql.com/downloads/mysql/` (8.0+ khuyến nghị)
- **JFreeChart**: `https://www.jfree.org/jfreechart/` / Maven Central: `https://central.sonatype.com/artifact/org.jfree/jfreechart`
- **MigLayout**: `https://www.miglayout.com/` / Maven Central: `https://central.sonatype.com/artifact/com.miglayout/miglayout-swing`

## Yêu cầu

- Java/JDK **17**
- Maven **3.9+**

## Cách setup

1) Trỏ đến thư mục project:

```bash
cd project_java_desk_app
```

1) Build để tải dependencies:

```bash
mvn clean compile
```

## Cách chạy

Chạy bằng Maven (khuyến nghị):

```bash
mvn exec:java
```

Hoặc build jar:

```bash
mvn package
```

## Lưu ý về database (MySQL)

- Ứng dụng kết nối MySQL qua JDBC. Mặc định: `localhost:3306`, database `expense_manager`, user `root`, password rỗng.
- Cấu hình qua **system properties** hoặc **biến môi trường**:
  - `db.host` / `DB_HOST` — host (mặc định `localhost`)
  - `db.port` / `DB_PORT` — port (mặc định `3306`)
  - `db.name` / `DB_NAME` — tên database (mặc định `expense_manager`)
  - `db.user` / `DB_USER` — user (mặc định `root`)
  - `db.password` / `DB_PASSWORD` — mật khẩu
- Trước khi chạy app: tạo database và chạy script khởi tạo schema/seed:
  - `mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS expense_manager;"`
  - `mysql -u root -p expense_manager < src/main/java/com/expensemanager/db/init.sql`

## Cấu trúc thư mục

```text
src/main/java/com/expensemanager/
  App.java                    # entry point
  controller/                 # controller (MVC)
  model/                      # model (entity)
  view/                       # view (UI Swing)
  dao/                        # DAO (truy cập dữ liệu)
  db/                         # kết nối MySQL + init.sql (schema & seed)
  util/                       # tiện ích (format, UI factory/utils, context...)
  img/                        # assets hình ảnh (icon UI)
```

## Tính năng chính (tóm tắt)

- **Dashboard**: KPI, charts (last 7 days / by category / cashflow), cảnh báo budget, thêm giao dịch
- **Transactions**: tạo giao dịch (dialog), tìm kiếm & lọc, danh sách theo thời gian
- **Budget**: thêm/sửa budget theo tháng & danh mục, tổng quan và danh sách theo category
