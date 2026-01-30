
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    created_at TEXT NOT NULL
);


CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    icon_path TEXT NOT NULL,
    legend_chart_color TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('expense','income')),
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL
);


CREATE TABLE IF NOT EXISTS transactions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    amount INTEGER NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('expense','income')),
    category_id TEXT NOT NULL REFERENCES categories(id),
    wallet_type TEXT NOT NULL CHECK (wallet_type IN ('cash','bank_transfer','card','e_wallet')),
    note TEXT,
    transaction_date TEXT NOT NULL,
    transaction_time TEXT NOT NULL,
    month_key TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);


CREATE TABLE IF NOT EXISTS budgets (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    category_id TEXT NOT NULL REFERENCES categories(id),
    month_key TEXT NOT NULL,
    amount INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(user_id, category_id, month_key)
);


CREATE TABLE IF NOT EXISTS wallet_types (
    id TEXT PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    icon_path TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL
);


CREATE INDEX IF NOT EXISTS idx_tx_user_month ON transactions(user_id, month_key);
CREATE INDEX IF NOT EXISTS idx_tx_user_date ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_budget_user_month ON budgets(user_id, month_key);


INSERT INTO categories (id, name, icon_path, legend_chart_color, type, is_active, created_at)
SELECT * FROM (
    SELECT 'cat-exp-food' AS id, 'Food' AS name, 'src/main/java/com/expensemanager/img/category/food.png' AS icon_path, '#f5c8af' AS legend_chart_color, 'expense' AS type, 1 AS is_active, datetime('now') AS created_at
    UNION ALL SELECT 'cat-exp-transport', 'Transport', 'src/main/java/com/expensemanager/img/category/transport.png', '#84d2f4', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-housing', 'Housing', 'src/main/java/com/expensemanager/img/category/housing.png', '#cad93f', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-bills', 'Bills', 'src/main/java/com/expensemanager/img/category/bill.png', '#e4b031', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-shopping', 'Shopping', 'src/main/java/com/expensemanager/img/category/shopping.png', '#48b24f', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-entertainment', 'Entertainment', 'src/main/java/com/expensemanager/img/category/entertainment.png', '#e57438', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-healthcare', 'Healthcare', 'src/main/java/com/expensemanager/img/category/healthcare.png', '#d21f75', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-education', 'Education', 'src/main/java/com/expensemanager/img/category/education.png', '#4770b3', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-exp-other', 'Other', 'src/main/java/com/expensemanager/img/category/others.png', '#05338d', 'expense', 1, datetime('now')
    UNION ALL SELECT 'cat-inc-salary', 'Salary', 'src/main/java/com/expensemanager/img/category/salary.png', '#d21f75', 'income', 1, datetime('now')
    UNION ALL SELECT 'cat-inc-freelance', 'Freelance', 'src/main/java/com/expensemanager/img/category/freelance.png', '#10B981', 'income', 1, datetime('now')
    UNION ALL SELECT 'cat-inc-affiliate', 'Affiliate', 'src/main/java/com/expensemanager/img/category/affiliate.png', '#14B8A6', 'income', 1, datetime('now')
    UNION ALL SELECT 'cat-inc-selling', 'Selling', 'src/main/java/com/expensemanager/img/category/selling.png', '#16A34A', 'income', 1, datetime('now')
    UNION ALL SELECT 'cat-inc-other', 'Other Income', 'src/main/java/com/expensemanager/img/category/other_income.png', '#4ADE80', 'income', 1, datetime('now')
) AS seed
WHERE (SELECT COUNT(*) FROM categories) = 0;



INSERT INTO wallet_types (id, name, display_name, icon_path, sort_order, is_active, created_at)
SELECT * FROM (
    SELECT 'wallet-cash' AS id, 'cash' AS name, 'Cash' AS display_name, 'src/main/java/com/expensemanager/img/wallet/cash.png' AS icon_path, 1 AS sort_order, 1 AS is_active, datetime('now') AS created_at
    UNION ALL SELECT 'wallet-bank', 'bank_transfer', 'Bank Transfer', 'src/main/java/com/expensemanager/img/wallet/bank.png', 2, 1, datetime('now')
    UNION ALL SELECT 'wallet-card', 'card', 'Visa Card', 'src/main/java/com/expensemanager/img/wallet/card.png', 3, 1, datetime('now')
    UNION ALL SELECT 'wallet-ewallet', 'e_wallet', 'E-Wallet', 'src/main/java/com/expensemanager/img/wallet/ewallet.png', 4, 1, datetime('now')
) AS seed
WHERE (SELECT COUNT(*) FROM wallet_types) = 0;