package com.yxh.fangs.bean;

public class NoticeBean {
    //北斗定位消息
    public final static int NOTICE_BEIDOU = 1;
    //警告消息
    public final static int NOTICE_ALERT = 2;
    //图片消息
    public final static int NOTICE_NOTICE_IMAGE = 3;
    //短消息
    public final static int NOTICE_SMS = 4;
    //台风消息
    public final static int NOTICE_TYPHOON = 5;
    //天气消息
    public final static int NOTICE_WEATHER = 6;
    private int noticeType;
    private String noticeTitle;
    private String noticeTime;

    public NoticeBean(int noticeType, String noticeTitle, String noticeTime) {
        this.noticeType = noticeType;
        this.noticeTitle = noticeTitle;
        this.noticeTime = noticeTime;
    }

    public int getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(int noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeTitle() {
        return noticeTitle == null ? "" : noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeTime() {
        return noticeTime == null ? "" : noticeTime;
    }

    public void setNoticeTime(String noticeTime) {
        this.noticeTime = noticeTime;
    }
}
