package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;

import java.util.ArrayList;
import java.util.List;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    private List<Last24HoursBean.RowsBean> dataList;
    private OnItemClickListener listener;

    public NoticeListAdapter(List<Last24HoursBean.RowsBean> dataList) {
        this.dataList = dataList;
    }

    public List<Last24HoursBean.RowsBean> getDataList() {
        if (dataList == null) {
            return new ArrayList<>();
        }
        return dataList;
    }

    public void setDataList(List<Last24HoursBean.RowsBean> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
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
        Last24HoursBean.RowsBean noticeBean = dataList.get(position);
        int imgResourceId = R.mipmap.ic_notice_sms;
        String NoticeTypeName = "";
        switch (noticeBean.getMessageType()) {
            case NoticeType.NOTICE_BEIDOU:
                imgResourceId = R.mipmap.ic_notice_beidou;
                NoticeTypeName = "[北斗通报信息]";
                break;
            case NoticeType.NOTICE_ALERT:
                imgResourceId = R.mipmap.ic_notice_alert;
                NoticeTypeName = "[预警信息]";
                break;
            case NoticeType.NOTICE_NOTICE_IMAGE:
                imgResourceId = R.mipmap.ic_notice_image;
                NoticeTypeName = "[图片信息]";
                break;
            case NoticeType.NOTICE_SMS:
                imgResourceId = R.mipmap.ic_notice_sms;
                NoticeTypeName = "[短信息]";
                break;
            case NoticeType.NOTICE_TYPHOON:
                imgResourceId = R.mipmap.ic_notice_typhoon;
                NoticeTypeName = "[台风信息]";
                break;
            case NoticeType.NOTICE_WEATHER:
                imgResourceId = R.mipmap.ic_notice_weather;
                NoticeTypeName = "[气象消息]";
                break;
        }
        holder.ivNotice.setImageResource(imgResourceId);
        holder.tvNotice.setText(NoticeTypeName + noticeBean.getTitle());
        holder.tvNoticeTime.setText(noticeBean.getPublishTime());

        // 配置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }
}
