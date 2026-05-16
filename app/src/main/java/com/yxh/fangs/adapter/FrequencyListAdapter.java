package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.FrequencyChannel;

import java.util.List;
import java.util.Locale;

public class FrequencyListAdapter extends RecyclerView.Adapter<FrequencyListAdapter.ViewHolder> {

    private final List<FrequencyChannel> dataList;
    private OnItemClickListener listener;
    private int selectedPosition;

    public FrequencyListAdapter(List<FrequencyChannel> dataList, int selectedPosition) {
        this.dataList = dataList;
        this.selectedPosition = selectedPosition;
    }

    // 设置点击事件
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int selectedPosition) {
        int oldPosition = this.selectedPosition;
        this.selectedPosition = selectedPosition;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChannel;
        TextView tvFrequency;
        TextView tvOffset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChannel = itemView.findViewById(R.id.tv_channel);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvOffset = itemView.findViewById(R.id.tv_offset);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_frequency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FrequencyChannel channel = dataList.get(position);
        holder.tvChannel.setText(formatChannelName(channel));
        holder.tvFrequency.setText(formatFrequency(channel));
        holder.tvOffset.setText(formatOffset(channel.getOffsetHz()));
        holder.itemView.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private String formatChannelName(FrequencyChannel channel) {
        return String.format(Locale.US, "信道%02d", channel.getChannelNo());
    }

    private String formatFrequency(FrequencyChannel channel) {
        return String.format(Locale.US, "%.4fMHz", channel.getFrequencyMhz());
    }

    private String formatOffset(int offsetHz) {
        String sign = offsetHz > 0 ? "+" : "";
        return String.format(Locale.US, "频偏 %s%dHz", sign, offsetHz);
    }
}
