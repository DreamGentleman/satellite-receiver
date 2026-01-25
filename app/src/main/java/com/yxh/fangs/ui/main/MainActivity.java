package com.yxh.fangs.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.VectorElement;
import com.bigemap.bmcore.listener.OperationCallback;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.locaiton.LocationRepository;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.Sos2Fragment;
import com.yxh.fangs.ui.history.HistoryMessageActivity;
import com.yxh.fangs.ui.setting.SettingActivity;
import com.yxh.fangs.ui.sos.SosActivity;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.SerialNumberParserV2;

public class MainActivity extends BaseActivity implements OperationCallback {

    private MainUiBinder ui;
    private MapController mapController;
    private MessageDispatcher dispatcher;
    private MessagePollingManager pollingManager;

    private LocationRepository locationRepository;

    private ActivityResultLauncher<Intent> sosLauncher;
    private ActivityResultLauncher<Intent> layoutLauncher;

    private double longitudeData = 0.0;
    private double latitudeData = 0.0;

    private LayerType selectedLayerType = LayerType.LOCATION;
    private WeatherBean selectedWeatherBean;
    private String drawContent = "台风预警,气象信息";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ui = new MainUiBinder(this);

        mapController = new MapController(this, getSupportFragmentManager(), R.id.flt_container);
        mapController.initMapAsync(this); // OperationCallback = Main3

        dispatcher = new MessageDispatcher(this, ui, mapController,
                new MessageDispatcher.DispatchState() {
                    @Override
                    public void setSelectedWeather(WeatherBean bean) {
                        selectedWeatherBean = bean;
                    }

                    @Override
                    public WeatherBean getSelectedWeather() {
                        return selectedWeatherBean;
                    }

                    @Override
                    public void setSelectedLayer(LayerType type) {
                        selectedLayerType = type;
                    }

                    @Override
                    public LayerType getSelectedLayer() {
                        return selectedLayerType;
                    }

                    @Override
                    public double getLon() {
                        return longitudeData;
                    }

                    @Override
                    public double getLat() {
                        return latitudeData;
                    }
                }
        );


        pollingManager = new MessagePollingManager(dispatcher);

        initLaunchers();
        bindUi();
        initLicenseValidityPeriod();
        initNotice();

        checkPermissionAndStartLocation();
        pollingManager.start(); // 轮询历史消息
    }

    private void initNotice() {
        NoticeClickRouter router = new NoticeClickRouter(this, dispatcher);
        ui.bindNoticeClickListener(router);
    }

    private void bindUi() {
        ui.bindCommonClicks(
                () -> startActivity(new Intent(MainActivity.this, HistoryMessageActivity.class)),
                () -> startActivity(new Intent(MainActivity.this, SettingActivity.class)),
                () -> {
                    if (selectedWeatherBean == null) {
                        Toast.makeText(this, "暂无最新天气信息！", Toast.LENGTH_SHORT).show();
                    } else {
                        dispatcher.showWeatherDetail(selectedWeatherBean);
                    }
                },
                () -> sosLauncher.launch(new Intent(MainActivity.this, SosActivity.class)),
                () -> {
                    Intent intent = new Intent(MainActivity.this, LayoutActivity.class);
                    intent.putExtra("drawContent", drawContent);
                    layoutLauncher.launch(intent);
                },
                () -> mapController.animateTo(longitudeData, latitudeData, 0.0),
                enabled -> dispatcher.setTurnOnNotice(enabled)
        );

        ui.bindRefresh(() -> dispatcher.fetchAndHandleMessages(true));
    }

    private void initLaunchers() {
        sosLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        boolean sosResult = result.getData().getBooleanExtra("sos_result", false);
                        String sosContent = result.getData().getStringExtra("sos_content");
                        String sosType = result.getData().getStringExtra("sos_type");
                        if (sosResult) {
                            FragmentManager fm = getSupportFragmentManager();
                            if (fm.findFragmentByTag("sos") == null) {
                                Sos2Fragment.newInstance(sosType, sosContent).show(fm, "sos");
                            }
                        }
                    }
                }
        );

        layoutLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        drawContent = result.getData().getStringExtra("handleSosSelected");
                        dispatcher.applyDrawContent(drawContent);
                    }
                }
        );
    }

    private void initLicenseValidityPeriod() {
        String licenseValidityPeriod = "未授权";
        String key = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");

        if (!TextUtils.isEmpty(key)) {
            String parsed = SerialNumberParserV2.parseDateTime(key, "QX1SB");
            if (!TextUtils.isEmpty(parsed) && parsed.length() == 14) {
                parsed = parsed.substring(0, parsed.length() - 6);
                licenseValidityPeriod =
                        parsed.substring(0, 4) + "年"
                                + Integer.parseInt(parsed.substring(4, 6)) + "月"
                                + Integer.parseInt(parsed.substring(6, 8)) + "日";
            }
        }

        ui.setLicensePeriodText(licenseValidityPeriod);
    }

    // ========= 权限定位 =========

    private void checkPermissionAndStartLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        locationRepository = new LocationRepository(this);
        locationRepository.start(new LocationRepository.Callback() {
            @Override
            public void onLocationChanged(Location location) {
                longitudeData = location.getLongitude();
                latitudeData = location.getLatitude();
                dispatcher.uploadDeviceLocation(DeviceUtils.getDeviceId(MainActivity.this), longitudeData, latitudeData);
            }

            @Override
            public void onError(String msg) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "未授予定位权限，无法获取位置", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationRepository != null) locationRepository.stop();
        pollingManager.stop();
        mapController.release();
        dispatcher.release();
    }

    // ========= OperationCallback: 地图加载完成 =========

    @Override
    public void onCreateEarthComplete() {
        mapController.onEarthReady();

        // 默认视角
        mapController.animateTo(126.5292, 33.3617, 0.0);

        // 先应用图层选项
        dispatcher.applyDrawContent(drawContent);

        // 默认显示船只位置
        dispatcher.setSelectedLayer(LayerType.LOCATION);
        mapController.drawPoint(LayerType.LOCATION, longitudeData, latitudeData, R.mipmap.ic_fishing_vessel, 1f, 0);
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
    public void callbackScreenCenterPoint(GeoPoint center, double altitude, long l, int i) {
    }

    @Override
    public void onSingleTapConfirmed(android.view.MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onLongPress(android.view.MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onCallbackSiWeiHistoryData(String[] strings) {
    }

    @Override
    public void onCallbackDrawElementStepEditing(VectorElement vectorElement) {

    }

    @Override
    public void onCallbackDrawElementStepCreated(VectorElement vectorElement) {

    }

    @Override
    public void onClickedElement(com.bigemap.bmcore.entity.VectorElement vectorElement) {
    }

    @Override
    public void onLongClickedElement(com.bigemap.bmcore.entity.VectorElement vectorElement) {
    }

    @Override
    public void onChangeMapSourceComplete(com.bigemap.bmcore.entity.MapConfig mapConfig) {
    }

    @Override
    public void onChangeMapTypeGroupComplete(com.bigemap.bmcore.entity.MapConfig mapConfig) {
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
    public void onLoadVectorFileComplete(com.bigemap.bmcore.entity.VectorElement vectorElement) {
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
}
