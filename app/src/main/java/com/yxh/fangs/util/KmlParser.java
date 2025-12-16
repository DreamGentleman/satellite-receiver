package com.yxh.fangs.util;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * 工程级 KML 解析工具类
 * 支持：
 * - Style / StyleMap
 * - LineString
 * - Polygon
 * - KML 2.2 namespace
 */
public class KmlParser {

    private static final String KML_NS = "http://www.opengis.net/kml/2.2";

    /* ===================== 对外入口 ===================== */

    public static KmlData parse(String kmlText) throws Exception {
        Document doc = buildDocument(kmlText);

        Map<String, KmlStyle> styleMap = parseStyles(doc);
        Map<String, String> styleAliasMap = parseStyleMaps(doc);

        List<KmlFeature> features = parsePlacemarks(doc, styleMap, styleAliasMap);

        return new KmlData(features);
    }

    /* ===================== Document ===================== */

    private static Document buildDocument(String kml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(kml));
        return builder.parse(is);
    }

    /* ===================== Style ===================== */

    private static Map<String, KmlStyle> parseStyles(Document doc) {
        Map<String, KmlStyle> map = new HashMap<>();

        NodeList styles = doc.getElementsByTagNameNS(KML_NS, "Style");
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            String id = style.getAttribute("id");
            if (id == null || id.isEmpty()) continue;

            KmlStyle kmlStyle = new KmlStyle();

            NodeList lineStyles = style.getElementsByTagNameNS(KML_NS, "LineStyle");
            if (lineStyles.getLength() > 0) {
                Element ls = (Element) lineStyles.item(0);
                String color = getText(ls, "color");
                String width = getText(ls, "width");

                if (color != null) kmlStyle.lineColor = parseKmlColor(color);
                if (width != null) kmlStyle.lineWidth = Float.parseFloat(width);
            }

            NodeList polyStyles = style.getElementsByTagNameNS(KML_NS, "PolyStyle");
            if (polyStyles.getLength() > 0) {
                Element ps = (Element) polyStyles.item(0);
                String color = getText(ps, "color");
                if (color != null) kmlStyle.fillColor = parseKmlColor(color);
            }

            map.put(id, kmlStyle);
        }
        return map;
    }

    /* ===================== StyleMap ===================== */

    private static Map<String, String> parseStyleMaps(Document doc) {
        Map<String, String> map = new HashMap<>();

        NodeList styleMaps = doc.getElementsByTagNameNS(KML_NS, "StyleMap");
        for (int i = 0; i < styleMaps.getLength(); i++) {
            Element sm = (Element) styleMaps.item(i);
            String id = sm.getAttribute("id");
            if (id == null || id.isEmpty()) continue;

            NodeList pairs = sm.getElementsByTagNameNS(KML_NS, "Pair");
            for (int j = 0; j < pairs.getLength(); j++) {
                Element pair = (Element) pairs.item(j);
                String key = getText(pair, "key");
                String styleUrl = getText(pair, "styleUrl");

                if ("normal".equals(key) && styleUrl != null && styleUrl.startsWith("#")) {
                    map.put(id, styleUrl.substring(1));
                }
            }
        }
        return map;
    }

    /* ===================== Placemark ===================== */

    private static List<KmlFeature> parsePlacemarks(
            Document doc,
            Map<String, KmlStyle> styles,
            Map<String, String> styleAlias
    ) {
        List<KmlFeature> list = new ArrayList<>();

        NodeList placemarks = doc.getElementsByTagNameNS(KML_NS, "Placemark");
        for (int i = 0; i < placemarks.getLength(); i++) {
            Element pm = (Element) placemarks.item(i);

            String name = getText(pm, "name");
            String styleUrl = getText(pm, "styleUrl");

            KmlStyle style = resolveStyle(styleUrl, styles, styleAlias);

            // LineString
            NodeList lines = pm.getElementsByTagNameNS(KML_NS, "LineString");
            if (lines.getLength() > 0) {
                Element line = (Element) lines.item(0);
                String coord = getText(line, "coordinates");
                list.add(KmlFeature.line(name, parseCoordinates(coord), style));
                continue;
            }

            // Polygon
            NodeList polys = pm.getElementsByTagNameNS(KML_NS, "Polygon");
            if (polys.getLength() > 0) {
                Element poly = (Element) polys.item(0);
                NodeList rings = poly.getElementsByTagNameNS(KML_NS, "LinearRing");
                if (rings.getLength() > 0) {
                    String coord = getText((Element) rings.item(0), "coordinates");
                    list.add(KmlFeature.polygon(name, parseCoordinates(coord), style));
                }
            }
        }
        return list;
    }

    /* ===================== 工具方法 ===================== */

    private static String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagNameNS(KML_NS, tag);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent().trim();
    }

    private static KmlStyle resolveStyle(
            String styleUrl,
            Map<String, KmlStyle> styles,
            Map<String, String> alias
    ) {
        if (styleUrl == null || !styleUrl.startsWith("#")) return null;
        String id = styleUrl.substring(1);
        if (alias.containsKey(id)) id = alias.get(id);
        return styles.get(id);
    }

    private static List<GeoPoint> parseCoordinates(String text) {
        List<GeoPoint> list = new ArrayList<>();
        if (text == null) return list;

        String[] lines = text.trim().split("\\s+");
        for (String s : lines) {
            String[] arr = s.split(",");
            if (arr.length < 2) continue;
            double lon = Double.parseDouble(arr[0]);
            double lat = Double.parseDouble(arr[1]);
            list.add(new GeoPoint(lat, lon));
        }
        return list;
    }

    /**
     * KML 颜色格式：aabbggrr
     */
    private static int parseKmlColor(String c) {
        long v = Long.parseLong(c, 16);
        int a = (int) ((v >> 24) & 0xff);
        int b = (int) ((v >> 16) & 0xff);
        int g = (int) ((v >> 8) & 0xff);
        int r = (int) (v & 0xff);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /* ===================== 数据模型 ===================== */

    public static class KmlData {
        public final List<KmlFeature> features;
        public KmlData(List<KmlFeature> features) {
            this.features = features;
        }
    }

    public static class KmlFeature {
        public enum Type { LINE, POLYGON }

        public final Type type;
        public final String name;
        public final List<GeoPoint> points;
        public final KmlStyle style;

        private KmlFeature(Type type, String name, List<GeoPoint> points, KmlStyle style) {
            this.type = type;
            this.name = name;
            this.points = points;
            this.style = style;
        }

        public static KmlFeature line(String name, List<GeoPoint> pts, KmlStyle style) {
            return new KmlFeature(Type.LINE, name, pts, style);
        }

        public static KmlFeature polygon(String name, List<GeoPoint> pts, KmlStyle style) {
            return new KmlFeature(Type.POLYGON, name, pts, style);
        }
    }

    public static class KmlStyle {
        public int lineColor = 0xFF000000;
        public float lineWidth = 2f;
        public int fillColor = 0x33000000;
    }

    public static class GeoPoint {
        public final double lat;
        public final double lon;
        public GeoPoint(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
