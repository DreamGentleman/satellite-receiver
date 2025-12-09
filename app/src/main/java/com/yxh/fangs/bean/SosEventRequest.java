package com.yxh.fangs.bean;

public class SosEventRequest {

    private String deviceSn;
    private String longitude;
    private String latitude;
    private String emergencyLevel;
    private String description;
    private String remark;

    public SosEventRequest() {
    }

    public SosEventRequest(String deviceSn, String longitude, String latitude, String emergencyLevel, String description, String remark) {
        this.deviceSn = deviceSn;
        this.longitude = longitude;
        this.latitude = latitude;
        this.emergencyLevel = emergencyLevel;
        this.description = description;
        this.remark = remark;
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

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "SosEventRequest{" +
                "deviceSn='" + deviceSn + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", emergencyLevel='" + emergencyLevel + '\'' +
                ", description='" + description + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
