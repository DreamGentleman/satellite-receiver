package com.yxh.fangs.bean;

public class MessageBean {
    private String acceptTime;
    private String message;

    public MessageBean(String acceptTime, String message) {
        this.acceptTime = acceptTime;
        this.message = message;
    }

    public String getAcceptTime() {
        return acceptTime == null ? "" : acceptTime;
    }

    public void setAcceptTime(String acceptTime) {
        this.acceptTime = acceptTime;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
