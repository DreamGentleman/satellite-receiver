package com.yxh.fangs.bean;

public class KmlStyle {
    private String lineColor;
    private float lineWidth;
    private String fillColor;
    private boolean fill = true;

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

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }
}
