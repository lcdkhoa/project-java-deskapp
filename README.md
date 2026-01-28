# Personal Expense Manager (Java Desktop)

Ứng dụng desktop quản lý chi tiêu cá nhân (Java Swing) theo Functional Specification v1.0.

## Tech stack

- **Ngôn ngữ**: Java **17** (LTS)
- **UI**: Swing + FlatLaf
- **Biểu đồ**: JFreeChart
- **Layout**: MigLayout
- **Database**: SQLite (local) + JDBC
- **Kiến trúc**: MVC (`model` / `view` / `controller`) + `dao`

## Links (tải & tài liệu)

- **Java 17 (JDK)**: `https://learn.microsoft.com/vi-vn/java/openjdk/download` (Microsoft Build of OpenJDK) / `https://adoptium.net/temurin/releases/?version=17`
- **Maven**: `https://maven.apache.org/download.cgi`
- **FlatLaf**: `https://www.formdev.com/flatlaf/` / Maven Central: `https://central.sonatype.com/artifact/com.formdev/flatlaf`
- **SQLite JDBC (sqlite-jdbc)**: `https://github.com/xerial/sqlite-jdbc` / Maven Central: `https://central.sonatype.com/artifact/org.xerial/sqlite-jdbc`
- **SQLite (engine)**: `https://www.sqlite.org/download.html`
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

## Lưu ý về database

- File DB mặc định đang nằm trong project tại: `src/main/java/com/expensemanager/db/expense.db`
- Script khởi tạo schema/seed: `src/main/java/com/expensemanager/db/init.sql`

## Cấu trúc thư mục

```text
src/main/java/com/expensemanager/
  App.java                    # entry point
  controller/                 # controller (MVC)
  model/                      # model (entity)
  view/                       # view (UI Swing)
  dao/                        # DAO (truy cập dữ liệu)
  db/                         # kết nối DB + file DB + init.sql
  util/                       # tiện ích (format, UI factory/utils, context...)
  img/                        # assets hình ảnh (icon UI)
```

## Tính năng chính (tóm tắt)

- **Dashboard**: KPI, charts (last 7 days / by category / cashflow), cảnh báo budget, thêm giao dịch
- **Transactions**: tạo giao dịch (dialog), tìm kiếm & lọc, danh sách theo thời gian
- **Budget**: thêm/sửa budget theo tháng & danh mục, tổng quan và danh sách theo category
