package com.expensemanager.view.TransactionView;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;

import java.util.List;

/**
 * Listener interface for CreateTransactionDialog events.
 * Allows dialogs to communicate with controllers without direct database
 * access.
 */
public interface TransactionDialogListener {

    /**
     * Get categories by type.
     * 
     * @param type "expense" or "income"
     * @return list of categories
     */
    List<Category> getCategoriesByType(String type);

    /**
     * Called when a new transaction is saved.
     * 
     * @param transaction the new transaction
     * @throws Exception if save fails
     */
    void onTransactionCreated(Transaction transaction) throws Exception;

    /**
     * Called to refresh views after changes.
     */
    void onRefreshRequired();
}
