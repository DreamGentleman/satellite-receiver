package com.yxh.fangs.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Last24HoursBean {

    private int total;
    private int code;
    private String msg;
    private List<RowsBean> rows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

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

    public List<RowsBean> getRows() {
        return rows == null ? new ArrayList<>() : rows;
    }

    public List<RowsBean> getSortRow() {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        // 拷贝一份，避免影响外部原始数据
        List<RowsBean> sorted = new ArrayList<>(rows);

        // publishTime 示例：2025-12-15 10:44:48
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Collections.sort(sorted, new Comparator<RowsBean>() {
            @Override
            public int compare(RowsBean o1, RowsBean o2) {
                long t1 = parseTimeSafe(o1 == null ? null : o1.getPublishTime(), sdf);
                long t2 = parseTimeSafe(o2 == null ? null : o2.getPublishTime(), sdf);

                // 倒序：最新在前
                return Long.compare(t2, t1);
            }
        });

        return sorted;
    }

    public void setRows(List<RowsBean> rows) {
        this.rows = rows;
    }

    private long parseTimeSafe(String timeStr, SimpleDateFormat sdf) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0L;
        try {
            Date d = sdf.parse(timeStr.trim());
            return d == null ? 0L : d.getTime();
        } catch (Exception e) {
            return 0L;
        }
    }

    public static class RowsBean {

        private String id;
        private String messageType;
        private String title;
        private String content;
        private String forecastPeriod;
        private String publishUnit;
        private String publishTime;
        private String level;
        private Object forecastList;

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessageType() {
            return messageType == null ? "" : messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content == null ? "" : content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getForecastPeriod() {
            return forecastPeriod == null ? "" : forecastPeriod;
        }

        public void setForecastPeriod(String forecastPeriod) {
            this.forecastPeriod = forecastPeriod;
        }

        public String getPublishUnit() {
            return publishUnit == null ? "" : publishUnit;
        }

        public void setPublishUnit(String publishUnit) {
            this.publishUnit = publishUnit;
        }

        public String getPublishTime() {
            return publishTime == null ? "" : publishTime;
        }

        public void setPublishTime(String publishTime) {
            this.publishTime = publishTime;
        }

        public String getLevel() {
            return level == null ? "" : level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Object getForecastList() {
            return forecastList;
        }

        public void setForecastList(Object forecastList) {
            this.forecastList = forecastList;
        }
    }
}
