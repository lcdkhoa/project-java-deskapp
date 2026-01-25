# Personal Expense Manager

Desktop app for personal expense tracking. Built per Functional Specification – v1.0.

## Tech stack

- **Language:** Java (JDK 17+; spec recommends JDK 21)
- **GUI:** Java Swing + FlatLaf
- **DB:** SQLite (local, single-user)
- **Data:** JDBC + DAO
- **Architecture:** MVC
- **Charts:** JFreeChart

## Build & run

```bash
mvn clean compile exec:java
```

Or:

```bash
mvn package
java -cp "target/classes;target/dependency/*" com.expensemanager.App
```

(On Unix use `:` instead of `;` in the classpath.)

DB file: `~/.expense-manager/expense.db`. Delete it to reset and re-seed categories.

## Features (per spec)

- **Dashboard:** Month selector, 4 KPI cards, Last 7 Days (bar), By Category (donut), Monthly Cashflow (line), Budget warnings, Add Transaction
- **Transactions:** Create (modal), Search & filter, List by month
- **Budget:** Add Budget (category, amount; max 500.000.000 đ), Monthly overview, Budget by category
- **Analytics:** Spending trend, Monthly forecast, spending habits and category analysis (basic)
- **UI:** Light/Dark toggle (top-right), VND formatting, primary Blue/Indigo

## DB schema (Section 6)

- `users`, `categories`, `transactions`, `budgets` + indexes `idx_tx_user_month`, `idx_tx_user_date`, `idx_budget_user_month`
- Categories seeded from 6.5.2 (Expense) and 6.5.3 (Income)
