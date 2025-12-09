package com.yxh.fangs.util;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {

    private static OkHttpClient instance;

    public static OkHttpClient getInstance() {
        if (instance == null) {

            // 打印日志（必须加这个依赖）
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 统一 Header（可选）
            Interceptor headerInterceptor = chain -> {
                return chain.proceed(
                        chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json")
                                .build()
                );
            };

            instance = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
        }

        return instance;
    }
}
