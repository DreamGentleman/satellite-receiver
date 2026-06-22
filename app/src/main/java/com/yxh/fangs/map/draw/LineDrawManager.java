package com.yxh.fangs.map.draw;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.constant.Constants;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;

import java.util.ArrayList;
import java.util.List;

public class LineDrawManager {

    private final EarthFragment earthFragment;

    public LineDrawManager(EarthFragment earthFragment) {
        this.earthFragment = earthFragment;
    }

    /**
     * 画普通线
     */
    public long drawLine(String name, List<GeoPoint> points, String color, float width) {
        long root = earthFragment.getRootLayerId();
        VectorElement layer = earthFragment.onCreateLayer(root, name, true);

        VectorElement line = new VectorElement(
                layer.id,
                VectorElement.TYPE_LINE,
                name
        );

        line.geoPoints.addAll(points);
        line.outlineColor = isEmpty(color) ? "#FFFFFFFF" : color;
        line.outlineWidth = String.valueOf(width > 0 ? width : 2f);
        line.showLabel = false;

        return earthFragment.drawElement(line, true);
    }

    /**
     * 画带文字标注的线（KML 常用）
     */
    public List<Long> drawLineWithLabel(String name, List<GeoPoint> points, String color, float width) {
        List<Long> ids = new ArrayList<>();
        long lineId = drawLine(name, points, color, width);
        if (lineId > 0) {
            ids.add(lineId);
        }

        if (name != null && !name.isEmpty()) {
            GeoPoint center = computeLineCenter(points);
            long labelId = drawTextLabel(name, center);
            if (labelId > 0) {
                ids.add(labelId);
            }
        }

        return ids;
    }

    /**
     * 计算线的中点
     */
    private GeoPoint computeLineCenter(List<GeoPoint> pts) {
        if (pts == null || pts.isEmpty()) return null;
        return pts.get(pts.size() / 2);
    }

    /**
     * 画线标签
     */
    private long drawTextLabel(String text, GeoPoint center) {
        if (center == null) return -1;

        long root = earthFragment.getRootLayerId();
        VectorElement layer = earthFragment.onCreateLayer(
                root,
                "LINE_LABEL_" + text,
                true
        );

        VectorElement label = new VectorElement(
                layer.id,
                VectorElement.TYPE_POINT,
                text
        );

        label.showIcon = false;
        label.showLabel = true;
        label.labelColor = "#FFFFFFFF";
        label.labelSize = "10";
        label.labelAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;
        label.description = text;
        label.geoPoints.add(center);

        return earthFragment.drawElement(label, true);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
