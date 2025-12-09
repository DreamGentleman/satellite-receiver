package com.yxh.fangs.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.adapter.FrequencyListAdapter;

import java.util.ArrayList;

public class SettingActivity extends BaseActivity {

    private RecyclerView rv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        rv = findViewById(R.id.rv_frequency);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("信道" + (i + 1) + "：10.0000MHz");
        }
        FrequencyListAdapter adapter = new FrequencyListAdapter(list);
        rv.setAdapter(adapter);
    }
}
