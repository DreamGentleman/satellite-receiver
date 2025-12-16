package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<Last24HoursBean.RowsBean> dataList;
    private OnItemClickListener listener;

    public MessageListAdapter(List<Last24HoursBean.RowsBean> dataList) {
        this.dataList = dataList;
    }

    public MessageListAdapter() {
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Last24HoursBean.RowsBean noticeBean = dataList.get(position);
        holder.tvTime.setText(noticeBean.getPublishTime());
        holder.tvMessage.setText(noticeBean.getContent());

        // 配置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });


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
        holder.tvMessage.setText(NoticeTypeName + noticeBean.getTitle());
        holder.tvTime.setText(noticeBean.getPublishTime());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    // 设置点击事件
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }
    }
}
