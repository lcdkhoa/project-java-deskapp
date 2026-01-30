package com.expensemanager.view.BudgetView;

import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;

import java.util.List;

/**
 * Listener interface for BudgetDialog events.
 * Allows dialogs to communicate with controllers without direct database
 * access.
 */
public interface BudgetDialogListener {

    /**
     * Get available categories that can have a budget added for the current month.
     * (Categories that don't already have a budget)
     * 
     * @return list of available categories
     */
    List<Category> getAvailableCategories();

    /**
     * Called when a new budget is saved.
     * 
     * @param budget the new budget
     * @throws Exception if save fails
     */
    void onBudgetCreated(Budget budget) throws Exception;

    /**
     * Called when an existing budget is updated.
     * 
     * @param budget the updated budget
     * @throws Exception if update fails
     */
    void onBudgetUpdated(Budget budget) throws Exception;

    /**
     * Called to refresh views after changes.
     */
    void onRefreshRequired();
}
