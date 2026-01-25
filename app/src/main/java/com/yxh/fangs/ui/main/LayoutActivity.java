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
import com.yxh.fangs.application.MyApplication;
import com.yxh.fangs.config.AppConstants;
import com.yxh.fangs.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 所有 CheckBox 统一管理
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();
    private String selectLayout = "";
    private ImageView ivIncreaseFrequency;
    private ImageView ivDecreaseFrequency;
    private int forecastHourRange;
    private TextView tvShow;
    ;

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

        // 初始化并加入列表
//        addCheckBox(R.id.cb_1);
//        addCheckBox(R.id.cb_2);
//        addCheckBox(R.id.cb_3);
//        addCheckBox(R.id.cb_4);
//        addCheckBox(R.id.cb_5);
//        addCheckBox(R.id.cb_6);
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
                MyApplication.getInstance().showLayoutText = value;
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }


    /**
     * 统一初始化 CheckBox
     */
    private void addCheckBox(int id) {
        CheckBox checkBox = findViewById(id);
        checkBox.setOnClickListener(this::onCheckBoxClick);
        checkBoxList.add(checkBox);
    }

    /**
     * 核心逻辑：
     * 用户点击哪个，就只选中哪个，其余全部取消
     */
    private void onCheckBoxClick(View view) {
        CheckBox clicked = (CheckBox) view;

        // 1️⃣ 全部取消（不会触发点击事件）
        setNotCheckAll();

        // 2️⃣ 再选中当前
        clicked.setChecked(true);
    }

    /**
     * 取消全部选中
     */
    private void setNotCheckAll() {
        for (CheckBox cb : checkBoxList) {
            cb.setChecked(false);
        }
    }

    /**
     * 统一处理选中后的业务
     */
    private String handleSosSelected() {
        selectLayout = "";
        if (cb1.isChecked()) {
            selectLayout = selectLayout + "," + cb1.getText().toString();
        }
        if (cb2.isChecked()) {
            selectLayout = selectLayout + "," + cb2.getText().toString();
        }
        if (cb3.isChecked()) {
            selectLayout = selectLayout + "," + cb3.getText().toString();
        }
        if (cb4.isChecked()) {
            selectLayout = selectLayout + "," + cb4.getText().toString();
        }
        if (cb5.isChecked()) {
            selectLayout = selectLayout + "," + cb5.getText().toString();
        }
        if (cb6.isChecked()) {
            selectLayout = selectLayout + "," + cb6.getText().toString();
        }
        if (cb7.isChecked()) {
            selectLayout = selectLayout + "," + cb7.getText().toString();
        }
        return selectLayout;
    }
}
