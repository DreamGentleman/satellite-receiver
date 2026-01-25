package com.yxh.fangs.map.draw;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;
import com.yxh.fangs.bean.TyphoonBean2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 台风绘制管理器（统一入口）
 * <p>
 * 规则：
 * 1. 路径 = 所有 points
 * 2. 风圈 = 仅当前时间最近的一个
 * 3. 风圈中心 = 路径点
 * 4. 半径 = JSON 中给定
 */
public class TyphoonDrawManager2 {

    private final EarthFragment earth;
    private final List<Long> layerIds = new ArrayList<>();

    public TyphoonDrawManager2(EarthFragment earthFragment) {
        this.earth = earthFragment;
    }

    /* =============================
     * 对外唯一入口
     * ============================= */
    public void draw(TyphoonBean2 bean) {
        if (bean == null
                || bean.getTyphoonInfo() == null
                || bean.getTyphoonInfo().getMapData() == null
                || bean.getTyphoonInfo().getMapData().getCoordinates() == null) {
            return;
        }

        clear();

        List<GeoPoint> trackPoints = buildTrack(bean);
        if (trackPoints.size() >= 2) {
            drawTrack(trackPoints);
        }

        drawCurrentWindCircle(bean, trackPoints);
    }

    /* =============================
     * 1. 构建轨迹点
     * ============================= */
    private List<GeoPoint> buildTrack(TyphoonBean2 bean) {
        List<GeoPoint> list = new ArrayList<>();

        List<TyphoonBean2.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean> points =
                bean.getTyphoonInfo()
                        .getMapData()
                        .getCoordinates()
                        .getPoints();

        if (points == null) return list;

        for (var p : points) {
            list.add(new GeoPoint(p.getLng(), p.getLat()));
        }
        return list;
    }

    /* =============================
     * 2. 画台风轨迹
     * ============================= */
    private long drawTrack(List<GeoPoint> points) {
        long root = earth.getRootLayerId();

        VectorElement layer = earth.onCreateLayer(root, "Typhoon Track", true);
        VectorElement line = new VectorElement(
                layer.id,
                VectorElement.TYPE_LINE,
                "Typhoon Path"
        );

        line.outlineColor = "#FF00BFFF";
        line.outlineWidth = "5";
        line.geoPoints.addAll(points);

        long id = earth.drawElement(line, true);
        layerIds.add(layer.id);
        layerIds.add(id);
        return id;
    }

    /* =============================
     * 3. 只画当前时间最近的风圈
     * ============================= */
    private void drawCurrentWindCircle(
            TyphoonBean2 bean,
            List<GeoPoint> trackPoints
    ) {
        if (bean.getWindCircles() == null || bean.getWindCircles().isEmpty()) {
            return;
        }

        TyphoonBean2.WindCirclesBean current = findNearestWindCircle(bean.getWindCircles());
        if (current == null) return;

        int idx = current.getPathPointIndex();
        if (idx < 0 || idx >= trackPoints.size()) return;

        GeoPoint center = trackPoints.get(idx);

        drawCircle(
                center.lon,
                center.lat,
                current.getRadius(),
                current.getColor(),
                current.getDescription()
        );
    }

    /* =============================
     * 4. 找最近时间的风圈
     * ============================= */
    private TyphoonBean2.WindCirclesBean findNearestWindCircle(
            List<TyphoonBean2.WindCirclesBean> circles
    ) {
        long now = System.currentTimeMillis();

        TyphoonBean2.WindCirclesBean nearest = null;
        long minDelta = Long.MAX_VALUE;

        for (TyphoonBean2.WindCirclesBean c : circles) {
            long t = parseTimeToday(c.getExpectedTime());
            long delta = Math.abs(t - now);

            if (delta < minDelta) {
                minDelta = delta;
                nearest = c;
            }
        }
        return nearest;
    }

    /* =============================
     * 5. 时间字符串解析
     * 支持：11:00 / 17时45分
     * ============================= */
    private long parseTimeToday(String timeStr) {
        if (timeStr == null) return 0;

        timeStr = timeStr.replace("时", ":")
                .replace("分", "");

        String[] arr = timeStr.split(":");
        if (arr.length != 2) return 0;

        int hour = Integer.parseInt(arr[0]);
        int minute = Integer.parseInt(arr[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        return cal.getTimeInMillis();
    }

    /* =============================
     * 6. 画风圈（单个）
     * ============================= */
    private void drawCircle(
            double lon,
            double lat,
            double radiusMeters,
            String color,
            String label
    ) {
        long root = earth.getRootLayerId();

        VectorElement layer = earth.onCreateLayer(
                root,
                "Typhoon Wind Circle",
                true
        );

        VectorElement circle = new VectorElement(
                layer.id,
                VectorElement.TYPE_CIRCLE,
                label
        );

        circle.outlineColor = color != null ? color : "#FF0000";
        circle.outlineWidth = "2";
        circle.showLabel = true;
        circle.description = label;

        circle.geoPoints.add(new GeoPoint(lon, lat, 0));
        circle.geoPoints.add(new GeoPoint(radiusMeters, 0, 0));

        long id = earth.drawElement(circle, true);
        layerIds.add(layer.id);
        layerIds.add(id);
    }

    /* =============================
     * 7. 清理
     * ============================= */
    public void clear() {
        for (Long id : layerIds) {
            earth.removeElementFromEarth(id);
        }
        layerIds.clear();
    }
}
