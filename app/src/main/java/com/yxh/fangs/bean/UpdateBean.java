package com.yxh.fangs.bean;

import java.io.Serializable;

public class UpdateBean implements Serializable {

    /**
     * code : 200
     * msg : 操作成功
     * data : {"id":"1996120951234359297","versionCode":"1.0.1","versionName":"气象","updateContent":"000","forceUpdate":"0","packageUrl":"1996120686968041473","packageSize":0,"publishTime":"2025-12-03 15:34:56","remark":null,"url":"http://47.118.24.57:9001/ysj/2025/12/03/485d208e621345ccb919a4a98a031a12.apk"}
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
         * id : 1996120951234359297
         * versionCode : 1.0.1
         * versionName : 气象
         * updateContent : 000
         * forceUpdate : 0
         * packageUrl : 1996120686968041473
         * packageSize : 0
         * publishTime : 2025-12-03 15:34:56
         * remark : null
         * url : http://47.118.24.57:9001/ysj/2025/12/03/485d208e621345ccb919a4a98a031a12.apk
         */

        private String id;
        private int versionCode;
        private String versionName;
        private String updateContent;
        private String forceUpdate;
        private String packageUrl;
        private int packageSize;
        private String publishTime;
        private Object remark;
        private String url;

        public String getId() {
            return id == null ? "" : id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName == null ? "" : versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getUpdateContent() {
            return updateContent == null ? "" : updateContent;
        }

        public void setUpdateContent(String updateContent) {
            this.updateContent = updateContent;
        }

        public String getForceUpdate() {
            return forceUpdate == null ? "" : forceUpdate;
        }

        public void setForceUpdate(String forceUpdate) {
            this.forceUpdate = forceUpdate;
        }

        public String getPackageUrl() {
            return packageUrl == null ? "" : packageUrl;
        }

        public void setPackageUrl(String packageUrl) {
            this.packageUrl = packageUrl;
        }

        public int getPackageSize() {
            return packageSize;
        }

        public void setPackageSize(int packageSize) {
            this.packageSize = packageSize;
        }

        public String getPublishTime() {
            return publishTime == null ? "" : publishTime;
        }

        public void setPublishTime(String publishTime) {
            this.publishTime = publishTime;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }

        public String getUrl() {
            return url == null ? "" : url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
