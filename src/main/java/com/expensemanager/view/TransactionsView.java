package com.expensemanager.view;

/**
 * Transactions - Section 2. Search & Filter, Transaction list, Add Transaction.
 */
public class TransactionsView extends TransactionView {

    public TransactionsView(MainFrame main) {
        super(main);
    }

    void onShown() {
        refresh();
    }
}

