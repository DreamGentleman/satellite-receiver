package com.yxh.fangs.bean;

public class SosEventRequest {

    /**
     * version : 1.0
     * sendTime : 2025-12-12T10:30:00z
     * deviceSn : deviceSn_cs4
     * vesselInfo : {"mmsiCode":"qx-wgeqwrgrg444","vesselName":"闽油66889"}
     * position : {"latitude":"33.3617","longitude":"126.5292"}
     * distressType : 测试报警
     * crewNumber : 5
     * contact : VHF16频道/138xxxX8888
     */

    private String version;
    private String sendTime;
    private String deviceSn;
    private VesselInfoBean vesselInfo;
    private PositionBean position;
    private String distressType;
    private int crewNumber;
    private String contact;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public VesselInfoBean getVesselInfo() {
        return vesselInfo;
    }

    public void setVesselInfo(VesselInfoBean vesselInfo) {
        this.vesselInfo = vesselInfo;
    }

    public PositionBean getPosition() {
        return position;
    }

    public void setPosition(PositionBean position) {
        this.position = position;
    }

    public String getDistressType() {
        return distressType;
    }

    public void setDistressType(String distressType) {
        this.distressType = distressType;
    }

    public int getCrewNumber() {
        return crewNumber;
    }

    public void setCrewNumber(int crewNumber) {
        this.crewNumber = crewNumber;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public static class VesselInfoBean {
        /**
         * mmsiCode : qx-wgeqwrgrg444
         * vesselName : 闽油66889
         */

        private String mmsiCode;
        private String vesselName;

        public String getMmsiCode() {
            return mmsiCode;
        }

        public void setMmsiCode(String mmsiCode) {
            this.mmsiCode = mmsiCode;
        }

        public String getVesselName() {
            return vesselName;
        }

        public void setVesselName(String vesselName) {
            this.vesselName = vesselName;
        }
    }

    public static class PositionBean {
        /**
         * latitude : 33.3617
         * longitude : 126.5292
         */

        private String latitude;
        private String longitude;

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }
}
