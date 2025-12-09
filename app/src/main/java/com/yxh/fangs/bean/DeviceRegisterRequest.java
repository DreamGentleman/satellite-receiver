package com.yxh.fangs.bean;

public class DeviceRegisterRequest {

    private String deviceSn;
    private String deviceName;
    private String deviceModel;
    private String longitude;
    private String latitude;
    private String address;
    private String phonenumber;

    public DeviceRegisterRequest() {
    }

    public DeviceRegisterRequest(String deviceSn, String deviceName, String deviceModel, String longitude, String latitude, String address, String phonenumber) {
        this.deviceSn = deviceSn;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.phonenumber = phonenumber;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    @Override
    public String toString() {
        return "DeviceRegisterRequest{" +
                "deviceSn='" + deviceSn + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", address='" + address + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                '}';
    }
}
