package com.yxh.fangs.map.draw;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.TyphoonBean;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 台风绘制管理器
 * 统一管理：路径 / 中心 / 风圈 / 视角
 */
public class TyphoonDrawManager {

    private final EarthFragment earthFragment;
    private long typhoonLayerId = -1;

    public TyphoonDrawManager(EarthFragment earthFragment) {
        this.earthFragment = earthFragment;
        initLayer();
    }

    /**
     * 初始化台风根图层
     */
    private void initLayer() {
        long root = earthFragment.getRootLayerId();
        typhoonLayerId = earthFragment.onCreateLayer(root, "TyphoonLayer", true).id;
    }

    /**
     * ================= 对外唯一入口 =================
     */

    public void drawTyphoon(TyphoonBean bean) {
        clear();
        drawTrack(bean);
        drawCenter(bean);
        drawWindCircle(bean);
    }

    /** ================= 绘制部分 ================= */

    /**
     * 台风路径
     */
    private void drawTrack(TyphoonBean bean) {
        VectorElement line = new VectorElement(
                typhoonLayerId,
                VectorElement.TYPE_LINE,
                "TyphoonTrack"
        );

        List<GeoPoint> points = new ArrayList<>();
        for (TyphoonBean.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean p : bean.getTyphoonInfo().getMapData().getCoordinates().getPoints()) {
            points.add(new GeoPoint(p.getLng(), p.getLat()));
        }

        line.geoPoints.addAll(points);
        line.outlineWidth = "6";
        line.outlineColor = bean.getTyphoonInfo().getMapData().getCoordinates().getColor();
        line.showLabel = false;

        earthFragment.drawElement(line, true);
    }

    /**
     * 台风中心点
     */
    private void drawCenter(TyphoonBean bean) {
        TyphoonBean.TyphoonInfoBean.MapDataBean.CoordinatesBean coordinates = bean.getTyphoonInfo().getMapData().getCoordinates();
        TyphoonBean.TyphoonInfoBean.MapDataBean.CoordinatesBean.CenterBean c = coordinates.getCenter();

        VectorElement point = new VectorElement(
                typhoonLayerId,
                VectorElement.TYPE_POINT,
                bean.getTyphoonName()
        );

        point.geoPoints.add(new GeoPoint(c.getLng(), c.getLat()));

        point.image = getBytesFromRes(R.mipmap.ic_notice_typhoon, 0);         // 使用旋转后的位图
        point.showIcon = true;
        point.isCustomPath = false;
        point.iconScale = 2f;

        point.showLabel = true;
        point.labelColor = coordinates.getColor();
        point.labelSize = "14";
        point.description = bean.getTyphoonName();

        earthFragment.drawElement(point, true);
    }

    /**
     * 台风风圈
     */
    private void drawWindCircle(TyphoonBean bean) {
        TyphoonBean.TyphoonInfoBean.MapDataBean.CoordinatesBean coordinates = bean.getTyphoonInfo().getMapData().getCoordinates();
        TyphoonBean.TyphoonInfoBean.MapDataBean.CoordinatesBean.CenterBean c = coordinates.getCenter();

        double radius = windToRadius(bean.getMaximumWind());

        VectorElement circle = new VectorElement(
                typhoonLayerId,
                VectorElement.TYPE_CIRCLE,
                "TyphoonWind"
        );

        circle.geoPoints.add(new GeoPoint(c.getLng(), c.getLat()));
        circle.geoPoints.add(new GeoPoint(radius, 0, 0));

        circle.outlineWidth = "2";
        circle.outlineColor = coordinates.getColor();
        circle.showLabel = false;

        earthFragment.drawElement(circle, true);
    }


    /** ================= 工具 & 管理 ================= */

    /**
     * 清理台风图层
     */
    public void clear() {

        //TODO
//        if (typhoonLayerId != -1) {
//            earthFragment.clearLayer(typhoonLayerId);
//        }
    }

    /**
     * 风力 → 半径（米）
     */
    private double windToRadius(int windLevel) {
        switch (windLevel) {
            case 8:
                return 80_000;
            case 9:
                return 100_000;
            case 10:
                return 120_000;
            case 11:
                return 150_000;
            case 12:
                return 180_000;
            default:
                return 60_000;
        }
    }

    public byte[] getBytesFromRes(int resId, float angle) {
        // 2. 加载 bitmap
        Bitmap bmp = BitmapFactory.decodeResource(earthFragment.getResources(), resId);

        // 3. 旋转 bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        // 4. 转 byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
