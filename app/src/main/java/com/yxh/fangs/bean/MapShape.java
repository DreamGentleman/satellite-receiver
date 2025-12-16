package com.yxh.fangs.bean;

public class MapShape {

    public String type; // circle / rectangle
    public Coordinates coordinates;

    public static class Coordinates {
        public Center center;      // circle / rectangle 都有
        public double radius;      // circle
        public Bounds bounds;      // rectangle / circle
        public String color;
        public double fillOpacity;
    }

    public static class Center {
        public double lng;
        public double lat;
    }

    public static class Bounds {
        public Point sw;
        public Point ne;
    }

    public static class Point {
        public double lng;
        public double lat;
    }
}

