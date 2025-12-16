package com.yxh.fangs.bean;

public class KmlStyle {
    String lineColor;   // Android 格式 #AARRGGBB
    float lineWidth;
    String fillColor;

    public String getLineColor() {
        return lineColor == null ? "" : lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public String getFillColor() {
        return fillColor == null ? "" : fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }
}
