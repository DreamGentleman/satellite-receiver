package com.yxh.fangs.core.tts;

import android.content.Context;

interface SpeechSynthesizer {

    void init(Context context, Runnable onReady);

    boolean speak(String text);

    void stop();

    void release();

    boolean isReady();
}
