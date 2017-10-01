package com.sachin.filemanager.utils;

public final class Actions {
    private static Runnable action = null;

    public static Runnable getAction() {
        return action;
    }

    public static void setAction(Runnable action){
        Actions.action = action;
    }

    public static boolean hashAction() {
        return getAction() != null;
    }
}
