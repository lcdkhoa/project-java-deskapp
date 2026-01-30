CREATE DATABASE IF NOT EXISTS expense_manager;
USE expense_manager;

CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_path VARCHAR(512) NOT NULL,
    legend_chart_color VARCHAR(32) NOT NULL,
    type VARCHAR(16) NOT NULL,
    is_active INT NOT NULL DEFAULT 1,
    created_at VARCHAR(64) NOT NULL,
    CONSTRAINT chk_category_type CHECK (type IN ('expense','income'))
);

CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(36) PRIMARY KEY,
    amount BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    wallet_type VARCHAR(32) NOT NULL,
    note TEXT,
    transaction_date VARCHAR(16) NOT NULL,
    transaction_time VARCHAR(16) NOT NULL,
    month_key VARCHAR(7) NOT NULL,
    created_at VARCHAR(64) NOT NULL,
    updated_at VARCHAR(64) NOT NULL,
    CONSTRAINT chk_tx_type CHECK (type IN ('expense','income')),
    CONSTRAINT chk_tx_wallet CHECK (wallet_type IN ('cash','bank_transfer','card','e_wallet')),
    CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS budgets (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    month_key VARCHAR(7) NOT NULL,
    amount BIGINT NOT NULL,
    created_at VARCHAR(64) NOT NULL,
    updated_at VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_budget_category_month (category_id, month_key),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS wallet_types (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    icon_path VARCHAR(512),
    sort_order INT NOT NULL DEFAULT 0,
    is_active INT NOT NULL DEFAULT 1,
    created_at VARCHAR(64) NOT NULL
);

CREATE INDEX idx_tx_month ON transactions(month_key);
CREATE INDEX idx_tx_date ON transactions(transaction_date);
CREATE INDEX idx_budget_month ON budgets(month_key);

-- Seed categories (only if empty)
INSERT INTO categories (id, name, icon_path, legend_chart_color, type, is_active, created_at)
SELECT * FROM (
    SELECT 'cat-exp-food' AS id, 'Food' AS name, 'src/main/java/com/expensemanager/img/category/food.png' AS icon_path, '#f5c8af' AS legend_chart_color, 'expense' AS type, 1 AS is_active, CAST(NOW() AS CHAR) AS created_at
    UNION ALL SELECT 'cat-exp-transport', 'Transport', 'src/main/java/com/expensemanager/img/category/transport.png', '#84d2f4', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-housing', 'Housing', 'src/main/java/com/expensemanager/img/category/housing.png', '#cad93f', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-bills', 'Bills', 'src/main/java/com/expensemanager/img/category/bill.png', '#e4b031', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-shopping', 'Shopping', 'src/main/java/com/expensemanager/img/category/shopping.png', '#48b24f', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-entertainment', 'Entertainment', 'src/main/java/com/expensemanager/img/category/entertainment.png', '#e57438', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-healthcare', 'Healthcare', 'src/main/java/com/expensemanager/img/category/healthcare.png', '#d21f75', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-education', 'Education', 'src/main/java/com/expensemanager/img/category/education.png', '#4770b3', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-exp-other', 'Other', 'src/main/java/com/expensemanager/img/category/others.png', '#05338d', 'expense', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-inc-salary', 'Salary', 'src/main/java/com/expensemanager/img/category/salary.png', '#d21f75', 'income', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-inc-freelance', 'Freelance', 'src/main/java/com/expensemanager/img/category/freelance.png', '#10B981', 'income', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-inc-affiliate', 'Affiliate', 'src/main/java/com/expensemanager/img/category/affiliate.png', '#14B8A6', 'income', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-inc-selling', 'Selling', 'src/main/java/com/expensemanager/img/category/selling.png', '#16A34A', 'income', 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'cat-inc-other', 'Other Income', 'src/main/java/com/expensemanager/img/category/other_income.png', '#4ADE80', 'income', 1, CAST(NOW() AS CHAR)
) AS seed
WHERE (SELECT COUNT(*) FROM categories) = 0;

-- Seed wallet_types (only if empty)
INSERT INTO wallet_types (id, name, display_name, icon_path, sort_order, is_active, created_at)
SELECT * FROM (
    SELECT 'wallet-cash' AS id, 'cash' AS name, 'Cash' AS display_name, 'src/main/java/com/expensemanager/img/wallet/cash.png' AS icon_path, 1 AS sort_order, 1 AS is_active, CAST(NOW() AS CHAR) AS created_at
    UNION ALL SELECT 'wallet-bank', 'bank_transfer', 'Bank Transfer', 'src/main/java/com/expensemanager/img/wallet/bank.png', 2, 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'wallet-card', 'card', 'Visa Card', 'src/main/java/com/expensemanager/img/wallet/card.png', 3, 1, CAST(NOW() AS CHAR)
    UNION ALL SELECT 'wallet-ewallet', 'e_wallet', 'E-Wallet', 'src/main/java/com/expensemanager/img/wallet/ewallet.png', 4, 1, CAST(NOW() AS CHAR)
) AS seed
WHERE (SELECT COUNT(*) FROM wallet_types) = 0;
