package com.expensemanager.view.BudgetView;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;

import java.util.List;
import java.util.Map;

public class BudgetViewData {

    public final long totalBudget;
    public final long totalSpent;
    public final long remaining;
    public final double spentPercent;

    public final List<BudgetDAO.BudgetUsedRow> budgetUsedRows;
    public final Map<String, Category> categoryMap;
    public final Map<String, Budget> budgetMap;

    public BudgetViewData(
            long totalBudget,
            long totalSpent,
            List<BudgetDAO.BudgetUsedRow> budgetUsedRows,
            Map<String, Category> categoryMap,
            Map<String, Budget> budgetMap) {
        this.totalBudget = totalBudget;
        this.totalSpent = totalSpent;
        this.remaining = totalBudget - totalSpent;
        this.spentPercent = totalBudget > 0 ? (totalSpent * 100.0 / totalBudget) : 0;
        this.budgetUsedRows = budgetUsedRows;
        this.categoryMap = categoryMap;
        this.budgetMap = budgetMap;
    }
}
