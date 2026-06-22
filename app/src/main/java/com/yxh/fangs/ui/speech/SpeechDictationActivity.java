package com.yxh.fangs.ui.speech;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yxh.fangs.R;
import com.yxh.fangs.ui.main.BaseActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeechDictationActivity extends BaseActivity implements IflytekIatSession.Callback {

    private EditText etResult;
    private TextView tvStatus;
    private TextView tvStart;
    private TextView tvStop;

    private IflytekIatSession iatSession;
    private AudioTranscoder audioTranscoder;
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String> recordPermissionLauncher;
    private ActivityResultLauncher<String> audioPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_dictation);
        initLaunchers();
        initViews();
        initSpeech();
    }

    private void initLaunchers() {
        recordPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        startMicrophoneDictation();
                    } else {
                        showStatus("未授予录音权限");
                    }
                }
        );

        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        startAudioFileDictation(uri);
                    }
                }
        );
    }

    private void initViews() {
        etResult = findViewById(R.id.et_result);
        tvStatus = findViewById(R.id.tv_status);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);

        tvStart.setOnClickListener(v -> requestRecordAndStart());
        tvStop.setOnClickListener(v -> stopDictation());
        findViewById(R.id.tv_import_audio).setOnClickListener(v -> audioPickerLauncher.launch("audio/*"));
        findViewById(R.id.tv_clear).setOnClickListener(v -> etResult.setText(""));
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
    }

    private void initSpeech() {
        audioTranscoder = new AudioTranscoder(this);
        iatSession = new IflytekIatSession(this, this);
        iatSession.init();
    }

    private void requestRecordAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startMicrophoneDictation();
        } else {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startMicrophoneDictation() {
        if (!ensureRecognizerReady()) {
            return;
        }
        etResult.setText("");
        iatSession.startMicrophone();
    }

    private void startAudioFileDictation(Uri uri) {
        if (!ensureRecognizerReady()) {
            return;
        }
        etResult.setText("");
        setListeningState(true, "正在读取本地录音");
        audioExecutor.execute(() -> {
            showStatus("正在转换录音格式");
            File audioFile = audioTranscoder.transcodeTo16kMonoPcm(uri);
            if (audioFile == null) {
                setListeningState(false, "无法读取录音文件");
                return;
            }
            iatSession.recognizePcmFile(audioFile);
        });
    }

    private void stopDictation() {
        if (iatSession != null) {
            iatSession.stop();
        }
    }

    private boolean ensureRecognizerReady() {
        if (iatSession == null || !iatSession.isReady()) {
            showStatus("听写初始化失败");
            return false;
        }
        return true;
    }

    @Override
    public void onReady() {
        showStatus("离线听写已就绪");
    }

    @Override
    public void onStatusChanged(String status) {
        showStatus(status);
    }

    @Override
    public void onListeningChanged(boolean listening) {
        runOnUiThread(() -> {
            tvStart.setEnabled(!listening);
            tvStop.setEnabled(listening);
            if (listening) {
                tvStatus.setText("正在识别");
            }
        });
    }

    @Override
    public void onTextResult(String text) {
        runOnUiThread(() -> {
            etResult.append(text);
            etResult.setSelection(etResult.length());
        });
    }

    @Override
    public void onError(String message) {
        setListeningState(false, message);
    }

    private void setListeningState(boolean listening, String status) {
        runOnUiThread(() -> {
            tvStart.setEnabled(!listening);
            tvStop.setEnabled(listening);
            tvStatus.setText(status);
        });
    }

    private void showStatus(String status) {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                tvStatus.setText(status);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (iatSession != null) {
            iatSession.release();
            iatSession = null;
        }
        audioExecutor.shutdownNow();
        super.onDestroy();
    }
}
