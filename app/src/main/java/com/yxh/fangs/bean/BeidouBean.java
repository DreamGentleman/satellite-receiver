package com.yxh.fangs.bean;

public class BeidouBean {

    /**
     * beidouChannel : 测试A-1
     * satelliteId : 测试A-1
     * signalStrength : 1
     */

    private String beidouChannel;
    private String satelliteId;
    private int signalStrength;

    public String getBeidouChannel() {
        return beidouChannel;
    }

    public void setBeidouChannel(String beidouChannel) {
        this.beidouChannel = beidouChannel;
    }

    public String getSatelliteId() {
        return satelliteId;
    }

    public void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }
}
