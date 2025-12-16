package com.yxh.fangs.bean;

import java.util.List;

public class TyphoonBean {

    /**
     * typhoonName : 山竹
     * latitude : 34.899150
     * longitude : 127.048693
     * maximumWind : 1
     * movingSpeed : 1
     * movingDirection : 点1: 经度136.831011, 纬度39.198205 → 点2: 经度133.313773, 纬度37.335224 → 点3: 经度130.280156, 纬度34.669359 → 点4: 经度128.565502, 纬度30.902225 → 点5: 经度125.048264, 纬度30.600094 → 点6: 经度121.706888, 纬度32.398516 → 点7: 经度117.266376, 纬度34.633208
     * typhoonInfo : {"mapData":{"type":"polyline","coordinates":{"points":[{"lng":136.83101113022065,"lat":39.19820534889482},{"lng":133.31377326639046,"lat":37.33522435930641}],"bounds":{"sw":{"lng":117.26637551266504,"lat":30.600093873550072},"ne":{"lng":136.83101113022065,"lat":39.19820534889482}},"center":{"lng":127.04869332144284,"lat":34.89914961122244},"length":2414846.188797506,"color":"#722ed1"}},"drawTime":"2025-12-15T00:15:29.635Z"}
     */

    private String typhoonName;
    private String latitude;
    private String longitude;
    private int maximumWind;
    private int movingSpeed;
    private String movingDirection;
    private TyphoonInfoBean typhoonInfo;

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

    public static class TyphoonInfoBean {
        /**
         * mapData : {"type":"polyline","coordinates":{"points":[{"lng":136.83101113022065,"lat":39.19820534889482},{"lng":133.31377326639046,"lat":37.33522435930641}],"bounds":{"sw":{"lng":117.26637551266504,"lat":30.600093873550072},"ne":{"lng":136.83101113022065,"lat":39.19820534889482}},"center":{"lng":127.04869332144284,"lat":34.89914961122244},"length":2414846.188797506,"color":"#722ed1"}}
         * drawTime : 2025-12-15T00:15:29.635Z
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
             * coordinates : {"points":[{"lng":136.83101113022065,"lat":39.19820534889482},{"lng":133.31377326639046,"lat":37.33522435930641}],"bounds":{"sw":{"lng":117.26637551266504,"lat":30.600093873550072},"ne":{"lng":136.83101113022065,"lat":39.19820534889482}},"center":{"lng":127.04869332144284,"lat":34.89914961122244},"length":2414846.188797506,"color":"#722ed1"}
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
                /**
                 * points : [{"lng":136.83101113022065,"lat":39.19820534889482},{"lng":133.31377326639046,"lat":37.33522435930641}]
                 * bounds : {"sw":{"lng":117.26637551266504,"lat":30.600093873550072},"ne":{"lng":136.83101113022065,"lat":39.19820534889482}}
                 * center : {"lng":127.04869332144284,"lat":34.89914961122244}
                 * length : 2414846.188797506
                 * color : #722ed1
                 */

                private BoundsBean bounds;
                private CenterBean center;
                private double length;
                private String color;
                private List<PointsBean> points;

                public BoundsBean getBounds() {
                    return bounds;
                }

                public void setBounds(BoundsBean bounds) {
                    this.bounds = bounds;
                }

                public CenterBean getCenter() {
                    return center;
                }

                public void setCenter(CenterBean center) {
                    this.center = center;
                }

                public double getLength() {
                    return length;
                }

                public void setLength(double length) {
                    this.length = length;
                }

                public String getColor() {
                    return color;
                }

                public void setColor(String color) {
                    this.color = color;
                }

                public List<PointsBean> getPoints() {
                    return points;
                }

                public void setPoints(List<PointsBean> points) {
                    this.points = points;
                }

                public static class BoundsBean {
                    /**
                     * sw : {"lng":117.26637551266504,"lat":30.600093873550072}
                     * ne : {"lng":136.83101113022065,"lat":39.19820534889482}
                     */

                    private SwBean sw;
                    private NeBean ne;

                    public SwBean getSw() {
                        return sw;
                    }

                    public void setSw(SwBean sw) {
                        this.sw = sw;
                    }

                    public NeBean getNe() {
                        return ne;
                    }

                    public void setNe(NeBean ne) {
                        this.ne = ne;
                    }

                    public static class SwBean {
                        /**
                         * lng : 117.26637551266504
                         * lat : 30.600093873550072
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

                    public static class NeBean {
                        /**
                         * lng : 136.83101113022065
                         * lat : 39.19820534889482
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

                public static class CenterBean {
                    /**
                     * lng : 127.04869332144284
                     * lat : 34.89914961122244
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

                public static class PointsBean {
                    /**
                     * lng : 136.83101113022065
                     * lat : 39.19820534889482
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
}
