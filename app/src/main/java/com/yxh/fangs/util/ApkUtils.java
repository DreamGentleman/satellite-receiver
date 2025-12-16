package com.yxh.fangs.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * Created by F on 2017/5/4.
 * APk工具类
 */

public class ApkUtils {

    /**
     * 安装一个apk文件
     */
    public static void install(Context context, File uriFile) {
        Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", uriFile);
            installAPKIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installAPKIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installAPKIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(installAPKIntent);
        } else {
            installAPKIntent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
            installAPKIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(installAPKIntent);
        }
    }

    /**
     * 卸载一个app
     */
    public static void uninstall(Context context, String packageName) {
        try {
            //通过程序的包名创建URI
            Uri packageURI = Uri.parse("package:" + packageName);
            //创建Intent意图
            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
            //执行卸载程序
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取平台版本号
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String namne = "";
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            namne = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return namne;
    }
}
