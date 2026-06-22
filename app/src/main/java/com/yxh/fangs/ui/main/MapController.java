package com.yxh.fangs.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapController {

    private final Context ctx;
    private final FragmentManager fm;
    private final int containerId;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService kmlExecutor = Executors.newSingleThreadExecutor();

    private EarthFragment earth;
    private boolean earthReady = false;
    private boolean enforcingZoomLimit = false;

    private final LayerManager layerManager = new LayerManager();
    private LineDrawManager lineDrawManager;
    private PlaneDrawManager planeDrawManager;

    private final List<Long> tempLayerIds = new ArrayList<>();
    private final Map<Integer, byte[]> iconBytesCache = new HashMap<>();
    private final EnumSet<LayerType> loadingBaseLayers = EnumSet.noneOf(LayerType.class);
    private long locationElementId = -1L;

    private static final String TAG_EARTH_FRAGMENT = "TAG_EARTH_FRAGMENT";
    private static final int OFFLINE_MAX_ALTITUDE = 4000000;
    private static final int KML_DRAW_BATCH_SIZE = 12;
    private static final long KML_DRAW_BATCH_DELAY_MS = 10L;

    public MapController(Context ctx, FragmentManager fm, int containerId) {
        this.ctx = ctx;
        this.fm = fm;
        this.containerId = containerId;
    }

    public void initMapAsync(OperationCallback callback) {
        new Thread(() -> {
            copyAssetsIfNeeded();
            mainHandler.post(() -> {
                earth = EarthFragment.getInstance(callback);
                fm.beginTransaction().add(containerId, earth, TAG_EARTH_FRAGMENT).commitAllowingStateLoss();
            });
        }).start();
    }

    public void onEarthReady() {
        earthReady = true;
        addOfflineMap();
        switchToOfflineProvider();

        initDrawManagers();
    }

    private void initDrawManagers() {
        lineDrawManager = new LineDrawManager(earth);
        planeDrawManager = new PlaneDrawManager(earth);
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
        kmlExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
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

    public void enforceZoomLimit(GeoPoint center, double altitude) {
        if (!isReady() || center == null || enforcingZoomLimit) {
            return;
        }
        if (altitude <= OFFLINE_MAX_ALTITUDE) {
            return;
        }

        enforcingZoomLimit = true;
        double lon = center.lon;
        double lat = center.lat;
        mainHandler.post(() -> {
            animateTo(lon, lat, 0.0, OFFLINE_MAX_ALTITUDE);
            enforcingZoomLimit = false;
        });
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
        if (!isReady()) return;
        List<Long> ids = layerManager.getLayers(type);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            earth.setElementVisible(id, false);
        }
    }

    public void showLayer(LayerType type) {
        if (!isReady()) return;
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

    public void drawBaseLineSmooth(LayerType type, Runnable onComplete) {
        if (!isReady()) {
            notifyComplete(onComplete);
            return;
        }

        String asset = getKmlAsset(type);
        if (asset == null) {
            notifyComplete(onComplete);
            return;
        }

        List<Long> exist = layerManager.getLayers(type);
        if (exist != null && !exist.isEmpty()) {
            showLayer(type);
            notifyComplete(onComplete);
            return;
        }

        if (loadingBaseLayers.contains(type)) {
            mainHandler.postDelayed(() -> drawBaseLineSmooth(type, onComplete), 120L);
            return;
        }

        loadingBaseLayers.add(type);
        if (lineDrawManager == null || planeDrawManager == null) {
            initDrawManagers();
        }

        kmlExecutor.execute(() -> {
            try {
                List<KmlRenderer.KmlFeature> features = KmlRenderer.parseKmlFishingZone(ctx, asset);
                mainHandler.post(() -> drawKmlFeaturesInBatches(type, features, 0, onComplete));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    loadingBaseLayers.remove(type);
                    Toast.makeText(ctx, "KML解析失败: " + asset, Toast.LENGTH_LONG).show();
                    notifyComplete(onComplete);
                });
            }
        });
    }

    private void drawKml(LayerType layerType, String kmlAsset) {
        List<Long> exist = layerManager.getLayers(layerType);
        if (exist != null && !exist.isEmpty()) {
            showLayer(layerType);
            return;
        }

        if (lineDrawManager == null || planeDrawManager == null) {
            initDrawManagers();
        }
        KmlRenderer.drawKmlFishingZone(ctx, earth, layerManager, layerType, lineDrawManager, planeDrawManager, kmlAsset);
    }

    private void drawKmlFeaturesInBatches(
            LayerType layerType,
            List<KmlRenderer.KmlFeature> features,
            int start,
            Runnable onComplete
    ) {
        if (!isReady()) {
            loadingBaseLayers.remove(layerType);
            notifyComplete(onComplete);
            return;
        }

        int end = Math.min(start + KML_DRAW_BATCH_SIZE, features.size());
        for (int i = start; i < end; i++) {
            List<Long> elementIds = KmlRenderer.drawFeature(features.get(i), lineDrawManager, planeDrawManager);
            layerManager.addLayer(layerType, elementIds);
        }

        if (end < features.size()) {
            mainHandler.postDelayed(
                    () -> drawKmlFeaturesInBatches(layerType, features, end, onComplete),
                    KML_DRAW_BATCH_DELAY_MS
            );
            return;
        }

        loadingBaseLayers.remove(layerType);
        showLayer(layerType);
        notifyComplete(onComplete);
    }

    private String getKmlAsset(LayerType type) {
        switch (type) {
            case FISHING_GROUND:
                return "yuqv.kml";
            case NO_FISHING_LINE:
                return "jilun.kml";
            case COAST_LINE:
                return "linghai.kml";
            case CKFA:
                return "zhonghan.kml";
            case CJFA:
                return "zhongri.kml";
            default:
                return null;
        }
    }

    private void notifyComplete(Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
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

    public void updateLocationPoint(double lon, double lat, int resId, float iconScale, float angle) {
        if (!isReady()) {
            return;
        }
        if (locationElementId > 0) {
            removeElement(locationElementId);
            locationElementId = -1L;
        }
        locationElementId = drawPoint(LayerType.LOCATION, lon, lat, resId, iconScale, angle);
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
        if (angle == 0f && iconBytesCache.containsKey(resId)) {
            return iconBytesCache.get(resId);
        }
        Bitmap bmp = BitmapFactory.decodeResource(ctx.getResources(), resId);
        if (bmp == null) {
            return new byte[0];
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        if (angle == 0f) {
            iconBytesCache.put(resId, bytes);
        }
        if (rotated != bmp) {
            rotated.recycle();
        }
        bmp.recycle();
        return bytes;
    }

    public EarthFragment getEarth() {
        return earth;
    }

    public void removeElement(long elementId) {
        if (!isReady()) return;
        if (elementId <= 0) return;
        try {
            earth.removeElementFromEarth(elementId);
            layerManager.removeLayer(elementId);
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
    }
}
