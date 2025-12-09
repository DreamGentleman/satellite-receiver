package com.yxh.fangs.ui.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yxh.fangs.R;


public class SosDialog extends AlertDialog {
    private Context mContext;
    private OnPromptButtonClickedListener mPromptButtonClickedListener;
    private String mPositiveButton;
    private int mLayoutResId;

    public static SosDialog newInstance(final Context context) {
        return new SosDialog(context);
    }

    public SosDialog(final Context context) {
        super(context);
        mLayoutResId = R.layout.dialog_sos;
        mContext = context;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ⭐ 关键 1：阻止 Dialog 抢焦点（否则一定破坏沉浸式）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(mLayoutResId, null);
        EditText etPassword = (EditText) view.findViewById(R.id.et_password);
        ImageView ivShowPassword = (ImageView) view.findViewById(R.id.iv_show_password);
        TextView tvSure = (TextView) view.findViewById(R.id.tv_sure);
        setPwdShowOrHide(etPassword, ivShowPassword);
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPromptButtonClickedListener != null) {
                    mPromptButtonClickedListener.onPositiveButtonClicked();
                }
                dismiss();
            }
        });

        setContentView(view);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (getScreenWidth() * 0.45);
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        // ⭐ 关键2：对 DecorView 设置沉浸式标志（Dialog 不抢焦点时才能生效）
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // ⭐ 关键3：显示后再恢复焦点（沉浸式会保持不变）
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public SosDialog setPromptButtonClickedListener(OnPromptButtonClickedListener buttonClickedListener) {
        this.mPromptButtonClickedListener = buttonClickedListener;
        return this;
    }

    public SosDialog setLayoutRes(int resId) {
        this.mLayoutResId = resId;
        return this;
    }

    public interface OnPromptButtonClickedListener {
        void onPositiveButtonClicked();

        void onNegativeButtonClicked();
    }

//    private int gePopupWidth() {
//        int distanceToBorder = (int) mContext.getResources().getDimension(R.dimen.rce_dimen_size_40);
//        return getScreenWidth() - 2 * (distanceToBorder);
//    }

    private int getScreenWidth() {
        return ((WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth();
    }

    /**
     * 设置密码显示与隐藏
     *
     * @param editText 密码输入框
     * @param iv       右侧小眼睛图标
     */
    public void setPwdShowOrHide(EditText editText, ImageView iv) {

        iv.setOnClickListener(v -> {
            // 当前是否为密码隐藏模式
            boolean isHidden = editText.getTransformationMethod() instanceof PasswordTransformationMethod;

            if (isHidden) {
                // 显示密码
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                iv.setImageResource(R.drawable.ic_eye_open);     // 👁 显示状态的图标
            } else {
                // 隐藏密码
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                iv.setImageResource(R.drawable.ic_eye_close);    // 👁‍🗨 隐藏状态图标（带斜杠）
            }

            // 光标保持在最后
            editText.setSelection(editText.getText().length());
        });
    }
    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
}
