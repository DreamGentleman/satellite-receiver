package com.yxh.fangs.ui.sos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.SosBean;
import com.yxh.fangs.bean.SosEventRequest;
import com.yxh.fangs.bean.SosEventResponse;
import com.yxh.fangs.data.network.HttpUtils;
import com.yxh.fangs.data.network.api.UrlUtils;
import com.yxh.fangs.ui.dialog.CustomSosFragment;
import com.yxh.fangs.ui.main.BaseActivity;
import com.yxh.fangs.util.ApkUtils;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.LogUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;

public class SosActivity extends BaseActivity {

    private TextView tvSosContent;
    private View vSos;
    private TextView tvSos;
    private TextView tvTime;
    private TextView tvBack;
    private CheckBox cb13;
    private CheckBox cb16;
    private SosBean sosBean;
    private SosBean.DistressCodeTableBean.CodeListBean selectedSos;
    private Disposable countDownDisposable;
    private Location lastLocation;
    /**
     * 所有 CheckBox 统一管理
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();
    private LocationManager locationManager;
    private int REQ_LOCATION = 1002;
    // 倒计时总秒数（30秒）
    private static final long TOTAL_SECONDS = 30;

    // 当前剩余秒数（初始30）
    private long remainSeconds = TOTAL_SECONDS;

    // 是否正在运行
    private boolean isRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        initView();
        initData();
        cb13.setChecked(true);
        List<SosBean.DistressCodeTableBean.CodeListBean> codeList = sosBean.getDistressCodeTable().getCodeList();
        for (int i = 0; i < codeList.size(); i++) {
            if ("火灾/爆炸".equals(codeList.get(i).getDistressName())) {
                selectedSos = codeList.get(i);
                break;
            }
        }

    }

    private void initData() {
        String json = loadJsonFromAssets(this, "sos.json");
        Gson gson = new Gson();
        sosBean = gson.fromJson(json, SosBean.class);
    }

    private void initView() {
        initLocation();
        tvSosContent = findViewById(R.id.tv_sos_content);
        vSos = findViewById(R.id.v_sos);
        tvSos = findViewById(R.id.tv_sos);
        tvTime = findViewById(R.id.tv_time);
        tvBack = findViewById(R.id.tv_back);
        cb13 = findViewById(R.id.cb_13);
        cb16 = findViewById(R.id.cb_16);

        // 初始化并加入列表
        addCheckBox(R.id.cb_1);
        addCheckBox(R.id.cb_2);
        addCheckBox(R.id.cb_3);
        addCheckBox(R.id.cb_4);
        addCheckBox(R.id.cb_5);
        addCheckBox(R.id.cb_6);
        addCheckBox(R.id.cb_7);
        addCheckBox(R.id.cb_8);
        addCheckBox(R.id.cb_10);
        addCheckBox(R.id.cb_11);
        addCheckBox(R.id.cb_12);
        addCheckBox(R.id.cb_13);
        addCheckBox(R.id.cb_14);
        addCheckBox(R.id.cb_15);
        addCheckBox(R.id.cb_16);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSos();
            }
        });
//        startCountDown(TOTAL_SECONDS);
    }

    private void submitSos() {
        SosEventRequest sosEventRequest = new SosEventRequest();
        sosEventRequest.setVersion(ApkUtils.getVersionName(this));
        sosEventRequest.setSendTime(nowTime());
        sosEventRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        SosEventRequest.VesselInfoBean vesselInfo = new SosEventRequest.VesselInfoBean();
        //TODO 获取实际信息
        vesselInfo.setMmsiCode("qx-wgeqwrgrg444");
        vesselInfo.setVesselName("闽油66889");
        sosEventRequest.setVesselInfo(vesselInfo);
        SosEventRequest.PositionBean position = new SosEventRequest.PositionBean();
        position.setLatitude(lastLocation == null ? "0" : lastLocation.getLatitude() + "");
        position.setLongitude(lastLocation == null ? "0" : lastLocation.getLongitude() + "");
        sosEventRequest.setPosition(position);
        sosEventRequest.setDistressType(new Gson().toJson(selectedSos));
        sosEventRequest.setCrewNumber(5);
        sosEventRequest.setContact("VHF16频道/138xxxX8888");
        String json = new Gson().toJson(sosEventRequest);
        LogUtils.i(json);
        HttpUtils.postJson(UrlUtils.getSosEventStartUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                SosEventResponse sosEventResponse = gson.fromJson(body, SosEventResponse.class);
                if (sosEventResponse.getCode() == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(SosActivity.this,
                                "SOS求救信息已经发送成功！",
                                Toast.LENGTH_SHORT).show();

                        Intent data = new Intent();
                        data.putExtra("sos_result", true);
                        data.putExtra("sos_content", json);
                        data.putExtra("sos_type", selectedSos.getDistressName());
                        setResult(RESULT_OK, data);

                        finish();
                    });
                } else {
                    onError(sosEventResponse.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(SosActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 统一初始化 CheckBox
     */
    private void addCheckBox(int id) {
        CheckBox checkBox = findViewById(id);
        checkBox.setOnClickListener(this::onCheckBoxClick);
        checkBoxList.add(checkBox);
    }

    /**
     * 核心逻辑：
     * 用户点击哪个，就只选中哪个，其余全部取消
     */
    private void onCheckBoxClick(View view) {
        CheckBox clicked = (CheckBox) view;

        // 1️⃣ 全部取消（不会触发点击事件）
        setNotCheckAll();

        // 2️⃣ 再选中当前
        clicked.setChecked(true);

        // 3️⃣ 业务处理（根据 id）
        handleSosSelected(clicked.getText().toString().toString());
    }

    /**
     * 取消全部选中
     */
    private void setNotCheckAll() {
        for (CheckBox cb : checkBoxList) {
            cb.setChecked(false);
        }
    }

    /**
     * 统一处理选中后的业务
     */
    private void handleSosSelected(String text) {
        if ("自定义报警".equals(text)) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag("sos") == null) {
                new CustomSosFragment().show(fm, "sos");
                pauseCountDown();
            }
        } else {
            SosBean.DistressCodeTableBean distressCodeTable = sosBean.getDistressCodeTable();
            List<SosBean.DistressCodeTableBean.CodeListBean> codeList = distressCodeTable.getCodeList();
            for (int i = 0; i < codeList.size(); i++) {
                if (text.equals(codeList.get(i).getDistressName())) {
                    selectedSos = codeList.get(i);
                    tvSosContent.setText(selectedSos.getCoreDescription());
                    return;
                }
            }
        }
    }

    /**
     * 从 assets 读取 json
     */
    private String loadJsonFromAssets(Context context, String fileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getAssets().open(fileName),
                        StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public void startCountDown(long startSeconds) {

        // 防止重复启动
        cancelCountDown();

        isRunning = true;
        remainSeconds = startSeconds;

        countDownDisposable = io.reactivex.rxjava3.core.Observable
                .intervalRange(0,                 // 从 0 开始
                        startSeconds + 1,                // 一共发 31 次（0~30）
                        0,
                        1,
                        java.util.concurrent.TimeUnit.SECONDS
                )
                .map(aLong -> startSeconds  - aLong)   // 转成 30 → 0
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(
                        seconds -> {
                            remainSeconds = seconds;
                            tvTime.setText(seconds + "");
                        },
                        Throwable::printStackTrace,
                        () -> {
                            isRunning = false;
                            submitSos();
                        }
                );
    }

    private String nowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.CHINA);
        return sdf.format(new Date());
    }

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 没权限就申请
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
            return;
        }

        // 先拿一次“最近一次定位”（可能为空，但很快）
        Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps != null) {
            lastLocation = gps;
        } else if (net != null) {
            lastLocation = net;
        }

        // 再持续更新（gps/network任选一个都行，我这里两个都监听，增强可用性）
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, locationListener);
        } catch (Exception ignore) {
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5, locationListener);
        } catch (Exception ignore) {
        }
    }


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownDisposable != null && !countDownDisposable.isDisposed()) {
            countDownDisposable.dispose();
        }
    }

    public void setCustomContent(String content) {
        if (TextUtils.isEmpty(content)) {
            cb13.setChecked(true);
            cb16.setChecked(false);
            return;
        }
        if (selectedSos == null) {
            return;
        }
        SosBean.DistressCodeTableBean distressCodeTable = sosBean.getDistressCodeTable();
        List<SosBean.DistressCodeTableBean.CodeListBean> codeList = distressCodeTable.getCodeList();
        for (int i = 0; i < codeList.size(); i++) {
            if ("自定义报警".equals(codeList.get(i).getDistressName())) {
                selectedSos = codeList.get(i);
                selectedSos.setCoreDescription(content);
                tvSosContent.setText(selectedSos.getCoreDescription());
                return;
            }
        }
    }

    // 暂停
    public void pauseCountDown() {
//        if (!isRunning) return;
//        isRunning = false;
//        cancelCountDown(); // 停止订阅，保持 remainSeconds 不变
    }

    // 继续
    public void resumeCountDown() {
//        if (isRunning) return;
//        if (remainSeconds <= 0) {
//            // 已经结束了就直接提交
//            submitSos();
//            return;
//        }
//        startCountDown(remainSeconds);
    }

    private void cancelCountDown() {
        if (countDownDisposable != null && !countDownDisposable.isDisposed()) {
            countDownDisposable.dispose();
        }
        countDownDisposable = null;
    }
}
