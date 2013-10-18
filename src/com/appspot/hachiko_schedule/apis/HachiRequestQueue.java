package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import com.android.volley.*;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.io.File;

/**
 *401エラーを自動でハンドルしてくれる{@link RequestQueue}.
 */
public class HachiRequestQueue extends RequestQueue {
    private static final String DEFAULT_CACHE_DIR = "volley";
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private final Context context;

    public HachiRequestQueue(final Context context) {
        super(
                new DiskBasedCache(new File(context.getCacheDir(), DEFAULT_CACHE_DIR)),
                createNetwork(context),
                DEFAULT_NETWORK_THREAD_POOL_SIZE,
                new ExecutorDelivery(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void postError(final Request<?> request, VolleyError error) {
                        if (isAuthFailure(error)
                                && !UserAPI.REGISTER.equals(request.getUrl())) {
                            HachikoLogger.warn("auth error, try to login...");
                            loginAndRetry(context, request);
                        } else {
                            super.postError(request, error);
                        }
                    }
                });
        this.context = context;
    }

    private static Network createNetwork(Context context) {
        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (Build.VERSION.SDK_INT >= 9) {
            return new BasicNetwork(new HurlStack());
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            return new BasicNetwork(new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));
        }
    }

    @Override
    public Request add(Request request) {
        // TODO: remove
        // TechCrunch審査のため，アドホックにタイムアウトを長く設定
        request.setRetryPolicy(new DefaultRetryPolicy(
                10 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (HachikoPreferences.getBooleanFromDefaultPref(
                context,
                HachikoPreferences.KEY_USE_SUPER_LONG_LIFE_REQUEST,
                HachikoPreferences.USE_SUPER_LONG_LIFE_REQUEST)) {
            request.setRetryPolicy(new DefaultRetryPolicy(
                    /* デバッグのためタイムアウトを1日に設定 */ 36000 * 1000,
                    1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
        return super.add(request);
    }

    private static boolean isAuthFailure(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
            return true;
        }
        return error instanceof NetworkError;
    }

    private static void loginAndRetry(Context context, final Request originalRequest) {
        // TODO: サーバとの認証方式が固まり次第実装
        originalRequest.deliverError(new VolleyError());
    }
}
