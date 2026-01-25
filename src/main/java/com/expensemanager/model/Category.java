package com.expensemanager.model;

import java.time.Instant;

/**
 * Model for categories table - Section 6.3.2.
 * type: expense | income. Icon & color per 6.5.2 / 6.5.3.
 */
public class Category {
    private String id;
    private String name;
    private String icon;
    private String color;
    private String type; // expense | income
    private boolean isActive;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
