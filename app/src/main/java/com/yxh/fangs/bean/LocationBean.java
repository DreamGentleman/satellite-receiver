package com.yxh.fangs.bean;

public class LocationBean {
    private double latitude;
    private double longitude;
    private String ic;

    public LocationBean(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationBean(double latitude, double longitude, String ic) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ic = ic;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getIc() {
        return ic == null ? "" : ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }
}
