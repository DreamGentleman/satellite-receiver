package com.yxh.fangs.bean;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {
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
    /**
     * 主键
     *
     * @PrimaryKey(autoGenerate = true)自动生成主主键，默认为false
     */
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "message_uid")
    @NonNull
    private String uid;
    @ColumnInfo(name = "message_type")
    private String type;
    @ColumnInfo(name = "message_content")
    private String content;
    @ColumnInfo(name = "message_time")
    private String time;

    public Message() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
