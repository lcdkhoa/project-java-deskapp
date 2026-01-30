package com.expensemanager.view.BudgetView;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import java.util.List;

public interface BudgetDialogListener {
    List<Category> getAvailableCategories();

    void onBudgetCreated(Budget budget) throws Exception;

    void onBudgetUpdated(Budget budget) throws Exception;

    void onRefreshRequired();
}
