package com.yxh.fangs.util;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.yxh.fangs.bean.UpdateBean;
import com.yxh.fangs.ui.dialog.AppUpdateFragment;
import com.yxh.fangs.ui.widget.LoadingDialog;

public class AppUpdateUtil {
    private static AppCompatActivity activity;
    private static CallBack callBack;
    private LoadingDialog dialog;
    private boolean isShowDialog;

    public AppUpdateUtil(AppCompatActivity activity, CallBack callBack) {
        AppUpdateUtil.activity = activity;
        AppUpdateUtil.callBack = callBack;
        if (dialog == null) {
            dialog = new LoadingDialog(activity);
        }
    }

    public static AppUpdateUtil getInstance(AppCompatActivity activity, CallBack callBack) {
        return new AppUpdateUtil(activity, callBack);
    }

    public void checkVersion() {
        checkVersion(true);
    }

    public void checkVersion(boolean isShowDialog) {
        HttpUtils.get(UrlUtils.checkUpdate(), new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                LogUtils.json(body);
                Gson gson = new Gson();
                UpdateBean updateBean = gson.fromJson(body, UpdateBean.class);
                if (updateBean.getCode() == 200) {
                    if (ApkUtils.getVersionCode(activity) < updateBean.getData().getVersionCode()) {
                        showAppUpdateDialog();
                    } else {
                        callBack.onUpdateSuccess();
                    }
                } else {
                    onError(updateBean.getMsg());
                }

            }

            @Override
            public void onError(String msg) {
                callBack.onUpdateError(msg);
            }
        });
    }

    public interface CallBack {
        void onUpdateError(String message);

        void onUpdateSuccess();
    }

    private void showAppUpdateDialog() {
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.findFragmentByTag("AppUpdate") == null) {
            new AppUpdateFragment().show(fm, "AppUpdate");
        }
    }
}
