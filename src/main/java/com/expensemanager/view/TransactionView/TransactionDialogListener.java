package com.expensemanager.view.TransactionView;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.WalletType;

import java.util.List;

public interface TransactionDialogListener {

    List<Category> getCategoriesByType(String type);

    List<WalletType> getWalletTypes();

    void onTransactionCreated(Transaction transaction) throws Exception;

    void onRefreshRequired();
}
