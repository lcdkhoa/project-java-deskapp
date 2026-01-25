package com.expensemanager.model;

import java.time.Instant;

/**
 * Model for users table - Section 6.3.1.
 */
public class User {
    private String id;
    private String email;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
