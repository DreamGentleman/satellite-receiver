package com.yxh.fangs.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtils {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .build();

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onProgress(int progress); // 0~100

        void onSuccess(File file);

        void onError(String msg);
    }

    /**
     * 文件下载（推荐入口）
     *
     * @param context  Context
     * @param url      文件地址
     * @param fileName 保存文件名（如 update.apk / data.zip）
     */
    public static void download(Context context,
                                String url,
                                String fileName,
                                Callback callback) {

        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            callback.onError("无法获取存储目录");
            return;
        }

        File target = new File(dir, fileName);
        downloadInternal(url, target, callback);
    }

    /**
     * 核心下载逻辑
     */
    private static void downloadInternal(String url,
                                         File target,
                                         Callback callback) {

        Request request = new Request.Builder()
                .url(url)
                .build();
        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postError(callback, e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postError(callback, "下载失败：" + response.code());
                    return;
                }

                InputStream is = null;
                FileOutputStream fos = null;

                try {
                    long total = response.body().contentLength();
                    long sum = 0;

                    is = response.body().byteStream();
                    fos = new FileOutputStream(target);

                    byte[] buffer = new byte[8 * 1024];
                    int len;
                    int lastProgress = 0;

                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        sum += len;

                        if (total > 0) {
                            int progress = (int) (sum * 100f / total);
                            if (progress != lastProgress) {
                                lastProgress = progress;
                                postProgress(callback, progress);
                            }
                        }
                    }

                    fos.flush();
                    postSuccess(callback, target);

                } catch (Exception e) {
                    postError(callback, e.getMessage());
                } finally {
                    try {
                        if (is != null) is.close();
                        if (fos != null) fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    /* ===== 主线程回调 ===== */

    private static void postProgress(Callback callback, int progress) {
        MAIN.post(() -> callback.onProgress(progress));
    }

    private static void postSuccess(Callback callback, File file) {
        MAIN.post(() -> callback.onSuccess(file));
    }

    private static void postError(Callback callback, String msg) {
        MAIN.post(() -> callback.onError(msg));
    }
}
