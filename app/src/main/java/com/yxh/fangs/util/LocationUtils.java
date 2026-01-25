package com.yxh.fangs.util;

import com.bigemap.bmcore.entity.GeoPoint;

import java.util.List;

public class LocationUtils {
    public static GeoPoint computePolygonCentroid(List<GeoPoint> pts) {
        double lonSum = 0, latSum = 0;
        for (GeoPoint p : pts) {
            lonSum += p.lon;
            latSum += p.lat;
        }
        return new GeoPoint(lonSum / pts.size(), latSum / pts.size());
    }
}
