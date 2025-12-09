package com.yxh.fangs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yxh.fangs.ui.main.SplashActivity;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {

            Log.d("BootReceiver", "收到开机广播，准备启动首页");

            Intent launchIntent = new Intent(context, SplashActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 必须！！

            context.startActivity(launchIntent);
        }
    }
}
