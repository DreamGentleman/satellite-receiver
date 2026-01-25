package com.yxh.fangs.map.draw;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;

public class PointDrawManager {

    private final EarthFragment earth;

    public PointDrawManager(EarthFragment earth) {
        this.earth = earth;
    }

    public long draw(double lon, double lat, byte[] icon) {
        VectorElement point = new VectorElement(
                earth.getRootLayerId(),
                VectorElement.TYPE_POINT,
                "point"
        );
        point.image = icon;
        point.geoPoints.add(new GeoPoint(lon, lat));
        return earth.drawElement(point, true);
    }
}
