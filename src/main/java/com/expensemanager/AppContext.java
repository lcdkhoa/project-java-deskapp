package com.expensemanager;

/**
 * Holds current user id for single-user. Set after DB init.
 */
public final class AppContext {
    private static volatile String userId;

    public static String getUserId() { return userId; }
    public static void setUserId(String id) { userId = id; }
}
