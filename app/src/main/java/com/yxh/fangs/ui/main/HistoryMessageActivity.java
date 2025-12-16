package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yxh.fangs.R;
import com.yxh.fangs.adapter.MessageListAdapter;
import com.yxh.fangs.bean.BeidouBean;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.Message;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.TyphoonBean;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.room.MessageDatabase;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.dialog.WeatherDialog;
import com.yxh.fangs.util.HttpUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.UrlUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class HistoryMessageActivity extends BaseActivity {

    private RecyclerView rv;
    private int messageType = Message.NOTICE_SMS;
    private MessageDatabase db;
    private MessageListAdapter adapter;
    private Button btnShortMsg;
    private Button btnGovMsg;
    private Button btnTyphoon;
    private Button btnBeidou;
    private Button btnImage;
    private TextView tvBack;
    private String selectedType = NoticeType.NOTICE_WEATHER;
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<Last24HoursBean.RowsBean> rows;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_message);
        initViews();
        initRecyclerView();
        initData();
//        initDb();
//        loadMessagesByType(messageType);

    }

    private void initData() {
        HttpUtils.get(UrlUtils.history(1, 1000), new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                Last24HoursBean last24HoursBean = gson.fromJson(body, Last24HoursBean.class);
                if (last24HoursBean.getCode() == 200) {
                    rows = last24HoursBean.getRows();
                    adapter.setDataList(paseData(last24HoursBean.getRows()));
                }
            }

            @Override
            public void onError(String msg) {
                // 错误处理
            }
        });
    }

    private List<Last24HoursBean.RowsBean> paseData(List<Last24HoursBean.RowsBean> rows) {
        ArrayList<Last24HoursBean.RowsBean> newList = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            if (selectedType.equals(rows.get(i).getMessageType())) {
                newList.add(rows.get(i));
            }
        }
        return newList;
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new MessageListAdapter();
        adapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Last24HoursBean.RowsBean rowsBean = adapter.getDataList().get(position);
                switch (rowsBean.getMessageType()) {
                    case NoticeType.NOTICE_BEIDOU: {
                        Gson gson = new Gson();
                        BeidouBean beidouBean = gson.fromJson(rowsBean.getContent(), BeidouBean.class);
                        String content = "北斗通道号为：" + beidouBean.getBeidouChannel() + "，卫星编号为：" + beidouBean.getSatelliteId() + "，信号强度：" + beidouBean.getSignalStrength();
                        MessageDialog dialog = MessageDialog.newInstance(HistoryMessageActivity.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                        dialog.show();
                    }
                    break;
                    case NoticeType.NOTICE_ALERT: {
                        Gson gson = new Gson();
                        WarnBean warnBean = gson.fromJson(rowsBean.getContent(), WarnBean.class);
                        String content = warnBean.getWarningLevel();
                        MessageDialog dialog = MessageDialog.newInstance(HistoryMessageActivity.this, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                        dialog.show();
                    }
                    break;
                    case NoticeType.NOTICE_NOTICE_IMAGE: {
                        Gson gson = new Gson();
                        ImageBean imageBean = gson.fromJson(rowsBean.getContent(), ImageBean.class);
                        ImageCache.base64 = imageBean.getBase64();
                        Intent intent = new Intent(HistoryMessageActivity.this, ImageDetailActivity.class);
                        intent.putExtra("time", rowsBean.getPublishTime());
                        startActivity(intent);
                    }
                    break;
                    case NoticeType.NOTICE_SMS: {
                        MessageDialog dialog = MessageDialog.newInstance(HistoryMessageActivity.this, rowsBean.getTitle(), rowsBean.getContent(), rowsBean.getPublishTime());
                        dialog.show();
                    }
                    break;
                    case NoticeType.NOTICE_TYPHOON: {
                        Gson gson = new Gson();
                        TyphoonBean typhoonBean = gson.fromJson(rowsBean.getContent(), TyphoonBean.class);
                        MessageDialog dialog = MessageDialog.newInstance(HistoryMessageActivity.this, rowsBean.getTitle(), typhoonBean.getMovingDirection(), rowsBean.getTitle());
                        dialog.show();
                    }
                    break;
                    case NoticeType.NOTICE_WEATHER: {
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<WarnBean>>() {
                        }.getType();
                        List<WeatherBean> list = gson.fromJson(rowsBean.getContent(), type);
                        WeatherDialog dialog = WeatherDialog.newInstance(HistoryMessageActivity.this, rowsBean.getTitle());
                        dialog.show();
                    }
                    break;
                }

            }
        });
        rv.setAdapter(adapter);
    }

    private void initViews() {
        btnShortMsg = findViewById(R.id.btn_short_message);
        btnGovMsg = findViewById(R.id.btn_government_affairs_message);
        btnTyphoon = findViewById(R.id.btn_typhoon);
        btnBeidou = findViewById(R.id.btn_beidou_message);
        btnImage = findViewById(R.id.btn_image);
        tvBack = findViewById(R.id.tv_back);
        btnGovMsg.setSelected(true);
        rv = findViewById(R.id.rv_message);

        // 点击事件
        btnShortMsg.setOnClickListener(v -> {
            setSelected(btnShortMsg);
            selectedType= NoticeType.NOTICE_SMS;
            loadMessagesByType(Message.NOTICE_SMS);   // 4
            adapter.setDataList(paseData(rows));
        });

        btnGovMsg.setOnClickListener(v -> {
            selectedType= NoticeType.NOTICE_WEATHER;
            setSelected(btnGovMsg);
            loadMessagesByType(Message.NOTICE_WEATHER); // 6
            adapter.setDataList(paseData(rows));
        });

        btnTyphoon.setOnClickListener(v -> {
            selectedType= NoticeType.NOTICE_TYPHOON;
            setSelected(btnTyphoon);
            loadMessagesByType(Message.NOTICE_TYPHOON);  // 5
            adapter.setDataList(paseData(rows));
        });

        btnBeidou.setOnClickListener(v -> {
            selectedType= NoticeType.NOTICE_BEIDOU;
            setSelected(btnBeidou);
            loadMessagesByType(Message.NOTICE_BEIDOU);   // 1
            adapter.setDataList(paseData(rows));
        });

        btnImage.setOnClickListener(v -> {
            selectedType= NoticeType.NOTICE_NOTICE_IMAGE;
            setSelected(btnImage);
            loadMessagesByType(Message.NOTICE_NOTICE_IMAGE); // 3
            adapter.setDataList(paseData(rows));
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setSelected(Button selectedBtn) {
        Button[] allBtns = {btnShortMsg, btnGovMsg, btnTyphoon, btnBeidou, btnImage};

        for (Button btn : allBtns) {
            if (btn == selectedBtn) {
                btn.setSelected(true);
            } else {
                btn.setSelected(false);
            }
        }
    }

    /**
     * 通过 RxJava 查询数据库
     */
    private void loadMessagesByType(int type) {
//        disposable.add(db.messageDao().queryRxAllByType(type).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(list -> {
//            adapter.setDataList(list);
//            adapter.notifyDataSetChanged();
//        }, throwable -> {
//            Log.e("RX", "查询失败：" + throwable.getMessage());
//        }));
    }

    private void initDb() {
        db = MessageDatabase.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
