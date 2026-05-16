package com.yxh.fangs.core.tts;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.iflytek.aikit.core.AeeEvent;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiListener;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiText;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class IflytekXttsSynthesizer implements SpeechSynthesizer {

    private static final String TAG = "IflytekXttsSynth";
    private static final String ABILITY_ID = "e2e44feff";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final AtomicBoolean listenerRegistered = new AtomicBoolean(false);
    private final Object handleLock = new Object();

    private HandlerThread audioThread;
    private Handler audioHandler;
    private volatile AudioTrack audioTrack;
    private AiHandle aiHandle;
    private boolean ready;
    private volatile boolean acceptingAudio = false;
    private Runnable onReady;

    private final AiListener aiListener = new AiListener() {
        @Override
        public void onResult(int handleID, List<AiResponse> responses, Object usrContext) {
            if (responses == null || responses.isEmpty()) {
                return;
            }

            for (AiResponse response : responses) {
                if (response == null || !"audio".equals(response.getKey())) {
                    continue;
                }

                byte[] audio = response.getValue();
                if (acceptingAudio && audio != null && audio.length > 0) {
                    writeAudio(audio);
                }
            }
        }

        @Override
        public void onEvent(int handleID, int event, List<AiResponse> eventData, Object usrContext) {
            if (event == AeeEvent.AEE_EVENT_END.getValue()) {
                endCurrentHandle();
            }
        }

        @Override
        public void onError(int handleID, int err, String msg, Object usrContext) {
            Log.e(TAG, "讯飞 XTTS 合成失败 err=" + err + ", msg=" + msg);
            stop();
        }
    };

    @Override
    public void init(Context context, Runnable onReady) {
        this.onReady = onReady;
        ensureAudioThread();

        if (listenerRegistered.compareAndSet(false, true)) {
            AiHelper.getInst().registerListener(ABILITY_ID, aiListener);
        }

        ready = true;
        if (onReady != null) {
            onReady.run();
        }
    }

    @Override
    public boolean speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        if (!ready) {
            Log.w(TAG, "讯飞 XTTS 尚未初始化");
            return false;
        }

        stop();
        acceptingAudio = true;
        initAudioTrack();

        AiRequest params = AiRequest.builder()
                .param("vcn", "xiaofeng")
                .param("language", 1)
                .param("textEncoding", "UTF-8")
                .param("pitch", 50)
                .param("volume", 80)
                .param("speed", 50)
                .build();

        AiHandle handle = AiHelper.getInst().start(ABILITY_ID, params, null);
        if (handle == null || handle.getCode() != 0) {
            int code = handle == null ? -1 : handle.getCode();
            Log.e(TAG, "start 失败 code=" + code);
            stop();
            return false;
        }

        synchronized (handleLock) {
            aiHandle = handle;
        }

        AiRequest request = AiRequest.builder()
                .payload(AiText.get("text").data(text).valid())
                .build();
        int ret = AiHelper.getInst().write(request, handle);
        if (ret != 0) {
            Log.e(TAG, "write 失败 code=" + ret);
            stop();
            return false;
        }

        return true;
    }

    private void ensureAudioThread() {
        if (audioThread != null) {
            return;
        }

        audioThread = new HandlerThread("iflytek-xtts-audio");
        audioThread.start();
        audioHandler = new Handler(audioThread.getLooper());
    }

    private void initAudioTrack() {
        ensureAudioThread();
        audioHandler.post(() -> {
            if (!acceptingAudio) {
                return;
            }
            releaseAudioTrack();
            int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            if (minBufferSize <= 0) {
                Log.e(TAG, "AudioTrack minBufferSize 异常: " + minBufferSize);
                acceptingAudio = false;
                return;
            }
            if (!acceptingAudio) {
                return;
            }
            AudioTrack track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
            );
            audioTrack = track;
            try {
                track.play();
            } catch (IllegalStateException e) {
                Log.e(TAG, "AudioTrack play 失败", e);
                releaseAudioTrack();
                acceptingAudio = false;
            }
        });
    }

    private void writeAudio(byte[] audio) {
        if (!acceptingAudio) {
            return;
        }
        ensureAudioThread();
        audioHandler.post(() -> {
            if (acceptingAudio && audioTrack != null) {
                try {
                    audioTrack.write(audio, 0, audio.length);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "AudioTrack write 已停止", e);
                }
            }
        });
    }

    private void endCurrentHandle() {
        AiHandle handle;
        synchronized (handleLock) {
            handle = aiHandle;
            aiHandle = null;
        }

        if (handle != null) {
            try {
                AiHelper.getInst().end(handle);
            } catch (Throwable e) {
                Log.w(TAG, "end handle 失败", e);
            }
        }
    }

    @Override
    public void stop() {
        acceptingAudio = false;
        endCurrentHandle();
        if (audioHandler != null) {
            audioHandler.removeCallbacksAndMessages(null);
            audioHandler.post(this::releaseAudioTrack);
        }
        releaseAudioTrack();
    }

    @Override
    public void release() {
        stop();
        ready = false;
        onReady = null;

        try {
            AiHelper.getInst().engineUnInit(ABILITY_ID);
        } catch (Throwable e) {
            Log.w(TAG, "engineUnInit 失败", e);
        }

        if (audioThread != null) {
            audioThread.quitSafely();
            audioThread = null;
            audioHandler = null;
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    private void stopAudioTrack() {
        if (audioTrack != null) {
            AudioTrack track = audioTrack;
            audioTrack = null;
            try {
                track.pause();
                track.flush();
                track.stop();
            } catch (IllegalStateException ignored) {
            }
            track.release();
        }
    }

    private void releaseAudioTrack() {
        stopAudioTrack();
    }
}
