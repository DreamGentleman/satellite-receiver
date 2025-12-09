package com.yxh.fangs.ui.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yxh.fangs.R;


public class RemindDialog extends AlertDialog {
    private Context mContext;
    private OnPromptButtonClickedListener mPromptButtonClickedListener;
    private String mPositiveButton;
    private String mMessage;
    private int mLayoutResId;

    public static RemindDialog newInstance(final Context context, String message) {
        return new RemindDialog(context, message);
    }

    public RemindDialog(final Context context, String message) {
        super(context);
        mLayoutResId = R.layout.dialog_remind;
        mContext = context;
        mMessage = message;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ⭐ 关键 1：阻止 Dialog 抢焦点（否则一定破坏沉浸式）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(mLayoutResId, null);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
        TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
        TextView tvSure = (TextView) view.findViewById(R.id.tv_sure);
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPromptButtonClickedListener != null) {
                    mPromptButtonClickedListener.onPositiveButtonClicked();
                }
                dismiss();
            }
        });
        tvMessage.setText(mMessage);

        setContentView(view);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (getScreenWidth() * 0.45);
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);
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

    public RemindDialog setPromptButtonClickedListener(OnPromptButtonClickedListener buttonClickedListener) {
        this.mPromptButtonClickedListener = buttonClickedListener;
        return this;
    }

    public RemindDialog setLayoutRes(int resId) {
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
