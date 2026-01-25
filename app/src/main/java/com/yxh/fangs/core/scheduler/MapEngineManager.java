package com.yxh.fangs.core.scheduler;

import android.content.Context;

import com.bigemap.bmcore.BMEngine;
import com.yxh.fangs.util.LogUtils;

import java.io.File;

/**
 * Bigemap 引擎统一管理类
 * 负责：引擎初始化 / 路径配置 / 离线模式
 */
public class MapEngineManager {

    private static final String TAG = "MapEngineManager：";

    /**
     * Bigemap Key（建议后续放到配置文件）
     */
    private static final String MAP_KEY = "bda2ea3fb18fdd9a4a6d922389576df7";

    /**
     * 是否已初始化，防止重复 init
     */
    private static boolean initialized = false;

    private MapEngineManager() {
        // 禁止 new
    }

    /**
     * 引擎初始化（Application 或首次进入地图时调用）
     *
     * @param context       Context
     * @param enableTerrain 是否启用地形（离线 DEM）
     */
    public static synchronized void init(Context context, boolean enableTerrain) {
        if (initialized) {
            LogUtils.i(TAG + "BMEngine already initialized");
            return;
        }

        Context appContext = context.getApplicationContext();

        try {
            // 1. 预初始化（Key 校验）
            BMEngine.preInit(appContext, MAP_KEY);

            // 2. 资源路径（Bigemap 所需根目录）
            String enginePath = getEngineRootPath(appContext);

            // 确保目录存在
            ensureDir(enginePath);

            // 3. 正式初始化
            BMEngine.init(
                    appContext,
                    enginePath,
                    enableTerrain
            );

            initialized = true;
            LogUtils.i(TAG + "BMEngine init success, path=" + enginePath);

        } catch (Exception e) {
            LogUtils.e(TAG + "BMEngine init failed", e);
            throw new RuntimeException("BMEngine init failed", e);
        }
    }

    /**
     * 获取 Bigemap 引擎根目录
     * 通常用于：icon / map / 离线包
     */
    public static String getEngineRootPath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator;
    }

    /**
     * 是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 确保目录存在
     */
    private static void ensureDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            LogUtils.i(TAG + "create dir: " + path + " -> " + ok);
        }
    }
}