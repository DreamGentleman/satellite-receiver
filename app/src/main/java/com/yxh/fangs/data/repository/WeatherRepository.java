package com.yxh.fangs.data.repository;

import com.yxh.fangs.R;

public class WeatherRepository {

    public static int getWeatherIcon(String weatherPhenomenon) {
        switch (weatherPhenomenon) {
            case "晴":
                return R.mipmap.ic_fine;
            case "多云":
                return R.mipmap.ic_cloudy;
            case "阴":
                return R.mipmap.ic_cloudy_sky;
            case "雷阵雨":
                return R.mipmap.ic_thunder_shower;
            case "雷阵雨伴冰雹":
                return R.mipmap.ic_thunderstorms_with_hail;
            case "雨夹雪":
                return R.mipmap.ic_sleet;
            case "小雨":
                return R.mipmap.ic_sprinkle;
            case "中雨":
                return R.mipmap.ic_moderate_rain;
            case "大雨":
                return R.mipmap.ic_heavy_rain;
            case "暴雨":
                return R.mipmap.ic_torrential_rain;
            case "大暴雨":
                return R.mipmap.ic_downpour;
            case "特大暴雨":
                return R.mipmap.ic_heavy_downpour;
            case "小雪":
                return R.mipmap.ic_scouther;
            case "中雪":
                return R.mipmap.ic_moderate_snow;
            case "大雪":
                return R.mipmap.ic_heavy_snow;
            case "暴雪":
                return R.mipmap.ic_blizzard;
            case "雾":
                return R.mipmap.ic_fog;
            case "冻雨":
                return R.mipmap.ic_ice_rain;
            case "沙尘暴":
                return R.mipmap.ic_sand_storm;
            case "扬沙或浮尘":
                return R.mipmap.ic_sand_or_dust;
            case "强沙尘暴":
                return R.mipmap.ic_strong_sandstorm;
            case "霾":
                return R.mipmap.ic_haze;
        }
        return R.mipmap.ic_fine;
    }
}
