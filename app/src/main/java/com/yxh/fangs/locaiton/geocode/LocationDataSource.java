package com.yxh.fangs.locaiton.geocode;

import com.yxh.fangs.locaiton.model.LocationPoint;

import io.reactivex.rxjava3.core.Observable;

public interface LocationDataSource {

    /**
     * 获取一次定位（只负责定位）
     */
    Observable<LocationPoint> getCurrentLocation();

    /**
     * 是否可用
     */
    boolean isAvailable();
}
