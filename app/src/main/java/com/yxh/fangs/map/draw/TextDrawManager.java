package com.yxh.fangs.map.draw;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;

public class TextDrawManager {

    private final EarthFragment earth;

    public TextDrawManager(EarthFragment earth) {
        this.earth = earth;
    }

    public long draw(String text, String color, GeoPoint center) {
        if (center == null) return -1;
        long rootID = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(rootID, "LABEL_" + text, true);
        VectorElement label = new VectorElement(layer.id, VectorElement.TYPE_POINT, text);
        label.showLabel = true;
        label.description = text;
        label.labelColor = color; // 黑色文字
        label.geoPoints.add(center);
        return earth.drawElement(label, true);
    }
}
