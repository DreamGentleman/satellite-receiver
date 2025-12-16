package com.yxh.fangs.bean;

public class WeatherBean {

    /**
     * forecastPeriod : 00-24
     * seaArea : 渤海
     * longitudeRange : 121.3357-124.6281
     * latitudeRange : 37.1206-39.7005
     * weatherPhenomenon : 小雨
     * windDirection : 北风
     * windForce : 2
     * waveHeight : 1.5
     * visibility : 5
     * remark : 描述
     * riskLevel : 1
     * mapJson : {"type":"circle","coordinates":{"center":{"lng":122.98188704138916,"lat":38.41055825094609},"radius":143596.56415988095,"area":64779556443.36138,"perimeter":902243.8220908347,"color":"#1890ff","fillOpacity":0.2,"bounds":{"sw":{"lng":121.33566628555967,"lat":37.120614268194736},"ne":{"lng":124.62810779721865,"lat":39.70050223369744}}}}
     */

    private String forecastPeriod;
    private String seaArea;
    private String longitudeRange;
    private String latitudeRange;
    private String weatherPhenomenon;
    private String windDirection;
    private String windForce;
    private String waveHeight;
    private String visibility;
    private String remark;
    private String riskLevel;
    private String mapJson;

    public String getForecastPeriod() {
        return forecastPeriod;
    }

    public void setForecastPeriod(String forecastPeriod) {
        this.forecastPeriod = forecastPeriod;
    }

    public String getSeaArea() {
        return seaArea;
    }

    public void setSeaArea(String seaArea) {
        this.seaArea = seaArea;
    }

    public String getLongitudeRange() {
        return longitudeRange;
    }

    public void setLongitudeRange(String longitudeRange) {
        this.longitudeRange = longitudeRange;
    }

    public String getLatitudeRange() {
        return latitudeRange;
    }

    public void setLatitudeRange(String latitudeRange) {
        this.latitudeRange = latitudeRange;
    }

    public String getWeatherPhenomenon() {
        return weatherPhenomenon;
    }

    public void setWeatherPhenomenon(String weatherPhenomenon) {
        this.weatherPhenomenon = weatherPhenomenon;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindForce() {
        return windForce;
    }

    public void setWindForce(String windForce) {
        this.windForce = windForce;
    }

    public String getWaveHeight() {
        return waveHeight;
    }

    public void setWaveHeight(String waveHeight) {
        this.waveHeight = waveHeight;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getMapJson() {
        return mapJson;
    }

    public void setMapJson(String mapJson) {
        this.mapJson = mapJson;
    }
}
