package com.yxh.fangs.ui.main;

import android.content.Context;
import android.widget.Toast;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.yxh.fangs.bean.KmlStyle;
import com.yxh.fangs.map.draw.LineDrawManager;
import com.yxh.fangs.map.draw.PlaneDrawManager;
import com.yxh.fangs.map.layer.LayerManager;
import com.yxh.fangs.map.layer.LayerType;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class KmlRenderer {

    public static class KmlFeature {
        public final boolean polygon;
        public final String name;
        public final List<GeoPoint> points;
        public final KmlStyle style;

        private KmlFeature(boolean polygon, String name, List<GeoPoint> points, KmlStyle style) {
            this.polygon = polygon;
            this.name = name;
            this.points = points;
            this.style = style;
        }
    }

    private static String loadAsset(Context ctx, String fileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ctx.getAssets().open(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private static String convertKmlColor(String kmlColor) {
        if (kmlColor == null) return null;

        String s = kmlColor.trim();
        if (s.isEmpty()) return null;

        s = s.replace("#", "");
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        if (s.length() != 8) return null;

        // KML: AABBGGRR -> Android: AARRGGBB
        String aa = s.substring(0, 2);
        String bb = s.substring(2, 4);
        String gg = s.substring(4, 6);
        String rr = s.substring(6, 8);

        return "#" + aa + rr + gg + bb;
    }

    private static String getTagValue(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }

    private static KmlStyle parseStyleElement(Element styleElement) {
        if (styleElement == null) return null;

        KmlStyle ks = new KmlStyle();

        NodeList lineStyles = styleElement.getElementsByTagName("LineStyle");
        if (lineStyles.getLength() > 0) {
            Element lineStyle = (Element) lineStyles.item(0);
            ks.setLineColor(convertKmlColor(getTagValue(lineStyle, "color")));
            String width = getTagValue(lineStyle, "width");
            if (width != null) {
                try { ks.setLineWidth(Float.parseFloat(width)); } catch (Exception ignored) {}
            }
        }

        NodeList polyStyles = styleElement.getElementsByTagName("PolyStyle");
        if (polyStyles.getLength() > 0) {
            Element polyStyle = (Element) polyStyles.item(0);
            ks.setFillColor(convertKmlColor(getTagValue(polyStyle, "color")));
        }
        return ks;
    }

    private static String getCoordinates(Element parent) {
        return parent.getElementsByTagName("coordinates").item(0).getTextContent().trim();
    }

    private static List<GeoPoint> parseCoordinates(String coordsText) {
        java.util.ArrayList<GeoPoint> list = new java.util.ArrayList<>();
        String[] rows = coordsText.split("\\s+");
        for (String row : rows) {
            String[] parts = row.split(",");
            if (parts.length < 2) continue;
            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            list.add(new GeoPoint(lon, lat));
        }
        return list;
    }

    public static void drawKmlFishingZone(
            Context ctx,
            EarthFragment earth,
            LayerManager layerManager,
            LayerType selectedLayerType,
            LineDrawManager lineDrawManager,
            PlaneDrawManager planeDrawManager,
            String assetFileName
    ) {
        try {
            List<KmlFeature> features = parseKmlFishingZone(ctx, assetFileName);
            for (KmlFeature feature : features) {
                List<Long> elementIds = drawFeature(feature, lineDrawManager, planeDrawManager);
                layerManager.addLayer(selectedLayerType, elementIds);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ctx, "KML解析失败: " + assetFileName, Toast.LENGTH_LONG).show();
        }
    }

    public static List<KmlFeature> parseKmlFishingZone(Context ctx, String assetFileName) throws Exception {
        String kml = loadAsset(ctx, assetFileName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new java.io.StringReader(kml));
        org.w3c.dom.Document doc = builder.parse(is);

        Map<String, KmlStyle> styleMap = new HashMap<>();

        NodeList styleList = doc.getElementsByTagName("Style");
        for (int i = 0; i < styleList.getLength(); i++) {
            Element style = (Element) styleList.item(i);
            String styleId = style.getAttribute("id");
            if (styleId == null || styleId.isEmpty()) continue;
            styleMap.put("#" + styleId, parseStyleElement(style));
        }

        List<KmlFeature> features = new ArrayList<>();
        NodeList placemarks = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < placemarks.getLength(); i++) {
            Element placemark = (Element) placemarks.item(i);

            String name = getTagValue(placemark, "name");
            KmlStyle style = resolveStyle(placemark, styleMap);

            NodeList lineList = placemark.getElementsByTagName("LineString");
            if (lineList.getLength() > 0) {
                Element line = (Element) lineList.item(0);
                List<GeoPoint> points = parseCoordinates(getCoordinates(line));
                if (points.size() >= 2) {
                    features.add(new KmlFeature(false, name, points, style));
                }
                continue;
            }

            NodeList polyList = placemark.getElementsByTagName("Polygon");
            if (polyList.getLength() > 0) {
                Element polygon = (Element) polyList.item(0);
                NodeList lrList = polygon.getElementsByTagName("LinearRing");
                if (lrList.getLength() == 0) continue;

                Element lr = (Element) lrList.item(0);
                List<GeoPoint> points = parseCoordinates(getCoordinates(lr));
                if (points.size() >= 3) {
                    features.add(new KmlFeature(true, name, points, style));
                }
            }
        }
        return features;
    }

    public static List<Long> drawFeature(
            KmlFeature feature,
            LineDrawManager lineDrawManager,
            PlaneDrawManager planeDrawManager
    ) {
        KmlStyle style = feature.style;
        if (feature.polygon) {
            return planeDrawManager.draw(
                    feature.name,
                    feature.points,
                    style != null ? style.getFillColor() : null,
                    style != null ? style.getLineColor() : null,
                    style != null ? style.getLineWidth() : 0
            );
        }

        return lineDrawManager.drawLineWithLabel(
                feature.name,
                feature.points,
                style != null ? style.getLineColor() : null,
                style != null ? style.getLineWidth() : 0
        );
    }

    private static KmlStyle resolveStyle(Element placemark, Map<String, KmlStyle> styleMap) {
        NodeList inlineStyles = placemark.getElementsByTagName("Style");
        if (inlineStyles.getLength() > 0) {
            return parseStyleElement((Element) inlineStyles.item(0));
        }

        String styleUrl = getTagValue(placemark, "styleUrl");
        return styleUrl != null ? styleMap.get(styleUrl) : null;
    }
}
