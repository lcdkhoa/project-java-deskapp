package com.expensemanager.model;

import java.time.Instant;

/**
 * Model for budgets table - Section 6.3.4.
 * UNIQUE(user_id, category_id, month_key). amount = monthly budget.
 */
public class Budget {
    private String id;
    private String userId;
    private String categoryId;
    private String monthKey;
    private long amount;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
