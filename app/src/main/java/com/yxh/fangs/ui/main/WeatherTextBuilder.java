package com.yxh.fangs.ui.main;

import android.text.TextUtils;

import com.yxh.fangs.bean.WeatherBean;

public class WeatherTextBuilder {

    public static String buildForecastText(WeatherBean bean) {
        if (bean == null) return "";

        StringBuilder sb = new StringBuilder();

        sb.append("未来（24）小时（")
                .append(bean.getSeaArea())
                .append("）区域，");

        String[] lonArr = bean.getLongitudeRange().split("-");
        String[] latArr = bean.getLatitudeRange().split("-");

        sb.append("北纬")
                .append(toDms(latArr[0]))
                .append("，东经")
                .append(toDms(lonArr[0]))
                .append("，到北纬")
                .append(toDms(latArr[1]))
                .append("，东经")
                .append(toDms(lonArr[1]))
                .append("，");

        sb.append("预计有")
                .append(bean.getWeatherPhenomenon())
                .append(bean.getWindDirection())
                .append(bean.getWindForce())
                .append("级")
                .append("浪高")
                .append(bean.getWaveHeight())
                .append("米")
                .append("能见度")
                .append(bean.getVisibility())
                .append("千米")
                .append(TextUtils.isEmpty(bean.getRemark()) ? "。" : ("," + bean.getRemark() + "。"));

        return sb.toString();
    }

    private static String toDms(String value) {
        double d = Double.parseDouble(value);

        int degree = (int) d;
        double m1 = (d - degree) * 60;
        int minute = (int) m1;
        int second = (int) ((m1 - minute) * 60);

        return degree + "度" + minute + "分" + second + "秒";
    }
}
