package com.yxh.fangs.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * 全局日志工具类
 * 支持：自动 TAG、JSON 格式化、超长日志分段打印、一键开关
 */
public class LogUtils {

    // 是否开启日志（上线必须改为 false）
    private static boolean DEBUG = true;

    // Gson（支持 Pretty Printing）
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // 默认 TAG
    private static final String DEFAULT_TAG = "AppLog";

    /**
     * 初始化日志开关
     */
    public static void init(boolean isDebug) {
        DEBUG = isDebug;
    }

    /**
     * 获取自动 TAG = 类名
     */
    private static String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 4) {
            String className = stackTrace[4].getClassName();
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return DEFAULT_TAG;
    }

    // ---------------- 基础日志 ----------------

    public static void d(String msg) {
        if (!DEBUG) return;
        Log.d(getTag(), msg);
    }

    public static void i(String msg) {
        if (!DEBUG) return;
        Log.i(getTag(), msg);
    }

    public static void w(String msg) {
        if (!DEBUG) return;
        Log.w(getTag(), msg);
    }

    public static void e(String msg) {
        if (!DEBUG) return;
        Log.e(getTag(), msg);
    }

    public static void e(String msg, Throwable t) {
        if (!DEBUG) return;
        Log.e(getTag(), msg, t);
    }

    // ---------------- JSON 格式化日志 ----------------

    public static void json(String json) {
        if (!DEBUG) return;

        if (json == null || json.trim().length() == 0) {
            Log.d(getTag(), "Empty or null JSON");
            return;
        }

        try {
            String pretty = gson.toJson(JsonParser.parseString(json));
            longLog(pretty);
        } catch (Exception e) {
            Log.e(getTag(), "Invalid JSON:\n" + json);
        }
    }

    // ---------------- 超长日志分段打印 ----------------

    public static void longLog(String msg) {
        if (!DEBUG) return;

        int maxLength = 3500;
        if (msg.length() <= maxLength) {
            Log.d(getTag(), msg);
            return;
        }

        int start = 0;
        int end;
        while (start < msg.length()) {
            end = Math.min(start + maxLength, msg.length());
            Log.d(getTag(), msg.substring(start, end));
            start = end;
        }
    }
}
