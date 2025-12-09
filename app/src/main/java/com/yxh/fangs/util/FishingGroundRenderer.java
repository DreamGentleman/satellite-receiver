package com.yxh.fangs.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bigemap.bmcore.EarthFragment;
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

public class FishingGroundRenderer {

    private static final String TAG = "FishingRenderer";

    private final Activity activity;
    private final EarthFragment earthFragment;

    // 超过这个点数就做抽稀
    private static final int MAX_POINTS_PER_RING = 2000;
    // 抽稀容差（度），越大越“简陋”，越小越精细
    private static final double SIMPLIFY_TOLERANCE = 0.00001;

    public FishingGroundRenderer(Activity activity, EarthFragment earthFragment) {
        this.activity = activity;
        this.earthFragment = earthFragment;
    }

    /**
     * 对外入口：从 assets 加载并渲染
     *
     * @param assetFileName 比如 "shandongyuqumian119.json"
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

                // 1. 先在子线程里构造所有 VectorElement（重 CPU）
                List<VectorElement> elements = buildAllElements(collection);

                long t1 = System.currentTimeMillis();
                Log.i(TAG, "build elements cost: " + (t1 - t0) + " ms, size=" + elements.size());

                // 2. 再切回主线程一个个画（调 drawElement 必须在主线程）
                activity.runOnUiThread(() -> drawElementsBatch(elements));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 从 assets 读取大 JSON 文件
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
     * 在子线程中构造全部渔场面要素
     */
    @SuppressWarnings("unchecked")
    private List<VectorElement> buildAllElements(FeatureCollection collection) {
        List<VectorElement> list = new ArrayList<>();

        for (Feature feature : collection.getFeatures()) {
            if (feature == null || feature.getGeometry() == null) continue;

            Geometry geometry = feature.getGeometry();
            String type = geometry.getType();
            Object coords = geometry.getCoordinates();

            if ("Polygon".equalsIgnoreCase(type)) {

                // Polygon: coordinates[ring][point][double]
                List<List<List<Double>>> polygon =
                        (List<List<List<Double>>>) coords;

                buildPolygonElements(polygon, list);

            } else if ("MultiPolygon".equalsIgnoreCase(type)) {

                // MultiPolygon: coordinates[polygon][ring][point][double]
                List<List<List<List<Double>>>> multi =
                        (List<List<List<List<Double>>>>) coords;

                for (List<List<List<Double>>> polygon : multi) {
                    buildPolygonElements(polygon, list);
                }
            }
        }
        return list;
    }

    /**
     * 一个 Polygon（包含外环 + 若干内洞） → 多个 VectorElement
     */
    private void buildPolygonElements(List<List<List<Double>>> coordinates,
                                      List<VectorElement> outList) {
        if (coordinates == null) return;

        for (List<List<Double>> ring : coordinates) {

            List<GeoPoint> pts = new ArrayList<>();
            for (List<Double> p : ring) {
                if (p == null || p.size() < 2) continue;
                double lon = p.get(0);
                double lat = p.get(1);
                pts.add(new GeoPoint(lat, lon));  // Bigemap: lat, lon
            }

            if (pts.size() < 3) continue;

            // 点太多先抽稀
            if (pts.size() > MAX_POINTS_PER_RING) {
                pts = simplify(pts, SIMPLIFY_TOLERANCE);
            }

            VectorElement ve = createPolygon(pts);
            outList.add(ve);
        }
    }

    /**
     * 批量绘制，防止一次性塞太多导致卡顿
     */
    private void drawElementsBatch(List<VectorElement> elements) {
        if (elements == null || elements.isEmpty()) return;

        new Thread(() -> {
            int batchCount = 0;

            for (VectorElement ve : elements) {
                try {
                    activity.runOnUiThread(() -> earthFragment.drawElement(ve, false));
                    batchCount++;

                    // 每画 20 个稍微缓一下，给 GL / UI 一点喘息时间
                    if (batchCount % 20 == 0) {
                        Thread.sleep(40);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "drawElementsBatch done, total=" + batchCount);
        }).start();
    }

    /**
     * 构造 Bigemap Polygon VectorElement
     */
    private VectorElement createPolygon(List<GeoPoint> points) {
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

        // 样式：这里可以按需改颜色
        // 建议：大面积区域用透明填充，减少 GPU 压力
        ve.attribute = "strokeColor:#FF0000;fillColor:#33FF0000;strokeWidth:2";
        ve.description = "Fishing Ground";

        return ve;
    }

    // ===================== 抽稀算法（Douglas-Peucker）=====================

    /**
     * Douglas-Peucker 抽稀
     */
    private List<GeoPoint> simplify(List<GeoPoint> points, double tolerance) {
        if (points == null || points.size() < 3) return points;

        boolean[] marked = new boolean[points.size()];
        marked[0] = true;
        marked[points.size() - 1] = true;

        dp(points, 0, points.size() - 1, tolerance, marked);

        List<GeoPoint> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (marked[i]) {
                result.add(points.get(i));
            }
        }
        Log.i(TAG, "simplify: " + points.size() + " -> " + result.size());
        return result;
    }

    private void dp(List<GeoPoint> pts, int start, int end,
                    double eps, boolean[] marked) {
        if (end <= start + 1) return;

        double maxDist = 0;
        int index = 0;

        GeoPoint a = pts.get(start);
        GeoPoint b = pts.get(end);

        for (int i = start + 1; i < end; i++) {
            double dist = perpendicularDistance(pts.get(i), a, b);
            if (dist > maxDist) {
                maxDist = dist;
                index = i;
            }
        }

        if (maxDist > eps) {
            marked[index] = true;
            dp(pts, start, index, eps, marked);
            dp(pts, index, end, eps, marked);
        }
    }

    /**
     * 计算点到线段（a-b）的垂直距离（使用经纬度，粗略即可）
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