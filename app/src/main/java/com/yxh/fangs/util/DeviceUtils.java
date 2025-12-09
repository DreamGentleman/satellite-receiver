package com.yxh.fangs.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.UUID;

public class DeviceUtils {


    private static final String KEY_DEVICE_ID = "key_device_unique_id";

    /**
     * 获取设备唯一ID（ANDROID_ID → 不可用则生成 UUID）
     */
    public static String getDeviceId(Context context) {

        // 1. 先从 SP 取（若之前生成过，直接返回）
        String savedId = SPUtils.getString(KEY_DEVICE_ID, "");
        if (!TextUtils.isEmpty(savedId)) {
            return savedId;
        }

        // 2. 获取 ANDROID_ID
        String androidId = null;
        try {
            androidId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 判断 ANDROID_ID 是否可用
        if (isInvalidAndroidId(androidId)) {
            // 获取不到时 → 生成随机 UUID
            androidId = generateUUID();
        }

        // 4. 保存到 SP，保持稳定（只生成一次）
        SPUtils.putString(KEY_DEVICE_ID, androidId);

        return androidId;
    }

    /**
     * 判断 ANDROID_ID 是否不可用
     */
    private static boolean isInvalidAndroidId(String id) {
        if (TextUtils.isEmpty(id)) return true;
        if ("9774d56d682e549c".equals(id)) return true; // 安卓早期 Bug 固定值
        return id.length() < 8; // 明显异常情况
    }

    /**
     * 生成稳定 UUID
     */
    private static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取设备名称（用户可修改，如 "Camille 的手机"）
     */
    public static String getDeviceName(Context context) {
        // Android 7.0+ 可读取设备名称
        String customName = Settings.Global.getString(
                context.getContentResolver(),
                "device_name"
        );
        if (customName != null && customName.length() > 0) {
            return customName;
        }

        // 读取不到时，使用 Manufacturer + Model
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model != null && model.startsWith(manufacturer)) {
            return model;
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * 获取设备型号（真实型号，用于上报后台，如 "MI 10", "P50"）
     */
    public static String getDeviceModel() {
        return Build.MODEL; // 固定型号
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}


