package com.yxh.fangs.bean;

public class DeviceRegisterRequest {

    /**
     * deviceSn : deviceSn_cs4
     * deviceName : deviceName_cs
     * deviceModel : deviceModel_cs
     * licenseKey : MjAyNS0xMi0yMCAwMDowMDowMA==
     */

    private String deviceSn;
    private String deviceName;
    private String deviceModel;
    private String licenseKey;

    public String getDeviceSn() {
        return deviceSn == null ? "" : deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceName() {
        return deviceName == null ? "" : deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel == null ? "" : deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getLicenseKey() {
        return licenseKey == null ? "" : licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }
}
