package com.yxh.fangs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;

import java.util.List;

public class FrequencyListAdapter extends RecyclerView.Adapter<FrequencyListAdapter.ViewHolder> {

    private List<String> dataList;
    private OnItemClickListener listener;

    public FrequencyListAdapter(List<String> dataList) {
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
        TextView tvFrequency;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
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
        String frequency = dataList.get(position);
        holder.tvFrequency.setText(frequency);

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
