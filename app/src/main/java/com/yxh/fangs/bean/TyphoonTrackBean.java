package com.yxh.fangs.bean;

import java.util.List;

public class TyphoonTrackBean {

    /**
     * typhoonName : Typhoon Mangkhut
     * latitude : 37.411125
     * longitude : 123.200513
     * maximumWind : 14
     * movingSpeed : 100
     * movingDirection : Point1: lng 118.774836, lat 35.675147 -> Point2: lng 125.188223, lat 35.995785 -> Point3: lng 127.626189, lat 39.147103
     * windCircles : [{"id":"wind-circle-1767085923233-0","radius":100,"color":"#ff4d4f","description":"Gale-force Wind Radius (Level 7)","strokeWidth":2,"strokeOpacity":0.8,"fillOpacity":0.1,"pathPointIndex":0,"centerLat":35.67514743608467,"centerLng":118.77483613273253,"expectedTime":"11:00"},{"id":"wind-circle-1767085925526-1","radius":100,"color":"#ff4d4f","description":"Gale-force Wind Radius (Level 7)","strokeWidth":2,"strokeOpacity":0.8,"fillOpacity":0.1,"pathPointIndex":1,"centerLat":35.995785386420344,"centerLng":125.18822332036054,"expectedTime":"12:00"},{"id":"wind-circle-1767085926965-2","radius":100,"color":"#ff4d4f","description":"Gale-force Wind Radius (Level 7)","strokeWidth":2,"strokeOpacity":0.8,"fillOpacity":0.1,"pathPointIndex":2,"centerLat":39.14710270770074,"centerLng":127.62618899784928,"expectedTime":"13:00"}]
     * typhoonInfo : {"mapData":{"type":"polyline","coordinates":{"points":[{"lng":118.77483613273253,"lat":35.67514743608467},{"lng":125.18822332036054,"lat":35.995785386420344},{"lng":127.62618899784928,"lat":39.14710270770074}]}},"drawTime":"2025-12-30T09:12:39.554Z"}
     */

    private String typhoonName;
    private String latitude;
    private String longitude;
    private int maximumWind;
    private int movingSpeed;
    private String movingDirection;
    private TyphoonInfoBean typhoonInfo;
    private List<WindCirclesBean> windCircles;

    public String getTyphoonName() {
        return typhoonName;
    }

    public void setTyphoonName(String typhoonName) {
        this.typhoonName = typhoonName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getMaximumWind() {
        return maximumWind;
    }

    public void setMaximumWind(int maximumWind) {
        this.maximumWind = maximumWind;
    }

    public int getMovingSpeed() {
        return movingSpeed;
    }

    public void setMovingSpeed(int movingSpeed) {
        this.movingSpeed = movingSpeed;
    }

    public String getMovingDirection() {
        return movingDirection;
    }

    public void setMovingDirection(String movingDirection) {
        this.movingDirection = movingDirection;
    }

    public TyphoonInfoBean getTyphoonInfo() {
        return typhoonInfo;
    }

    public void setTyphoonInfo(TyphoonInfoBean typhoonInfo) {
        this.typhoonInfo = typhoonInfo;
    }

    public List<WindCirclesBean> getWindCircles() {
        return windCircles;
    }

    public void setWindCircles(List<WindCirclesBean> windCircles) {
        this.windCircles = windCircles;
    }

    public static class TyphoonInfoBean {
        /**
         * mapData : {"type":"polyline","coordinates":{"points":[{"lng":118.77483613273253,"lat":35.67514743608467},{"lng":125.18822332036054,"lat":35.995785386420344},{"lng":127.62618899784928,"lat":39.14710270770074}]}}
         * drawTime : 2025-12-30T09:12:39.554Z
         */

        private MapDataBean mapData;
        private String drawTime;

        public MapDataBean getMapData() {
            return mapData;
        }

        public void setMapData(MapDataBean mapData) {
            this.mapData = mapData;
        }

        public String getDrawTime() {
            return drawTime;
        }

        public void setDrawTime(String drawTime) {
            this.drawTime = drawTime;
        }

        public static class MapDataBean {
            /**
             * type : polyline
             * coordinates : {"points":[{"lng":118.77483613273253,"lat":35.67514743608467},{"lng":125.18822332036054,"lat":35.995785386420344},{"lng":127.62618899784928,"lat":39.14710270770074}]}
             */

            private String type;
            private CoordinatesBean coordinates;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public CoordinatesBean getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(CoordinatesBean coordinates) {
                this.coordinates = coordinates;
            }

            public static class CoordinatesBean {
                private List<PointsBean> points;

                public List<PointsBean> getPoints() {
                    return points;
                }

                public void setPoints(List<PointsBean> points) {
                    this.points = points;
                }

                public static class PointsBean {
                    /**
                     * lng : 118.77483613273253
                     * lat : 35.67514743608467
                     */

                    private double lng;
                    private double lat;

                    public double getLng() {
                        return lng;
                    }

                    public void setLng(double lng) {
                        this.lng = lng;
                    }

                    public double getLat() {
                        return lat;
                    }

                    public void setLat(double lat) {
                        this.lat = lat;
                    }
                }
            }
        }
    }

    public static class WindCirclesBean {
        /**
         * id : wind-circle-1767085923233-0
         * radius : 100
         * color : #ff4d4f
         * description : Gale-force Wind Radius (Level 7)
         * strokeWidth : 2
         * strokeOpacity : 0.8
         * fillOpacity : 0.1
         * pathPointIndex : 0
         * centerLat : 35.67514743608467
         * centerLng : 118.77483613273253
         * expectedTime : 11:00
         */

        private String id;
        private int radius;
        private String color;
        private String description;
        private int strokeWidth;
        private double strokeOpacity;
        private double fillOpacity;
        private int pathPointIndex;
        private double centerLat;
        private double centerLng;
        private String expectedTime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getStrokeWidth() {
            return strokeWidth;
        }

        public void setStrokeWidth(int strokeWidth) {
            this.strokeWidth = strokeWidth;
        }

        public double getStrokeOpacity() {
            return strokeOpacity;
        }

        public void setStrokeOpacity(double strokeOpacity) {
            this.strokeOpacity = strokeOpacity;
        }

        public double getFillOpacity() {
            return fillOpacity;
        }

        public void setFillOpacity(double fillOpacity) {
            this.fillOpacity = fillOpacity;
        }

        public int getPathPointIndex() {
            return pathPointIndex;
        }

        public void setPathPointIndex(int pathPointIndex) {
            this.pathPointIndex = pathPointIndex;
        }

        public double getCenterLat() {
            return centerLat;
        }

        public void setCenterLat(double centerLat) {
            this.centerLat = centerLat;
        }

        public double getCenterLng() {
            return centerLng;
        }

        public void setCenterLng(double centerLng) {
            this.centerLng = centerLng;
        }

        public String getExpectedTime() {
            return expectedTime;
        }

        public void setExpectedTime(String expectedTime) {
            this.expectedTime = expectedTime;
        }
    }
}
