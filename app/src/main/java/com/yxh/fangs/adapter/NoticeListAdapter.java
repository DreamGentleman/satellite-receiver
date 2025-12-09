package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.NoticeBean;

import java.util.List;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    private List<NoticeBean> dataList;
    private OnItemClickListener listener;

    public NoticeListAdapter(List<NoticeBean> dataList) {
        this.dataList = dataList;
    }

    // 设置点击事件
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotice;
        TextView tvNotice;
        TextView tvNoticeTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotice = itemView.findViewById(R.id.iv_notice);
            tvNotice = itemView.findViewById(R.id.tv_notice);
            tvNoticeTime = itemView.findViewById(R.id.tv_notice_time);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoticeBean noticeBean = dataList.get(position);
        int imgResourceId = R.mipmap.ic_notice_sms;
        switch (noticeBean.getNoticeType()) {
            case NoticeBean.NOTICE_BEIDOU:
                imgResourceId = R.mipmap.ic_notice_beidou;
                break;
            case NoticeBean.NOTICE_ALERT:
                imgResourceId = R.mipmap.ic_notice_alert;
                break;
            case NoticeBean.NOTICE_NOTICE_IMAGE:
                imgResourceId = R.mipmap.ic_notice_image;
                break;
            case NoticeBean.NOTICE_SMS:
                imgResourceId = R.mipmap.ic_notice_sms;
                break;
            case NoticeBean.NOTICE_TYPHOON:
                imgResourceId = R.mipmap.ic_notice_typhoon;
                break;
            case NoticeBean.NOTICE_WEATHER:
                imgResourceId = R.mipmap.ic_notice_weather;
                break;
        }
        holder.ivNotice.setImageResource(imgResourceId);
        holder.tvNotice.setText(noticeBean.getNoticeTitle());
        holder.tvNoticeTime.setText(noticeBean.getNoticeTime());

        // 配置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
