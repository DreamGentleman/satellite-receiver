package com.yxh.fangs.bean;

import java.util.List;

public class WarnBean {

    /**
     * warningLevel : 蓝色预警
     * warningEndTime : 2025-12-15 08:13:40
     * warningArea : {"type":"polygon","coordinates":{"points":[{"lng":120.78361337649527,"lat":38.58252615935333},{"lng":124.03705840053823,"lat":37.85750715625203},{"lng":122.98188704138916,"lat":36.10237644873644}],"bounds":{"sw":{"lng":118.80516707809076,"lat":34.84987503195418},"ne":{"lng":124.03705840053823,"lat":38.58252615935333}},"center":{"lng":121.4211127393145,"lat":36.71620059565375},"area":1.3171531968798071E11,"perimeter":1360003.9468804682,"color":"#faad14","fillOpacity":0.2}}
     * drawTime : 2025-12-15T00:13:43.384Z
     */

    private String warningLevel;
    private String warningEndTime;
    private WarningAreaBean warningArea;
    private String drawTime;

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }

    public String getWarningEndTime() {
        return warningEndTime;
    }

    public void setWarningEndTime(String warningEndTime) {
        this.warningEndTime = warningEndTime;
    }

    public WarningAreaBean getWarningArea() {
        return warningArea;
    }

    public void setWarningArea(WarningAreaBean warningArea) {
        this.warningArea = warningArea;
    }

    public String getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(String drawTime) {
        this.drawTime = drawTime;
    }

    public static class WarningAreaBean {
        /**
         * type : polygon
         * coordinates : {"points":[{"lng":120.78361337649527,"lat":38.58252615935333},{"lng":124.03705840053823,"lat":37.85750715625203},{"lng":122.98188704138916,"lat":36.10237644873644}],"bounds":{"sw":{"lng":118.80516707809076,"lat":34.84987503195418},"ne":{"lng":124.03705840053823,"lat":38.58252615935333}},"center":{"lng":121.4211127393145,"lat":36.71620059565375},"area":1.3171531968798071E11,"perimeter":1360003.9468804682,"color":"#faad14","fillOpacity":0.2}
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
             * points : [{"lng":120.78361337649527,"lat":38.58252615935333},{"lng":124.03705840053823,"lat":37.85750715625203},{"lng":122.98188704138916,"lat":36.10237644873644}]
             * bounds : {"sw":{"lng":118.80516707809076,"lat":34.84987503195418},"ne":{"lng":124.03705840053823,"lat":38.58252615935333}}
             * center : {"lng":121.4211127393145,"lat":36.71620059565375}
             * area : 1.3171531968798071E11
             * perimeter : 1360003.9468804682
             * color : #faad14
             * fillOpacity : 0.2
             */

            private BoundsBean bounds;
            private CenterBean center;
            private double area;
            private double perimeter;
            private String color;
            private double fillOpacity;
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

            public double getArea() {
                return area;
            }

            public void setArea(double area) {
                this.area = area;
            }

            public double getPerimeter() {
                return perimeter;
            }

            public void setPerimeter(double perimeter) {
                this.perimeter = perimeter;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

            public double getFillOpacity() {
                return fillOpacity;
            }

            public void setFillOpacity(double fillOpacity) {
                this.fillOpacity = fillOpacity;
            }

            public List<PointsBean> getPoints() {
                return points;
            }

            public void setPoints(List<PointsBean> points) {
                this.points = points;
            }

            public static class BoundsBean {
                /**
                 * sw : {"lng":118.80516707809076,"lat":34.84987503195418}
                 * ne : {"lng":124.03705840053823,"lat":38.58252615935333}
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
                     * lng : 118.80516707809076
                     * lat : 34.84987503195418
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
                     * lng : 124.03705840053823
                     * lat : 38.58252615935333
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
                 * lng : 121.4211127393145
                 * lat : 36.71620059565375
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
                 * lng : 120.78361337649527
                 * lat : 38.58252615935333
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
