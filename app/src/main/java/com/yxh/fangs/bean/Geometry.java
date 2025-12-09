package com.yxh.fangs.bean;

import java.util.List;

public class Geometry {
    private String type;
    private Object coordinates; // 可以是 Polygon 或 MultiPolygon

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Object coordinates) {
        this.coordinates = coordinates;
    }
}
