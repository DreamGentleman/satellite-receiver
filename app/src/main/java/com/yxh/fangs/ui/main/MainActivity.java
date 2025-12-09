package com.yxh.fangs.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.adapter.NoticeListAdapter;
import com.yxh.fangs.application.MyApplication;
import com.yxh.fangs.bean.DeviceLocationRecordRequest;
import com.yxh.fangs.bean.DeviceLocationRecordResponse;
import com.yxh.fangs.bean.DeviceStatusChangeRequest;
import com.yxh.fangs.bean.LocationBean;
import com.yxh.fangs.bean.MessageResponse;
import com.yxh.fangs.bean.NoticeBean;
import com.yxh.fangs.bean.SosEventRequest;
import com.yxh.fangs.bean.SosEventResponse;
import com.yxh.fangs.bean.StatusLogResponse;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.dialog.RemindDialog;
import com.yxh.fangs.ui.dialog.SosDialog;
import com.yxh.fangs.ui.dialog.WeatherDialog;
import com.yxh.fangs.util.BigemapEnhancedFishingRenderer;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.HttpUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.TTSManager;
import com.yxh.fangs.util.UrlUtils;
import com.yxh.fangs.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements OperationCallback, TextToSpeech.OnInitListener {

    private EarthFragment mEarthFragment;
    // 标记地图是否已加载
    private boolean isEarthReady = false;
    private double longitudeData = 0.0;
    private double latitudeData = 0.0;
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
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private TextView tvScrollingMessage;
    private List<Long> typhoonLayerIds = new ArrayList<>();
    private TextToSpeech tts;
    private boolean isTtsReady;
    private Disposable disposable;
    private MessageDialog dialog;
    private boolean turnOnNotice = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 使用 RxJava 进行异步初始化
        Observable.fromCallable(() -> {
                    initEngine();      // 引擎初始化
                    copyFiles();       // 拷贝 assets
                    return true;       // 占位返回
                })
                .subscribeOn(Schedulers.io())              // 上面 3 个操作放到 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 回主线程
                .subscribe(result -> {
                    initFragment();       // 创建 EarthFragment
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(this, "初始化失败: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
        initLocation();
        // 检查并请求权限
        checkPermissionAndStartLocation();
        //初始化视图
        initView();
        // 点击事件
        initClicked();
        //TODO 模拟消息
        initNotice();
        uploadDeviceLocation();
        uploadReceiverStatusLog();
//        TTSManager.getInstance().init(this, () -> {
//            TTSManager.getInstance().speak("语音功能初始化成功！");
//        });

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
                    Toast.makeText(MainActivity.this, "设备变更信息上传成功！", Toast.LENGTH_SHORT).show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadDeviceLocation() {
        DeviceLocationRecordRequest deviceLocationRecordRequest = new DeviceLocationRecordRequest();
        deviceLocationRecordRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        deviceLocationRecordRequest.setLongitude("120.278616");
        deviceLocationRecordRequest.setLatitude("39.022933");
        deviceLocationRecordRequest.setAddress("东海");
        deviceLocationRecordRequest.setSpeed(1);
        deviceLocationRecordRequest.setDirection(45);
        deviceLocationRecordRequest.setAltitude(50);
        String json = new Gson().toJson(deviceLocationRecordRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getDeviceLocationAddUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                DeviceLocationRecordResponse response = gson.fromJson(body, DeviceLocationRecordResponse.class);
                if (response.getCode() == 200) {
                    Toast.makeText(MainActivity.this, "位置信息上传成功！", Toast.LENGTH_SHORT).show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadDeviceSos() {
        SosEventRequest sosEventRequest = new SosEventRequest();
        sosEventRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        sosEventRequest.setLongitude("120.278616");
        sosEventRequest.setLatitude("39.022933");
        sosEventRequest.setEmergencyLevel("1");
        sosEventRequest.setDescription("这是急救信息！");
        sosEventRequest.setRemark("这是急救信息的备注！");
        String json = new Gson().toJson(sosEventRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getSosEventStartUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                SosEventResponse response = gson.fromJson(body, SosEventResponse.class);
                if (response.getCode() == 200) {
                    RemindDialog dialog = RemindDialog.newInstance(MainActivity.this, "SOS紧急求助信息已发送");
                    dialog.show();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        tvScrollingMessage = findViewById(R.id.tv_scrolling_message);
        tvScrollingMessage.setSelected(true);
    }

    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //TODO
        longitudeData = 126.5292;
        latitudeData = 33.3617;
        // 创建 LocationRequest
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L   // 每 5 秒更新一次
        ).build();

        // 回调：持续接收定位结果
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                for (android.location.Location location : result.getLocations()) {
                    longitudeData = location.getLongitude();
                    latitudeData = location.getLatitude();
                    String text = "当前经度：" + longitudeData + "\n当前纬度：" + latitudeData;
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();

                    // 地图加载完，只跳转一次
                    if (isEarthReady) {
                        //TODO
                        longitudeData = 126.5292;
                        latitudeData = 33.3617;
                        onAnimateTo(longitudeData, latitudeData, 0.0);
                        isEarthReady = false;
                    }
                }
            }
        };
    }

    private void initFragment() {
        mEarthFragment = EarthFragment.getInstance(this);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flt_container, mEarthFragment, TAG_EARTH_FRAGMENT)
                .commitAllowingStateLoss();
    }

    private void copyFiles() {
        // 拷贝文件到文件系统
        Utils.INSTANCE.copyAssets(this, "img", getFilesDir().getPath());
        Utils.INSTANCE.copyAssets(this, "map", getFilesDir().getPath());
        // Utils.INSTANCE.copyAssets(this, "同名图标", BMEngine.getIconPath());
    }

    private void initEngine() {
        // 初始化地图配置
        BMEngine.preInit(this, "bda2ea3fb18fdd9a4a6d922389576df7");
        // 1、Context 2、自定义图标存放位置 3、是否加载地形
        BMEngine.init(this, getFilesDir().getPath() + File.separator, false);
    }

    private void initNotice() {
        RecyclerView rvNotice = findViewById(R.id.rv_notice);
        rvNotice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvNotice.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(15, 4, 15, 0);
            }
        });
        ArrayList<NoticeBean> noticeList = new ArrayList<>();
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_BEIDOU, "[北斗通报信息]当前你的所处的经度为126.5292，维度为33.3617", "接收时间:2025年10月09日 10时"));
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_ALERT, "[预警信息]前方强台风即将来袭", "接收时间:2025年10月09日 9时"));
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_NOTICE_IMAGE, "[图片信息]你有一张新图片", "接收时间:2025年10月09日 8时"));
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_SMS, "[短信息]你有一条新消息", "接收时间:2025年10月09日 7时"));
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_TYPHOON, "[台风信息]前方有台风来袭", "接收时间:2025年10月09日 6时"));
        noticeList.add(new NoticeBean(NoticeBean.NOTICE_WEATHER, "[气象消息]当前天气晴朗", "接收时间:2025年10月09日 5时"));
        NoticeListAdapter adapter = new NoticeListAdapter(noticeList);
        adapter.setOnItemClickListener(new NoticeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                NoticeBean noticeBean = noticeList.get(position);
                speak(noticeBean.getNoticeTitle());
                switch (noticeBean.getNoticeType()) {
                    case NoticeBean.NOTICE_BEIDOU: {
                        MessageDialog dialog = MessageDialog.newInstance(MainActivity.this, noticeBean.getNoticeTitle());
                        dialog.show();
                    }
                    break;
                    case NoticeBean.NOTICE_ALERT: {
                        MessageDialog dialog = MessageDialog.newInstance(MainActivity.this, noticeBean.getNoticeTitle());
                        dialog.show();
                    }
                    break;
                    case NoticeBean.NOTICE_NOTICE_IMAGE:
                        startActivity(new Intent(MainActivity.this, ImageDetailActivity.class));
                        break;
                    case NoticeBean.NOTICE_SMS: {
                        MessageDialog dialog = MessageDialog.newInstance(MainActivity.this, noticeBean.getNoticeTitle());
                        dialog.show();
                    }
                    break;
                    case NoticeBean.NOTICE_TYPHOON: {
                        MessageDialog dialog = MessageDialog.newInstance(MainActivity.this, noticeBean.getNoticeTitle());
                        dialog.show();
                    }
                    break;
                    case NoticeBean.NOTICE_WEATHER: {
                        WeatherDialog dialog = WeatherDialog.newInstance(MainActivity.this, "[青岛沿海]\n黄岛区大风黄色预警，预警起始时间:2024-07-1621 50:35，请过往船只注意并加强防御");
                        dialog.show();
                    }
                    break;
                }
            }
        });
        // 创建 TTS 实例
        tts = new TextToSpeech(this, this);
        rvNotice.setAdapter(adapter);
//        Observable.interval(10, 20, TimeUnit.SECONDS)
//                // 参数说明：
//                // 参数1 = 第1次延迟时间；
//                // 参数2 = 间隔时间数字；
//                // 参数3 = 时间单位；
//                // 该例子发送的事件特点：延迟2s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
//                /*
//                 * 步骤2：每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
//                 *  即每隔1秒产生1个数字前，就发送1次网络请求，从而实现轮询需求
//                 **/
//                .subscribeOn(Schedulers.computation())              // 上游在 IO
//                .observeOn(AndroidSchedulers.mainThread()) // 下游切到主线程
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                        disposable = d;
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Long aLong) {
//                        if (turnOnNotice) {
//                            try {
//                                NoticeBean noticeBean = noticeList.get((int) (aLong % noticeList.size()));
//                                if (dialog != null && dialog.isShowing()) {
//                                    dialog.dismiss();
//                                }
//                                dialog = MessageDialog.newInstance(MainActivity.this, noticeBean.getNoticeTitle());
//                                dialog.show();
//                                speak(noticeBean.getNoticeTitle());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
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

        Observable.interval(10, 30, TimeUnit.SECONDS)
                // 参数说明：
                // 参数1 = 第1次延迟时间；
                // 参数2 = 间隔时间数字；
                // 参数3 = 时间单位；
                // 该例子发送的事件特点：延迟2s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
                /*
                 * 步骤2：每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
                 *  即每隔1秒产生1个数字前，就发送1次网络请求，从而实现轮询需求
                 **/
                .subscribeOn(Schedulers.io())              // 上游在 IO
                .observeOn(Schedulers.io()) // 下游切到主线程
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        HttpUtils.get(UrlUtils.getMessageReceiveUrl(DeviceUtils.getDeviceId(MainActivity.this)), new HttpUtils.HttpCallback() {
                            @Override
                            public void onSuccess(String body) {
                                LogUtils.json(body);
                                Gson gson = new Gson();
                                MessageResponse response = gson.fromJson(body, MessageResponse.class);
                                if (response.getCode() == 200) {
                                    if (response.getData() != null && !response.getData().isEmpty()) {
                                        for (int i = 0; i < response.getData().size(); i++) {
                                            MessageResponse.MessageItem item = response.getData().get(i);
                                            List<MessageResponse.MessageItem> historyItem = MyApplication.getInstance().getData();
                                            if (!historyItem.contains(item)) {
                                                MyApplication.getInstance().addData(item);
                                                if (turnOnNotice) {
                                                    try {
                                                        if (dialog != null && dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }
                                                        dialog = MessageDialog.newInstance(MainActivity.this, item.getContent());
                                                        dialog.show();
                                                        speak(item.getContent());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "当前无最新消息！", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    onError(response.getMsg());
                                }
                            }

                            @Override
                            public void onError(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void speak(String text) {
        if (tts == null || !isTtsReady) {
            Toast.makeText(MainActivity.this, "语音转换异常！", Toast.LENGTH_LONG).show();
            return;
        }
        tts.speak(text + "。",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "utteranceId_" + System.currentTimeMillis());
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
        findViewById(R.id.tv_historical_data).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryMessageActivity.class));
        });
        findViewById(R.id.tv_parameter_settings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        });
        findViewById(R.id.tv_weather).setOnClickListener(v -> {
            WeatherDialog dialog = WeatherDialog.newInstance(MainActivity.this, "[青岛沿海]\n黄岛区大风黄色预警，预警起始时间:2024-07-1621 50:35，请过往船只注意并加强防御");
            dialog.show();
        });
        findViewById(R.id.iv_sos).setOnClickListener(v -> {
            SosDialog dialog = SosDialog.newInstance(MainActivity.this);
            dialog.setPromptButtonClickedListener(new SosDialog.OnPromptButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked() {
                    uploadDeviceSos();
                }

                @Override
                public void onNegativeButtonClicked() {

                }
            });
            dialog.show();
        });
        findViewById(R.id.iv_typhoon_cleanup).setOnClickListener(v -> {
            removeAllTyphoonCircles();
        });
        findViewById(R.id.iv_bluetooth).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });
        findViewById(R.id.iv_volume).setOnClickListener(v -> {
            if (turnOnNotice) {
                turnOnNotice = false;
                Toast.makeText(this, "通知已关闭！", Toast.LENGTH_SHORT).show();
            } else {
                turnOnNotice = true;
                Toast.makeText(this, "通知已开启！", Toast.LENGTH_SHORT).show();
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
        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "未授予定位权限，无法获取位置", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        disposable.dispose();
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
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "台风轨迹", true);
        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_LINE, "台风路径");
        typhoonLayerIds.add(layer.id);
        vector.outlineWidth = "5";
        vector.outlineColor = "#FF00BFFF"; // 深天蓝
        vector.geoPoints.addAll(trackPoints);
        mEarthFragment.drawElement(vector, true);
    }

    // 绘制固定大小的风圈（不叠加、不渐变）
    public void addTyphoonCircle(double lon, double lat, String color, double radiusMeters) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "台风风圈_" + System.currentTimeMillis(), true);
        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "风圈");
        typhoonLayerIds.add(layer.id);
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
                    Toast.makeText(MainActivity.this, "强台风警告", Toast.LENGTH_SHORT).show();
                }

                Log.e("Fangs", "台风等级：" + level + "\n经度:" + lon + ", 纬度:" + lat);
            }, index * 2000L);
        }
    }

    private void toAddPointInMap(double lon, double lat, String ic) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

        VectorElement vector = new VectorElement(layer.id, VectorElement.TYPE_POINT, "点");
        vector.description = "描述";

        vector.isCustomPath = true;
        vector.iconPath = ic;
//        if (vector.isCustomPath) {
//            vector.iconPath = "自定义.png";
//        } else {
//            vector.iconPath = ic;
//        }

        vector.iconScale = 0.6f;
        vector.iconAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER;

        vector.showLabel = false;
        vector.labelColor = "#FF00FF00";

        GeoPoint geo = new GeoPoint(lon, lat, 0.0);
        vector.geoPoints.add(geo);

        long id = mEarthFragment.drawElement(vector, true);
        BMEngine.setElementDescription(id, "测试");
    }

    private void toAddPointInMap(double lon, double lat) {
        long rootID = mEarthFragment.getRootLayerId();
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

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
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

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
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

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
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

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
        VectorElement layer =
                mEarthFragment.onCreateLayer(rootID, "", true);

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

        List<Provider> providers = BMEngine.getMapProviders();
        if (!providers.isEmpty()) {
            Provider provider = providers.get(1); // 1、内置在线地图
            mEarthFragment.changeMapSource(provider.mapId);
            //在线地图
            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_BKG")) {
                mEarthFragment.animateToOfflineArea();
            }
            //离线地图
            if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_PKG")) {
                mEarthFragment.animateToOfflineArea();
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
            startTyphoonSimulation();
            initWind();
            initFishingGround();
        }, 5000L);

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
                String type = geometry.getString("type");

                // 创建图层
                VectorElement layer =
                        mEarthFragment.onCreateLayer(rootLayerId, "渔区_" + i, true);

                if (type.equals("Polygon")) {
                    JSONArray coords = geometry.getJSONArray("coordinates");
                    drawOnePolygon(coords, layer.id);

                } else if (type.equals("MultiPolygon")) {
                    JSONArray multi = geometry.getJSONArray("coordinates");

                    // MultiPolygon = 多个 polygon
                    for (int j = 0; j < multi.length(); j++) {
                        JSONArray poly = multi.getJSONArray(j);
                        drawOnePolygon(poly, layer.id);
                    }
                }
            }

            // 移动视角至渔区中心（山东外海）
//            mEarthFragment.animateTo(
//                    new GeoPoint(122.5, 36.5),
//                    200000,  // 200km 视角
//                    2.0,
//                    -90.0
//            );

            Toast.makeText(this, "渔区全部绘制完成", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "渔区绘制失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void drawOnePolygon(JSONArray polygonArray, long layerId) throws Exception {

        // polygonArray = [ [ [lon,lat], [lon,lat], ... ] ]
        // 外环在第一层，用 polygonArray.getJSONArray(0)

        JSONArray outerRing = polygonArray.getJSONArray(0);

        // 创建面
        VectorElement polygon =
                new VectorElement(layerId, VectorElement.TYPE_PLANE, "渔区");

        // 设置非常明显的样式
        polygon.attribute =
                "strokeColor:#FF0000; strokeWidth:20; fillColor:#55FF0000";

        // 添加点（GeoJSON 是 [lon, lat]）
        for (int k = 0; k < outerRing.length(); k++) {
            JSONArray pt = outerRing.getJSONArray(k);

            double lon = pt.getDouble(0);
            double lat = pt.getDouble(1);

            polygon.geoPoints.add(new GeoPoint(lon, lat));
        }

        // ⭐ 必须：闭合 polygon，否则 Bigemap 会崩溃
        if (!isClosed(polygon.geoPoints)) {
            polygon.geoPoints.add(new GeoPoint(
                    polygon.geoPoints.get(0).lon,
                    polygon.geoPoints.get(0).lat
            ));
        }

        // 绘制
        mEarthFragment.drawElement(polygon, true);
    }

    private boolean isClosed(List<GeoPoint> pts) {
        if (pts.size() < 3) return false;

        GeoPoint a = pts.get(0);
        GeoPoint b = pts.get(pts.size() - 1);

        return Math.abs(a.lon - b.lon) < 1e-6 &&
                Math.abs(a.lat - b.lat) < 1e-6;
    }

    private void initWind() {
        ArrayList<LocationBean> locationList = new ArrayList<>();
        locationList.add(new LocationBean(39.022933, 120.278616, "ic_wind.png"));
        locationList.add(new LocationBean(38.388520, 120.056895, "ic_wind_1.png"));
        locationList.add(new LocationBean(38.214523, 123.641380, "ic_wind_1.png"));
        locationList.add(new LocationBean(34.465327, 125.230379, "ic_wind_2.png"));
        locationList.add(new LocationBean(32.772956, 125.526006, "ic_wind_3.png"));
        locationList.add(new LocationBean(30.825909, 124.971705, "ic_wind_3.png"));
        locationList.add(new LocationBean(30.029324, 126.819377, "ic_wind.png"));
        locationList.add(new LocationBean(28.286816, 124.786937, "ic_wind_2.png"));

        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < locationList.size(); i++) {
            final int index = i;
            int finalI = i;
            handler.postDelayed(() -> {
                double lon = locationList.get(finalI).getLongitude();
                double lat = locationList.get(finalI).getLatitude();
                String ic = locationList.get(finalI).getIc();
                toAddPointInMap(lon, lat, ic);
            }, index * 3000L);
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
        Toast.makeText(
                this,
                "当前经度：" + longitudeData + "\n当前纬度：" + latitudeData,
                Toast.LENGTH_SHORT
        ).show();
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

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Toast.makeText(this, "语音引擎初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // ① 尝试多种中文 Locale，兼容不同厂商实现
        Locale[] candidates = new Locale[]{
                Locale.SIMPLIFIED_CHINESE,   // 推荐
                Locale.CHINA,                // zh_CN
                Locale.CHINESE,              // zh
                new Locale("zh", "CN")       // 再手写一遍
        };

        int result = TextToSpeech.LANG_NOT_SUPPORTED;

        for (Locale loc : candidates) {
            result = tts.setLanguage(loc);
            Log.d("tts", "try locale: " + loc + " result = " + result);
            if (result == TextToSpeech.LANG_AVAILABLE
                    || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                    || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                break; // 找到一个认为“支持”的就结束循环
            }
        }

        // ② 对于大陆很多机型，这里的返回值经常不靠谱
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // ❗不要直接跟用户说“不支持中文”
            // 很多手机会返回这两个值但实际能读中文
            Log.w("tts", "setLanguage 返回不支持中文，先尝试直接播报测试语音");
            // 如果你实在想提示，可以改成引导用户检查系统设置，不要一口咬死“不支持”
            // Toast.makeText(this, "当前设备语音引擎可能未启用中文，请在系统设置中检查文本转语音功能", Toast.LENGTH_LONG).show();
        }

        // ③ 用耳朵做最终判断：直接播一段测试语音
        tts.speak("语音播报功能已经准备就绪",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "init_test");

        isTtsReady = true;
    }

    public static String loadJsonFromAssets(Context context, String fileName) {
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

}
