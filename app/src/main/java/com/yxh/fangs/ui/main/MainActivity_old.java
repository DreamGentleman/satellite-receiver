package com.yxh.fangs.ui.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigemap.bmcore.BMEngine;
import com.bigemap.bmcore.EarthFragment;
import com.bigemap.bmcore.constant.Constants;
import com.bigemap.bmcore.entity.CustomMapSource;
import com.bigemap.bmcore.entity.DefaultStyle;
import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.MapConfig;
import com.bigemap.bmcore.entity.Provider;
import com.bigemap.bmcore.entity.VectorElement;
import com.bigemap.bmcore.listener.OperationCallback;
import com.bigemap.bmcore.sp.StyleUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.yxh.fangs.R;
import com.yxh.fangs.adapter.NoticeListAdapter;
import com.yxh.fangs.bean.BeidouBean;
import com.yxh.fangs.bean.DeviceLocationRecordRequest;
import com.yxh.fangs.bean.DeviceLocationRecordResponse;
import com.yxh.fangs.bean.DeviceStatusChangeRequest;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.KmlStyle;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.LocationBean;
import com.yxh.fangs.bean.MapShape;
import com.yxh.fangs.bean.Message;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.SosEventRequest;
import com.yxh.fangs.bean.SosEventResponse;
import com.yxh.fangs.bean.StatusLogResponse;
import com.yxh.fangs.bean.TyphoonBean;
import com.yxh.fangs.bean.TyphoonBean2;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.core.tts.TTSManager;
import com.yxh.fangs.data.local.db.MessageDatabase;
import com.yxh.fangs.data.network.HttpUtils;
import com.yxh.fangs.data.network.api.UrlUtils;
import com.yxh.fangs.data.repository.WeatherRepository;
import com.yxh.fangs.locaiton.LocationRepository;
import com.yxh.fangs.map.draw.LineDrawManager;
import com.yxh.fangs.map.draw.PlaneDrawManager;
import com.yxh.fangs.map.draw.TyphoonDrawManager2;
import com.yxh.fangs.map.layer.LayerManager;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.dialog.RemindDialog;
import com.yxh.fangs.ui.dialog.Sos2Fragment;
import com.yxh.fangs.ui.dialog.WeatherDialog;
import com.yxh.fangs.ui.history.HistoryMessageActivity;
import com.yxh.fangs.ui.image.ImageDetailActivity;
import com.yxh.fangs.ui.setting.SettingActivity;
import com.yxh.fangs.ui.sos.SosActivity;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.SerialNumberParserV2;
import com.yxh.fangs.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity_old extends BaseActivity implements OperationCallback {
    private ArrayList<AlertDialog> dialogs = new ArrayList<>();
    private static final String TAG_EARTH_FRAGMENT = "TAG_EARTH_FRAGMENT";
    private static final String TAG_PAGE_ONE_FRAGMENT = "TAG_PAGE_ONE_FRAGMENT";
    private static final String TAG_PAGE_TWO_FRAGMENT = "TAG_PAGE_TWO_FRAGMENT";
    private static final String TEST_MAP_SOURCE_URL = "http://services.arcgisonline.com/ArcGIS/services/World_Imagery/MapServer?mapname=Layers&layer=_alllayers&format=PNG&level={z}&row={y}&column={x}";
    private static final String TEST_MAP_SOURCE_URL1 = "https://webst0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=6";
    private static final String TEST_MAP_SOURCE_URL2 = "https://wprd0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=8";
    private static final String TEST_MAP_SOURCE_URL3 = "https://hssk.hngqyun.cn:9000/bigemap.6h6bjjiu/tiles/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiY3VzXzZucHJ1OXNmIiwiYSI6IjA3N2Fnc3F3OTN3dW03OXhtN2VtNDB0dnAiLCJ0Ijo0fQ.9gsbkTLAIbujYmFCgLdXX0b2KVM4DVuxG2ZDRj31PsQ";
    private EarthFragment mEarthFragment;
    // 标记地图是否已加载
    private boolean isEarthReady = false;
    private double longitudeData = 0.0;
    private double latitudeData = 0.0;
    private TextView tvScrollingMessage;
    private TextView tvValidityPeriod;
    private SmartRefreshLayout refreshLayout;
    private final List<Long> typhoonLayerIds = new ArrayList<>();
    private Disposable disposable;
    private MessageDialog dialog;
    private boolean turnOnNotice = true;
    private LayerManager layerManager = new LayerManager();
    private NoticeListAdapter noticeListAdapter;
    private Disposable pollingDisposable;
    private String readNotice = "";
    private ActivityResultLauncher<Intent> sosLauncher;
    private ActivityResultLauncher<Intent> layoutLauncher;
    private LayerType selectedLayerType;
    private WeatherBean selectedWeatherBean;
    private LocationRepository locationRepository;
    private LineDrawManager lineDrawManager;
    private PlaneDrawManager planeDrawManager;
    private long tyElementId = -1;
    //            private String drawContent = "渔场,机轮拖网渔业禁渔线,领海基线,中韩渔业协定水域,中日渔业协定水域";
    private String drawContent = "台风预警,气象信息";

    public String loadJsonFromAssets(Context context, String fileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap();
        // 检查并请求权限
        checkPermissionAndStartLocation();
        //初始化视图
        initView();
        // 点击事件
        initClicked();
        //TODO 模拟消息
        initNotice();
//        uploadReceiverStatusLog();
        initDataBase();
        initLicenseValidityPeriod();
        initLast24HoursMessage();
        initLaunch();
    }

    private void initLaunch() {
        sosLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            boolean sosResult = data.getBooleanExtra("sos_result", false);
                            String sosContent = data.getStringExtra("sos_content");
                            String sosType = data.getStringExtra("sos_type");
                            if (sosResult) {
                                FragmentManager fm = getSupportFragmentManager();
                                if (fm.findFragmentByTag("sos") == null) {
                                    Sos2Fragment.newInstance(sosType, sosContent).show(fm, "sos");
                                }
                            }
                        }
                    }
                }
        );
        layoutLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            drawContent = data.getStringExtra("handleSosSelected");
                            if (TextUtils.isEmpty(drawContent)) {
                                hideLayer(LayerType.FISHING_GROUND);
                                hideLayer(LayerType.NO_FISHING_LINE);
                                hideLayer(LayerType.COAST_LINE);
                                hideLayer(LayerType.CKFA);
                                hideLayer(LayerType.CJFA);
                                hideLayer(LayerType.TYPHOON);
                                hideLayer(LayerType.RAINSTORM);
                                return;
                            }
                            boolean showFishing = drawContent.contains("渔场");
                            boolean showNoFishLine = drawContent.contains("机轮拖网渔业禁渔线");
                            boolean showCoast = drawContent.contains("领海基线");
                            boolean showCKFA = drawContent.contains("中韩渔业协定水域");
                            boolean showCJFA = drawContent.contains("中日渔业协定水域");
                            boolean showTyphoon = drawContent.contains("台风预警");
                            boolean showRain = drawContent.contains("气象信息");

                            hideLayer(LayerType.FISHING_GROUND);
                            hideLayer(LayerType.NO_FISHING_LINE);
                            hideLayer(LayerType.COAST_LINE);
                            hideLayer(LayerType.CKFA);
                            hideLayer(LayerType.CJFA);
                            hideLayer(LayerType.TYPHOON);
                            hideLayer(LayerType.RAINSTORM);

                            if (showFishing) drawBaseLine(LayerType.FISHING_GROUND);
                            if (showNoFishLine) drawBaseLine(LayerType.NO_FISHING_LINE);
                            if (showCoast) drawBaseLine(LayerType.COAST_LINE);
                            if (showCKFA) drawBaseLine(LayerType.CKFA);
                            if (showCJFA) drawBaseLine(LayerType.CJFA);
                            if (showTyphoon) showLayer(LayerType.TYPHOON);
                            if (showRain) showLayer(LayerType.RAINSTORM);
                        }
                    }
                }
        );
    }

    private void initLast24HoursMessage() {
        pollingDisposable = Observable
                // 立即一次，之后每 30s
                .interval(10, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())   // 定时器在线程池跑
                .subscribe(tick -> {
                    getHistoryMessage();
                }, throwable -> {
                    // interval 自身异常（一般不会进）
                });
    }

    private void getHistoryMessage() {
        HttpUtils.get(UrlUtils.history(1, 1000), new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.finishRefresh();
                }
                LogUtils.json(body);
                Gson gson = new Gson();
                Last24HoursBean last24HoursBean = gson.fromJson(body, Last24HoursBean.class);
                boolean handled = false;
                if (last24HoursBean.getCode() == 200) {
                    noticeListAdapter.setDataList(last24HoursBean.getRows());

                    for (int i = 0; i < last24HoursBean.getRows().size(); i++) {
                        Last24HoursBean.RowsBean rowsBean = last24HoursBean.getRows().get(i);
                        if (readNotice.contains(rowsBean.getId())) {
                            continue;
                        }
                        readNotice = readNotice + "," + rowsBean.getId();
                        if (!dialogs.isEmpty()) {
                            for (int i1 = 0; i1 < dialogs.size(); i1++) {
                                AlertDialog alertDialog = dialogs.get(i1);
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                    dialogs.remove(alertDialog);
                                }
                            }
                        }
                        switch (rowsBean.getMessageType()) {
                            case NoticeType.NOTICE_BEIDOU: {
                                BeidouBean beidouBean = gson.fromJson(rowsBean.getContent(), BeidouBean.class);
                                String content = "北斗通道号为：" + beidouBean.getBeidouChannel() + "，卫星编号为：" + beidouBean.getSatelliteId() + "，信号强度：" + beidouBean.getSignalStrength();
                                speak("您有一条北斗消息，" + content);
                                MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                                dialog.show();
                                dialogs.add(dialog);
                                if (i == 0) {
                                    tvScrollingMessage.setText(content);
                                }
                            }
                            handled = true;
                            break;
                            case NoticeType.NOTICE_ALERT: {
                                WarnBean warnBean = gson.fromJson(rowsBean.getContent(), WarnBean.class);
                                String content = warnBean.getWarningLevel();
                                speak("您有一条预警信息，" + content);
                                MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                                dialog.show();
                                dialogs.add(dialog);

                                if (i == 0) {
                                    tvScrollingMessage.setText(content);
                                }

                                selectedLayerType = LayerType.RAINSTORM;
                                if (warnBean == null
                                        || warnBean.getWarningArea() == null
                                        || warnBean.getWarningArea().getCoordinates() == null) {
                                    return;
                                }

                                WarnBean.WarningAreaBean area = warnBean.getWarningArea();
                                WarnBean.WarningAreaBean.CoordinatesBean c = area.getCoordinates();

                                switch (area.getType()) {

                                    /* ======================
                                     * 1. 多边形预警 polygon
                                     * ====================== */
                                    case "polygon": {

                                        if (c.getPoints() == null || c.getPoints().size() < 3)
                                            return;

                                        List<GeoPoint> geoPoints = new ArrayList<>();
                                        for (WarnBean.WarningAreaBean.CoordinatesBean.PointsBean p : c.getPoints()) {
                                            geoPoints.add(new GeoPoint(p.getLng(), p.getLat(), 0));
                                        }

                                        // 闭合（Bigemap 面必须闭合）
                                        GeoPoint first = geoPoints.get(0);
                                        GeoPoint last = geoPoints.get(geoPoints.size() - 1);
                                        if (first.lon != last.lon || first.lat != last.lat) {
                                            geoPoints.add(new GeoPoint(first.lon, first.lat, 0));
                                        }

                                        planeDrawManager.draw(warnBean.getWarningLevel(), geoPoints, c.getColor(), c.getColor(), 6);
                                        break;
                                    }

                                    /* ======================
                                     * 2. 圆形预警 circle
                                     * ====================== */
                                    case "circle": {

                                        if (c.getCenter() == null) return;

                                        addTyphoonCircle(
                                                c.getCenter().getLng(),
                                                c.getCenter().getLat(),
                                                c.getColor(),
                                                c.getArea() > 0
                                                        ? Math.sqrt(c.getArea() / Math.PI)  // 有 area 就反算半径
                                                        : 100_000                            // 兜底 100km
                                                , warnBean.getWarningLevel()
                                        );
                                        break;
                                    }

                                    /* ======================
                                     * 3. 矩形预警 rectangle
                                     * ====================== */
                                    case "rectangle": {

                                        if (c.getBounds() == null) return;

                                        drawRectangle(
                                                c.getBounds().getSw().getLng(),
                                                c.getBounds().getSw().getLat(),
                                                c.getBounds().getNe().getLng(),
                                                c.getBounds().getNe().getLat(),
                                                c.getColor(), warnBean.getWarningLevel()
                                        );
                                        break;
                                    }

                                    /* ======================
                                     * 4. 点预警 marker
                                     * ====================== */
                                    case "marker": {

                                        if (c.getCenter() == null) return;

                                        int icon;
                                        switch (warnBean.getWarningLevel()) {
                                            case "红色预警":
                                                icon = R.mipmap.ic_sos_1;
                                                break;
                                            case "黄色预警":
                                                icon = R.mipmap.ic_sos_1;
                                                break;
                                            case "蓝色预警":
                                            default:
                                                icon = R.mipmap.ic_sos_1;
                                                break;
                                        }

                                        toAddPointInMap(
                                                c.getCenter().getLng(),
                                                c.getCenter().getLat(),
                                                icon,
                                                1.0f,
                                                0
                                        );
                                        break;
                                    }
                                }
                            }
                            handled = true;
                            break;
                            case NoticeType.NOTICE_NOTICE_IMAGE: {
                                ImageBean imageBean = gson.fromJson(rowsBean.getContent(), ImageBean.class);
                                if (imageBean == null) {
                                    Toast.makeText(MainActivity_old.this, "图片信息异常！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                ImageCache.base64 = imageBean.getBase64();
                                speak("您有一条图片消息");
                                Intent intent = new Intent(MainActivity_old.this, ImageDetailActivity.class);
                                intent.putExtra("time", rowsBean.getPublishTime());
                                startActivity(intent);

                                if (i == 0) {
                                    tvScrollingMessage.setText("您有一条图片消息");
                                }
                            }
                            handled = true;
                            break;
                            case NoticeType.NOTICE_SMS: {
                                speak("您有一条短消息，" + rowsBean.getTitle());
                                MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), rowsBean.getContent(), rowsBean.getPublishTime());
                                dialog.show();
                                dialogs.add(dialog);
                                if (i == 0) {
                                    tvScrollingMessage.setText(rowsBean.getTitle());
                                }
                            }
                            handled = true;
                            break;
                            case NoticeType.NOTICE_TYPHOON: {
//                                TyphoonBean typhoonBean = gson.fromJson(rowsBean.getContent(), TyphoonBean.class);
//                                speak("您有一条台风" + typhoonBean.getTyphoonName() + "的消息");
//                                MessageDialog dialog = MessageDialog.newInstance(MainActivity.this, rowsBean.getTitle(), typhoonBean.getMovingDirection(), rowsBean.getTitle());
//                                dialog.show();
//                                dialogs.add(dialog);
//                                if (i == 0) {
//                                    tvScrollingMessage.setText("您有一条台风" + typhoonBean.getTyphoonName() + "的消息");
//                                }
//                                selectedLayerType = LayerType.TYPHOON;
//                                if (typhoonBean == null
//                                        || typhoonBean.getTyphoonInfo() == null
//                                        || typhoonBean.getTyphoonInfo().getMapData() == null
//                                        || typhoonBean.getTyphoonInfo().getMapData().getCoordinates() == null) {
//                                    return;
//                                }
//                                TyphoonDrawManager typhoonManager =
//                                        new TyphoonDrawManager(mEarthFragment);
//
//                                typhoonManager.drawTyphoon(typhoonBean);
                                drawTest2(rowsBean.getContent(), Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                            }
                            handled = true;
                            break;
                            case NoticeType.NOTICE_WEATHER: {
                                selectedLayerType = LayerType.RAINSTORM;
                                Type type2 = new TypeToken<List<WeatherBean>>() {
                                }.getType();
                                List<WeatherBean> list2 = new Gson().fromJson(rowsBean.getContent(), type2);
                                if (list2 != null && !list2.isEmpty()) {
                                    WeatherBean weatherBean = list2.get(0);
                                    selectedWeatherBean = weatherBean;
                                    String message = buildForecastText(weatherBean);
                                    speak(message);
                                    WeatherDialog dialog = WeatherDialog.newInstance(MainActivity_old.this, message, WeatherRepository.getWeatherIcon(weatherBean.getWeatherPhenomenon()));
                                    dialog.show();
                                    dialogs.add(dialog);
                                    if (i == 0) {
                                        tvScrollingMessage.setText(message);
                                    }
                                }


                                for (int j = 0; j < list2.size(); j++) {
                                    WeatherBean weatherBean = list2.get(j);
                                    MapShape shape = new Gson().fromJson(weatherBean.getMapJson(), MapShape.class);
                                    if ("circle".equals(shape.type)) {
                                        addTyphoonCircle(
                                                shape.coordinates.center.lng,
                                                shape.coordinates.center.lat,
                                                shape.coordinates.color,
                                                shape.coordinates.radius, ""
                                        );
                                        toAddPointInMap(shape.coordinates.center.lng, shape.coordinates.center.lat, WeatherRepository.getWeatherIcon(weatherBean.getWeatherPhenomenon()), 2f, 0);
                                    } else if ("rectangle".equals(shape.type)) {
                                        drawRectangle(
                                                shape.coordinates.bounds.sw.lng,
                                                shape.coordinates.bounds.sw.lat,
                                                shape.coordinates.bounds.ne.lng,
                                                shape.coordinates.bounds.ne.lat,
                                                shape.coordinates.color, ""
                                        );
                                        toAddPointInMap(shape.coordinates.center.lng, shape.coordinates.center.lat, WeatherRepository.getWeatherIcon(weatherBean.getWeatherPhenomenon()), 2f, 0);
                                    }
                                }
                            }
                            handled = true;
                            break;
                        }
                        if (handled) {
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(String msg) {
                // 错误处理
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.finishRefresh();
                }
            }
        });
    }

    private void initLicenseValidityPeriod() {
        String licenseValidityPeriod = "未授权";
        String licenseValidityPeriodKey = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");

        if (TextUtils.isEmpty(licenseValidityPeriodKey)) {
            licenseValidityPeriod = "未授权";
        } else {
            String parsedDateTime = SerialNumberParserV2.parseDateTime(licenseValidityPeriodKey, "QX1SB");
            if (TextUtils.isEmpty(parsedDateTime) || parsedDateTime.length() != 14) {
                licenseValidityPeriod = "未授权";
            } else {
                parsedDateTime = parsedDateTime.substring(0, parsedDateTime.length() - 6);
                licenseValidityPeriod = parsedDateTime.substring(0, 4) + "年" + Integer.parseInt(parsedDateTime.substring(4, 6)) + "月" + Integer.parseInt(parsedDateTime.substring(6, 8)) + "日";
            }
        }
        tvValidityPeriod.setText(licenseValidityPeriod);
    }

    private void initMap() {
        Observable.fromCallable(() -> {
                    copyFiles();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ok -> initFragment(),
                        e -> Toast.makeText(this, "地图初始化失败", Toast.LENGTH_LONG).show()
                );
    }

    //初始化消息数据库
    private void initDataBase() {
        MessageDatabase db = MessageDatabase.getInstance(this);
        String json = loadJsonFromAssets(this, "message.json");
        // 解析 JSON
        Gson gson = new Gson();
        List<Message> list = gson.fromJson(json, new TypeToken<List<Message>>() {
        }.getType());
        // 转数组（Room 插入用）
        Message[] array = list.toArray(new Message[0]);
        // 真正执行插入（Completable 必须 subscribe 才会执行）
        db.messageDao().insertMessages(array).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            SPUtils.putBoolean(AppConstants.MESSAGEINIT, true);
            Log.d("ROOM", "导入成功");
        }, error -> Log.e("ROOM", "导入失败: " + error.getMessage()));

    }

    private void uploadReceiverStatusLog() {
        DeviceStatusChangeRequest deviceStatusChangeRequest = new DeviceStatusChangeRequest();
        deviceStatusChangeRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        deviceStatusChangeRequest.setNewStatus("0");
        deviceStatusChangeRequest.setChangeReason("这是变更原因！");
        deviceStatusChangeRequest.setRepairProgress("这是进度！");
        deviceStatusChangeRequest.setRepairStartTime("2025-12-05 17:30:37");
        deviceStatusChangeRequest.setRepairEndTime("2025-12-06 17:30:37");
        deviceStatusChangeRequest.setRemark("这是备注信息！");
        String json = new Gson().toJson(deviceStatusChangeRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getReceiverStatusLogUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                StatusLogResponse response = gson.fromJson(body, StatusLogResponse.class);
                if (response.getCode() == 200) {
                    Toast.makeText(MainActivity_old.this, "设备变更信息上传成功！", Toast.LENGTH_SHORT).show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity_old.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadDeviceLocation(double longitudeData, double latitudeData) {
        LogUtils.i("开始上传定位信息");
        DeviceLocationRecordRequest deviceLocationRecordRequest = new DeviceLocationRecordRequest();
        deviceLocationRecordRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        deviceLocationRecordRequest.setLongitude(String.valueOf(longitudeData));
        deviceLocationRecordRequest.setLatitude(String.valueOf(latitudeData));
//        deviceLocationRecordRequest.setAddress("");
//        deviceLocationRecordRequest.setSpeed(1);
//        deviceLocationRecordRequest.setDirection(45);
//        deviceLocationRecordRequest.setAltitude(50);
        String json = new Gson().toJson(deviceLocationRecordRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getDeviceLocationAddUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.i("定位信息上传成功");
                LogUtils.json(body);
                Gson gson = new Gson();
                DeviceLocationRecordResponse response = gson.fromJson(body, DeviceLocationRecordResponse.class);
                if (response.getCode() == 200) {
                    Toast.makeText(MainActivity_old.this, "位置信息上传成功！", Toast.LENGTH_LONG).show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity_old.this, msg, Toast.LENGTH_SHORT).show();
                LogUtils.i("定位信息上传失败");
            }
        });
    }

    private void uploadDeviceSos() {
        SosEventRequest sosEventRequest = new SosEventRequest();
        sosEventRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
//        sosEventRequest.setLongitude(String.valueOf(longitudeData));
//        sosEventRequest.setLatitude(String.valueOf(latitudeData));
//        sosEventRequest.setEmergencyLevel("1");
//        sosEventRequest.setDescription("这是急救信息！");
//        sosEventRequest.setRemark("这是急救信息的备注！");
        String json = new Gson().toJson(sosEventRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getSosEventStartUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                SosEventResponse response = gson.fromJson(body, SosEventResponse.class);
                if (response.getCode() == 200) {
                    RemindDialog dialog = RemindDialog.newInstance(MainActivity_old.this, "SOS紧急求助信息已发送");
                    dialog.show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity_old.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
//        Button btn11 = findViewById(R.id.btn_11);
//        Button btn12 = findViewById(R.id.btn_12);
//        Button btn13 = findViewById(R.id.btn_13);
//        btn11.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (tyElementId != -1) {
//                    mEarthFragment.setElementVisible(tyElementId, false);
//                }
//                drawTest(11);
//            }
//        });
//        btn12.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (tyElementId != -1) {
//                    mEarthFragment.setElementVisible(tyElementId, false);
//                }
//                drawTest(12);
//            }
//        });
//        btn13.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (tyElementId != -1) {
//                    mEarthFragment.setElementVisible(tyElementId, false);
//                }
//                drawTest(13);
//            }
//        });
//        tvValidityPeriod = findViewById(R.id.tv_validity_period);
//        refreshLayout = findViewById(R.id.refreshLayout);
//        tvScrollingMessage = findViewById(R.id.tv_scrolling_message);
//        tvScrollingMessage.setSelected(true);
//        refreshLayout.setEnableRefresh(true);
//        refreshLayout.setEnableLoadMore(false);
//        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
//                getHistoryMessage();
//            }
//        });
    }

    private void initFragment() {
        mEarthFragment = EarthFragment.getInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.flt_container, mEarthFragment, TAG_EARTH_FRAGMENT).commitAllowingStateLoss();
    }

    private void copyFiles() {
        boolean copied = SPUtils.getBoolean(AppConstants.KEYASSETSCOPIED, false);
        if (copied) {
            LogUtils.i("assets 已拷贝，跳过");
            return;
        }

        long start = System.currentTimeMillis();

        Utils.INSTANCE.copyAssets(this, "img", getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(this, "map", getFilesDir().getPath());

        SPUtils.putBoolean(AppConstants.KEYASSETSCOPIED, true);

        LogUtils.i("assets 拷贝完成，耗时：" + (System.currentTimeMillis() - start) + "ms");
        // 拷贝文件到文件系统
        Utils.INSTANCE.copyAssets(this, "img", getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(this, "map", getFilesDir().getPath());
        // Utils.INSTANCE.copyAssets(this, "同名图标", BMEngine.getIconPath());
    }

    private void initNotice() {
        RecyclerView rvNotice = findViewById(R.id.rv_notice);
        rvNotice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvNotice.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(15, 7, 10, 0);
            }
        });
        noticeListAdapter = new NoticeListAdapter(null);
        noticeListAdapter.setOnItemClickListener(new NoticeListAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(int position) {
                    Last24HoursBean.RowsBean rowsBean = noticeListAdapter.getDataList().get(position);
                    switch (rowsBean.getMessageType()) {
                        case NoticeType.NOTICE_BEIDOU: {
                            Gson gson = new Gson();
                            BeidouBean beidouBean = gson.fromJson(rowsBean.getContent(), BeidouBean.class);
                            String content = "北斗通道号为：" + beidouBean.getBeidouChannel() + "，卫星编号为：" + beidouBean.getSatelliteId() + "，信号强度：" + beidouBean.getSignalStrength();
                            speak("您有一条北斗消息，" + content);
                            MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                            dialog.show();
                        }
                        break;
                        case NoticeType.NOTICE_ALERT: {
                            Gson gson = new Gson();
                            WarnBean warnBean = gson.fromJson(rowsBean.getContent(), WarnBean.class);
                            String content = warnBean.getWarningLevel();
                            speak("您有一条预警信息，" + content);
                            MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                            dialog.show();
                        }
                        break;
                        case NoticeType.NOTICE_NOTICE_IMAGE: {
                            Gson gson = new Gson();
                            ImageBean imageBean = gson.fromJson(rowsBean.getContent(), ImageBean.class);
                            if (imageBean == null) {
                                Toast.makeText(MainActivity_old.this, "图片信息异常！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageCache.base64 = imageBean.getBase64();
                            speak("您有一条图片消息");
                            Intent intent = new Intent(MainActivity_old.this, ImageDetailActivity.class);
                            intent.putExtra("time", rowsBean.getPublishTime());
                            startActivity(intent);
                        }
                        break;
                        case NoticeType.NOTICE_SMS: {
                            speak("您有一条短消息，" + rowsBean.getTitle());
                            MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), rowsBean.getContent(), rowsBean.getPublishTime());
                            dialog.show();
                        }
                        break;
                        case NoticeType.NOTICE_TYPHOON: {
                            Gson gson = new Gson();
                            TyphoonBean typhoonBean = gson.fromJson(rowsBean.getContent(), TyphoonBean.class);
                            speak("您有一条台风" + typhoonBean.getTyphoonName() + "的消息");
                            MessageDialog dialog = MessageDialog.newInstance(MainActivity_old.this, rowsBean.getTitle(), typhoonBean.getMovingDirection(), rowsBean.getTitle());
                            dialog.show();
                        }
                        break;
                        case NoticeType.NOTICE_WEATHER: {
                            Type type2 = new TypeToken<List<WeatherBean>>() {
                            }.getType();
                            List<WeatherBean> list2 = new Gson().fromJson(rowsBean.getContent(), type2);

                            if (list2 != null && !list2.isEmpty()) {
                                WeatherBean weatherBean = list2.get(0);
                                String message = buildForecastText(weatherBean);
                                speak(message);
                                WeatherDialog dialog = WeatherDialog.newInstance(MainActivity_old.this, message, WeatherRepository.getWeatherIcon(weatherBean.getWeatherPhenomenon()));
                                dialog.show();
                                dialogs.add(dialog);
                            }
                        }
                        break;
                    }
                }
        });
        rvNotice.setAdapter(noticeListAdapter);

//        Observable.interval(10, 30, TimeUnit.SECONDS)
//                // 参数说明：
//                // 参数1 = 第1次延迟时间；
//                // 参数2 = 间隔时间数字；
//                // 参数3 = 时间单位；
//                // 该例子发送的事件特点：延迟2s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
//                /*
//                 * 步骤2：每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
//                 *  即每隔1秒产生1个数字前，就发送1次网络请求，从而实现轮询需求
//                 **/.subscribeOn(Schedulers.io())              // 上游在 IO
//                .observeOn(Schedulers.io()) // 下游切到主线程
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                        disposable = d;
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Long aLong) {
//                        HttpUtils.get(UrlUtils.getMessageReceiveUrl(DeviceUtils.getDeviceId(MainActivity.this)), new HttpUtils.HttpCallback() {
//                            @Override
//                            public void onSuccess(String body) {
//                                LogUtils.json(body);
//                                Gson gson = new Gson();
//                                MessageResponse response = gson.fromJson(body, MessageResponse.class);
//                                if (response.getCode() == 200) {
//                                    if (response.getData() != null && !response.getData().isEmpty()) {
//                                        for (int i = 0; i < response.getData().size(); i++) {
//                                            MessageResponse.MessageItem item = response.getData().get(i);
//                                            List<MessageResponse.MessageItem> historyItem = MyApplication.getInstance().getData();
//                                            if (!historyItem.contains(item)) {
//                                                MyApplication.getInstance().addData(item);
//                                                if (turnOnNotice) {
//                                                    try {
//                                                        if (dialog != null && dialog.isShowing()) {
//                                                            dialog.dismiss();
//                                                        }
//                                                        dialog = MessageDialog.newInstance(MainActivity.this, item.getContent());
//                                                        dialog.show();
//                                                        speak(item.getContent());
//                                                    } catch (Exception e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        Toast.makeText(MainActivity.this, "当前无最新消息！", Toast.LENGTH_SHORT).show();
//                                    }
//                                } else {
//                                    onError(response.getMsg());
//                                }
//                            }
//
//                            @Override
//                            public void onError(String msg) {
//                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

    }


    private void speak(String text) {
        if (!turnOnNotice) {
            return;
        }
        TTSManager.getInstance().speak(text);
    }

    private void initClicked() {
        findViewById(R.id.tv_historical_data).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity_old.this, HistoryMessageActivity.class));
        });
        findViewById(R.id.tv_parameter_settings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity_old.this, SettingActivity.class));
        });
        findViewById(R.id.tv_weather).setOnClickListener(v -> {
            if (selectedWeatherBean == null) {
                Toast.makeText(this, "暂无最新天气信息！", Toast.LENGTH_SHORT).show();
            } else {
                String message = buildForecastText(selectedWeatherBean);
                speak(message);
                WeatherDialog dialog2 = WeatherDialog.newInstance(MainActivity_old.this, message, WeatherRepository.getWeatherIcon(selectedWeatherBean.getWeatherPhenomenon()));
                dialog2.show();
            }
        });
        findViewById(R.id.iv_sos).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity_old.this, SosActivity.class);
            sosLauncher.launch(intent);
        });

        findViewById(R.id.iv_layout).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity_old.this, LayoutActivity.class);
            intent.putExtra("drawContent", drawContent);
            layoutLauncher.launch(intent);
        });
        findViewById(R.id.iv_location).setOnClickListener(v -> {
            onAnimateTo(longitudeData, latitudeData, 0.0);
        });
        TextView ivVolume = findViewById(R.id.iv_volume);
        ivVolume.setSelected(true);
        ivVolume.setOnClickListener(v -> {
            if (turnOnNotice) {
                turnOnNotice = false;
                v.setSelected(false);
                Toast.makeText(this, "语音通知已关闭！", Toast.LENGTH_SHORT).show();
            } else {
                turnOnNotice = true;
                v.setSelected(true);
                Toast.makeText(this, "语音通知已开启！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeAllTyphoonCircles() {
        for (Long id : typhoonLayerIds) {
            mEarthFragment.removeElementFromEarth(id);
        }
        typhoonLayerIds.clear();
    }

    // 动态权限加载
    private void checkPermissionAndStartLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            startLocationUpdates();
        }
    }

    // 已授权，授权之后在加载地图
    private void startLocationUpdates() {
        locationRepository = new LocationRepository(this);
        locationRepository.start(new LocationRepository.Callback() {
            @Override
            public void onLocationChanged(Location location) {
                longitudeData = location.getLongitude();
                latitudeData = location.getLatitude();
                uploadDeviceLocation(longitudeData, latitudeData);
            }

            @Override
            public void onError(String msg) {

            }
        });
    }

    // 权限回调
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
        if (locationRepository != null) {
            locationRepository.stop();
        }
        if (disposable != null) {
            disposable.dispose();
        }
        if (pollingDisposable != null) {
            pollingDisposable.dispose();
        }
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
        String path = getFilesDir().getPath() + File.separator + "测试数据.bmpkg";
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
        typhoonLayerIds.add(layer.id);
        vector.outlineWidth = "5";
        vector.outlineColor = "#FF00BFFF"; // 深天蓝
        vector.geoPoints.addAll(trackPoints);
        long elementId = mEarthFragment.drawElement(vector, true);

        layerManager.addLayer(selectedLayerType, elementId);
    }

    // 绘制固定大小的风圈（不叠加、不渐变）
    public void addTyphoonCircle(double lon, double lat, String color, double radiusMeters) {
        addTyphoonCircle(lon, lat, color, radiusMeters, "风圈");
    }

    public long addTyphoonCircle(double lon, double lat, String color, double radiusMeters, String detail) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "台风风圈_" + System.currentTimeMillis(), true);
        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, detail);
        typhoonLayerIds.add(layer.id);
        vector.outlineColor = color;
        vector.outlineWidth = "2";
        vector.showLabel = true;
        vector.description = detail;

        GeoPoint geo1 = new GeoPoint(lon, lat, 0.0);
        GeoPoint geo2 = new GeoPoint(radiusMeters, 0.0, 0.0);
        vector.geoPoints.add(geo1);
        vector.geoPoints.add(geo2);
        long elementId = mEarthFragment.drawElement(vector, true);
//        selectedLayerType = LayerType.TYPHOON;
//        layerManager.addLayer(selectedLayerType, elementId);
        return elementId;
    }

    public void drawRectangle(double swLng, double swLat,
                              double neLng, double neLat,
                              String color) {
        drawRectangle(swLng, swLat, neLng, neLat, color, "");
    }

    public void drawRectangle(double swLng, double swLat,
                              double neLng, double neLat,
                              String color, String name) {

        long rootID = mEarthFragment.getRootLayerId();

        VectorElement layer = mEarthFragment.onCreateLayer(
                rootID,
                "矩形区域_" + System.currentTimeMillis(),
                true
        );

        VectorElement vector = new VectorElement(
                layer.id,
                VectorElement.TYPE_RECT,
                name
        );

        typhoonLayerIds.add(layer.id);

        vector.outlineColor = color;
        vector.outlineWidth = "2";
        vector.showLabel = true;
        vector.description = name;

        // 左下
        vector.geoPoints.add(new GeoPoint(swLng, swLat, 0.0));
        // 右下
        vector.geoPoints.add(new GeoPoint(neLng, swLat, 0.0));
        // 右上
        vector.geoPoints.add(new GeoPoint(neLng, neLat, 0.0));
        // 左上
        vector.geoPoints.add(new GeoPoint(swLng, neLat, 0.0));
        // 闭合
        vector.geoPoints.add(new GeoPoint(swLng, swLat, 0.0));

        long elementId = mEarthFragment.drawElement(vector, true);

        layerManager.addLayer(selectedLayerType, elementId);
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

        String[] levels = new String[]{"热带风暴", "强热带风暴", "台风", "强台风", "超强台风"};

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
                    Toast.makeText(MainActivity_old.this, "强台风警告", Toast.LENGTH_SHORT).show();
                }

                Log.e("Fangs", "台风等级：" + level + "\n经度:" + lon + ", 纬度:" + lat);
            }, index * 2000L);
        }
    }

    public byte[] getBytesFromRes(int resId, float angle) {
        // 2. 加载 bitmap
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);

        // 3. 旋转 bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        // 4. 转 byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private long toAddPointInMap(double lon, double lat, int ic) {
        return toAddPointInMap(lon, lat, ic, 0);
    }

    private long toAddPointInMap(double lon, double lat, int ic, float angle) {
        return toAddPointInMap(lon, lat, ic, 0.6f, angle);
    }

    private long toAddPointInMap(double lon, double lat, int ic, float iconScale, float angle) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer = mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_POINT, "点");
        vector.description = "描述";

//        vector.isCustomPath = true;
        vector.image = getBytesFromRes(ic, angle);         // 使用旋转后的位图
        vector.showIcon = true;
        vector.isCustomPath = false;

        vector.iconScale = iconScale;
        vector.iconAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;

        vector.showLabel = false;
        vector.labelColor = "#FF00FF00";

        GeoPoint geo = new GeoPoint(lon, lat, 0.0);
        vector.geoPoints.add(geo);

        long id = mEarthFragment.drawElement(vector, true);
        layerManager.addLayer(selectedLayerType, id);
        return id;
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
            vector.iconPath = "170.png";
        }

        vector.iconScale = 1.5f;
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

        new Handler().postDelayed(() -> {
            VectorElement element1 = mEarthFragment.getThisElementAttribute(id);
            element1.geoPoints.add(new GeoPoint(113.565, 22.161));
            element1.outlineColor = "#FFFF0000";
            mEarthFragment.setThisElementAttribute(element1);

            new Handler().postDelayed(() -> {
                VectorElement element2 = mEarthFragment.getThisElementAttribute(id);
                element2.geoPoints.add(new GeoPoint(113.566, 22.162));
                element2.outlineColor = "#FF00FF00";
                mEarthFragment.setThisElementAttribute(element2);

                new Handler().postDelayed(() -> {
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
            Log.e("Fangs", "==onCallbackDrawElementStepEditing==");
        });
    }

    @Override
    public void onCallbackDrawElementStepCreated(VectorElement vectorElement) {
        Log.e("Fangs", "==onCallbackDrawElementStepCreated==");
    }

    // time 跳转时间
    private void onAnimateTo(double lon, double lat, double time) {
        onAnimateTo(lon, lat, time, OFFLINE_MAX_ALTITUDE);
    }

    // time 跳转时间
    private void onAnimateTo(double lon, double lat, double time, int height) {
        GeoPoint geoPoint = new GeoPoint(lon, lat);
        double pitch = -90.0;
        mEarthFragment.animateTo(geoPoint, height, time, pitch);
    }

    // 在线地图
    @Override
    public void onCreateEarthComplete() {
//todo
//        addMapSource(TEST_MAP_SOURCE_URL3);
//        addMapSourceList();
        addOfflineMap();

        Log.e("Fangs", "=====");
        isEarthReady = true;

//        List<Provider> providers = BMEngine.getMapProviders();
//        if (!providers.isEmpty()) {
//            Provider provider = providers.get(1); // 1、内置在线地图
//            mEarthFragment.changeMapSource(provider.mapId);
//            //在线地图
//            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_BKG")) {
//                mEarthFragment.animateToOfflineArea();
//            }
//            //离线地图
//            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_PKG")) {
//                mEarthFragment.animateToOfflineArea();
//            }
//
//        }
        // ✅ 强制切换离线 Provider
        List<Provider> providers = BMEngine.getMapProviders();
        if (providers != null && !providers.isEmpty()) {
            for (Provider provider : providers) {
                Log.e("BM_PROVIDER", "id=" + provider.mapId + ", name=" + provider.mapName);
                if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP")) {
                    mEarthFragment.changeMapSource(provider.mapId);
                    mEarthFragment.animateToOfflineArea();
                    break;
                }
            }
        }
        // 默认视角
        if (longitudeData == 0.0 && latitudeData == 0.0) {
            onAnimateTo(113.5, 22.2, 0.0);
        } else {
            onAnimateTo(longitudeData, latitudeData, 0.0);
        }

        BMEngine.isShowBuilding(false);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            initDrawUtils();
            if (!TextUtils.isEmpty(drawContent)) {
                if (drawContent.contains("渔场")) {
                    drawBaseLineByName("渔场");
                }
                if (drawContent.contains("机轮拖网渔业禁渔线")) {
                    drawBaseLineByName("机轮拖网渔业禁渔线");
                }
                if (drawContent.contains("领海基线")) {
                    drawBaseLineByName("领海基线");
                }
                if (drawContent.contains("中韩渔业协定水域")) {
                    drawBaseLineByName("中韩渔业协定水域");
                }
                if (drawContent.contains("中日渔业协定水域")) {
                    drawBaseLineByName("中日渔业协定水域");
                }
            }
        }, 5000L);
        onAnimateTo(126.5292, 33.3617, 0.0);
        selectedLayerType = LayerType.LOCATION;
        toAddPointInMap(longitudeData, latitudeData, R.mipmap.ic_fishing_vessel, 1f, 0);

    }

    private void drawBaseLineByName(String layerTypeName) {
        if ("渔场".equals(layerTypeName)) {
            drawBaseLine(LayerType.FISHING_GROUND);
        } else if ("机轮拖网渔业禁渔线".equals(layerTypeName)) {
            drawBaseLine(LayerType.NO_FISHING_LINE);
        } else if ("领海基线".equals(layerTypeName)) {
            drawBaseLine(LayerType.COAST_LINE);
        } else if ("中韩渔业协定水域".equals(layerTypeName)) {
            drawBaseLine(LayerType.CKFA);
        } else if ("中日渔业协定水域".equals(layerTypeName)) {
            drawBaseLine(LayerType.CJFA);
        }
    }

    private void drawBaseLine(LayerType layerType) {
        switch (layerType) {
            case FISHING_GROUND:
                drawYuChang();
                break;
            case NO_FISHING_LINE:
                drawNO_FISHING_LINE();
                break;
            case COAST_LINE:
                drawCOAST_LINE();
                break;
            case CKFA:
                drawCKFA();
                break;
            case CJFA:
                drawCJFA();
                break;
        }

    }

    private void drawCOAST_LINE() {
        List<Long> layers = layerManager.getLayers(LayerType.COAST_LINE);
        if (layers != null && !layers.isEmpty()) {
            showLayer(LayerType.COAST_LINE);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                selectedLayerType = LayerType.COAST_LINE;
                drawKmlFishingZone("linghai.kml");
            }, 3000L);
        }
    }

    private void drawNO_FISHING_LINE() {
        List<Long> layers = layerManager.getLayers(LayerType.NO_FISHING_LINE);
        if (layers != null && !layers.isEmpty()) {
            showLayer(LayerType.NO_FISHING_LINE);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                selectedLayerType = LayerType.NO_FISHING_LINE;
                drawKmlFishingZone("jilun.kml");
            }, 3000L);
        }
    }

    private void drawCKFA() {
        List<Long> layers = layerManager.getLayers(LayerType.CKFA);
        if (layers != null && !layers.isEmpty()) {
            showLayer(LayerType.CKFA);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                selectedLayerType = LayerType.CKFA;
                drawKmlFishingZone("zhonghan.kml");
            }, 3000L);
        }
    }

    private void drawCJFA() {
        List<Long> layers = layerManager.getLayers(LayerType.CJFA);
        if (layers != null && !layers.isEmpty()) {
            showLayer(LayerType.CJFA);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                selectedLayerType = LayerType.CJFA;
                drawKmlFishingZone("zhongri.kml");
            }, 3000L);
        }
    }

    private void drawYuChang() {
        List<Long> layers = layerManager.getLayers(LayerType.FISHING_GROUND);
        if (layers != null && !layers.isEmpty()) {
            showLayer(LayerType.FISHING_GROUND);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                selectedLayerType = LayerType.FISHING_GROUND;
                drawKmlFishingZone("yuqv.kml");
            }, 3000L);
        }
    }

    public long drawTest2(String json, int hour) {
        TyphoonBean2 typhoonBean = new Gson().fromJson(json, TyphoonBean2.class);

        TyphoonDrawManager2 typhoonManager =
                new TyphoonDrawManager2(mEarthFragment);

        typhoonManager.draw(typhoonBean);

        List<GeoPoint> trackPoints = new ArrayList<>();

        List<TyphoonBean2.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean> points =
                typhoonBean.getTyphoonInfo()
                        .getMapData()
                        .getCoordinates()
                        .getPoints();

        for (var p : points) {
            trackPoints.add(new GeoPoint(p.getLng(), p.getLat()));
        }

        drawTyphoonTrack(trackPoints);

        TyphoonBean2.WindCirclesBean current =
                findNearestWindCircle(typhoonBean.getWindCircles(), hour);


        int idx = current.getPathPointIndex();

        GeoPoint center = trackPoints.get(idx);

        hideLayer(LayerType.TYPHOON);
        tyElementId = addTyphoonCircle(
                center.lon,
                center.lat,
                current.getColor(),
                current.getRadius() * 1000, // 你 JSON 给的就是半径
                current.getDescription()    // “7级风圈”
        );
        layerManager.addLayer(LayerType.TYPHOON, tyElementId);
        return tyElementId;
    }

    public long drawTest(int hour) {
        TyphoonBean2 typhoonBean = new Gson().fromJson(loadJsonFromAssets(this, "typhoon2.json"), TyphoonBean2.class);

        TyphoonDrawManager2 typhoonManager =
                new TyphoonDrawManager2(mEarthFragment);

        typhoonManager.draw(typhoonBean);

        List<GeoPoint> trackPoints = new ArrayList<>();

        List<TyphoonBean2.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean> points =
                typhoonBean.getTyphoonInfo()
                        .getMapData()
                        .getCoordinates()
                        .getPoints();

        for (var p : points) {
            trackPoints.add(new GeoPoint(p.getLng(), p.getLat()));
        }

        drawTyphoonTrack(trackPoints);

        TyphoonBean2.WindCirclesBean current =
                findNearestWindCircle(typhoonBean.getWindCircles(), hour);


        int idx = current.getPathPointIndex();

        GeoPoint center = trackPoints.get(idx);

        hideLayer(LayerType.TYPHOON);
        tyElementId = addTyphoonCircle(
                center.lon,
                center.lat,
                current.getColor(),
                current.getRadius() * 1000, // 你 JSON 给的就是半径
                hour + "点位置"      // “7级风圈”
        );
        layerManager.addLayer(LayerType.TYPHOON, tyElementId);
        return tyElementId;
    }

    private void initDrawUtils() {
        lineDrawManager = new LineDrawManager(mEarthFragment);
        planeDrawManager = new PlaneDrawManager(mEarthFragment);
    }

    public void hideLayer(LayerType type) {
        List<Long> list = layerManager.getLayers(type);
        if (list != null && !list.isEmpty()) {
            for (Long id : list) {
                mEarthFragment.setElementVisible(id, false);
            }
        }
    }

    public void showLayer(LayerType type) {
        List<Long> list = layerManager.getLayers(type);
        if (list != null && !list.isEmpty()) {
            for (Long id : list) {
                mEarthFragment.setElementVisible(id, true);
            }
        }
    }

    private void initFishingGround() {

        String json = loadJsonFromAssets(this, "shandongyuqumian119.json");

        try {
            JSONObject root = new JSONObject(json);
            JSONArray features = root.getJSONArray("features");

            long rootLayerId = mEarthFragment.getRootLayerId();

            // 每个 feature 画一个 polygon 或 multipolygon
            for (int i = 0; i < features.length(); i++) {

                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONObject properties = feature.getJSONObject("properties");   // ⭐ 新增
                String name = properties.optString("NAME", "");                // ⭐ 新增
                String type = geometry.getString("type");

                // 创建图层
                VectorElement layer = mEarthFragment.onCreateLayer(rootLayerId, "渔区_" + i, true);

                if (type.equals("Polygon")) {
                    JSONArray coords = geometry.getJSONArray("coordinates");
                    drawOnePolygon(coords, layer.id, name);    // ⭐ 传 name

                } else if (type.equals("MultiPolygon")) {
                    JSONArray multi = geometry.getJSONArray("coordinates");

                    // MultiPolygon = 多个 polygon
                    for (int j = 0; j < multi.length(); j++) {
                        JSONArray poly = multi.getJSONArray(j);
                        drawOnePolygon(poly, layer.id, name);    // ⭐ 传 name
                    }
                }
                layerManager.addLayer(selectedLayerType, layer.id);
            }

            // 移动视角至渔区中心（山东外海）
//            mEarthFragment.animateTo(
//                    new GeoPoint(122.5, 36.5),
//                    200000,  // 200km 视角
//                    2.0,
//                    -90.0
//            );

//            Toast.makeText(this, "渔区全部绘制完成", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "渔区绘制失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void drawOnePolygon(JSONArray polygonArray, long layerId, String name)
            throws Exception {

        JSONArray outerRing = polygonArray.getJSONArray(0);

        // ===== 1. 画面 =====
        VectorElement polygon = new VectorElement(
                layerId, VectorElement.TYPE_PLANE, "渔区");

        polygon.attribute = "strokeColor:#FF0000; strokeWidth:20; fillColor:#55FF0000";
        polygon.outlineWidth = "2";

        double sumLon = 0;
        double sumLat = 0;
        int count = 0;

        for (int k = 0; k < outerRing.length(); k++) {
            JSONArray pt = outerRing.getJSONArray(k);
            double lon = pt.getDouble(0);
            double lat = pt.getDouble(1);

            polygon.geoPoints.add(new GeoPoint(lon, lat));

            sumLon += lon;
            sumLat += lat;
            count++;
        }

        // 必须闭合
        if (!isClosed(polygon.geoPoints)) {
            polygon.geoPoints.add(new GeoPoint(
                    polygon.geoPoints.get(0).lon,
                    polygon.geoPoints.get(0).lat));
        }

        long polygonId = mEarthFragment.drawElement(polygon, true);
        layerManager.addLayer(selectedLayerType, polygonId);

        // ===== 2. 画 NAME（用 Point + Label） =====
        if (name != null && !name.isEmpty() && count > 0) {

            double centerLon = sumLon / count;
            double centerLat = sumLat / count;

            VectorElement labelPoint = new VectorElement(
                    layerId,
                    VectorElement.TYPE_POINT,
                    name
            );

            labelPoint.geoPoints.add(new GeoPoint(centerLon, centerLat));

            // 不显示图标，只显示文字
            labelPoint.showIcon = false;

            // ⭐ Bigemap 的文字就是 Label
            labelPoint.showLabel = true;
            labelPoint.labelColor = "#FFFFFFFF";   // 白色
            labelPoint.labelSize = "10";              // 字号
            labelPoint.labelAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;

            // 文字内容（不同版本用 description / name）
            labelPoint.description = name;

            long labelId = mEarthFragment.drawElement(labelPoint, true);
            layerManager.addLayer(selectedLayerType, labelId);
        }
    }


    private boolean isClosed(List<GeoPoint> pts) {
        if (pts.size() < 3) return false;

        GeoPoint a = pts.get(0);
        GeoPoint b = pts.get(pts.size() - 1);

        return Math.abs(a.lon - b.lon) < 1e-6 && Math.abs(a.lat - b.lat) < 1e-6;
    }

    private void initWind() {
        ArrayList<LocationBean> locationList = new ArrayList<>();
        locationList.add(new LocationBean(39.022933, 120.278616, R.mipmap.ic_wind_3));
        locationList.add(new LocationBean(38.388520, 120.056895, R.mipmap.ic_wind_4));
        locationList.add(new LocationBean(38.214523, 123.641380, R.mipmap.ic_wind_5));
        locationList.add(new LocationBean(34.465327, 125.230379, R.mipmap.ic_wind_6));
        locationList.add(new LocationBean(32.772956, 125.526006, R.mipmap.ic_wind_7));
        locationList.add(new LocationBean(30.825909, 124.971705, R.mipmap.ic_wind_8));
        locationList.add(new LocationBean(30.029324, 126.819377, R.mipmap.ic_wind_10));
        locationList.add(new LocationBean(28.286816, 124.786937, R.mipmap.ic_wind_12));

        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < locationList.size(); i++) {
            final int index = i;
            int finalI = i;
            handler.postDelayed(() -> {
                double lon = locationList.get(finalI).getLongitude();
                double lat = locationList.get(finalI).getLatitude();
                int ic = locationList.get(finalI).getIc();
                // 1. 随机角度（0~360）
                float angle = (float) (Math.random() * 360f);
                layerManager.addLayer(selectedLayerType, toAddPointInMap(lon, lat, ic, angle));
            }, index * 3000L);
        }
    }

    private void initWeather() {
        ArrayList<LocationBean> locationList = new ArrayList<>();
        locationList.add(new LocationBean(37.372753, 119.373551, R.mipmap.ic_cloudy));
        locationList.add(new LocationBean(37.869470, 121.568474, R.mipmap.ic_cloudy_sky));
        locationList.add(new LocationBean(36.353621, 121.571741, R.mipmap.ic_downpour));
        locationList.add(new LocationBean(38.903766, 120.627793, R.mipmap.ic_fine));
        locationList.add(new LocationBean(40.284387, 121.089539, R.mipmap.ic_heavy_rain));
        locationList.add(new LocationBean(37.217907, 123.836121, R.mipmap.ic_heavy_downpour));
        locationList.add(new LocationBean(35.608900, 120.562195, R.mipmap.ic_sand_storm));
        locationList.add(new LocationBean(35.143091, 119.946961, R.mipmap.ic_strong_sandstorm));

        for (int i = 0; i < locationList.size(); i++) {
            double lon = locationList.get(i).getLongitude();
            double lat = locationList.get(i).getLatitude();
            int ic = locationList.get(i).getIc();
            layerManager.addLayer(selectedLayerType, toAddPointInMap(lon, lat, ic));
        }
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

    private static final int OFFLINE_MAX_ALTITUDE = 4000000; // 12万米，城市级

    private boolean correcting = false;

    @Override
    public void callbackScreenCenterPoint(GeoPoint center, double altitude, long l, int i) {
        if (altitude > OFFLINE_MAX_ALTITUDE && !correcting) {
            correcting = true;
            runOnUiThread(() -> {
                mEarthFragment.animateTo(center, OFFLINE_MAX_ALTITUDE, 0, -90);
                correcting = false;
            });
        }
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
        List<Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty() && index >= 0 && index < providers.size()) {
            Provider provider = providers.get(index);
            mEarthFragment.changeMapSource(provider.mapId);
        }
    }

    // 离线地图加载
    public void changeOfflineMapSource() {
        List<Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty()) {
            Provider provider = providers.get(providers.size() - 1);
            mEarthFragment.changeMapSource(provider.mapId);

            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_PKG")) {
                mEarthFragment.animateToOfflineArea();
            }
            // 动态获取经纬度
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
        Toast.makeText(this, "当前经度：" + longitudeData + "\n当前纬度：" + latitudeData, Toast.LENGTH_SHORT).show();
        mEarthFragment.updateLocation(geoPoint, 100.0, 0.0); // 1、位置 2、精度 3、方向
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
        Toast.makeText(getApplicationContext(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 手画线
    public void onDrawLineElement() {
        onDrawElement(Constants.DRAW_ELEMENT_TYPE_LINE);
        Toast.makeText(getApplicationContext(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 手画面
    public void onDrawPlaneElement() {
        onDrawElement(Constants.DRAW_ELEMENT_TYPE_PLANE);
        Toast.makeText(getApplicationContext(), "点击屏幕", Toast.LENGTH_LONG).show();
    }

    // 撤销当前线绘制
    public void toRetreatDrawingElement() {
        mEarthFragment.toRetreatDrawingElement();
    }

    public void loadKMLFile() {
        String url = "";
        if (url.isEmpty()) {
            Toast.makeText(getApplicationContext(), "添加路径", Toast.LENGTH_LONG).show();
        } else {
            long rootID = mEarthFragment.getRootLayerId();
            mEarthFragment.loadKMLFile(rootID, url);
        }
    }

    private String convertKmlColor(String kmlColor) {
        if (kmlColor == null) return null;   // ★ 关键

        String s = kmlColor.trim();
        if (s.isEmpty()) return null;

        s = s.replace("#", "");
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        if (s.length() != 8) return null;
        if (!s.matches("[0-9a-fA-F]{8}")) return null;

        // KML: AABBGGRR -> Android: AARRGGBB
        String aa = s.substring(0, 2);
        String bb = s.substring(2, 4);
        String gg = s.substring(4, 6);
        String rr = s.substring(6, 8);

        return "#" + rr + gg + bb;
    }

    private String getTagValue(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }

    private KmlStyle parseStyleElement(Element styleElement) {
        if (styleElement == null) return null;

        KmlStyle ks = new KmlStyle();

        // LineStyle
        NodeList lineStyles = styleElement.getElementsByTagName("LineStyle");
        if (lineStyles.getLength() > 0) {
            Element lineStyle = (Element) lineStyles.item(0);

            String color = getTagValue(lineStyle, "color");
            ks.setLineColor(convertKmlColor(color));

            String width = getTagValue(lineStyle, "width");
            if (width != null) {
                try {
                    ks.setLineWidth(Float.parseFloat(width));
                } catch (Exception ignored) {
                }
            }
        }

        // PolyStyle
        NodeList polyStyles = styleElement.getElementsByTagName("PolyStyle");
        if (polyStyles.getLength() > 0) {
            Element polyStyle = (Element) polyStyles.item(0);
            String color = getTagValue(polyStyle, "color");
            ks.setFillColor(convertKmlColor(color));
        }

        return ks;
    }

    public void drawKmlFishingZone(String assetFileName) {
        try {
            String kml = loadJsonFromAssets(this, assetFileName);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new java.io.StringReader(kml));
            org.w3c.dom.Document doc = builder.parse(is);

            /* ===============================
             * 1. 解析 Document 级 Style（可选）
             * =============================== */
            Map<String, KmlStyle> styleMap = new HashMap<>();

            NodeList styleList = doc.getElementsByTagName("Style");
            for (int i = 0; i < styleList.getLength(); i++) {
                Element style = (Element) styleList.item(i);
                String styleId = style.getAttribute("id");
                if (styleId == null || styleId.isEmpty()) continue;

                KmlStyle ks = parseStyleElement(style);
                styleMap.put("#" + styleId, ks);
            }

            /* ===============================
             * 2. 解析 Placemark
             * =============================== */
            NodeList placemarks = doc.getElementsByTagName("Placemark");

            for (int i = 0; i < placemarks.getLength(); i++) {
                Element placemark = (Element) placemarks.item(i);

                String name = getTagValue(placemark, "name");

                /* ===== 样式解析（关键） ===== */
                KmlStyle style = null;

                // ① 优先读取 Placemark 内联 Style（你这个 KML 就是这种）
                NodeList inlineStyles = placemark.getElementsByTagName("Style");
                if (inlineStyles.getLength() > 0) {
                    style = parseStyleElement((Element) inlineStyles.item(0));
                }

                // ② 如果没有内联 Style，再走 styleUrl
                if (style == null) {
                    String styleUrl = getTagValue(placemark, "styleUrl");
                    if (styleUrl != null) {
                        style = styleMap.get(styleUrl);
                    }
                }

                /* ===== LineString ===== */
                NodeList lineList = placemark.getElementsByTagName("LineString");
                if (lineList.getLength() > 0) {
                    Element line = (Element) lineList.item(0);
                    String coordinates = getCoordinates(line);
                    List<GeoPoint> points = parseCoordinates(coordinates);

                    if (points != null && points.size() >= 2) {
                        long elementId = lineDrawManager.drawLineWithLabel(name, points, style != null ? style.getLineColor() : null, style != null ? style.getLineWidth() : 0);
                        layerManager.addLayer(selectedLayerType, elementId);
                    }
                    continue;
                }

                /* ===== Polygon ===== */
                NodeList polyList = placemark.getElementsByTagName("Polygon");
                if (polyList.getLength() > 0) {
                    Element polygon = (Element) polyList.item(0);

                    NodeList lrList = polygon.getElementsByTagName("LinearRing");
                    if (lrList.getLength() == 0) continue;

                    Element lr = (Element) lrList.item(0);
                    String coordinates = getCoordinates(lr);
                    List<GeoPoint> points = parseCoordinates(coordinates);

                    if (points != null && points.size() >= 3) {
                        List<Long> elementIds = planeDrawManager.draw(name, points, style != null ? style.getFillColor() : null, style != null ? style.getLineColor() : null, style != null ? style.getLineWidth() : 0);
                        layerManager.addLayer(selectedLayerType, elementIds);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "KML解析失败: " + assetFileName, Toast.LENGTH_LONG).show();
        }
    }

    private String getCoordinates(Element parent) {
        return parent.getElementsByTagName("coordinates").item(0).getTextContent().trim();
    }

    private List<GeoPoint> parseCoordinates(String coordsText) {
        List<GeoPoint> list = new ArrayList<>();
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

    private List<GeoPoint> parseKmlCoordinates(String coordinatesText) {
        List<GeoPoint> list = new ArrayList<>();

        String[] lines = coordinatesText.split("\\s+");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue;

            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);

            list.add(new GeoPoint(lon, lat));
        }

        return list;
    }

    public String buildForecastText(WeatherBean bean) {
        if (bean == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 1. 预报时效
        sb.append("未来（24）小时（")
                .append(bean.getSeaArea())
                .append("）区域，");

        // 2. 经纬度范围
        String[] lonArr = bean.getLongitudeRange().split("-");
        String[] latArr = bean.getLatitudeRange().split("-");

        sb.append("北纬")
                .append(toDms(latArr[0]))
                .append("，东经")
                .append(toDms(lonArr[0]))
                .append("，到北纬")
                .append(toDms(latArr[1]))
                .append("，东经")
                .append(toDms(lonArr[1]))
                .append("，");

        // 3. 天气要素
        sb.append("预计有")
                .append(bean.getWeatherPhenomenon())
                .append(bean.getWindDirection())
                .append(bean.getWindForce())
                .append("级")
                .append("浪高")
                .append(bean.getWaveHeight())
                .append("米")
                .append("能见度")
                .append(bean.getVisibility())
                .append("千米")
                .append(TextUtils.isEmpty(bean.getRemark()) ? "。" : ("," + bean.getRemark() + "。"));


        return sb.toString();
    }


    /**
     * 十进制度 -> 度分秒
     * 37.1206 -> 37度07分14秒
     */
    private String toDms(String value) {
        double d = Double.parseDouble(value);

        int degree = (int) d;
        double m1 = (d - degree) * 60;
        int minute = (int) m1;
        int second = (int) ((m1 - minute) * 60);

        return degree + "度" + minute + "分" + second + "秒";
    }


    private long parseTimeToday(String timeStr) {
        // 支持 11:00 或 17时45分
        timeStr = timeStr.replace("时", ":").replace("分", "");

        String[] arr = timeStr.split(":");
        if (arr.length != 2) return 0;

        int hour = Integer.parseInt(arr[0]);
        int minute = Integer.parseInt(arr[1]);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);

        return cal.getTimeInMillis();
    }

    private TyphoonBean2.WindCirclesBean findNearestWindCircle(List<TyphoonBean2.WindCirclesBean> circles, int hour) {
//        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long now = cal.getTimeInMillis();


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
}
