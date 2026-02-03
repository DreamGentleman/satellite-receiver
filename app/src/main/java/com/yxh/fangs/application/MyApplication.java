package com.yxh.fangs.application;

import android.app.Application;

import com.aliyun.emas.apm.Apm;
import com.aliyun.emas.apm.ApmOptions;
import com.aliyun.emas.apm.crash.ApmCrashAnalysisComponent;
import com.aliyun.emas.apm.mem.monitor.ApmMemMonitorComponent;
import com.aliyun.emas.apm.performance.ApmPerformanceComponent;
import com.aliyun.emas.apm.remote.log.ApmRemoteLogComponent;
import com.yxh.fangs.bean.MessageResponse;
import com.yxh.fangs.core.scheduler.MapEngineManager;
import com.yxh.fangs.core.tts.TTSManager;
import com.yxh.fangs.util.SPUtils;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {
    // 单例 Application
    private static MyApplication instance;

    // 全局消息列表
    private final List<MessageResponse.MessageItem> data = new ArrayList<>();
    public String showLayoutText = "";

    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this);
        instance = this;
        MapEngineManager.init(this, true);
        TTSManager.getInstance().init(this, () -> {
            TTSManager.getInstance().speak("语音功能初始化成功！");
        });
        initAliyun();
    }

    private void initAliyun() {
        Apm.preStart(new ApmOptions.Builder()
                //必须配置application
                .setApplication(this)
                //必须配置EMAS的appKey
                .setAppKey("335661311")
                //必须配置EMAS的appSecret
                .setAppSecret("f2d1f5b7a53641189c5e1f4e60fe9301")
                //使用性能分析或者远程日志，必须配置EMAS的appRsaSecret
                .setAppRsaSecret("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBrAcssqQFaYBQzg5rBxK6kZ1OyyLKBr0w0dvGXFLWuhOwOBrc+C71k8/qLW2mFJcjiqf1AMgZ6bHOhMWd2iJy0OFO5RuDZ28+X6ZYxToxaz6Vm44WPuEB+IiDBNcLTy7wSJiajqJf5s4ckOQSjMbSVI4padB90gtGTlIplOETgQIDAQAB")
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

    public List<MessageResponse.MessageItem> getData() {
        return data;
    }

    public void addData(MessageResponse.MessageItem item) {
        if (!data.contains(item)) {
            data.add(item);
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }
}