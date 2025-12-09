package com.yxh.fangs.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.bigemap.bmcore.BMEngine;
import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.constant.Constants;
import com.bigemap.bmcore.entity.CustomMapSource;
import com.bigemap.bmcore.entity.DefaultStyle;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.MapConfig;
import com.bigemap.bmcore.entity.VectorElement;
import com.bigemap.bmcore.listener.OperationCallback;
import com.bigemap.bmcore.sp.StyleUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.LocationBean;
import com.yxh.fangs.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends BaseActivity implements OperationCallback {

    private EarthFragment mEarthFragment;
    // 标记地图是否已加载
    private boolean isEarthReady = false;
    private double longitudeData = 0.0;
    private double latitudeData = 0.0;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 初始化位置请求
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L   // 每 5 秒更新一次
        ).build();

        // 定位回调
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                for (android.location.Location location : result.getLocations()) {
                    longitudeData = location.getLongitude();
                    latitudeData = location.getLatitude();

                    String text = "当前经度：" + location.getLongitude()
                            + "\n当前纬度：" + location.getLatitude();
                    Toast.makeText(MainActivity2.this, text, Toast.LENGTH_SHORT).show();

                    if (isEarthReady) {
                        onAnimateTo(longitudeData, latitudeData, 0.0);
                        // 避免每次都跳转
                        isEarthReady = false;
                    }
                }
            }
        };
        longitudeData = 126.5292;
        latitudeData = 33.3617;
        // 检查并请求权限
        checkPermissionAndStartLocation();

        // 初始化地图配置
        BMEngine.preInit(this, "bda2ea3fb18fdd9a4a6d922389576df7");
        // 1、Context 2、自定义图标存放位置 3、是否加载地形
        BMEngine.init(this, getFilesDir().getPath() + File.separator, false);

        // 拷贝文件到文件系统
        Utils.INSTANCE.copyAssets(this, "img", getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(this, "map", getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(this, "img", BMEngine.getGGIconPath());
        // Utils.copyAssets(this, "同名图标", BMEngine.getIconPath());

        // 添加在线地图 / 离线地图（现在是在 startLocationUpdates 里调用）
        // addMapSource(TEST_MAP_SOURCE_URL3);
        // addMapSourceList();
        // addOfflineMap();

        mEarthFragment = EarthFragment.getInstance(this);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flt_container, mEarthFragment, TAG_EARTH_FRAGMENT)
                .commitAllowingStateLoss();

        // 点击事件
        initClicked();
    }

    private void initClicked() {
        findViewById(R.id.offlineTv).setOnClickListener(v -> changeOfflineMapSource());        // 离线地图加载
        findViewById(R.id.onlineTv).setOnClickListener(v -> changeOnlineMapSource(1));        // 在线地图加载
        findViewById(R.id.locationTv).setOnClickListener(v -> updateLocation());              // 当前位置
        findViewById(R.id.lineTv).setOnClickListener(v -> toAddLineInMap());                  // 画线
        findViewById(R.id.drawLineTv).setOnClickListener(v -> onDrawLineElement());           // 手画线
        findViewById(R.id.pointTv).setOnClickListener(v -> toAddPointInMap());                // 画点
        findViewById(R.id.drawPointTv).setOnClickListener(v -> onDrawPointElement());         // 手画点
        findViewById(R.id.drawPlaneTv).setOnClickListener(v -> onDrawPlaneElement());         // 手画面
        findViewById(R.id.drawRevocationTv).setOnClickListener(v -> toRetreatDrawingElement());// 撤销当前线绘制
        findViewById(R.id.typhoonTv).setOnClickListener(v -> startTyphoonSimulation());       // 模拟台风轨迹风圈绘制
    }

    // 动态权限加载
    private void checkPermissionAndStartLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100
            );
        } else {
            startLocationUpdates();
        }
    }

    // 已授权，授权之后在加载地图
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        // 添加在线地图
        addMapSource(TEST_MAP_SOURCE_URL3);
        addMapSourceList();
        // 添加离线地图
        addOfflineMap();
    }

    // 权限回调
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "未授予定位权限，无法获取位置", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void addMapSource(String url) {
        // 1、创建自定义地图源
        CustomMapSource custom = new CustomMapSource();
        custom.name = "Arcgis";
        custom.url = url;       // 瓦片链接地址
        custom.tileSize = 256;  // 瓦片大小
        custom.projection = true; // 投影true:墨卡托，false:经纬度直投
        custom.minLevel = 0;    // 最小层级
        custom.maxLevel = 19;   // 最大层级
        custom.cacheKey = "ArcGis"; // 缓存标识
        // 2、加入引擎中
        BMEngine.addCustomMapSource(custom);
    }

    private void addMapSourceList() {
        // 1、创建自定义图层1
        CustomMapSource custom1 = new CustomMapSource();
        custom1.name = "高德影像";
        custom1.url = TEST_MAP_SOURCE_URL1;
        custom1.tileSize = 256;
        custom1.projection = true;
        custom1.minLevel = 0;
        custom1.maxLevel = 19;
        custom1.cacheKey = "高德影像";

        // 2、创建自定义图层2
        CustomMapSource custom2 = new CustomMapSource();
        custom2.name = "高德街道";
        custom2.url = TEST_MAP_SOURCE_URL2;
        custom2.tileSize = 256;
        custom2.projection = true;
        custom2.minLevel = 0;
        custom2.maxLevel = 19;
        custom2.cacheKey = "高德街道";

        List<CustomMapSource> list = new ArrayList<>();
        list.add(custom1);
        list.add(custom2);

        // 2、加入引擎中
        BMEngine.addMapLayerList("地图源", "可以空", list);
    }

    public void addOfflineMap() {
        String name = "澳门特别行政区_卫图";
        String icon = "";
        String path = getFilesDir().getPath() + File.separator + "澳门特别行政区_卫图.bmpkg";
        List<String> strings = new ArrayList<>();
        strings.add(path);
        BMEngine.addOfflineMap(name, icon, strings);
    }

    // 工具：把十六进制颜色（#RRGGBB 或 #AARRGGBB）加入 alpha
    private String colorWithAlpha(String hexColor, int alphaInt) {
        int alpha = Math.max(0, Math.min(255, alphaInt));
        String alphaHex = String.format("%02X", alpha);
        String c = hexColor.replace("#", "");
        if (c.length() == 6) {
            return "#" + alphaHex + c;
        } else if (c.length() == 8) {
            return "#" + alphaHex + c.substring(2);
        } else {
            return "#" + alphaHex + c;
        }
    }

    /**
     * 绘制风圈（用若干同心圆模拟填充）
     * lon,lat: 圆心经纬度
     * color: 形如 "#RRGGBB" 或 "#AARRGGBB"
     * radiusMeters: 半径（米）
     */
    public void addTyphoonCircleSimulatedFill(double lon, double lat, String color, double radiusMeters) {
        long rootID = mEarthFragment.getRootLayerId();
        // 创建独立图层，用于这一组同心圆（方便后续管理/删除）
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "台风风圈_" + System.currentTimeMillis(), true);

        int rings = 6;
        for (int i = 0; i < rings; i++) {
            double t = (double) i / (rings - 1); // 0..1
            double r = radiusMeters * (1.0 - 0.8 * t); // 最内圈为 radius * 0.2
            int alpha = (int) (40 + (160 * (1.0 - t))); // 40 .. 200
            String colorWithA = colorWithAlpha(color, alpha);

            VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "风圈_ring_" + i);
            vector.outlineColor = colorWithA;
            vector.outlineWidth = (i == 0) ? "6" : "2";
            vector.showLabel = false;
            vector.description = "风圈";

            GeoPoint geo1 = new GeoPoint(lon, lat, 0.0);
            GeoPoint geo2 = new GeoPoint(r, 0.0, 0.0);
            vector.geoPoints.add(geo1);
            vector.geoPoints.add(geo2);
            mEarthFragment.drawElement(vector, true);
        }
    }

    // 绘制台风路径
    public void drawTyphoonTrack(List<GeoPoint> trackPoints) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "台风轨迹", true);
        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_LINE, "台风路径");
        vector.outlineWidth = "5";
        vector.outlineColor = "#FF00BFFF"; // 深天蓝
        vector.geoPoints.addAll(trackPoints);
        mEarthFragment.drawElement(vector, true);
    }

    // 绘制固定大小的风圈（不叠加、不渐变）
    public void addTyphoonCircle(double lon, double lat, String color, double radiusMeters) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "台风风圈_" + System.currentTimeMillis(), true);
        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "风圈");
        vector.outlineColor = color;
        vector.outlineWidth = "2";
        vector.showLabel = true;
        vector.description = "风圈";

        GeoPoint geo1 = new GeoPoint(lon, lat, 0.0);
        GeoPoint geo2 = new GeoPoint(radiusMeters, 0.0, 0.0);
        vector.geoPoints.add(geo1);
        vector.geoPoints.add(geo2);

        mEarthFragment.drawElement(vector, true);
    }

    // 模拟台风移动 + 动态更新等级与风圈
    public void startTyphoonSimulation() {
        ArrayList<LocationBean> locationList = new ArrayList<>();
        locationList.add(new LocationBean(34.582871, 123.510237));
        locationList.add(new LocationBean(32.079378, 124.919638));
        locationList.add(new LocationBean(30.243978, 123.389687));
        locationList.add(new LocationBean(26.870304, 123.205269));
        locationList.add(new LocationBean(25.958292, 121.090243));

        Handler handler = new Handler(Looper.getMainLooper());
        List<GeoPoint> trackPoints = new ArrayList<>();

        double baseLon = 123.0;
        double baseLat = 18.0;

        String[] levels = new String[]{
                "热带风暴",
                "强热带风暴",
                "台风",
                "强台风",
                "超强台风"
        };

        for (int i = 0; i < levels.length; i++) {
            final int index = i;
            int finalI = i;
            handler.postDelayed(() -> {
                double lon = locationList.get(finalI).getLongitude();
                double lat = locationList.get(finalI).getLatitude();
                trackPoints.add(new GeoPoint(lon, lat));

                // 更新轨迹线
                drawTyphoonTrack(trackPoints);

                String level = levels[index];
                String color;
                double radius;

                switch (level) {
                    case "热带风暴":
                        color = "#FFFF00";
                        radius = 30000.0;
                        break;
                    case "强热带风暴":
                        color = "#FFA500";
                        radius = 60000.0;
                        break;
                    case "台风":
                        color = "#FF0000";
                        radius = 90000.0;
                        break;
                    case "强台风":
                        color = "#8B0000";
                        radius = 120000.0;
                        break;
                    case "超强台风":
                        color = "#800080";
                        radius = 150000.0;
                        break;
                    default:
                        color = "#00FF00";
                        radius = 20000.0;
                        break;
                }

                // 绘制风圈
                addTyphoonCircle(lon, lat, color, radius);

                // 移动视角到当前点（如需）
                // onAnimateTo(lon, lat, 1.0);

                if ("强台风".equals(level)) {
                    Toast.makeText(MainActivity2.this, "警告警告", Toast.LENGTH_SHORT).show();
                }

                Log.e("Fangs", "台风等级：" + level + "\n经度:" + lon + ", 纬度:" + lat);
            }, index * 2000L);
        }
    }

    private void toAddPointInMap(double lon, double lat) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_POINT, "点");
        vector.description = "描述";

        vector.isCustomPath = false;
        if (vector.isCustomPath) {
            vector.iconPath = "自定义.png";
        } else {
            vector.iconPath = "ic_wind.png";
        }

        vector.iconScale = 0.6f;
        vector.iconAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;

        vector.showLabel = true;
        vector.labelColor = "#FF00FF00";

        GeoPoint geo = new GeoPoint(lon, lat, 0.0);
        vector.geoPoints.add(geo);

        long id = mEarthFragment.drawElement(vector, true);
        BMEngine.setElementDescription(id, "测试");
    }

    private void toAddLineInMap(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_LINE, "线");
        vector.description = "描述";
        vector.outlineWidth = "5";
        vector.outlineColor = "#FF00FF00";

        vector.showLabel = true;
        vector.labelColor = "#FF00FF00";

        vector.geoPoints.add(geoPoint1);
        vector.geoPoints.add(geoPoint2);

        long id = mEarthFragment.drawElement(vector, true);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            VectorElement element1 = mEarthFragment.getThisElementAttribute(id);
            element1.geoPoints.add(new GeoPoint(113.565, 22.161));
            element1.outlineColor = "#FFFF0000";
            mEarthFragment.setThisElementAttribute(element1);

            handler.postDelayed(() -> {
                VectorElement element2 = mEarthFragment.getThisElementAttribute(id);
                element2.geoPoints.add(new GeoPoint(113.566, 22.162));
                element2.outlineColor = "#FF00FF00";
                mEarthFragment.setThisElementAttribute(element2);

                handler.postDelayed(() -> {
                    VectorElement element3 = mEarthFragment.getThisElementAttribute(id);
                    element3.geoPoints.add(new GeoPoint(113.567, 22.161));
                    element3.outlineColor = "#FFFF0000";
                    mEarthFragment.setThisElementAttribute(element3);
                }, 2000);
            }, 2000);
        }, 2000);
    }

    private void toAddCircleInMap(double lon, double lat) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "圆");
        vector.description = "描述";
        vector.showLabel = true;
        vector.labelColor = "#FF00FF00";

        GeoPoint geo1 = new GeoPoint(lon, lat, 0.0);
        GeoPoint geo2 = new GeoPoint(1000.0, 0.0, 0.0);
        vector.geoPoints.add(geo1);
        vector.geoPoints.add(geo2);

        mEarthFragment.drawElement(vector, true);
    }

    private void toAddEllipseInMap(double lon, double lat) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_ELLIPSE, "椭圆");
        vector.description = "描述";
        vector.showLabel = true;
        vector.labelColor = "#FF00FF00";

        GeoPoint geo1 = new GeoPoint(lon, lat, 0.0);
        GeoPoint geo2 = new GeoPoint(1000.0, 500.0, 0.0); // lon长半轴 lat短半轴
        GeoPoint geo3 = new GeoPoint(12.0, 0.0, 0.0);     // lon角度
        vector.geoPoints.add(geo1);
        vector.geoPoints.add(geo2);
        vector.geoPoints.add(geo3);

        mEarthFragment.drawElement(vector, true);
    }

    private void onDrawElement(int type) {
        StyleUtils.init(this);
        DefaultStyle style = new DefaultStyle();
        style.polylineColor = BMEngine.argb2rgba("#FFFF0000");
        style.polylineWidth = 2f;
        BMEngine.setDefaultStyle(style);

        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);
        mEarthFragment.toStartDrawElement(type, layer.id);
        // 绘制完成后会回调 onCallbackDrawElementStepEditing()
    }

    @Override
    public void onCallbackDrawElementStepEditing(VectorElement vectorElement) {
        runOnUiThread(() -> {
            mEarthFragment.toStopDrawElement();
            mEarthFragment.toCancelDrawingElement();
            // 关键：这里设置自定义图标
            vectorElement.isCustomPath = true;
            vectorElement.iconPath = "ic_wind.png";  // 只写文件名！
            vectorElement.iconScale = 0.6f;
            vectorElement.showLabel = false;

            // 把修改提交给引擎
            mEarthFragment.setThisElementAttribute(vectorElement);
            Log.e("Fangs", "==onCallbackDrawElementStepEditing==");
        });
    }

    @Override
    public void onCallbackDrawElementStepCreated(VectorElement vectorElement) {
        Log.e("Fangs", "==onCallbackDrawElementStepCreated==");
    }

    // time 跳转时间
    private void onAnimateTo(double lon, double lat, double time) {
        GeoPoint geoPoint = new GeoPoint(lon, lat);
        int height = 5000000;
        double pitch = -90.0;
        mEarthFragment.animateTo(geoPoint, height, time, pitch);
    }

    // 在线地图
    @Override
    public void onCreateEarthComplete() {
        Log.e("Fangs", "=====");
        isEarthReady = true;

        List<com.bigemap.bmcore.entity.Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty()) {
            com.bigemap.bmcore.entity.Provider provider = providers.get(1); // 1、内置在线地图
            mEarthFragment.changeMapSource(provider.mapId);
            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_BKG")) {
                mEarthFragment.animateToOfflineArea();
            }
        }

        if (longitudeData == 0.0 && latitudeData == 0.0) {
            onAnimateTo(113.5, 22.2, 0.0);
        } else {
            onAnimateTo(longitudeData, latitudeData, 0.0);
        }

        BMEngine.isShowBuilding(false);
        startTyphoonSimulation();
        double[][] points = {
                {34.582871, 123.510237},
                {32.079378, 124.919638},
                {30.243978, 123.389687},
                {26.870304, 123.205269},
                {25.958292, 121.090243}
        };

        for (double[] p : points) {
            toAddPointInMap(p[1], p[0]);
        }
//        startWind();
    }

    private void startWind() {
        long rootID = mEarthFragment.getRootLayerId();

        // 关键：创建一个独立图层，用来存放所有风点！！
        VectorElement windLayer = mEarthFragment.onCreateLayer(rootID, "风点图层", true);

        // 5 个风点位置
        double[][] points = {
                {34.582871, 123.510237},
                {32.079378, 124.919638},
                {30.243978, 123.389687},
                {26.870304, 123.205269},
                {25.958292, 121.090243}
        };

        for (double[] p : points) {
            addWindPoint(windLayer.id, p[0], p[1]);
        }
    }
    private void addWindPoint(long layerId, double lon, double lat) {

        VectorElement vector = new VectorElement(layerId, VectorElement.TYPE_POINT, "wind");
        vector.isCustomPath = true;
        vector.iconPath = "ic_wind.png";   // 你的风图标
        vector.iconScale = 0.7f;
        vector.showLabel = false;

        vector.geoPoints.add(new GeoPoint(lon, lat, 0.0));

        mEarthFragment.drawElement(vector, true);
    }

    @Override
    public void onCreateEarthFail(int i) {
    }

    @Override
    public void onScroll() {
    }

    @Override
    public void callbackEarthOrientation(float v) {
    }

    @Override
    public void callbackScreenCenterPoint(GeoPoint geoPoint, double v, long l, int i) {
        // 缩放等级逻辑可在此实现
    }

    @Override
    public void onSingleTapConfirmed(MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onLongPress(MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onCallbackSiWeiHistoryData(String[] strings) {
    }

    @Override
    public void onClickedElement(VectorElement vectorElement) {
        VectorElement element = mEarthFragment.getThisElementAttribute(vectorElement.id);
    }

    @Override
    public void onLongClickedElement(VectorElement vectorElement) {
        VectorElement element = mEarthFragment.getThisElementAttribute(vectorElement.id);
    }

    @Override
    public void onChangeMapSourceComplete(MapConfig mapConfig) {
    }

    @Override
    public void onChangeMapTypeGroupComplete(MapConfig mapConfig) {
    }

    @Override
    public void onCallbackHistoricalImagery(int[] ints) {
    }

    @Override
    public void onCallbackHistoricalImagery(String[] strings) {
    }

    @Override
    public void onCallbackAddedTrackPoint(GeoPoint geoPoint) {
    }

    @Override
    public void onLoadVectorFileStart(int i) {
    }

    @Override
    public void onLoadVectorFileDoing() {
    }

    @Override
    public void onLoadVectorFileComplete(boolean b, long l) {
    }

    @Override
    public void onLoadVectorFileComplete(VectorElement vectorElement) {
    }

    @Override
    public byte[] onFormatStringToPicture(String s) {
        return new byte[0];
    }

    @Override
    public byte[] webPToPng(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public boolean onUpdateOfflineCallback(int i, int i1) {
        return false;
    }

    // 设置在线地图加载
    public void changeOnlineMapSource(int index) {
        List<com.bigemap.bmcore.entity.Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty() && index >= 0 && index < providers.size()) {
            com.bigemap.bmcore.entity.Provider provider = providers.get(index);
            mEarthFragment.changeMapSource(provider.mapId);
        }
    }

    // 离线地图加载
    public void changeOfflineMapSource() {
        List<com.bigemap.bmcore.entity.Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty()) {
            com.bigemap.bmcore.entity.Provider provider = providers.get(providers.size() - 1);
            mEarthFragment.changeMapSource(provider.mapId);

            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_PKG")) {
                mEarthFragment.animateToOfflineArea();
            }

            toAddPointInMap(longitudeData, latitudeData);
            onAnimateTo(longitudeData, latitudeData, 0.0);
        }
    }

    public void toTestAnimateTo() {
        toAddPointInMap(longitudeData, latitudeData);
        onAnimateTo(longitudeData, latitudeData, 3.0);
    }

    public void updateLocation() {
        GeoPoint geoPoint = new GeoPoint(longitudeData, latitudeData);
        Toast.makeText(
                MainActivity2.this,
                "当前经度：" + longitudeData + "\n当前纬度：" + latitudeData,
                Toast.LENGTH_SHORT
        ).show();
        mEarthFragment.updateLocation(geoPoint, 100.0, 0.0);
    }

    // 画点
    public void toAddPointInMap() {
        toAddPointInMap(longitudeData, latitudeData);
        onAnimateTo(longitudeData, latitudeData, 3.0);
    }

    // 画线
    public void toAddLineInMap() {
        GeoPoint geoPoint1 = new GeoPoint(longitudeData, latitudeData);
        GeoPoint geoPoint2 = new GeoPoint(longitudeData + 0.000001, latitudeData + 0.000001);
        toAddLineInMap(geoPoint1, geoPoint2);
        onAnimateTo(longitudeData, latitudeData, 0.0);
    }

    // 添加圆
    public void toAddCircleInMap() {
        toAddCircleInMap(longitudeData, latitudeData);
        onAnimateTo(longitudeData, latitudeData, 0.0);
    }

    // 添加椭圆
    public void toAddEllipseInMap() {
        toAddEllipseInMap(longitudeData, latitudeData);
        onAnimateTo(longitudeData, latitudeData, 0.0);
    }

    // 手画点
    public void onDrawPointElement() {
        onDrawElement(Constants.DRAW_ELEMENT_TYPE_POINT);
        Toast.makeText(getApplication(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 手画线
    public void onDrawLineElement() {
        onDrawElement(Constants.DRAW_ELEMENT_TYPE_LINE);
        Toast.makeText(getApplication(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 手画面
    public void onDrawPlaneElement() {
        onDrawElement(Constants.DRAW_ELEMENT_TYPE_PLANE);
        Toast.makeText(getApplication(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 撤销当前线绘制
    public void toRetreatDrawingElement() {
        mEarthFragment.toRetreatDrawingElement();
    }

    public void loadKMLFile() {
        String url = "";
        if (url.isEmpty()) {
            Toast.makeText(getApplication(), "添加路径", Toast.LENGTH_LONG).show();
        } else {
            long rootID = mEarthFragment.getRootLayerId();
            mEarthFragment.loadKMLFile(rootID, url);
        }
    }

    private static final String TAG_EARTH_FRAGMENT = "TAG_EARTH_FRAGMENT";
    private static final String TAG_PAGE_ONE_FRAGMENT = "TAG_PAGE_ONE_FRAGMENT";
    private static final String TAG_PAGE_TWO_FRAGMENT = "TAG_PAGE_TWO_FRAGMENT";

    private static final String TEST_MAP_SOURCE_URL =
            "http://services.arcgisonline.com/ArcGIS/services/World_Imagery/MapServer?mapname=Layers&layer=_alllayers&format=PNG&level={z}&row={y}&column={x}";
    private static final String TEST_MAP_SOURCE_URL1 =
            "https://webst0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=6";
    private static final String TEST_MAP_SOURCE_URL2 =
            "https://wprd0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=8";
    private static final String TEST_MAP_SOURCE_URL3 =
            "https://hssk.hngqyun.cn:9000/bigemap.6h6bjjiu/tiles/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiY3VzXzZucHJ1OXNmIiwiYSI6IjA3N2Fnc3F3OTN3dW03OXhtN2VtNDB0dnAiLCJ0Ijo0fQ.9gsbkTLAIbujYmFCgLdXX0b2KVM4DVuxG2ZDRj31PsQ";
}
