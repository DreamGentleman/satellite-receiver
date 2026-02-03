package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.yxh.fangs.R;

public class WeatherDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_WEATHER = "arg_weather";

    private OnPromptButtonClickedListener mListener;

    public static WeatherDialogFragment newInstance(String message, int weatherRes) {
        WeatherDialogFragment fragment = new WeatherDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putInt(ARG_WEATHER, weatherRes);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPromptButtonClickedListener(OnPromptButtonClickedListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_weather, null, false);

        TextView tvMessage = view.findViewById(R.id.tv_message);
        TextView tvSure = view.findViewById(R.id.tv_sure);
        ImageView ivWeather = view.findViewById(R.id.iv_weather);

        Bundle args = getArguments();
        String message = args != null ? args.getString(ARG_MESSAGE) : "";
        int weather = args != null ? args.getInt(ARG_WEATHER) : 0;

        tvMessage.setText(message);
        ivWeather.setImageResource(weather);

        tvSure.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onPositiveButtonClicked();
            }
            dismiss();
        });

        dialog.setContentView(view);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;

        Window window = dialog.getWindow();

        // ⭐ 关键 1：先阻止 Dialog 抢焦点
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // 宽度 45%
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (getScreenWidth(requireContext()) * 0.45f);
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        // ⭐ 关键 2：设置沉浸式
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // ⭐ 关键 3：恢复焦点（沉浸式状态保持）
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            fullScreenImmersive(getDialog().getWindow().getDecorView());
        }
    }

    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    public interface OnPromptButtonClickedListener {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }
}
