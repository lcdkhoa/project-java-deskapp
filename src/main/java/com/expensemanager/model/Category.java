package com.expensemanager.model;

import java.time.Instant;

public class Category {
    private String id;
    private String name;
    private String iconPath;
    private String legendChartColor;
    private String type;
    private boolean isActive;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getLegendChartColor() {
        return legendChartColor;
    }

    public void setLegendChartColor(String legendChartColor) {
        this.legendChartColor = legendChartColor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
