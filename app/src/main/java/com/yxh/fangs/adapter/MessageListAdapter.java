package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.MessageResponse.MessageItem;
import com.yxh.fangs.bean.MessageResponse;
import com.yxh.fangs.bean.NoticeBean;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<MessageResponse.MessageItem> dataList;
    private OnItemClickListener listener;

    public MessageListAdapter(List<MessageResponse.MessageItem> dataList) {
        this.dataList = dataList;
    }

    public List<MessageItem> getDataList() {
        if (dataList == null) {
            return new ArrayList<>();
        }
        return dataList;
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
        TextView tvTime;
        TextView tvMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }
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
        MessageResponse.MessageItem noticeBean = dataList.get(position);
        holder.tvTime.setText(noticeBean.getSendTime());
        holder.tvMessage.setText(noticeBean.getContent());

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
