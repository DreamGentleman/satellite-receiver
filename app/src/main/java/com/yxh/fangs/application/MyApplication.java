package com.yxh.fangs.application;

import android.app.Application;

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