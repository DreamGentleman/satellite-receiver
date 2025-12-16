package com.yxh.fangs.bean;

import java.util.ArrayList;
import java.util.List;

public class Last24HoursBean {

    /**
     * total : 9
     * rows : [{"id":"2000396595019431938","messageType":"WARNING","title":"测试QQ","content":"{\"warningLevel\":\"蓝色预警\",\"warningEndTime\":\"2025-12-15 10:44:38\",\"warningArea\":{\"type\":\"rectangle\",\"coordinates\":{\"bounds\":{\"sw\":{\"lng\":121.00344074298467,\"lat\":36.03133177633189},\"ne\":{\"lng\":123.81723103404882,\"lat\":38.51378825951165}},\"center\":{\"lng\":122.41033588851674,\"lat\":37.27256001792177},\"area\":68881697390.8502,\"perimeter\":1051209.9882315786,\"color\":\"#52c41a\",\"fillOpacity\":0.2}},\"drawTime\":\"2025-12-15T02:44:39.614Z\"}","forecastPeriod":"00-24","publishUnit":"测试QQ","publishTime":"2025-12-15 10:44:48","forecastList":null},{"id":"2000396105686761473","messageType":"WARNING","title":"测试bb","content":"{\"warningLevel\":\"红色预警\",\"warningEndTime\":\"2025-12-15 10:42:40\",\"warningArea\":{\"type\":\"marker\",\"coordinates\":{\"position\":{\"lng\":123.0698179879849,\"lat\":37.96152331396614}}},\"drawTime\":\"2025-12-15T02:42:41.175Z\"}","forecastPeriod":"00-24","publishUnit":"测试bb","publishTime":"2025-12-15 10:42:52","forecastList":null},{"id":"2000395954075254785","messageType":"WARNING","title":"测试标题22","content":"{\"warningLevel\":\"黄色预警\",\"warningEndTime\":\"2025-12-15 10:42:02\",\"warningArea\":{\"type\":\"circle\",\"coordinates\":{\"center\":{\"lng\":120.21206222362284,\"lat\":38.993572058209466},\"radius\":187985.16358143164,\"area\":111018926086.17682,\"perimeter\":1181145.6177826023,\"color\":\"#1890ff\",\"fillOpacity\":0.2,\"bounds\":{\"sw\":{\"lng\":118.03931936101144,\"lat\":37.304880326432325},\"ne\":{\"lng\":122.38480508623424,\"lat\":40.68226378998661}}}},\"drawTime\":\"2025-12-15T02:42:03.761Z\"}","forecastPeriod":"00-24","publishUnit":"测试标题22","publishTime":"2025-12-15 10:42:16","forecastList":null},{"id":"2000393841312055297","messageType":"METEOROLOGICAL","title":"测试标题AA","content":"[\n  {\n    \"forecastPeriod\": \"00-24\",\n    \"seaArea\": \"渤海\",\n    \"longitudeRange\": \"121.3357-124.6281\",\n    \"latitudeRange\": \"37.1206-39.7005\",\n    \"weatherPhenomenon\": \"小雨\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"2\",\n    \"waveHeight\": \"1.5\",\n    \"visibility\": \"5\",\n    \"remark\": \"描述\",\n    \"riskLevel\": \"1\",\n    \"mapJson\": \"{\\\"type\\\":\\\"circle\\\",\\\"coordinates\\\":{\\\"center\\\":{\\\"lng\\\":122.98188704138916,\\\"lat\\\":38.41055825094609},\\\"radius\\\":143596.56415988095,\\\"area\\\":64779556443.36138,\\\"perimeter\\\":902243.8220908347,\\\"color\\\":\\\"#1890ff\\\",\\\"fillOpacity\\\":0.2,\\\"bounds\\\":{\\\"sw\\\":{\\\"lng\\\":121.33566628555967,\\\"lat\\\":37.120614268194736},\\\"ne\\\":{\\\"lng\\\":124.62810779721865,\\\"lat\\\":39.70050223369744}}}}\"\n  },\n  {\n    \"forecastPeriod\": \"00-24\",\n    \"seaArea\": \"渤海\",\n    \"longitudeRange\": \"121.2233-124.7845\",\n    \"latitudeRange\": \"34.7416-38.2382\",\n    \"weatherPhenomenon\": \"晴\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"1\",\n    \"waveHeight\": \"1\",\n    \"visibility\": \"1\",\n    \"remark\": \"\",\n    \"riskLevel\": \"0\",\n    \"mapJson\": \"{\\\"type\\\":\\\"rectangle\\\",\\\"coordinates\\\":{\\\"bounds\\\":{\\\"sw\\\":{\\\"lng\\\":121.22326810947403,\\\"lat\\\":34.74161249883172},\\\"ne\\\":{\\\"lng\\\":124.78447144660217,\\\"lat\\\":38.23818011979866}},\\\"center\\\":{\\\"lng\\\":123.00386977803811,\\\"lat\\\":36.48989630931519},\\\"area\\\":124056773131.64145,\\\"perimeter\\\":1415909.9946927815,\\\"color\\\":\\\"#52c41a\\\",\\\"fillOpacity\\\":0.2}}\"\n  },\n  {\n    \"forecastPeriod\": \"00-24\",\n    \"seaArea\": \"渤海\",\n    \"longitudeRange\": \"120.1241-125.0043\",\n    \"latitudeRange\": \"34.0162-38.0654\",\n    \"weatherPhenomenon\": \"晴\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"1\",\n    \"waveHeight\": \"1\",\n    \"visibility\": \"1\",\n    \"remark\": \"描述\",\n    \"riskLevel\": \"0\",\n    \"mapJson\": \"{\\\"type\\\":\\\"polygon\\\",\\\"coordinates\\\":{\\\"points\\\":[{\\\"lng\\\":123.15774893458062,\\\"lat\\\":38.06539235133249},{\\\"lng\\\":125.00429881309155,\\\"lat\\\":34.016241889667036},{\\\"lng\\\":120.12413127702709,\\\"lat\\\":34.452218472826566},{\\\"lng\\\":120.43188959011222,\\\"lat\\\":36.421282443649496},{\\\"lng\\\":123.42154177436797,\\\"lat\\\":37.64903402157866}],\\\"bounds\\\":{\\\"sw\\\":{\\\"lng\\\":120.12413127702709,\\\"lat\\\":34.016241889667036},\\\"ne\\\":{\\\"lng\\\":125.00429881309155,\\\"lat\\\":38.06539235133249}},\\\"center\\\":{\\\"lng\\\":122.56421504505931,\\\"lat\\\":36.04081712049977},\\\"area\\\":137851706332.3343,\\\"perimeter\\\":1501938.2792401898,\\\"color\\\":\\\"#faad14\\\",\\\"fillOpacity\\\":0.2}}\"\n  },\n  {\n    \"forecastPeriod\": \"00-24\",\n    \"seaArea\": \"渤海\",\n    \"longitudeRange\": \"123.024853-123.026853\",\n    \"latitudeRange\": \"37.543577-37.545577\",\n    \"weatherPhenomenon\": \"晴\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"1\",\n    \"waveHeight\": \"1\",\n    \"visibility\": \"1\",\n    \"remark\": \"描述\",\n    \"riskLevel\": \"0\",\n    \"mapJson\": \"{\\\"type\\\":\\\"marker\\\",\\\"coordinates\\\":{\\\"position\\\":{\\\"lng\\\":123.02585251468703,\\\"lat\\\":37.54457732085584}}}\"\n  }\n]","forecastPeriod":"00-24","publishUnit":"测试标题AA","publishTime":"2025-12-15 10:33:52","forecastList":null},{"id":"2000359073258205186","messageType":"TYPHOON","title":"测试A-5","content":"{\"typhoonName\":\"山竹\",\"latitude\":\"34.899150\",\"longitude\":\"127.048693\",\"maximumWind\":1,\"movingSpeed\":1,\"movingDirection\":\"点1: 经度136.831011, 纬度39.198205 → 点2: 经度133.313773, 纬度37.335224 → 点3: 经度130.280156, 纬度34.669359 → 点4: 经度128.565502, 纬度30.902225 → 点5: 经度125.048264, 纬度30.600094 → 点6: 经度121.706888, 纬度32.398516 → 点7: 经度117.266376, 纬度34.633208\",\"typhoonInfo\":{\"mapData\":{\"type\":\"polyline\",\"coordinates\":{\"points\":[{\"lng\":136.83101113022065,\"lat\":39.19820534889482},{\"lng\":133.31377326639046,\"lat\":37.33522435930641},{\"lng\":130.2801556088369,\"lat\":34.66935854524545},{\"lng\":128.56550215021963,\"lat\":30.90222470517144},{\"lng\":125.04826428638943,\"lat\":30.600093873550072},{\"lng\":121.7068883157507,\"lat\":32.39851580247402},{\"lng\":117.26637551266504,\"lat\":34.63320791137959}],\"bounds\":{\"sw\":{\"lng\":117.26637551266504,\"lat\":30.600093873550072},\"ne\":{\"lng\":136.83101113022065,\"lat\":39.19820534889482}},\"center\":{\"lng\":127.04869332144284,\"lat\":34.89914961122244},\"length\":2414846.188797506,\"color\":\"#722ed1\"}},\"drawTime\":\"2025-12-15T00:15:29.635Z\"}}","forecastPeriod":"00-24","publishUnit":"测试A-5","publishTime":"2025-12-15 08:15:43","forecastList":null},{"id":"2000358829271347201","messageType":"TEXT_MESSAGE","title":"测试A-4","content":"测试A-4","forecastPeriod":"00-24","publishUnit":"测试A-4","publishTime":"2025-12-15 08:14:44","forecastList":null},{"id":"2000358679857655810","messageType":"WARNING","title":"测试A-2","content":"{\"warningLevel\":\"蓝色预警\",\"warningEndTime\":\"2025-12-15 08:13:40\",\"warningArea\":{\"type\":\"polygon\",\"coordinates\":{\"points\":[{\"lng\":120.78361337649527,\"lat\":38.58252615935333},{\"lng\":124.03705840053823,\"lat\":37.85750715625203},{\"lng\":122.98188704138916,\"lat\":36.10237644873644},{\"lng\":119.99223485713347,\"lat\":34.84987503195418},{\"lng\":118.80516707809076,\"lat\":36.4566360115962},{\"lng\":121.13533716287829,\"lat\":38.41055825094609}],\"bounds\":{\"sw\":{\"lng\":118.80516707809076,\"lat\":34.84987503195418},\"ne\":{\"lng\":124.03705840053823,\"lat\":38.58252615935333}},\"center\":{\"lng\":121.4211127393145,\"lat\":36.71620059565375},\"area\":131715319687.98071,\"perimeter\":1360003.9468804682,\"color\":\"#faad14\",\"fillOpacity\":0.2}},\"drawTime\":\"2025-12-15T00:13:43.384Z\"}","forecastPeriod":"00-24","publishUnit":"测试A-2","publishTime":"2025-12-15 08:14:09","forecastList":null},{"id":"2000358275354783746","messageType":"BEIDOU_BROADCAST","title":"测试A-1","content":"{\"beidouChannel\":\"测试A-1\",\"satelliteId\":\"测试A-1\",\"signalStrength\":1}","forecastPeriod":"00-24","publishUnit":"测试A-1","publishTime":"2025-12-15 08:12:32","forecastList":null},{"id":"2000357740048347137","messageType":"METEOROLOGICAL","title":"测试","content":"[\n  {\n    \"forecastPeriod\": \"11\",\n    \"seaArea\": \"11\",\n    \"longitudeRange\": \"116.4875-122.2659\",\n    \"latitudeRange\": \"36.8185-41.3052\",\n    \"weatherPhenomenon\": \"晴\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"11\",\n    \"waveHeight\": \"11\",\n    \"visibility\": \"11\",\n    \"remark\": \"11\",\n    \"riskLevel\": \"0\",\n    \"mapJson\": \"{\\\"type\\\":\\\"circle\\\",\\\"coordinates\\\":{\\\"center\\\":{\\\"lng\\\":119.37671823096315,\\\"lat\\\":39.06184913429154},\\\"radius\\\":249733.8694056181,\\\"area\\\":195931726394.10858,\\\"perimeter\\\":1569124.1789544853,\\\"color\\\":\\\"#1890ff\\\",\\\"fillOpacity\\\":0.2,\\\"bounds\\\":{\\\"sw\\\":{\\\"lng\\\":116.48749095113219,\\\"lat\\\":36.818461877683404},\\\"ne\\\":{\\\"lng\\\":122.26594551079411,\\\"lat\\\":41.30523639089968}}}}\"\n  },\n  {\n    \"forecastPeriod\": \"11\",\n    \"seaArea\": \"11\",\n    \"longitudeRange\": \"120.3879-122.5422\",\n    \"latitudeRange\": \"37.5794-39.8423\",\n    \"weatherPhenomenon\": \"晴\",\n    \"windDirection\": \"北风\",\n    \"windForce\": \"11\",\n    \"waveHeight\": \"11\",\n    \"visibility\": \"11\",\n    \"remark\": \"11\",\n    \"riskLevel\": \"0\",\n    \"mapJson\": \"{\\\"type\\\":\\\"rectangle\\\",\\\"coordinates\\\":{\\\"bounds\\\":{\\\"sw\\\":{\\\"lng\\\":120.38792411681435,\\\"lat\\\":37.57941251343841},\\\"ne\\\":{\\\"lng\\\":122.54223230841036,\\\"lat\\\":39.842286020743394}},\\\"center\\\":{\\\"lng\\\":121.46507821261235,\\\"lat\\\":38.71084926709091},\\\"area\\\":47139263404.465294,\\\"perimeter\\\":878071.2410691986,\\\"color\\\":\\\"#52c41a\\\",\\\"fillOpacity\\\":0.2}}\"\n  }\n]","forecastPeriod":"00-24","publishUnit":"测试","publishTime":"2025-12-15 08:10:25","forecastList":null}]
     * code : 200
     * msg : 查询成功
     */

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
        if (rows == null) {
            return new ArrayList<>();
        }
        return rows;
    }

    public void setRows(List<RowsBean> rows) {
        this.rows = rows;
    }

    public static class RowsBean {
        /**
         * id : 2000396595019431938
         * messageType : WARNING
         * title : 测试QQ
         * content : {"warningLevel":"蓝色预警","warningEndTime":"2025-12-15 10:44:38","warningArea":{"type":"rectangle","coordinates":{"bounds":{"sw":{"lng":121.00344074298467,"lat":36.03133177633189},"ne":{"lng":123.81723103404882,"lat":38.51378825951165}},"center":{"lng":122.41033588851674,"lat":37.27256001792177},"area":68881697390.8502,"perimeter":1051209.9882315786,"color":"#52c41a","fillOpacity":0.2}},"drawTime":"2025-12-15T02:44:39.614Z"}
         * forecastPeriod : 00-24
         * publishUnit : 测试QQ
         * publishTime : 2025-12-15 10:44:48
         * forecastList : null
         */

        private String id;
        private String messageType;
        private String title;
        private String content;
        private String forecastPeriod;
        private String publishUnit;
        private String publishTime;
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

        public Object getForecastList() {
            return forecastList;
        }

        public void setForecastList(Object forecastList) {
            this.forecastList = forecastList;
        }
    }
}
