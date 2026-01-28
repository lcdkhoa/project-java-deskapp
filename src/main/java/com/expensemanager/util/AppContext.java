package com.expensemanager.util;

public final class AppContext {
    private static volatile String userId;

    private AppContext() {
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String id) {
        userId = id;
    }
}
