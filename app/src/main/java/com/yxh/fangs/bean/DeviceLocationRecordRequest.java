package com.yxh.fangs.bean;

public class DeviceLocationRecordRequest {

    private String deviceSn;
    private String longitude;
    private String latitude;
    private String address;
    private int speed;
    private int direction;
    private int altitude;

    public DeviceLocationRecordRequest() {
    }

    public DeviceLocationRecordRequest(String deviceSn, String longitude, String latitude, String address, int speed, int direction, int altitude) {
        this.deviceSn = deviceSn;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.speed = speed;
        this.direction = direction;
        this.altitude = altitude;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return "DeviceLocationRecordRequest{" +
                "deviceSn='" + deviceSn + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", address='" + address + '\'' +
                ", speed=" + speed +
                ", direction=" + direction +
                ", altitude=" + altitude +
                '}';
    }
}
