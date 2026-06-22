package com.yxh.fangs.ui.main;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yxh.fangs.R;
import com.yxh.fangs.adapter.NoticeListAdapter;
import com.yxh.fangs.bean.Last24HoursBean;

import java.util.List;

public class MainUiBinder {

    private final Activity activity;

    private final TextView tvScrollingMessage;
    private final TextView tvValidityPeriod;
    private final SmartRefreshLayout refreshLayout;
    private final View btnHistorical;
    private final View btnSetting;
    private final View btnSpeechDictation;
    private final View btnWeather;
    private final View btnSos;
    private final View btnLayout;
    private final View btnLocation;
    private final TextView ivVolume;
    private final RecyclerView rvNotice;
    private final NoticeListAdapter noticeListAdapter;

    public MainUiBinder(Activity activity) {
        this.activity = activity;

        tvValidityPeriod = activity.findViewById(R.id.tv_validity_period);
        tvScrollingMessage = activity.findViewById(R.id.tv_scrolling_message);
        refreshLayout = activity.findViewById(R.id.refreshLayout);
        rvNotice = activity.findViewById(R.id.rv_notice);

        btnHistorical = activity.findViewById(R.id.tv_historical_data);
        btnSetting = activity.findViewById(R.id.tv_parameter_settings);
        btnSpeechDictation = activity.findViewById(R.id.tv_speech_dictation);
        btnWeather = activity.findViewById(R.id.tv_weather);
        btnSos = activity.findViewById(R.id.iv_sos);
        btnLayout = activity.findViewById(R.id.iv_layout);
        btnLocation = activity.findViewById(R.id.iv_location);
        ivVolume = activity.findViewById(R.id.iv_volume);

        tvScrollingMessage.setSelected(true);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(false);
        rvNotice.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        rvNotice.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(15, 7, 10, 0);
            }
        });
        noticeListAdapter = new NoticeListAdapter(null);
        rvNotice.setAdapter(noticeListAdapter);
    }

    public void setLicensePeriodText(String text) {
        tvValidityPeriod.setText(text);
    }

    public void setScrollingText(String text) {
        tvScrollingMessage.setText(text);
    }

    public void finishRefreshIfNeeded() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.finishRefresh();
        }
    }

    public void bindRefresh(Runnable onRefresh) {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                onRefresh.run();
            }
        });
    }

    public void bindNoticeClickListener(NoticeClickRouter router) {
        noticeListAdapter.setOnItemClickListener(position -> {
            Last24HoursBean.RowsBean rowsBean = noticeListAdapter.getDataList().get(position);
            try {
                router.onNoticeClicked(rowsBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateNoticeList(List<Last24HoursBean.RowsBean> rows) {
        noticeListAdapter.setDataList(rows);
    }

    public void bindCommonClicks(Runnable openHistory, Runnable openSetting, Runnable openSpeechDictation, Runnable openWeather, Runnable openSos, Runnable openLayout, Runnable locate, VolumeToggle toggle) {
        btnHistorical.setOnClickListener(v -> openHistory.run());
        btnSetting.setOnClickListener(v -> openSetting.run());
        btnSpeechDictation.setOnClickListener(v -> openSpeechDictation.run());
        btnWeather.setOnClickListener(v -> openWeather.run());
        btnSos.setOnClickListener(v -> openSos.run());
        btnLayout.setOnClickListener(v -> openLayout.run());
        btnLocation.setOnClickListener(v -> locate.run());

        ivVolume.setSelected(true);
        ivVolume.setOnClickListener(v -> {
            boolean nowSelected = !v.isSelected();
            v.setSelected(nowSelected);

            if (nowSelected) {
                Toast.makeText(activity, "语音通知已开启！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "语音通知已关闭！", Toast.LENGTH_SHORT).show();
            }
            toggle.onToggle(nowSelected);
        });
    }

    public interface VolumeToggle {
        void onToggle(boolean enabled);
    }
}

