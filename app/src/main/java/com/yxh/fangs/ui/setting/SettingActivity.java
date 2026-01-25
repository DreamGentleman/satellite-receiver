package com.yxh.fangs.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yxh.fangs.R;
import com.yxh.fangs.adapter.FrequencyListAdapter;
import com.yxh.fangs.ui.main.BaseActivity;

import java.util.ArrayList;

public class SettingActivity extends BaseActivity {

    private RecyclerView rv;
    private TextView tvBack;
    private TextView tvOpenRadio;
    private TextView tvCloseRadio;
    private int frequency = 180;
    private ImageView ivIncreaseFrequency;
    private ImageView ivDecreaseFrequency;
    private TextView tvFrequencyOffset;
    private TextView tvFrequencySetting;
    private TextView tvFrequencyLockSDetting;
    private TextView tvTurnOnBluetooth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        tvBack = findViewById(R.id.tv_back);
        rv = findViewById(R.id.rv_frequency);
        tvOpenRadio = findViewById(R.id.tv_open_radio);
        tvCloseRadio = findViewById(R.id.tv_close_radio);
        ivIncreaseFrequency = findViewById(R.id.iv_increase_frequency);
        ivDecreaseFrequency = findViewById(R.id.iv_decrease_frequency);
        tvFrequencyOffset = findViewById(R.id.tv_frequency_offset);
        tvFrequencySetting = findViewById(R.id.tv_frequency_setting);
        tvFrequencyLockSDetting = findViewById(R.id.tv_frequency_lock_setting);
        tvTurnOnBluetooth = findViewById(R.id.tv_turn_on_bluetooth);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("信道" + (i + 1) + "：10.0000MHz");
        }
        FrequencyListAdapter adapter = new FrequencyListAdapter(list);
        rv.setAdapter(adapter);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvOpenRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableFMPower();
            }
        });
        tvCloseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableFMPower();
            }
        });
        ivIncreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frequency++;
                tvFrequencyOffset.setText(frequency + "HZ");
            }
        });
        ivDecreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frequency > 0) {
                    frequency--;
                    tvFrequencyOffset.setText(frequency + "HZ");
                }
            }
        });
        tvFrequencySetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, "频偏设置成功！", Toast.LENGTH_SHORT).show();
            }
        });
        tvFrequencyLockSDetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, "时间设置成功！", Toast.LENGTH_SHORT).show();
            }
        });
        tvTurnOnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }

    public void enableFMPower() {
        String cmd = "echo 1 > /sys/class/usb_hub_power/dev/vcc_en1 " + "\n";
        try {
            Process exeEcho = Runtime.getRuntime().exec("sh");
            exeEcho.getOutputStream().write(cmd.getBytes());
            exeEcho.getOutputStream().flush();
            Toast.makeText(this, "无线电已开启！", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "无线电开启失败，错误信息是" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void disableFMPower() {
        String cmd = "echo 0 > /sys/class/usb_hub_power/dev/vcc_en1 " + "\n";
        try {
            Process exeEcho = Runtime.getRuntime().exec("sh");
            exeEcho.getOutputStream().write(cmd.getBytes());
            exeEcho.getOutputStream().flush();
            Toast.makeText(this, "无线电已关闭！", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "无线电关闭失败，错误信息是" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
