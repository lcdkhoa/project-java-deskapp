package com.expensemanager.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates database schema per Section 6 - strict compliance.
 * users, categories, transactions, budgets + indexes.
 */
public class Schema {

    public static void createSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // 6.3.1 users
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    email TEXT UNIQUE NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);

            // 6.3.2 categories
            st.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('expense','income')),
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL
                )
                """);

            // 6.3.3 transactions
            st.execute("""
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
                )
                """);

            // 6.3.4 budgets
            st.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id TEXT PRIMARY KEY,
                    user_id TEXT NOT NULL REFERENCES users(id),
                    category_id TEXT NOT NULL REFERENCES categories(id),
                    month_key TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    UNIQUE(user_id, category_id, month_key)
                )
                """);

            // 6.5 Indexes
            st.execute("CREATE INDEX IF NOT EXISTS idx_tx_user_month ON transactions(user_id, month_key);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_tx_user_date ON transactions(user_id, transaction_date);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_budget_user_month ON budgets(user_id, month_key);");
        }
    }
}
