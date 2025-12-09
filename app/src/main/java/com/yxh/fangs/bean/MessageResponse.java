package com.yxh.fangs.bean;

import java.util.List;

public class MessageResponse {

    private int code;
    private String msg;
    private List<MessageItem> data;

    public MessageResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<MessageItem> getData() {
        return data;
    }

    public void setData(List<MessageItem> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    // ----------------------------
    //  内部类：消息项（MessageItem）
    // ----------------------------
    public static class MessageItem {

        private String id;
        private String messageId;
        private String title;
        private String content;
        private String priority;
        private String sendTime;
        private String targetType;
        private String deviceId;
        private String deviceSn;
        private String deviceName;
        private String deviceModel;
        private String status;
        private String readTime;
        private String isStarred;

        public MessageItem() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getSendTime() {
            return sendTime;
        }

        public void setSendTime(String sendTime) {
            this.sendTime = sendTime;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReadTime() {
            return readTime;
        }

        public void setReadTime(String readTime) {
            this.readTime = readTime;
        }

        public String getIsStarred() {
            return isStarred;
        }

        public void setIsStarred(String isStarred) {
            this.isStarred = isStarred;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MessageItem that = (MessageItem) o;

            // ⭐ 只根据 id 判断是否相同
            return id != null && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "MessageItem{" +
                    "id='" + id + '\'' +
                    ", messageId='" + messageId + '\'' +
                    ", title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", priority='" + priority + '\'' +
                    ", sendTime='" + sendTime + '\'' +
                    ", targetType='" + targetType + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    ", deviceSn='" + deviceSn + '\'' +
                    ", deviceName='" + deviceName + '\'' +
                    ", deviceModel='" + deviceModel + '\'' +
                    ", status='" + status + '\'' +
                    ", readTime='" + readTime + '\'' +
                    ", isStarred='" + isStarred + '\'' +
                    '}';
        }
    }
}
