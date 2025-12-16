package com.yxh.fangs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yxh.fangs.R;

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
                data.putExtra("handleSosSelected", handleSosSelected());
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
        //TODO 没加渔场
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
        return selectLayout;
    }
}
