package com.yxh.fangs.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.ImageCache;

public class ImageDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String time = getIntent().getStringExtra("time");
        setContentView(R.layout.activity_image_detail);
        ImageView ivBack = findViewById(R.id.iv_back);
        ImageView ivMessage = findViewById(R.id.iv_message);
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvTime = findViewById(R.id.tv_time);
        tvTime.setText("接收时间：" + time);
        try {
            loadBase64(ivMessage, ImageCache.base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ivBack.setOnClickListener(v -> {
            finish();
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public static void loadBase64(ImageView imageView, String base64) {
        if (base64 == null || base64.isEmpty()) return;

        // 如果包含 data:image/...;base64, 前缀，先去掉
        if (base64.contains(",")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }

        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageCache.base64 = "";
    }
}
