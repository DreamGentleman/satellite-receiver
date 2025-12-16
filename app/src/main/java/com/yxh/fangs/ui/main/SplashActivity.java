package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.DeviceRegisterRequest;
import com.yxh.fangs.bean.DeviceRegisterResponse;
import com.yxh.fangs.ui.dialog.RegisterFragment;
import com.yxh.fangs.util.AppConstants;
import com.yxh.fangs.util.AppUpdateUtil;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.HttpUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.NetworkUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.UrlUtils;

import java.nio.charset.StandardCharsets;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        tvInit = findViewById(R.id.tv_init);
        tvUsb = findViewById(R.id.tv_usb);
        tvSdr = findViewById(R.id.tv_sdr);
        tvFrequency = findViewById(R.id.tv_frequency);
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

    private void localCheckLicenseValidityPeriod() {
        String licenseValidityPeriodBase64 = SPUtils.getString(AppConstants.LICENSEVALIDITYPERIOD, "");
        if (TextUtils.isEmpty(licenseValidityPeriodBase64)) {
            showRegisterDialog();
        } else {
            try {
                // Base64 字符串解码成字节数组
                byte[] decode = Base64.decode(licenseValidityPeriodBase64, Base64.DEFAULT);
                // 如果本来是文本
                String licenseValidityPeriod = new String(decode, StandardCharsets.UTF_8);
                licenseValidityPeriod = licenseValidityPeriod.split("_")[0];
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date licenseValidityPeriodDate = sdf.parse(licenseValidityPeriod);
                if (now.before(licenseValidityPeriodDate)) {
//                    Toast.makeText(this, "授权有效期至" + licenseValidityPeriod, Toast.LENGTH_LONG).show();
                    StartSelfCheck();
                } else {
                    showRegisterDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showRegisterDialog();
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
                    try {
                        // Base64 字符串解码成字节数组
                        byte[] decode = Base64.decode(licenseKey, Base64.DEFAULT);
                        // 如果本来是文本
                        String licenseValidityPeriod = new String(decode, StandardCharsets.UTF_8);
                        licenseValidityPeriod = licenseValidityPeriod.split("_")[0];
//                        Toast.makeText(SplashActivity.this, "授权有效期至" + licenseValidityPeriod, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
