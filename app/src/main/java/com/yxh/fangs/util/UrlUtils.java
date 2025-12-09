package com.yxh.fangs.util;

public class UrlUtils {

    /**
     * 当前环境：dev = 开发，test = 测试，prod = 生产
     */
    private static final String ENV = "prod";

    /**
     * 根据环境切换 Base URL
     */
    private static String getBaseUrl() {
        switch (ENV) {
            case "dev":
                return "http://47.118.24.57:9000/";
            case "test":
                return "http://47.118.24.57:9000/";
            case "prod":
            default:
                return "http://47.118.24.57:9000/";
        }
    }

    public static String getReceiverRegisterUrl() {
        return getBaseUrl() + "prod-api/business/device/register";
    }

    public static String getReceiverStatusLogUrl() {
        return getBaseUrl() + "prod-api/business/deviceStatusLog";
    }

    public static String getDeviceLocationAddUrl() {
        return getBaseUrl() + "prod-api/business/deviceLocation";
    }

    public static String getSosEventStartUrl() {
        return getBaseUrl() + "prod-api/business/sosEvent";
    }

    public static String getMessageReceiveUrl(String deviceSn) {
        return getBaseUrl() + "/prod-api/business/messageStatus/listByDeviceSn?deviceSn=" + deviceSn;
    }
}
