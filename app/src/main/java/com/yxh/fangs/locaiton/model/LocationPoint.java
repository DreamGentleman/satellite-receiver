package com.yxh.fangs.locaiton.model;

public class LocationPoint {

    public final double latitude;
    public final double longitude;
    public final long time;

    public LocationPoint(double lat, double lng, long time) {
        this.latitude = lat;
        this.longitude = lng;
        this.time = time;
    }
}

