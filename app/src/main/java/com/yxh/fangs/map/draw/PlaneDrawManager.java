package com.yxh.fangs.map.draw;

import android.text.TextUtils;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;
import com.yxh.fangs.util.LocationUtils;

import java.util.ArrayList;
import java.util.List;

public class PlaneDrawManager {
    private final EarthFragment earth;
    private final TextDrawManager textDrawManager;

    public PlaneDrawManager(EarthFragment earth) {
        this.earth = earth;
        textDrawManager = new TextDrawManager(earth);
    }

    public List<Long> draw(String name, List<GeoPoint> pts, String fillColor, String lineColor, float lineWith) {
        ArrayList<Long> elementIds = new ArrayList<>();
        long root = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(root, name, true);
        VectorElement poly = new VectorElement(layer.id, VectorElement.TYPE_PLANE, name);
        poly.outlineWidth = String.valueOf(lineWith > 0 ? lineWith : 2f);
        poly.outlineColor = TextUtils.isEmpty(lineColor) ? "#FFFFFFFF" : lineColor;
        poly.geoPoints.addAll(pts);
        // 确保闭合
        if (!pts.get(0).equals(pts.get(pts.size() - 1))) {
            poly.geoPoints.add(pts.get(0));
        }
        elementIds.add(earth.drawElement(poly, true));
        if (!TextUtils.isEmpty(name)) {
            GeoPoint center = LocationUtils.computePolygonCentroid(pts);
            long labelId = textDrawManager.draw(name, "#FFFFFFFF", center);
            if (labelId > 0) {
                elementIds.add(labelId);
            }
        }
        return elementIds;
    }
}
