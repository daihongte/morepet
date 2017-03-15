package com.dlc.morepet.utils;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ${QianYan}
 * Edit on 2016/9/24.
 */
public class HttpManager {

    private final String TAG = getClass().getSimpleName();

    //上传文档
    public static final MediaType TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private OkHttpClient client;
    private volatile static HttpManager manager;

    private HttpManager() {


        client = new OkHttpClient();
//        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(AppConfig.getContext()));
//        client = new OkHttpClient.Builder()
//                .cookieJar(cookieJar).build();  //设置cookie
    }


    public static HttpManager getInstance() {
        if (manager == null) {
            synchronized (HttpManager.class) {
                if (manager == null) {
                    manager = new HttpManager();
                }
            }
        }
        return manager;
    }

    /**
     * 使用Get的请求进行异步访问
     *
     * @param url
     * @param params
     * @param listener
     */
    public void getByParams(final String url, final Map<String, String> params, final OnBackListener listener) {

        StringBuilder builder = new StringBuilder(url + "?");
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        Request request = new Request.Builder().url(builder.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailed("访问失败");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    if (response.isSuccessful()) {
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailed("返回错误");
                    }
                }

            }
        });
    }


    public void getByParams(final String url, final OnBackListener listener) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailed("访问失败");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    if (response.isSuccessful()) {
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailed("返回错误");
                    }
                }

            }
        });
    }

    /**
     * 以Post的方式异步请求
     *
     * @param url
     * @param params
     * @param listener
     */
    public void postFormParams(final String url, final Map<String, String> params, final OnBackListener listener) {
        FormBody.Builder builder = new FormBody.Builder();

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        FormBody build = builder.build();
        Request request = new Request.Builder().url(url).post(build).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailed("访问失败");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (listener != null) {
                    if (response.isSuccessful()) {
//                        response.body().
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailed("返回错误");
                    }
                }
            }
        });
    }

    /**
     * 以Post的方式异步请求
     *
     * @param url
     * @param params
     * @param listener
     */
    public void postFormParams(final String url, String cookie, final Map<String, String> params, final OnBackListener listener) {
        FormBody.Builder builder = new FormBody.Builder();

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        FormBody build = builder.build();
        Request request = new Request.Builder().url(url).addHeader("cookie", cookie).post(build).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailed("访问失败");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (listener != null) {
                    if (response.isSuccessful()) {
//                        response.body().
                        listener.onSuccess(response.body().string());
                    } else {
                        listener.onFailed("返回错误");
                    }
                }
            }
        });
    }

    public interface OnBackListener {
        void onSuccess(String msg);

        void onFailed(String msg);
    }
}
