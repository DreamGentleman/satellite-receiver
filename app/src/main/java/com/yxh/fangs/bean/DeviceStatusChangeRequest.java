package com.yxh.fangs.bean;

public class DeviceStatusChangeRequest {

    private String deviceSn;
    private String newStatus;
    private String changeReason;
    private String repairProgress;
    private String repairStartTime;
    private String repairEndTime;
    private String remark;

    public DeviceStatusChangeRequest() {
    }

    public DeviceStatusChangeRequest(String deviceSn, String newStatus, String changeReason, String repairProgress, String repairStartTime, String repairEndTime, String remark) {
        this.deviceSn = deviceSn;
        this.newStatus = newStatus;
        this.changeReason = changeReason;
        this.repairProgress = repairProgress;
        this.repairStartTime = repairStartTime;
        this.repairEndTime = repairEndTime;
        this.remark = remark;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getRepairProgress() {
        return repairProgress;
    }

    public void setRepairProgress(String repairProgress) {
        this.repairProgress = repairProgress;
    }

    public String getRepairStartTime() {
        return repairStartTime;
    }

    public void setRepairStartTime(String repairStartTime) {
        this.repairStartTime = repairStartTime;
    }

    public String getRepairEndTime() {
        return repairEndTime;
    }

    public void setRepairEndTime(String repairEndTime) {
        this.repairEndTime = repairEndTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "DeviceStatusChangeRequest{" +
                "deviceSn='" + deviceSn + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changeReason='" + changeReason + '\'' +
                ", repairProgress='" + repairProgress + '\'' +
                ", repairStartTime='" + repairStartTime + '\'' +
                ", repairEndTime='" + repairEndTime + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
