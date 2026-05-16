package com.yxh.fangs.ui.main.handler;

import android.text.TextUtils;

import com.yxh.fangs.bean.TyphoonTrackBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TyphoonTimeUtils {

    private static long parseTimeToday(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return 0;
        try {
            String s = timeStr.trim();
            // 有些接口会给 "1769349600000.0" 或带小数，先处理一下
            if (s.contains(".")) {
                s = s.substring(0, s.indexOf("."));
            }
            long ts = Long.parseLong(s);
            // 兼容 10 位（秒）时间戳：转毫秒
            if (ts > 0 && ts < 100000000000L) { // 小于 1e11 基本就是秒
                ts = ts * 1000L;
            }
            return ts;
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<TyphoonTrackBean.WindCirclesBean> findNearestWindCircles(List<TyphoonTrackBean.WindCirclesBean> circles, int hour) {

        List<TyphoonTrackBean.WindCirclesBean> result = new ArrayList<>();
        if (circles == null || circles.isEmpty()) return result;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long now = cal.getTimeInMillis();

        long min = 2 * 60 * 60 * 1000L; // 2 小时

        for (TyphoonTrackBean.WindCirclesBean c : circles) {
            long t = parseTimeToday(c.getExpectedTime());
            long delta = Math.abs(t - now);
            if (delta < min) {
                result.add(c);
            }
        }
        return result;
    }


    public static TyphoonTrackBean.WindCirclesBean findNearestWindCircle(List<TyphoonTrackBean.WindCirclesBean> circles, int hour) {
        if (circles == null || circles.isEmpty()) return null;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long now = cal.getTimeInMillis();

        TyphoonTrackBean.WindCirclesBean nearest = circles.get(0);
        long min = 2 * 60 * 60 * 1000L; // 2 小时

        for (TyphoonTrackBean.WindCirclesBean c : circles) {
            long t = parseTimeToday(c.getExpectedTime());
            long delta = Math.abs(t - now);
            if (delta < min) {
                min = delta;
                nearest = c;
            }
        }
        return nearest;
    }
}
