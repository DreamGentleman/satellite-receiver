package com.yxh.fangs.ui.main;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.adapter.MessageListAdapter;
import com.yxh.fangs.application.MyApplication;
import com.yxh.fangs.bean.MessageBean;
import com.yxh.fangs.bean.MessageResponse;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryMessageActivity extends BaseActivity implements TextToSpeech.OnInitListener {

    private RecyclerView rv;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_message);
        rv = findViewById(R.id.rv_message);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        ArrayList<MessageBean> list = new ArrayList<>();
//        list.add(new MessageBean("接收时间：2024年7月26日   14时47分", "我写信来告诉你一个好消息!我最近找到了一份新工作，并且我非常开心。这个工作给了我许多新机会和挑战，我期待着能够在这里展示我的才能和能力。在这个新的工作岗位上，我将会有更多的时间和精力来和你见面，我非常期待我们能够一起度过美好的时光。"));
//        list.add(new MessageBean("接收时间：2024年7月26日   14时47分", "我写信来告诉你一个好消息!我最近找到了一份新工作，并且我非常开心。"));
//        list.add(new MessageBean("接收时间：2024年7月26日   14时47分", "我写信来告诉你一个好消息!我最近找到了一份新工作，并且我非常开心。"));
        MessageListAdapter adapter = new MessageListAdapter(MyApplication.getInstance().getData());
        adapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                MessageResponse.MessageItem messageItem = adapter.getDataList().get(position);
                speak(messageItem.getContent());

            }
        });
        rv.setAdapter(adapter);
        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Toast.makeText(this, "语音引擎初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // ① 尝试多种中文 Locale，兼容不同厂商实现
        Locale[] candidates = new Locale[]{
                Locale.SIMPLIFIED_CHINESE,   // 推荐
                Locale.CHINA,                // zh_CN
                Locale.CHINESE,              // zh
                new Locale("zh", "CN")       // 再手写一遍
        };

        int result = TextToSpeech.LANG_NOT_SUPPORTED;

        for (Locale loc : candidates) {
            result = tts.setLanguage(loc);
            Log.d("tts", "try locale: " + loc + " result = " + result);
            if (result == TextToSpeech.LANG_AVAILABLE
                    || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                    || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                break; // 找到一个认为“支持”的就结束循环
            }
        }

        // ② 对于大陆很多机型，这里的返回值经常不靠谱
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // ❗不要直接跟用户说“不支持中文”
            // 很多手机会返回这两个值但实际能读中文
            Log.w("tts", "setLanguage 返回不支持中文，先尝试直接播报测试语音");
            // 如果你实在想提示，可以改成引导用户检查系统设置，不要一口咬死“不支持”
            // Toast.makeText(this, "当前设备语音引擎可能未启用中文，请在系统设置中检查文本转语音功能", Toast.LENGTH_LONG).show();
        }

        // ③ 用耳朵做最终判断：直接播一段测试语音
        tts.speak("语音播报功能已经准备就绪",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "init_test");

        isTtsReady = true;
    }

    private void speak(String text) {
        if (tts == null || !isTtsReady) {
            Toast.makeText(HistoryMessageActivity.this, "语音转换异常！", Toast.LENGTH_LONG).show();
            return;
        }
        tts.speak(text + "。",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "utteranceId_" + System.currentTimeMillis());
    }
}
