package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.BaseLibrary;
import com.iflytek.aikit.core.CoreListener;
import com.iflytek.aikit.core.ErrType;
import com.iflytek.aikit.core.LogLvl;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.DeviceRegisterRequest;
import com.yxh.fangs.bean.DeviceRegisterResponse;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.core.tts.TTSManager;
import com.yxh.fangs.data.network.HttpUtils;
import com.yxh.fangs.data.network.NetworkUtils;
import com.yxh.fangs.data.network.api.UrlUtils;
import com.yxh.fangs.ui.dialog.RegisterFragment;
import com.yxh.fangs.util.AppUpdateUtil;
import com.yxh.fangs.util.AssetCopyUtil;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.LicenseSerialParser;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {

    private TextView tvInit;
    private TextView tvUsb;
    private TextView tvSdr;
    private TextView tvFrequency;
    private Disposable disposable;
    private String APPID;
    private String APIKEY;
    private String APISECRET;
    private String WORK_DIR;
    private int authResult = -1;
    private boolean ttsInitialized = false;

    //授权结果回调
    private CoreListener coreListener = new CoreListener() {
        @Override
        public void onAuthStateChange(final ErrType type, final int code) {
            Log.i("科大讯飞", "core listener code:" + code);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (type) {
                        case AUTH:
                            authResult = code;
                            if (code == 0) {
                                initTtsAfterAIKitReady();
                            } else {
                                Log.e("科大讯飞", "SDK授权失败，授权码为:" + authResult);
                            }
                            break;
                        case HTTP:
                            Toast.makeText(SplashActivity.this, "SDK状态：HTTP认证结果" + code,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SplashActivity.this, "SDK状态：其他错误" + code,
                                    Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        tvInit = findViewById(R.id.tv_init);
        tvUsb = findViewById(R.id.tv_usb);
        tvSdr = findViewById(R.id.tv_sdr);
        tvFrequency = findViewById(R.id.tv_frequency);
        initAIKit();
        boolean online = NetworkUtils.isOnline(this);
        if (online) {
            AppUpdateUtil.getInstance(this, new AppUpdateUtil.CallBack() {
                @Override
                public void onUpdateError(String message) {
                    //检查授权有效期
                    localCheckLicenseValidityPeriod();
                }

                @Override
                public void onUpdateSuccess() {
                    String licenseValidityPeriodBase64 = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");
                    if (TextUtils.isEmpty(licenseValidityPeriodBase64)) {
                        showRegisterDialog();
                    } else {
                        getLicenseValidityPeriod(licenseValidityPeriodBase64);
                    }
                }
            }).checkVersion();
//            String licenseValidityPeriodBase64 = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");
//            if (TextUtils.isEmpty(licenseValidityPeriodBase64)) {
//                showRegisterDialog();
//            } else {
//                getLicenseValidityPeriod(licenseValidityPeriodBase64);
//            }
        } else {
            //检查授权有效期
            localCheckLicenseValidityPeriod();
        }
    }

    private void initAIKit() {
        APPID = getResources().getString(R.string.appId);
        APIKEY = getResources().getString(R.string.apiKey);
        APISECRET = getResources().getString(R.string.apiSecret);
        WORK_DIR = getCacheDir() + getResources().getString(R.string.workDir);
        File workDir = new File(WORK_DIR);
        if (!workDir.exists()) {
            workDir.mkdirs();
        }
        AssetCopyUtil.copyAssetsFolder(this, "xtts", workDir.getAbsolutePath() + "/xtts");

        AiHelper.getInst().setLogInfo(LogLvl.VERBOSE, 1, WORK_DIR + "/aeeLog.txt");
        //设定初始化参数
        BaseLibrary.Params params = BaseLibrary.Params.builder()
                .appId(APPID)  //您的应用ID，可从控制台查看
                .apiKey(APIKEY) //您的APIKEY，可从控制台查看
                .apiSecret(APISECRET) //您的APISECRET，可从控制台查看
                .workDir(WORK_DIR) //SDK的工作目录，需要确保有读写权限。一般用于存放离线能力资源，日志存放目录等使用。
                .build();
        //初始化SDK
        new Thread(new Runnable() {
            @Override
            public void run() {
                AiHelper.getInst().initEntry(SplashActivity.this.getApplicationContext(), params);
            }
        }).start();
        AiHelper.getInst().registerListener(coreListener);// 注册SDK 初始化状态监听
    }

    private void initTtsAfterAIKitReady() {
        if (ttsInitialized) {
            return;
        }
        ttsInitialized = true;
        TTSManager.getInstance().setEngine(TTSManager.Engine.IFLYTEK_XTTS);
        TTSManager.getInstance().init(this, () -> {
            TTSManager.getInstance().speakWith(
                    TTSManager.Engine.IFLYTEK_XTTS,
                    "语音功能初始化成功！"
            );
        });
    }

    private void localCheckLicenseValidityPeriod() {
        String licenseValidityPeriodKey = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");
        if (TextUtils.isEmpty(licenseValidityPeriodKey)) {
            showRegisterDialog();
        } else {
            String parsedDateTime = LicenseSerialParser.parseDateTime(licenseValidityPeriodKey, "QX1SB");
            if (TextUtils.isEmpty(parsedDateTime) || parsedDateTime.length() != 14) {
                showRegisterDialog();
            } else {
                parsedDateTime = parsedDateTime.substring(0, parsedDateTime.length() - 6);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
                try {
                    Date targetDate = sdf.parse(parsedDateTime);
                    Date now = new Date();
                    if (now.before(targetDate)) {
                        StartSelfCheck();
                    } else {
                        showRegisterDialog();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    showRegisterDialog();

                }
            }
        }
    }

    public void StartSelfCheck() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                // 参数说明：
                // 参数1 = 第1次延迟时间；
                // 参数2 = 间隔时间数字；
                // 参数3 = 时间单位；
                // 该例子发送的事件特点：延迟2s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
                /*
                 * 步骤2：每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
                 *  即每隔1秒产生1个数字前，就发送1次网络请求，从而实现轮询需求
                 **/
                .subscribeOn(Schedulers.computation())              // 上游在 IO
                .observeOn(AndroidSchedulers.mainThread()) // 下游切到主线程
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        if (aLong == 1) {
                            showLoading();
                        } else if (aLong == 4) {
                            hideLoading();
                        } else if (aLong == 5) {
                            tvInit.setVisibility(View.VISIBLE);
                        } else if (aLong == 6) {
                            tvUsb.setVisibility(View.VISIBLE);
                        } else if (aLong == 7) {
                            tvSdr.setVisibility(View.VISIBLE);
                        } else if (aLong == 8) {
                            tvFrequency.setVisibility(View.VISIBLE);
                        } else if (aLong == 10) {
                            if (disposable != null && !disposable.isDisposed()) {
                                disposable.dispose(); // 关闭订阅
                            }
                            gotoMain();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getLicenseValidityPeriod(String licenseKey) {
        DeviceRegisterRequest deviceRegisterRequest = new DeviceRegisterRequest();
        deviceRegisterRequest.setDeviceSn(DeviceUtils.getDeviceId(this));
        deviceRegisterRequest.setDeviceName(DeviceUtils.getDeviceName(this));
        deviceRegisterRequest.setDeviceModel(DeviceUtils.getDeviceModel());
        deviceRegisterRequest.setLicenseKey(licenseKey);
        String json = new Gson().toJson(deviceRegisterRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getReceiverRegisterUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                DeviceRegisterResponse response = gson.fromJson(body, DeviceRegisterResponse.class);
                if (response.getCode() == 200) {
                    SPUtils.putString(AppConstants.LICENSEVALIDITYPERIOD, licenseKey);
                    StartSelfCheck();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_LONG).show();
                showRegisterDialog();
            }
        });
    }

    private void showRegisterDialog() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("register") == null) {
            new RegisterFragment().show(fm, "register");
        }
    }

    private void gotoMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();  // 关闭当前页面
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose(); // 关闭订阅
        }
        hideLoading();
        super.onDestroy();
    }
}
