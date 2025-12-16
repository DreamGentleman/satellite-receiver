package com.yxh.fangs.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yxh.fangs.R;
import com.yxh.fangs.ui.main.SplashActivity;
import com.yxh.fangs.util.ApkUtils;
import com.yxh.fangs.util.DownloadUtils;

import java.io.File;


public class AppUpdateFragment extends DialogFragment implements View.OnClickListener {
    TextView tvContent;
    TextView tvNext;
    TextView tvDown;
    private String message;
    private String url;
    private boolean force;
    private boolean isDownload = false;
    private File file;
    private ImageView iv;
    private TextView tvVersion;
    private String versionName;
    private SplashActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SplashActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_update, container, false);
        initView(rootView);
        message = getArguments().getString("updateContent");
        url = getArguments().getString("url");
        force = getArguments().getBoolean("forceUpdate");
        versionName = getArguments().getString("versionName");
        tvContent.setText(message);
        tvVersion.setText(versionName);
        if (force) {
            tvNext.setVisibility(View.GONE);
        }
        return rootView;
    }

    private void initView(View rootView) {
        tvContent = (TextView) rootView.findViewById(R.id.tv_content);
        iv = (ImageView) rootView.findViewById(R.id.iv);
        tvNext = (TextView) rootView.findViewById(R.id.tv_next);
        tvNext.setOnClickListener(AppUpdateFragment.this);
        tvDown = (TextView) rootView.findViewById(R.id.tv_down);
        tvVersion = (TextView) rootView.findViewById(R.id.tv_version);
        tvDown.setOnClickListener(AppUpdateFragment.this);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_next) {
            dismissDialog();
            activity.StartSelfCheck();
        } else if (v.getId() == R.id.tv_down) {
            if (isDownload) {
                ApkUtils.install(activity, file);
            } else {
                tvNext.setClickable(false);
                tvNext.setTextColor(Color.parseColor("#aaaaaa"));
                tvDown.setClickable(false);
                download();
            }
        }
    }

    private void download() {
        DownloadUtils.download(
                getActivity(),
                url,
                "data.zip",
                new DownloadUtils.Callback() {
                    @Override
                    public void onProgress(int progress) {
                        tvDown.setText(progress + "%");
                    }

                    @Override
                    public void onSuccess(File file) {
                        ApkUtils.install(getActivity(), file);
                    }

                    @Override
                    public void onError(String msg) {
                        Log.i("更新出错", msg);
                        Toast.makeText(getActivity(), "APP下载失败！", Toast.LENGTH_LONG).show();
                        tvNext.setClickable(true);
                        tvNext.setTextColor(Color.parseColor("#9DA7B8"));
                        tvDown.setClickable(true);
                        tvDown.setText("重试");
                    }
                }
        );


    }

    public static String getFileNameFromUrl(String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return System.currentTimeMillis() + ".apk";
            }
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < url.length() - 1) {
                String fileName = url.substring(lastSlash + 1);
                // 简单合法性校验（防止 ?token=xxx 这种）
                if (!TextUtils.isEmpty(fileName) && fileName.contains(".")) {
                    return fileName;
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        // 兜底：时间戳
        return System.currentTimeMillis() + ".apk";
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

    public void dismissDialog() {//关闭DialogFragment,防止出现Can not perform this action after onSaveInstanceState的错误
        if (getActivity() != null && !getActivity().isFinishing()) {
            super.dismissAllowingStateLoss();
            dismiss();
        }
    }
}
