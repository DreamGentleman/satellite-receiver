package com.yxh.fangs.locaiton.cache;

import com.yxh.fangs.locaiton.model.LocationPoint;

public class LocationCache {

    private LocationPoint lastLocation;

    public void save(LocationPoint point) {
        lastLocation = point;
    }

    public LocationPoint getLast() {
        return lastLocation;
    }

    public boolean hasCache() {
        return lastLocation != null;
    }
}
