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
import com.yxh.fangs.bean.FrequencyChannel;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.ui.main.BaseActivity;
import com.yxh.fangs.util.SPUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingActivity extends BaseActivity {

    private static final int OFFSET_STEP_HZ = 10;
    private static final int MIN_OFFSET_HZ = -2000;
    private static final int MAX_OFFSET_HZ = 2000;
    private static final double[] DEFAULT_FREQUENCIES_MHZ = {
            4.2075, 4.2100, 6.3120, 6.3140, 8.4145,
            8.4165, 12.5770, 12.5790, 16.8045, 16.8065
    };

    private RecyclerView rv;
    private TextView tvBack;
    private TextView tvOpenRadio;
    private TextView tvCloseRadio;
    private int frequencyOffsetHz;
    private int selectedPosition;
    private List<FrequencyChannel> channels;
    private FrequencyListAdapter adapter;
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
        channels = loadChannels();
        selectedPosition = Math.min(
                Math.max(SPUtils.getInt(AppConstants.CURRENT_FREQUENCY_CHANNEL, 0), 0),
                channels.size() - 1
        );
        frequencyOffsetHz = channels.get(selectedPosition).getOffsetHz();
        adapter = new FrequencyListAdapter(channels, selectedPosition);
        rv.setAdapter(adapter);
        updateFrequencyOffsetText();
        adapter.setOnItemClickListener(position -> selectChannel(position, true));
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
                changeFrequencyOffset(OFFSET_STEP_HZ);
            }
        });
        ivDecreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrequencyOffset(-OFFSET_STEP_HZ);
            }
        });
        tvFrequencySetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentFrequencyOffset();
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

    private ArrayList<FrequencyChannel> loadChannels() {
        ArrayList<FrequencyChannel> result = new ArrayList<>();
        for (int i = 0; i < DEFAULT_FREQUENCIES_MHZ.length; i++) {
            int defaultOffset = i == 0 ? 180 : 0;
            int offset = SPUtils.getInt(offsetKey(i), defaultOffset);
            result.add(new FrequencyChannel(i + 1, DEFAULT_FREQUENCIES_MHZ[i], offset));
        }
        return result;
    }

    private void selectChannel(int position, boolean showToast) {
        if (position < 0 || position >= channels.size()) {
            return;
        }
        selectedPosition = position;
        FrequencyChannel channel = channels.get(position);
        frequencyOffsetHz = channel.getOffsetHz();
        SPUtils.putInt(AppConstants.CURRENT_FREQUENCY_CHANNEL, position);
        adapter.setSelectedPosition(position);
        updateFrequencyOffsetText();
        if (showToast) {
            Toast.makeText(
                    this,
                    String.format(Locale.US, "已切换到信道%02d：%.4fMHz", channel.getChannelNo(), channel.getFrequencyMhz()),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void changeFrequencyOffset(int deltaHz) {
        frequencyOffsetHz = Math.max(MIN_OFFSET_HZ, Math.min(MAX_OFFSET_HZ, frequencyOffsetHz + deltaHz));
        updateFrequencyOffsetText();
    }

    private void saveCurrentFrequencyOffset() {
        FrequencyChannel channel = channels.get(selectedPosition);
        channel.setOffsetHz(frequencyOffsetHz);
        SPUtils.putInt(offsetKey(selectedPosition), frequencyOffsetHz);
        SPUtils.putInt(AppConstants.CURRENT_FREQUENCY_CHANNEL, selectedPosition);
        adapter.notifyItemChanged(selectedPosition);
        Toast.makeText(
                this,
                String.format(Locale.US, "信道%02d频偏已设置为%s", channel.getChannelNo(), formatOffset(frequencyOffsetHz)),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateFrequencyOffsetText() {
        tvFrequencyOffset.setText(formatOffset(frequencyOffsetHz));
    }

    private String formatOffset(int offsetHz) {
        String sign = offsetHz > 0 ? "+" : "";
        return sign + offsetHz + "Hz";
    }

    private String offsetKey(int position) {
        return AppConstants.FREQUENCY_OFFSET_PREFIX + position;
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
