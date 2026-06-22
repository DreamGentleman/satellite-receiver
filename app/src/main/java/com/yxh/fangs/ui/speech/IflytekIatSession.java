package com.yxh.fangs.ui.speech;

import android.content.Context;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.yxh.fangs.util.IflytekIatJsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class IflytekIatSession {

    interface Callback {
        void onReady();

        void onStatusChanged(String status);

        void onListeningChanged(boolean listening);

        void onTextResult(String text);

        void onError(String message);
    }

    private static final int STREAM_FRAME_BYTES = 1280;
    private static final int STREAM_FRAME_INTERVAL_MS = 40;
    private static final int SESSION_RESULT_WAIT_MS = 1500;

    private Context context;
    private Callback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private volatile boolean feedingAudio;
    private volatile boolean fileStreamingMode;
    private volatile CountDownLatch segmentResultLatch;

    IflytekIatSession(Context context, Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
    }

    void init() {
        recognizer = SpeechRecognizer.createRecognizer(context, initListener);
    }

    boolean isReady() {
        return recognizer != null;
    }

    boolean startMicrophone() {
        if (!isReady()) {
            callback.onError("听写初始化失败");
            return false;
        }
        fileStreamingMode = false;
        setDefaultParams();
        int ret = recognizer.startListening(recognizerListener);
        if (ret == ErrorCode.SUCCESS) {
            callback.onListeningChanged(true);
            callback.onStatusChanged("请开始说话");
            return true;
        }
        callback.onError("听写启动失败：" + ret);
        return false;
    }

    void recognizePcmFile(File audioFile) {
        if (!isReady()) {
            callback.onError("听写初始化失败");
            return;
        }
        fileStreamingMode = true;
        feedingAudio = true;
        feedPcmFileContinuously(audioFile);
    }

    void stop() {
        feedingAudio = false;
        fileStreamingMode = false;
        if (recognizer != null && recognizer.isListening()) {
            recognizer.stopListening();
            callback.onStatusChanged("已停止输入，正在识别");
        }
    }

    void release() {
        feedingAudio = false;
        fileStreamingMode = false;
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }

    private void feedPcmFileContinuously(File audioFile) {
        try (FileInputStream input = new FileInputStream(audioFile)) {
            byte[] buffer = new byte[STREAM_FRAME_BYTES];
            int sessionIndex = 1;
            int read;

            while (feedingAudio && recognizer != null && (read = input.read(buffer)) > 0) {
                if (!recognizer.isListening()) {
                    waitForCurrentSessionResult();
                    if (!startStreamingRecognizerSession("正在识别第" + sessionIndex + "段")) {
                        break;
                    }
                    sessionIndex++;
                }

                recognizer.writeAudio(buffer, 0, read);
                sleepFrameInterval(read);
            }

            feedingAudio = false;
            fileStreamingMode = false;
            if (recognizer != null && recognizer.isListening()) {
                recognizer.stopListening();
                waitForCurrentSessionResult();
            }
            callback.onListeningChanged(false);
            callback.onStatusChanged("音频已全部写入，识别完成");
        } catch (IOException e) {
            if (recognizer != null) {
                recognizer.cancel();
            }
            fileStreamingMode = false;
            callback.onListeningChanged(false);
            callback.onError("读取转换音频失败：" + e.getMessage());
        }
    }

    private boolean startStreamingRecognizerSession(String status) {
        CountDownLatch startLatch = new CountDownLatch(1);
        boolean[] started = new boolean[]{false};
        CountDownLatch resultLatch = new CountDownLatch(1);
        segmentResultLatch = resultLatch;

        mainHandler.post(() -> {
            setDefaultParams();
            recognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
            recognizer.setParameter(SpeechConstant.VAD_BOS, "10000");
            recognizer.setParameter(SpeechConstant.VAD_EOS, "10000");
            recognizer.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");

            int ret = recognizer.startListening(recognizerListener);
            if (ret == ErrorCode.SUCCESS) {
                started[0] = true;
                callback.onListeningChanged(true);
                callback.onStatusChanged(status);
            } else {
                feedingAudio = false;
                fileStreamingMode = false;
                callback.onListeningChanged(false);
                callback.onError("录音识别启动失败：" + ret);
                resultLatch.countDown();
            }
            startLatch.countDown();
        });

        try {
            startLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return started[0];
    }

    private void waitForCurrentSessionResult() {
        CountDownLatch latch = segmentResultLatch;
        if (latch == null) {
            return;
        }
        try {
            latch.await(SESSION_RESULT_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sleepFrameInterval(int bytesWritten) {
        long sleepMs = Math.max(10L, bytesWritten * STREAM_FRAME_INTERVAL_MS / STREAM_FRAME_BYTES);
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setDefaultParams() {
        recognizer.setParameter(SpeechConstant.PARAMS, null);
        recognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        recognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        recognizer.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        recognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        recognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
        recognizer.setParameter(SpeechConstant.SAMPLE_RATE, String.valueOf(AudioTranscoder.TARGET_SAMPLE_RATE));
        recognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
        recognizer.setParameter(SpeechConstant.VAD_EOS, "1000");
        recognizer.setParameter(SpeechConstant.ASR_PTT, "1");
        recognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        File mscDir = context.getExternalFilesDir("msc");
        if (mscDir != null) {
            recognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, new File(mscDir, "iat.wav").getAbsolutePath());
        }
    }

    private String getResourcePath() {
        return ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "iat/common.jet")
                + ";"
                + ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "iat/sms_16k.jet");
    }

    private final InitListener initListener = code -> {
        if (code == ErrorCode.SUCCESS) {
            callback.onReady();
        } else {
            callback.onError("听写初始化失败：" + code);
        }
    };

    private final RecognizerListener recognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            callback.onStatusChanged("正在听写，音量：" + volume);
        }

        @Override
        public void onBeginOfSpeech() {
            callback.onListeningChanged(true);
            callback.onStatusChanged("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            callback.onStatusChanged("输入结束，正在识别");
        }

        @Override
        public void onResult(RecognizerResult result, boolean isLast) {
            String text = IflytekIatJsonParser.parse(result.getResultString());
            if (!text.isEmpty()) {
                callback.onTextResult(text);
            }
            if (isLast) {
                if (fileStreamingMode && feedingAudio) {
                    CountDownLatch latch = segmentResultLatch;
                    if (latch != null) {
                        latch.countDown();
                    }
                    callback.onStatusChanged("继续识别本地录音");
                } else {
                    feedingAudio = false;
                    callback.onListeningChanged(false);
                    callback.onStatusChanged("识别完成");
                }
            }
        }

        @Override
        public void onError(SpeechError error) {
            if (fileStreamingMode && feedingAudio) {
                CountDownLatch latch = segmentResultLatch;
                if (latch != null) {
                    latch.countDown();
                }
                callback.onStatusChanged("继续识别本地录音");
            } else {
                feedingAudio = false;
                callback.onListeningChanged(false);
                callback.onError(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };
}
