package com.yxh.fangs.ui.main;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.yxh.fangs.R;

public class ImageDetailActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> {
            finish();
        });
    }
}
