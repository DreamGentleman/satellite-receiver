package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yxh.fangs.R;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.util.SPUtils;

public class LayoutActivity extends BaseActivity {

    private TextView tvSure;
    private TextView tvBack;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;
    private CheckBox cb5;
    private CheckBox cb6;
    private CheckBox cb7;
    private ImageView ivIncreaseFrequency;
    private ImageView ivDecreaseFrequency;
    private int forecastHourRange;
    private TextView tvShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        initView();
        initData();
    }

    private void initData() {
        String drawContent = getIntent().getStringExtra("drawContent");
        if (TextUtils.isEmpty(drawContent)) {
            cb1.setChecked(false);
            cb2.setChecked(false);
            cb3.setChecked(false);
            cb4.setChecked(false);
            cb5.setChecked(false);
            cb6.setChecked(false);
            cb7.setChecked(false);
        } else {
            boolean showFishing = drawContent.contains("渔场");
            boolean showNoFishLine = drawContent.contains("机轮拖网渔业禁渔线");
            boolean showCoast = drawContent.contains("领海基线");
            boolean showCKFA = drawContent.contains("中韩渔业协定水域");
            boolean showCJFA = drawContent.contains("中日渔业协定水域");
            boolean showTyphoon = drawContent.contains("台风预警");
            boolean showRain = drawContent.contains("气象信息");
            cb1.setChecked(showFishing);
            cb2.setChecked(showNoFishLine);
            cb3.setChecked(showCoast);
            cb4.setChecked(showCKFA);
            cb5.setChecked(showCJFA);
            cb6.setChecked(showTyphoon);
            cb7.setChecked(showRain);
        }
        forecastHourRange = SPUtils.getInt(AppConstants.FORECASTHOURRANGE, 12);
        tvShow.setText(forecastHourRange + "小时");
    }

    private void initView() {
        tvSure = findViewById(R.id.tv_sure);
        tvBack = findViewById(R.id.tv_back);
        cb1 = findViewById(R.id.cb_1);
        cb2 = findViewById(R.id.cb_2);
        cb3 = findViewById(R.id.cb_3);
        cb4 = findViewById(R.id.cb_4);
        cb5 = findViewById(R.id.cb_5);
        cb6 = findViewById(R.id.cb_6);
        cb7 = findViewById(R.id.cb_7);
        ivIncreaseFrequency = findViewById(R.id.iv_increase_frequency);
        ivDecreaseFrequency = findViewById(R.id.iv_decrease_frequency);
        tvShow = findViewById(R.id.tv_show);

        ivIncreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forecastHourRange++;
                SPUtils.putInt(AppConstants.FORECASTHOURRANGE, forecastHourRange);
                tvShow.setText(forecastHourRange + "小时");
            }
        });
        ivDecreaseFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forecastHourRange > 1) {
                    forecastHourRange--;
                    SPUtils.putInt(AppConstants.FORECASTHOURRANGE, forecastHourRange);
                    tvShow.setText(forecastHourRange + "小时");
                }
            }
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                String value = handleSosSelected();
                data.putExtra("handleSosSelected", value);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    private String handleSosSelected() {
        StringBuilder selected = new StringBuilder();
        appendCheckedText(selected, cb1);
        appendCheckedText(selected, cb2);
        appendCheckedText(selected, cb3);
        appendCheckedText(selected, cb4);
        appendCheckedText(selected, cb5);
        appendCheckedText(selected, cb6);
        appendCheckedText(selected, cb7);
        return selected.toString();
    }

    private void appendCheckedText(StringBuilder selected, CheckBox checkBox) {
        if (checkBox.isChecked()) {
            selected.append(',').append(checkBox.getText());
        }
    }
}
