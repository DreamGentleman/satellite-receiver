package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yxh.fangs.R;
import com.yxh.fangs.bean.ImageCache;

public class RedImageDialogFragment extends DialogFragment {

    private final String publishTime;


    public RedImageDialogFragment(String publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireContext(), R.style.FullScreenDialog);
        dialog.setContentView(R.layout.dialog_image);

        dialog.setCanceledOnTouchOutside(false);

        // 中心缩放动画
        View card = dialog.findViewById(R.id.cardAlert);
        if (card != null) {
            card.setScaleX(0.3f);
            card.setScaleY(0.3f);
            card.setAlpha(0f);
            card.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(220).start();
        }

        Button btn = dialog.findViewById(R.id.btnConfirm);
        if (btn != null) {
            btn.setOnClickListener(v -> dismiss());
        }
        TextView tvTime = dialog.findViewById(R.id.tv_time);
        ImageView ivImage = dialog.findViewById(R.id.iv_image);
        tvTime.setText("预警时间：" + publishTime);
        try {
            loadBase64(ivImage, ImageCache.base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    @Override
    public void dismiss() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            Dialog d = getDialog();
            if (d != null) {
                View card = d.findViewById(R.id.cardAlert);
                if (card != null) {
                    card.animate()
                            .scaleX(0.3f)
                            .scaleY(0.3f)
                            .alpha(0f)
                            .setDuration(180)
                            .withEndAction(super::dismiss)
                            .start();
                    return;
                }
            }
            super.dismiss();
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
            //  容错处理,不做操作
            ignore.printStackTrace();
        }
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
}
