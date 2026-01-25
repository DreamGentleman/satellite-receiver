package com.yxh.fangs.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.yxh.fangs.data.local.db.MessageDatabase;
import com.yxh.fangs.data.network.HttpUtils;
import com.yxh.fangs.data.network.api.UrlUtils;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.dialog.WeatherDialog;
import com.yxh.fangs.ui.image.ImageDetailActivity;
import com.yxh.fangs.ui.main.BaseActivity;
import com.yxh.fangs.util.LogUtils;

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
        if (rows == null || rows.isEmpty()) {
            return newList;
        }
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
                        Type type2 = new TypeToken<List<WeatherBean>>() {
                        }.getType();
                        List<WeatherBean> list2 = new Gson().fromJson(rowsBean.getContent(), type2);

                        if (list2 != null && !list2.isEmpty()) {
                            WeatherBean weatherBean = list2.get(0);
                            String message = buildForecastText(weatherBean);
//                            speak(message);
                            WeatherDialog dialog = WeatherDialog.newInstance(HistoryMessageActivity.this, message, getWeather(weatherBean.getWeatherPhenomenon()));
                            dialog.show();
                        }
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
            selectedType = NoticeType.NOTICE_SMS;
            loadMessagesByType(Message.NOTICE_SMS);   // 4
            adapter.setDataList(paseData(rows));
        });

        btnGovMsg.setOnClickListener(v -> {
            selectedType = NoticeType.NOTICE_WEATHER;
            setSelected(btnGovMsg);
            loadMessagesByType(Message.NOTICE_WEATHER); // 6
            adapter.setDataList(paseData(rows));
        });

        btnTyphoon.setOnClickListener(v -> {
            selectedType = NoticeType.NOTICE_TYPHOON;
            setSelected(btnTyphoon);
            loadMessagesByType(Message.NOTICE_TYPHOON);  // 5
            adapter.setDataList(paseData(rows));
        });

        btnBeidou.setOnClickListener(v -> {
            selectedType = NoticeType.NOTICE_BEIDOU;
            setSelected(btnBeidou);
            loadMessagesByType(Message.NOTICE_BEIDOU);   // 1
            adapter.setDataList(paseData(rows));
        });

        btnImage.setOnClickListener(v -> {
            selectedType = NoticeType.NOTICE_NOTICE_IMAGE;
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

    public String buildForecastText(WeatherBean bean) {
        if (bean == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 1. 预报时效
        sb.append("未来（24）小时（")
                .append(bean.getSeaArea())
                .append("）区域，");

        // 2. 经纬度范围
        String[] lonArr = bean.getLongitudeRange().split("-");
        String[] latArr = bean.getLatitudeRange().split("-");

        sb.append("北纬")
                .append(toDms(latArr[0]))
                .append("，东经")
                .append(toDms(lonArr[0]))
                .append("，到北纬")
                .append(toDms(latArr[1]))
                .append("，东经")
                .append(toDms(lonArr[1]))
                .append("，");

        // 3. 天气要素
        sb.append("预计有")
                .append(bean.getWeatherPhenomenon())
                .append(bean.getWindDirection())
                .append(bean.getWindForce())
                .append("级")
                .append("浪高")
                .append(bean.getWaveHeight())
                .append("米")
                .append("能见度")
                .append(bean.getVisibility())
                .append("千米")
                .append(TextUtils.isEmpty(bean.getRemark()) ? "。" : ("," + bean.getRemark()));

        // 4. 备注（有才输出）
        if (bean.getRemark() != null && !bean.getRemark().trim().isEmpty()) {
            sb.append("（备注：")
                    .append(bean.getRemark())
                    .append("）");
        }

        return sb.toString();
    }

    /**
     * 00-24 -> 24
     */
    private String parseForecastHour(String period) {
        if (period == null || !period.contains("-")) {
            return "";
        }
        String[] arr = period.split("-");
        return arr[1];
    }

    /**
     * 十进制度 -> 度分秒
     * 37.1206 -> 37度07分14秒
     */
    private String toDms(String value) {
        double d = Double.parseDouble(value);

        int degree = (int) d;
        double m1 = (d - degree) * 60;
        int minute = (int) m1;
        int second = (int) ((m1 - minute) * 60);

        return degree + "度" + minute + "分" + second + "秒";
    }

    private int getWeather(String weatherPhenomenon) {
        switch (weatherPhenomenon) {
            case "晴":
                return R.mipmap.ic_fine;
            case "多云":
                return R.mipmap.ic_cloudy;
            case "阴天":
                return R.mipmap.ic_cloudy_sky;
            case "雷阵雨":
                return R.mipmap.ic_thunder_shower;
            case "雷阵雨伴冰雹":
                return R.mipmap.ic_thunderstorms_with_hail;
            case "雨夹雪":
                return R.mipmap.ic_sleet;
            case "小雨":
                return R.mipmap.ic_sprinkle;
            case "中雨":
                return R.mipmap.ic_moderate_rain;
            case "大雨":
                return R.mipmap.ic_heavy_rain;
            case "暴雨":
                return R.mipmap.ic_torrential_rain;
            case "大暴雨":
                return R.mipmap.ic_downpour;
            case "特大暴雨":
                return R.mipmap.ic_heavy_downpour;
            case "小雪":
                return R.mipmap.ic_scouther;
            case "中雪":
                return R.mipmap.ic_moderate_snow;
            case "大雪":
                return R.mipmap.ic_heavy_snow;
            case "暴雪":
                return R.mipmap.ic_blizzard;
            case "雾":
                return R.mipmap.ic_fog;
            case "冻雨":
                return R.mipmap.ic_ice_rain;
            case "沙尘暴":
                return R.mipmap.ic_sand_storm;
            case "扬沙或浮尘":
                return R.mipmap.ic_sand_or_dust;
            case "强沙尘暴":
                return R.mipmap.ic_strong_sandstorm;
            case "霾":
                return R.mipmap.ic_haze;
        }
        return R.mipmap.ic_fine;
    }
}
