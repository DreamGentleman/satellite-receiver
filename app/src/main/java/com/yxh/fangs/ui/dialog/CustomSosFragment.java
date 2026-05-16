package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yxh.fangs.R;
import com.yxh.fangs.ui.sos.SosActivity;

public class CustomSosFragment extends DialogFragment {

    private SosActivity sosActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sosActivity = (SosActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 提前禁止取消（防止系统提前处理）
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_custom_sos, container, false);
        EditText etContent = rootView.findViewById(R.id.et_content);
        Button btnCancel = rootView.findViewById(R.id.btnCancel);
        Button btnOk = rootView.findViewById(R.id.btnOk);
        btnCancel.setOnClickListener(v -> {
            dismissDialog();
            sosActivity.setCustomContent("");
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getContext(), "请输入自定义报警信息！", Toast.LENGTH_SHORT).show();
                } else {
                    setCustomContent(content);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            // ===== 1. Dialog 行为控制 =====
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            dialog.setOnKeyListener((d, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            });

            // ===== 2. 关键：恢复沉浸式 =====
            if (dialog.getWindow() != null) {
                View decorView = dialog.getWindow().getDecorView();

                int flags =
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

                decorView.setSystemUiVisibility(flags);

                // 防止 Window 再次抢焦点
                dialog.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                );

                dialog.getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                );

                Window window = dialog.getWindow();

                WindowManager.LayoutParams params = window.getAttributes();

                int screenWidth = getScreenWidth(getContext());
                params.width = screenWidth / 2;              // ⭐ 二分之一
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;

                window.setAttributes(params);
            }
        }
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
            //  容错处理,不做操作
        }
    }

    public void dismissDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            super.dismissAllowingStateLoss();
        }
    }

    public void setCustomContent(String content) {
        sosActivity.setCustomContent(content);
        dismissDialog();
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            sosActivity.resumeCountDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
