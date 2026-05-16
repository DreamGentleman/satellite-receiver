package com.yxh.fangs.core.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

final class AndroidTtsSynthesizer implements SpeechSynthesizer, TextToSpeech.OnInitListener {

    private static final String TAG = "AndroidTtsSynthesizer";

    private TextToSpeech tts;
    private boolean ready;
    private Runnable onReady;
    private final Queue<String> queue = new LinkedList<>();

    @Override
    public void init(Context context, Runnable onReady) {
        this.onReady = onReady;

        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), this);
        } else if (ready && onReady != null) {
            onReady.run();
        }
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            ready = false;
            Log.e(TAG, "初始化失败 status=" + status);
            return;
        }

        int result = tts.setLanguage(Locale.CHINESE);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            ready = false;
            Log.e(TAG, "不支持中文语言");
            return;
        }

        ready = true;
        if (onReady != null) {
            onReady.run();
        }
        speakNextIfIdle();
    }

    @Override
    public boolean speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        queue.add(text);
        speakNextIfIdle();
        return true;
    }

    private void speakNextIfIdle() {
        if (!ready || tts == null || tts.isSpeaking()) {
            return;
        }

        String next = queue.poll();
        if (next == null) {
            return;
        }

        tts.speak(next, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void stop() {
        queue.clear();
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    public void release() {
        stop();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
        ready = false;
        onReady = null;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
