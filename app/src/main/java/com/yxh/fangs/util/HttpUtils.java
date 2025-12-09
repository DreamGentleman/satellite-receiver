package com.yxh.fangs.util;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 回调接口
    public interface HttpCallback {
        void onSuccess(String body);
        void onError(String msg);
    }

    // GET
    public static void get(String url, HttpCallback callback) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        execute(request, callback);
    }

    // POST - 表单
    public static void postForm(String url, Map<String, String> params, HttpCallback callback) {

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (String key : params.keySet()) {
                formBuilder.add(key, params.get(key));
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();

        execute(request, callback);
    }

    // POST - JSON
    public static void postJson(String url, String json, HttpCallback callback) {

        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        execute(request, callback);
    }

    // 执行请求
    private static void execute(Request request, HttpCallback callback) {

        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();

                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        if (callback != null) callback.onSuccess(body);
                    } else {
                        if (callback != null) callback.onError("HTTP " + response.code());
                    }
                });
            }
        });
    }
}
