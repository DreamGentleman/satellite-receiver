package com.yxh.fangs.ui.dialog;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;

/**
 * 弹窗管理器：
 * 1）同一时间最多展示一个弹窗（新弹窗来会先关闭旧弹窗）
 * 2）无新弹窗 60 秒自动消失
 * 3）有新弹窗则刷新倒计时
 */
public class DialogStackManager {

    private static final long AUTO_DISMISS_MS = 60_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WeakReference<Activity> activityRef;
    private DialogFragment currentDialog;

    private final Runnable autoDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismissCurrent();
        }
    };

    public DialogStackManager(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    /**
     * 展示新弹窗：先关闭旧弹窗，再展示新弹窗，并刷新 60s 倒计时
     */
    public void show(DialogFragment dialog, FragmentManager manager, String tag) {
        Activity act = activityRef.get();
        if (act == null || act.isFinishing()) return;

        mainHandler.post(() -> {
            dismissCurrentInternal();      // ✅ 保证不叠加
            currentDialog = dialog;

            try {
                dialog.show(manager, tag);
            } catch (Exception ignored) {
            }

            refreshAutoDismiss();          // ✅ 刷新倒计时
        });
    }

    /**
     * 只有“有弹窗显示状态”时，刷新 60s 自动消失倒计时
     */
    public void refreshAutoDismiss() {
        mainHandler.removeCallbacks(autoDismissRunnable);
        mainHandler.postDelayed(autoDismissRunnable, AUTO_DISMISS_MS);
    }

    /**
     * 关闭当前弹窗（外部调用）
     */
    public void dismissCurrent() {
        mainHandler.post(this::dismissCurrentInternal);
    }

    private void dismissCurrentInternal() {
        mainHandler.removeCallbacks(autoDismissRunnable);

        if (currentDialog != null) {
            try {
                if (currentDialog.isAdded()) {
                    currentDialog.dismissAllowingStateLoss();
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            currentDialog = null;
        }
    }

    /**
     * Activity onDestroy 时调用，防止泄漏
     */
    public void release() {
        dismissCurrent();
        activityRef.clear();
    }
}
