package com.yxh.fangs.core.tts;

import android.content.Context;
import android.util.Log;

import java.util.EnumMap;
import java.util.Map;

public class TTSManager {

    public enum Engine {
        IFLYTEK_XTTS,
        ANDROID_SYSTEM
    }

    private static final String TAG = "TTSManager";
    private static final TTSManager INSTANCE = new TTSManager();

    private final Map<Engine, SpeechSynthesizer> synthesizers = new EnumMap<>(Engine.class);
    private Engine currentEngine = Engine.IFLYTEK_XTTS;
    private Context appContext;
    private Runnable onReady;

    public static TTSManager getInstance() {
        return INSTANCE;
    }

    private TTSManager() {
        synthesizers.put(Engine.IFLYTEK_XTTS, new IflytekXttsSynthesizer());
        synthesizers.put(Engine.ANDROID_SYSTEM, new AndroidTtsSynthesizer());
    }

    public void init(Context context, Runnable onReadyCallback) {
        this.appContext = context.getApplicationContext();
        this.onReady = onReadyCallback;

        getSynthesizer(currentEngine).init(appContext, onReadyCallback);
    }

    public void setEngine(Engine engine) {
        if (engine == null || engine == currentEngine) {
            return;
        }

        getSynthesizer(currentEngine).stop();
        currentEngine = engine;

        if (appContext != null) {
            getSynthesizer(currentEngine).init(appContext, onReady);
        }
    }

    public Engine getCurrentEngine() {
        return currentEngine;
    }

    /**
     * 默认播报：优先使用当前引擎。当前默认是科大讯飞 XTTS，失败时自动回退系统 TTS。
     */
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        SpeechSynthesizer synthesizer = getSynthesizer(currentEngine);
        boolean success = synthesizer.speak(text);
        if (!success && currentEngine != Engine.ANDROID_SYSTEM) {
            Log.w(TAG, "当前语音引擎播报失败，回退 Android 系统 TTS");
            speakWith(Engine.ANDROID_SYSTEM, text);
        }
    }

    /**
     * 指定语音引擎播报，后续工具类可以用这个方法按场景切换。
     */
    public void speakWith(Engine engine, String text) {
        if (engine == null || text == null || text.trim().isEmpty()) {
            return;
        }

        SpeechSynthesizer synthesizer = getSynthesizer(engine);
        if (appContext != null && !synthesizer.isReady()) {
            synthesizer.init(appContext, null);
        }
        synthesizer.speak(text);
    }

    public void stop() {
        for (SpeechSynthesizer synthesizer : synthesizers.values()) {
            synthesizer.stop();
        }
    }

    public void release() {
        for (SpeechSynthesizer synthesizer : synthesizers.values()) {
            synthesizer.release();
        }
        appContext = null;
        onReady = null;
        Log.i(TAG, "TTS 已释放");
    }

    private SpeechSynthesizer getSynthesizer(Engine engine) {
        return synthesizers.get(engine);
    }
}
