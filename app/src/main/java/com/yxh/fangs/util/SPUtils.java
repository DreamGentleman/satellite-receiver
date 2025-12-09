package com.yxh.fangs.util;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SPUtils {

    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    /**
     * 初始化，在 Application 中调用一次即可
     */
    public static void init(Context context) {
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences("app_sp", Context.MODE_PRIVATE);
            editor = sp.edit();
        }
    }

    /**
     * 保存 String
     */
    public static void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    /**
     * 获取 String
     */
    public static String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    /**
     * 保存 int
     */
    public static void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    /**
     * 获取 int
     */
    public static int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    /**
     * 保存 boolean
     */
    public static void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    /**
     * 获取 boolean
     */
    public static boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    /**
     * 保存 long
     */
    public static void putLong(String key, long value) {
        editor.putLong(key, value).apply();
    }

    /**
     * 获取 long
     */
    public static long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    /**
     * 保存 float
     */
    public static void putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    /**
     * 获取 float
     */
    public static float getFloat(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    /**
     * 保存 Set<String>
     */
    public static void putStringSet(String key, Set<String> value) {
        editor.putStringSet(key, value).apply();
    }

    /**
     * 获取 Set<String>
     */
    public static Set<String> getStringSet(String key, Set<String> defValue) {
        return sp.getStringSet(key, defValue);
    }

    /**
     * 移除某个 key
     */
    public static void remove(String key) {
        editor.remove(key).apply();
    }

    /**
     * 清空所有数据
     */
    public static void clear() {
        editor.clear().apply();
    }
}
