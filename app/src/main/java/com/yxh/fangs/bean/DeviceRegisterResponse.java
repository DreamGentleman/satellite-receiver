package com.yxh.fangs.bean;

public class DeviceRegisterResponse {

    /**
     * code : 200
     * msg : 操作成功
     * data : {"id":"2000038207204659202","deviceSn":"deviceSn_cs4","deviceName":"deviceName_cs","deviceModel":"deviceModel_cs","status":"0","installTime":"2025-12-14 11:00:42","licenseId":"2000038040380411906","licenseKey":"MjAyNS0xMi0yMCAwMDowMDowMA==","remark":null}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 2000038207204659202
         * deviceSn : deviceSn_cs4
         * deviceName : deviceName_cs
         * deviceModel : deviceModel_cs
         * status : 0
         * installTime : 2025-12-14 11:00:42
         * licenseId : 2000038040380411906
         * licenseKey : MjAyNS0xMi0yMCAwMDowMDowMA==
         * remark : null
         */

        private String id;
        private String deviceSn;
        private String deviceName;
        private String deviceModel;
        private String status;
        private String installTime;
        private String licenseId;
        private String licenseKey;
        private Object remark;

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public String getStatus() {
            return status == null ? "" : status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getInstallTime() {
            return installTime == null ? "" : installTime;
        }

        public void setInstallTime(String installTime) {
            this.installTime = installTime;
        }

        public String getLicenseId() {
            return licenseId == null ? "" : licenseId;
        }

        public void setLicenseId(String licenseId) {
            this.licenseId = licenseId;
        }

        public String getLicenseKey() {
            return licenseKey == null ? "" : licenseKey;
        }

        public void setLicenseKey(String licenseKey) {
            this.licenseKey = licenseKey;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }
    }
}
