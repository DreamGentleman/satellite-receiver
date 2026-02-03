package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.yxh.fangs.R;

public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_CONTENT = "arg_content";
    private static final String ARG_TIME = "arg_time";

    private OnPromptButtonClickedListener mListener;

    public static MessageDialogFragment newInstance(
            String title,
            String content,
            String time
    ) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPromptButtonClickedListener(OnPromptButtonClickedListener listener) {
        this.mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_message, null, false);

        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        TextView tvSure = view.findViewById(R.id.tv_sure);

        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE) : "";
        String content = args != null ? args.getString(ARG_CONTENT) : "";
        String time = args != null ? args.getString(ARG_TIME) : "";

        tvMessage.setText(title + "，" + content);

        if (TextUtils.isEmpty(time)) {
            tvTime.setVisibility(View.GONE);
        } else {
            tvTime.setText("接收时间：" + time);
        }

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
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getScreenWidth(requireContext()) * 0.45f);
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
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
            int uiOptions =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    public interface OnPromptButtonClickedListener {
        void onPositiveButtonClicked();

        void onNegativeButtonClicked();
    }
}