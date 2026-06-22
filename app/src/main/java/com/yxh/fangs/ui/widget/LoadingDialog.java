package com.yxh.fangs.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.yxh.fangs.R;

public class LoadingDialog {

    private Dialog dialog;
    private ImageView ivLoading;
    private TextView tvLoadingText;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context, R.style.LoadingDialogStyle);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(false);
        ivLoading = dialog.findViewById(R.id.iv_loading);
        tvLoadingText = dialog.findViewById(R.id.tv_loading_text);
        setMessage(context.getString(R.string.loading_default));
    }

    public void setMessage(String message) {
        if (tvLoadingText == null) {
            return;
        }
        tvLoadingText.setText(TextUtils.isEmpty(message)
                ? dialog.getContext().getString(R.string.loading_default)
                : message);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            // ---------- 🔥 保持沉浸式核心代码 Start ----------
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            dialog.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            // ---------- 🔥 保持沉浸式核心代码 End ----------

            Animation animation = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.rotate_loading);
            ivLoading.startAnimation(animation);
            dialog.show();

            // ---------- 🔥 让 Dialog 再次获得焦点（沉浸式仍保持） ----------
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            ivLoading.clearAnimation();
            dialog.dismiss();
        }
    }
}
