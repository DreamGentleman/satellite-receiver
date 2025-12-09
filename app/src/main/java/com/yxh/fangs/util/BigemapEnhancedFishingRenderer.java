package com.yxh.fangs.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.BoundingBox;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;
import com.google.gson.Gson;
import com.yxh.fangs.bean.Feature;
import com.yxh.fangs.bean.FeatureCollection;
import com.yxh.fangs.bean.Geometry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 增强版 Bigemap 渔场渲染器：
 * - 支持 Polygon / MultiPolygon
 * - 外环抽稀，防止点数过多导致 GPU/Shader 崩溃
 * - 自动缩放到全部渔区范围
 * - 分批绘制，避免一次性 drawElement 太多
 */
public class BigemapEnhancedFishingRenderer {

    private static final String TAG = "FishingRenderer";

    private final Activity activity;
    private final EarthFragment earthFragment;

    // 超过这个点数才做抽稀
    private static final int MAX_POINTS_PER_RING = 1500;
    // 抽稀容差（度），越小越精细；之前过大导致只剩 2 个点，这里调小
    private static final double SIMPLIFY_TOLERANCE_OUTER = 0.00001; // ~1m
    private static final double SIMPLIFY_TOLERANCE_INNER = 0.00002; // 洞可略粗一点

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BigemapEnhancedFishingRenderer(Activity activity, EarthFragment earthFragment) {
        this.activity = activity;
        this.earthFragment = earthFragment;
    }

    /**
     * 对外入口：从 assets 加载并渲染渔场
     *
     * @param assetFileName 例如 "shandongyuqumian119.json"
     */
    public void loadAndRenderFromAssets(String assetFileName) {
        new Thread(() -> {
            try {
                long t0 = System.currentTimeMillis();

                String json = loadJSONFromAssets(activity, assetFileName);
                if (json == null || json.isEmpty()) {
                    Log.e(TAG, "JSON is empty");
                    return;
                }

                FeatureCollection collection = new Gson().fromJson(json, FeatureCollection.class);
                if (collection == null || collection.getFeatures() == null) {
                    Log.e(TAG, "FeatureCollection is null or empty");
                    return;
                }

                // 1. 子线程里解析 + 构造所有 VectorElement，并计算总范围
                BuildResult result = buildAllElements(collection);

                long t1 = System.currentTimeMillis();
                Log.i(TAG, "build elements cost: " + (t1 - t0) + " ms, size=" + result.elements.size());

                // 2. 回到主线程，分批绘制 + 自动缩放
                mainHandler.post(() -> {
                    drawElementsBatch(result.elements);
                    if (result.hasBounds) {
                        try {
                            BoundingBox bbox = new BoundingBox(
                                    result.maxLat,  // latNorth
                                    result.minLat,  // latSouth
                                    result.minLon,  // lonWest
                                    result.maxLon   // lonEast
                            );
                            earthFragment.animateToArea(bbox);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 从 assets 读取 JSON（大文件友好）
     */
    private String loadJSONFromAssets(Context context, String fileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * 构建结果：包含所有元素 + 全局范围
     */
    private static class BuildResult {
        List<VectorElement> elements = new ArrayList<>();
        boolean hasBounds = false;
        double minLat = 0, maxLat = 0, minLon = 0, maxLon = 0;

        void updateBounds(List<GeoPoint> pts) {
            if (pts == null || pts.isEmpty()) return;
            for (GeoPoint p : pts) {
                if (!hasBounds) {
                    minLat = maxLat = p.getLat();
                    minLon = maxLon = p.getLon();
                    hasBounds = true;
                } else {
                    if (p.getLat() < minLat) minLat = p.getLat();
                    if (p.getLat() > maxLat) maxLat = p.getLat();
                    if (p.getLon() < minLon) minLon = p.getLon();
                    if (p.getLon() > maxLon) maxLon = p.getLon();
                }
            }
        }
    }

    /**
     * 在子线程中构建全部渔场要素
     */
    @SuppressWarnings("unchecked")
    private BuildResult buildAllElements(FeatureCollection collection) {
        BuildResult result = new BuildResult();

        List<Feature> features = collection.getFeatures();
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            if (feature == null || feature.getGeometry() == null) continue;

            Geometry geometry = feature.getGeometry();
            String type = geometry.getType();
            Object coords = geometry.getCoordinates();

            String name = null;
            if (feature.getProperties() != null) {
                name = feature.getProperties().getNAME();
            }
            int featureIndex = i; // 用于生成稳定颜色

            if ("Polygon".equalsIgnoreCase(type)) {

                // Polygon: coordinates[ring][point][double]
                List<List<List<Double>>> polygon =
                        (List<List<List<Double>>>) coords;

                buildPolygonElements(polygon, featureIndex, name, result);

            } else if ("MultiPolygon".equalsIgnoreCase(type)) {

                // MultiPolygon: coordinates[polygon][ring][point][double]
                List<List<List<List<Double>>>> multi =
                        (List<List<List<List<Double>>>>) coords;

                for (List<List<List<Double>>> polygon : multi) {
                    buildPolygonElements(polygon, featureIndex, name, result);
                }
            }
        }
        return result;
    }

    /**
     * 一个 Polygon（包含外环 + 若干内洞） → 多个 VectorElement
     */
    private void buildPolygonElements(List<List<List<Double>>> coordinates,
                                      int featureIndex,
                                      String name,
                                      BuildResult out) {
        if (coordinates == null) return;

        for (int ringIndex = 0; ringIndex < coordinates.size(); ringIndex++) {
            List<List<Double>> ring = coordinates.get(ringIndex);

            List<GeoPoint> pts = new ArrayList<>();
            for (List<Double> p : ring) {
                if (p == null || p.size() < 2) continue;
                double lon = p.get(0);
                double lat = p.get(1);
                pts.add(new GeoPoint(lat, lon));  // Bigemap: lat, lon
            }

            if (pts.size() < 3) continue;

            // 外环 && 点数过多 → 抽稀，内环可选抽稀
            if (ringIndex == 0 && pts.size() > MAX_POINTS_PER_RING) {
                pts = simplifySafe(pts, SIMPLIFY_TOLERANCE_OUTER);
            } else if (ringIndex > 0 && pts.size() > MAX_POINTS_PER_RING) {
                pts = simplifySafe(pts, SIMPLIFY_TOLERANCE_INNER);
            }

            if (pts.size() < 3) {
                // 抽稀后仍不足 3 点则忽略该 ring
                Log.w(TAG, "skip ring, points < 3 after simplify");
                continue;
            }

            out.updateBounds(pts);

            VectorElement ve = createPolygon(pts, featureIndex, name);
            out.elements.add(ve);
        }
    }

    /**
     * 分批绘制，避免一次性 drawElement 过多导致卡顿或崩溃
     */
    private void drawElementsBatch(List<VectorElement> elements) {
        if (elements == null || elements.isEmpty()) return;

        final int[] index = {0};
        final int batchSize = 20;
        final long batchDelayMs = 40;

        Runnable drawBatch = new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (index[0] < elements.size() && count < batchSize) {
                    try {
                        earthFragment.drawElement(elements.get(index[0]), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    index[0]++;
                    count++;
                }

                if (index[0] < elements.size()) {
                    mainHandler.postDelayed(this, batchDelayMs);
                } else {
                    Log.i(TAG, "drawElementsBatch done, total=" + elements.size());
                }
            }
        };

        mainHandler.post(drawBatch);
    }

    /**
     * 构造 Bigemap Polygon VectorElement，增加随机颜色 & 名称
     */
    private VectorElement createPolygon(List<GeoPoint> points,
                                        int featureIndex,
                                        String name) {
        VectorElement ve = new VectorElement();

        // geoPath: "lon lat;lon lat;..."
        StringBuilder path = new StringBuilder();
        for (GeoPoint p : points) {
            path.append(p.getLon())
                    .append(" ")
                    .append(p.getLat())
                    .append(";");
        }
        ve.geoPath = path.toString();

        // 稳定随机颜色：根据 featureIndex 生成
        int baseColor = colorFromIndex(featureIndex);
        String strokeColorHex = String.format(Locale.US, "#%06X", (0xFFFFFF & baseColor));
        String fillColorHex = "#33" + String.format(Locale.US, "%06X", (0xFFFFFF & baseColor)); // 带透明

        String displayName = (name == null || name.isEmpty()) ? ("FishingGround-" + featureIndex) : name;

        // Bigemap 样式 & 属性放在 attribute 中
        ve.attribute = "strokeColor:" + strokeColorHex
                + ";fillColor:" + fillColorHex
                + ";strokeWidth:2"
                + ";name:" + displayName;

        ve.description = displayName;

        return ve;
    }

    /**
     * 根据索引生成一个稳定的伪随机颜色（不太刺眼）
     */
    private int colorFromIndex(int index) {
        int seed = (index * 1103515245 + 12345);
        int r = 100 + (Math.abs(seed) % 156);           // 100~255
        int g = 100 + (Math.abs(seed / 7) % 156);
        int b = 100 + (Math.abs(seed / 13) % 156);
        return (r << 16) | (g << 8) | b;
    }

    // ===================== 安全抽稀算法（Douglas-Peucker） =====================

    /**
     * 安全版：抽稀后保证至少 3 个点，否则退回原始点
     */
    private List<GeoPoint> simplifySafe(List<GeoPoint> points, double tolerance) {
        if (points == null || points.size() < 3) return points;

        boolean[] marked = new boolean[points.size()];
        marked[0] = true;
        marked[points.size() - 1] = true;

        dp(points, 0, points.size() - 1, tolerance, marked);

        List<GeoPoint> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (marked[i]) result.add(points.get(i));
        }

        if (result.size() < 3) {
            Log.w(TAG, "simplifySafe: result < 3, fallback to original");
            return points;
        }

        Log.i(TAG, "simplify: " + points.size() + " -> " + result.size());
        return result;
    }

    private void dp(List<GeoPoint> pts, int start, int end,
                    double eps, boolean[] marked) {
        if (end <= start + 1) return;

        double maxDist = 0;
        int index = -1;

        GeoPoint a = pts.get(start);
        GeoPoint b = pts.get(end);

        for (int i = start + 1; i < end; i++) {
            double dist = perpendicularDistance(pts.get(i), a, b);
            if (dist > maxDist) {
                maxDist = dist;
                index = i;
            }
        }

        if (index != -1 && maxDist > eps) {
            marked[index] = true;
            dp(pts, start, index, eps, marked);
            dp(pts, index, end, eps, marked);
        }
    }

    /**
     * 计算点到线段（a-b）的垂直距离（使用经纬度，近似即可）
     */
    private double perpendicularDistance(GeoPoint p, GeoPoint a, GeoPoint b) {
        double x0 = p.getLon();
        double y0 = p.getLat();
        double x1 = a.getLon();
        double y1 = a.getLat();
        double x2 = b.getLon();
        double y2 = b.getLat();

        double num = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        double den = Math.hypot(y2 - y1, x2 - x1);
        if (den == 0) return 0;
        return num / den;
    }
}
