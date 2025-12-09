package com.yxh.fangs.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class TTSManager implements TextToSpeech.OnInitListener {

    private static final TTSManager INSTANCE = new TTSManager();

    private TextToSpeech tts = null;
    private boolean ready = false;
    private Runnable onReady = null;
    private final Queue<String> queue = new LinkedList<>();

    public static TTSManager getInstance() {
        return INSTANCE;
    }

    private TTSManager() {
    }

    /**
     * 初始化 TTS
     *
     * @param context         上下文
     * @param onReadyCallback 初始化完成后的回调
     */
    public void init(Context context, Runnable onReadyCallback) {
        this.onReady = onReadyCallback;

        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), this);
        } else if (ready) {
            if (onReadyCallback != null) onReadyCallback.run();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CHINESE);

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                Log.e("TTSManager", "不支持中文语言");
                ready = false;
                return;
            }

            ready = true;
            Log.i("TTSManager", "TTS 初始化成功");

            if (onReady != null) onReady.run();
            if (!queue.isEmpty()) speakNext();

        } else {
            Log.e("TTSManager", "TTS 初始化失败 status=" + status);
        }
    }

    /**
     * 播报文字
     */
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;

        queue.add(text);

        if (ready && !tts.isSpeaking()) {
            speakNext();
        }
    }

    /**
     * 播放队列中下一条
     */
    private void speakNext() {
        String next = queue.poll();
        if (next == null) return;

        tts.speak(next, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 释放资源
     */
    public void release() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        ready = false;
        tts = null;
        Log.i("TTSManager", "TTS 已释放");
    }
}
