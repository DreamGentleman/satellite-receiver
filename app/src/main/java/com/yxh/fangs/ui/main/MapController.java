package com.yxh.fangs.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentManager;

import com.bigemap.bmcore.BMEngine;
import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.constant.Constants;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.Provider;
import com.bigemap.bmcore.entity.VectorElement;
import com.bigemap.bmcore.listener.OperationCallback;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.map.draw.LineDrawManager;
import com.yxh.fangs.map.draw.PlaneDrawManager;
import com.yxh.fangs.map.layer.LayerManager;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapController {

    private final Context ctx;
    private final FragmentManager fm;
    private final int containerId;

    private EarthFragment earth;
    private boolean earthReady = false;

    private final LayerManager layerManager = new LayerManager();
    private LineDrawManager lineDrawManager;
    private PlaneDrawManager planeDrawManager;

    private final List<Long> tempLayerIds = new ArrayList<>();

    private static final String TAG_EARTH_FRAGMENT = "TAG_EARTH_FRAGMENT";
    private static final int OFFLINE_MAX_ALTITUDE = 4000000;

    public MapController(Context ctx, FragmentManager fm, int containerId) {
        this.ctx = ctx;
        this.fm = fm;
        this.containerId = containerId;
    }

    public void initMapAsync(OperationCallback callback) {
        new Thread(() -> {
            copyAssetsIfNeeded();
            new Handler(Looper.getMainLooper()).post(() -> {
                earth = EarthFragment.getInstance(callback);
                fm.beginTransaction().add(containerId, earth, TAG_EARTH_FRAGMENT).commitAllowingStateLoss();
            });
        }).start();
    }

    public void onEarthReady() {
        earthReady = true;
        addOfflineMap();
        switchToOfflineProvider();

        initDrawManagersDelayed();
    }

    private void initDrawManagersDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            lineDrawManager = new LineDrawManager(earth);
            planeDrawManager = new PlaneDrawManager(earth);
        }, 3000L);
    }

    private void copyAssetsIfNeeded() {
        boolean copied = SPUtils.getBoolean(AppConstants.KEYASSETSCOPIED, false);
        if (copied) {
            LogUtils.i("assets 已拷贝，跳过");
            return;
        }

        Utils.INSTANCE.copyAssets(ctx, "img", ctx.getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(ctx, "map", ctx.getFilesDir().getPath());

        SPUtils.putBoolean(AppConstants.KEYASSETSCOPIED, true);
        LogUtils.i("assets 拷贝完成");
    }

    public void release() {
        // 释放点位/清理等
    }

    public boolean isReady() {
        return earthReady && earth != null;
    }

    public void animateTo(double lon, double lat, double time) {
        animateTo(lon, lat, time, OFFLINE_MAX_ALTITUDE);
    }

    public void animateTo(double lon, double lat, double time, int height) {
        if (!isReady()) return;
        GeoPoint geoPoint = new GeoPoint(lon, lat);
        double pitch = -90.0;
        earth.animateTo(geoPoint, height, time, pitch);
    }

    // ====== provider/offline ======

    private void addOfflineMap() {
        String name = "澳门特别行政区_卫图";
        String path = ctx.getFilesDir().getPath() + File.separator + "测试数据.bmpkg";
        List<String> strings = new ArrayList<>();
        strings.add(path);
        BMEngine.addOfflineMap(name, "", strings);
    }

    private void switchToOfflineProvider() {
        List<Provider> providers = BMEngine.getMapProviders();
        if (providers == null || providers.isEmpty()) return;
        for (Provider p : providers) {
            if (p.mapId.startsWith("MAPID_BM_OFFLINEMAP")) {
                earth.changeMapSource(p.mapId);
                earth.animateToOfflineArea();
                break;
            }
        }
        BMEngine.isShowBuilding(false);
    }

    // ====== layer show/hide ======

    public void hideLayer(LayerType type) {
        List<Long> ids = layerManager.getLayers(type);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            earth.setElementVisible(id, false);
        }
    }

    public void showLayer(LayerType type) {
        List<Long> ids = layerManager.getLayers(type);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            earth.setElementVisible(id, true);
        }
    }

    public void hideAllBaseLayers() {
        hideLayer(LayerType.FISHING_GROUND);
        hideLayer(LayerType.NO_FISHING_LINE);
        hideLayer(LayerType.COAST_LINE);
        hideLayer(LayerType.CKFA);
        hideLayer(LayerType.CJFA);
    }

    // ====== drawing base line ======

    public void drawBaseLine(LayerType type) {
        if (!isReady()) return;
        switch (type) {
            case FISHING_GROUND:
                drawKml(type, "yuqv.kml");
                break;
            case NO_FISHING_LINE:
                drawKml(type, "jilun.kml");
                break;
            case COAST_LINE:
                drawKml(type, "linghai.kml");
                break;
            case CKFA:
                drawKml(type, "zhonghan.kml");
                break;
            case CJFA:
                drawKml(type, "zhongri.kml");
                break;
        }
    }

    private void drawKml(LayerType layerType, String kmlAsset) {
        List<Long> exist = layerManager.getLayers(layerType);
        if (exist != null && !exist.isEmpty()) {
            showLayer(layerType);
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 复用你原来的 KML解析绘制方法：建议你后续再拆 KmlRenderer
            KmlRenderer.drawKmlFishingZone(ctx, earth, layerManager, layerType, lineDrawManager, planeDrawManager, kmlAsset);
        }, 1000L);
    }

    // ====== common draw helpers ======

    public long drawPoint(LayerType layerType,double lon, double lat, int resId, float iconScale, float angle) {
        if (!isReady()) return -1;
        long rootID = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_POINT, "点");
        vector.showIcon = true;
        vector.isCustomPath = false;
        vector.image = getBytesFromRes(resId, angle);
        vector.iconScale = iconScale;
        vector.iconAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;
        vector.showLabel = false;

        vector.geoPoints.add(new GeoPoint(lon, lat, 0.0));

        long id = earth.drawElement(vector, true);
        layerManager.addLayer(layerType, id);
        return id;
    }

    public long drawCircle(LayerType layerType, double lon, double lat, String color, double radiusMeters, String label) {
        if (!isReady()) return -1;

        long rootID = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(rootID, "circle_" + System.currentTimeMillis(), true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, label);
        tempLayerIds.add(layer.id);

        vector.outlineColor = color;
        vector.outlineWidth = "2";
        vector.showLabel = true;
        vector.description = label;

        vector.geoPoints.add(new GeoPoint(lon, lat, 0.0));
        vector.geoPoints.add(new GeoPoint(radiusMeters, 0.0, 0.0));

        long id = earth.drawElement(vector, true);
        layerManager.addLayer(layerType, id);
        return id;
    }

    public long drawRectangle(LayerType layerType, double swLng, double swLat, double neLng, double neLat, String color, String name) {
        if (!isReady()) return -1;

        long rootID = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(rootID, "rect_" + System.currentTimeMillis(), true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_RECT, name);
        tempLayerIds.add(layer.id);

        vector.outlineColor = color;
        vector.outlineWidth = "2";
        vector.showLabel = true;
        vector.description = name;

        vector.geoPoints.add(new GeoPoint(swLng, swLat, 0.0));
        vector.geoPoints.add(new GeoPoint(neLng, swLat, 0.0));
        vector.geoPoints.add(new GeoPoint(neLng, neLat, 0.0));
        vector.geoPoints.add(new GeoPoint(swLng, neLat, 0.0));
        vector.geoPoints.add(new GeoPoint(swLng, swLat, 0.0));

        long id = earth.drawElement(vector, true);
        layerManager.addLayer(layerType, id);
        return id;
    }

    public long drawPolyline(LayerType layerType, List<GeoPoint> pts, String color, String width, String name) {
        if (!isReady() || pts == null || pts.size() < 2) return -1;

        long rootID = earth.getRootLayerId();
        VectorElement layer = earth.onCreateLayer(rootID, "polyline_" + System.currentTimeMillis(), true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_LINE, name);
        tempLayerIds.add(layer.id);

        vector.outlineColor = color;
        vector.outlineWidth = width;
        vector.geoPoints.addAll(pts);

        long id = earth.drawElement(vector, true);
        layerManager.addLayer(layerType, id);
        return id;
    }

    private byte[] getBytesFromRes(int resId, float angle) {
        Bitmap bmp = BitmapFactory.decodeResource(ctx.getResources(), resId);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public EarthFragment getEarth() {
        return earth;
    }

    public void removeElement(long elementId) {
        if (!isReady()) return;
        if (elementId <= 0) return;
        try {
            earth.removeElementFromEarth(elementId);
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
    }
}
