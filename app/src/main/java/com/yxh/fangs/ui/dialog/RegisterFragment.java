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

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.DeviceRegisterRequest;
import com.yxh.fangs.bean.DeviceRegisterResponse;
import com.yxh.fangs.ui.main.SplashActivity;
import com.yxh.fangs.util.AppConstants;
import com.yxh.fangs.util.DeviceUtils;
import com.yxh.fangs.util.HttpUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.SPUtils;
import com.yxh.fangs.util.UrlUtils;

public class RegisterFragment extends DialogFragment {

    private SplashActivity splashActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        splashActivity = (SplashActivity) context;
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
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_register, null);
        EditText etContent = rootView.findViewById(R.id.et_content);
        Button btnCancel = rootView.findViewById(R.id.btnCancel);
        Button btnOk = rootView.findViewById(R.id.btnOk);
        btnCancel.setOnClickListener(v -> {
            dismissDialog();
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String licContent = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(licContent)) {
                    Toast.makeText(getContext(), "请输入授权信息！", Toast.LENGTH_SHORT).show();
                } else {
                    getLicenseValidityPeriod(licContent);
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
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                );

                dialog.getWindow().clearFlags(
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
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

    public void getLicenseValidityPeriod(String licenseKey) {
        DeviceRegisterRequest deviceRegisterRequest = new DeviceRegisterRequest();
        deviceRegisterRequest.setDeviceSn(DeviceUtils.getDeviceId(getActivity()));
        deviceRegisterRequest.setDeviceName(DeviceUtils.getDeviceName(getActivity()));
        deviceRegisterRequest.setDeviceModel(DeviceUtils.getDeviceModel());
        deviceRegisterRequest.setLicenseKey(licenseKey);
        String json = new Gson().toJson(deviceRegisterRequest);
        LogUtils.json(json);
        HttpUtils.postJson(UrlUtils.getReceiverRegisterUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                DeviceRegisterResponse response = gson.fromJson(body, DeviceRegisterResponse.class);
                if (response.getCode() == 200) {
                    Toast.makeText(getContext(), response.getMsg(), Toast.LENGTH_SHORT).show();
                    SPUtils.putString(AppConstants.LICENSEVALIDITYPERIOD, licenseKey);
                    dismissDialog();
                    splashActivity.StartSelfCheck();
                } else {
                    onError(response.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
