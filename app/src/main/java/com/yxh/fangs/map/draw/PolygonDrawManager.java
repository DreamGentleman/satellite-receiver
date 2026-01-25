package com.yxh.fangs.map.draw;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;

import java.util.List;

public class PolygonDrawManager {

    private final EarthFragment earth;

    public PolygonDrawManager(EarthFragment earth) {
        this.earth = earth;
    }

    public long draw(String name, List<GeoPoint> pts, String fill, float stroke) {
        long root = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(root, name, true);
        VectorElement poly = new VectorElement(layer.id, VectorElement.TYPE_PLANE, name);
        poly.geoPoints.addAll(pts);
        poly.outlineColor = String.valueOf(stroke);
        poly.attribute = "fillColor:" + fill;
        return earth.drawElement(poly, true);
    }
}
