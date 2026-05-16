package com.yxh.fangs.application;

import android.app.Application;

import com.aliyun.emas.apm.Apm;
import com.aliyun.emas.apm.ApmOptions;
import com.aliyun.emas.apm.crash.ApmCrashAnalysisComponent;
import com.aliyun.emas.apm.mem.monitor.ApmMemMonitorComponent;
import com.aliyun.emas.apm.performance.ApmPerformanceComponent;
import com.aliyun.emas.apm.remote.log.ApmRemoteLogComponent;
import com.yxh.fangs.R;
import com.yxh.fangs.core.scheduler.MapEngineManager;
import com.yxh.fangs.util.SPUtils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this);
        MapEngineManager.init(this, true);
        initAliyun();
    }

    private void initAliyun() {
        Apm.preStart(new ApmOptions.Builder()
                //必须配置application
                .setApplication(this)
                //必须配置EMAS的appKey
                .setAppKey(getString(R.string.aliyun_app_key))
                //必须配置EMAS的appSecret
                .setAppSecret(getString(R.string.aliyun_app_secret))
                //使用性能分析或者远程日志，必须配置EMAS的appRsaSecret
                .setAppRsaSecret(getString(R.string.aliyun_app_rsa_secret))
                //配置使用崩溃分析功能
                .addComponent(ApmCrashAnalysisComponent.class)
                //配置使用内存分析功能, 2.1.0版本新增
                .addComponent(ApmMemMonitorComponent.class)
                //配置使用远程日志功能
                .addComponent(ApmRemoteLogComponent.class)
                //配置使用性能分析功能
                .addComponent(ApmPerformanceComponent.class)
                .build()
        );
    }
}
