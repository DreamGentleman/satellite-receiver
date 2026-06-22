package com.yxh.fangs.application;

public final class AppRuntimeState {

    private static volatile boolean enteredMain;

    private AppRuntimeState() {
    }

    public static void markEnteredMain() {
        enteredMain = true;
    }

    public static boolean hasEnteredMain() {
        return enteredMain;
    }
}
