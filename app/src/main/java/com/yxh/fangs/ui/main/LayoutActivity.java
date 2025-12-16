package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yxh.fangs.R;
import com.yxh.fangs.application.MyApplication;

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
    ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        initView();
        initData();
    }

    private void initData() {

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
        String sosContent = MyApplication.getInstance().showLayoutText;
        if (TextUtils.isEmpty(sosContent)) {
            cb1.setChecked(true);
            cb2.setChecked(true);
            cb3.setChecked(true);
            cb4.setChecked(true);
            cb5.setChecked(true);
            cb6.setChecked(true);
            cb7.setChecked(true);
        } else {
            boolean showFishing = sosContent.contains("渔场");
            boolean showNoFishLine = sosContent.contains("机轮拖网渔业禁渔线");
            boolean showCoast = sosContent.contains("领海基线");
            boolean showCKFA = sosContent.contains("中韩渔业协定水域");
            boolean showCJFA = sosContent.contains("中日渔业协定水域");
            boolean showTyphoon = sosContent.contains("台风预警");
            boolean showRain = sosContent.contains("气象信息");
            cb1.setChecked(showFishing);
            cb2.setChecked(showNoFishLine);
            cb3.setChecked(showCoast);
            cb4.setChecked(showCKFA);
            cb5.setChecked(showCJFA);
            cb6.setChecked(showTyphoon);
            cb7.setChecked(showRain);
        }
        // 初始化并加入列表
//        addCheckBox(R.id.cb_1);
//        addCheckBox(R.id.cb_2);
//        addCheckBox(R.id.cb_3);
//        addCheckBox(R.id.cb_4);
//        addCheckBox(R.id.cb_5);
//        addCheckBox(R.id.cb_6);

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
