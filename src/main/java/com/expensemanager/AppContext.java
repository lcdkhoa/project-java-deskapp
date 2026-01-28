package com.expensemanager;

public final class AppContext {
    private static volatile String userId;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String id) {
        userId = id;
    }
}
