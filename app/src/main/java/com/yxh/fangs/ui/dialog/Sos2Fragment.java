package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.SosEventResponse;
import com.yxh.fangs.util.HttpUtils;
import com.yxh.fangs.util.LogUtils;
import com.yxh.fangs.util.UrlUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Sos2Fragment extends DialogFragment {

    private TextView tvSos;
    private String sosContent;
    private String sosType;

    public static Sos2Fragment newInstance(String sosType, String sosContent) {

        Bundle args = new Bundle();
        args.putString("sosType", sosType);
        args.putString("sosContent", sosContent);
        Sos2Fragment fragment = new Sos2Fragment();
        fragment.setArguments(args);
        return fragment;
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
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sos, null);
        sosType = getArguments().getString("sosType");
        sosContent = getArguments().getString("sosContent");
        TextView tvSure = (TextView) rootView.findViewById(R.id.tv_sure);
        tvSos = (TextView) rootView.findViewById(R.id.tv_sos);
        String text = getContext().getString(R.string.sos_sending_tip, sosType);

        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf(sosType);
        int end = start + sosType.length();

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#F8E36F")),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannable.setSpan(
                new StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvSos.setText(spannable);
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                Message2Dialog.newInstance(getActivity(), "提示消息", "本次SOS紧急求助信息已取消", "").show();
            }
        });
        startPolling();
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

    private Disposable pollingDisposable;

    public void startPolling() {
        stopPolling(); // 防止重复启动
        pollingDisposable = Observable
                // 立即执行一次，之后每 1 分钟一次
                .interval(0, 1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(tick -> {
                    // 这里写你的轮询逻辑
                    doPollingRequest();
                }, throwable -> {
                    // 一般 interval 不会进这里，兜底
                    throwable.printStackTrace();
                });
    }

    private void doPollingRequest() {
        HttpUtils.postJson(UrlUtils.getSosEventStartUrl(), sosContent, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                SosEventResponse sosEventResponse = gson.fromJson(body, SosEventResponse.class);
                if (sosEventResponse.getCode() == 200) {
                    Toast.makeText(getContext(), "SOS求救信息已经发送成功！", Toast.LENGTH_SHORT).show();
                } else {
                    onError(sosEventResponse.getMsg());
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void stopPolling() {
        if (pollingDisposable != null && !pollingDisposable.isDisposed()) {
            pollingDisposable.dispose();
            pollingDisposable = null;
        }
    }

    @Override
    public void dismiss() {
        stopPolling();   // ⭐ 必须先停轮询
        super.dismiss();
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
