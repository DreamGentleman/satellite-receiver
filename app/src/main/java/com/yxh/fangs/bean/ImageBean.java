package com.yxh.fangs.bean;

public class ImageBean {

    /**
     * base64 : data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAxYAAAOICA......
     * info : {"size":77290,"width":790,"height":904,"description":"测试A-3","fileName":"cbd4d622-b68e-4443-a73a-2036327134d2.png","fileType":"image/png"}
     */

    private String base64;
    private InfoBean info;

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public static class InfoBean {
        /**
         * size : 77290
         * width : 790
         * height : 904
         * description : 测试A-3
         * fileName : cbd4d622-b68e-4443-a73a-2036327134d2.png
         * fileType : image/png
         */

        private int size;
        private int width;
        private int height;
        private String description;
        private String fileName;
        private String fileType;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
    }
}
