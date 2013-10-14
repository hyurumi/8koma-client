package com.appspot.hachiko_schedule.apis;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.appspot.hachiko_schedule.setup.GoogleAuthActivity;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.JSONUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import org.json.JSONObject;

import java.io.File;

/**
 *401エラーを自動でハンドルしてくれる{@link RequestQueue}.
 */
public class HachiRequestQueue extends RequestQueue {
    private static final String DEFAULT_CACHE_DIR = "volley";
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

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

    private static boolean isAuthFailure(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
            return true;
        }
        return error instanceof NetworkError;
    }

    private static void loginAndRetry(Context context, final Request originalRequest) {
        GoogleAuthPreferences authPreferences = new GoogleAuthPreferences(context);
        invalidateToken(context, authPreferences);
        try {
            String token = GoogleAuthUtil.getToken(
                    context, authPreferences.getAccountName(), GoogleAuthActivity.SCOPE);
            authPreferences.setToken(token);
        } catch (Exception e) {
            HachikoDialogs.showErrorDialogIfDeveloper(context, e, "google auth");
            HachikoLogger.error("Google Login failed", e);
            return;
        }
        JSONObject params = JSONUtils.jsonObject(
                "gmail", authPreferences.getAccountName(),
                "google_token", authPreferences.getToken());
        HachikoLogger.debug(authPreferences.getAccountName(), authPreferences.getToken());
        new HachikoCookieManager(context).invalidateSessionCookie();
        JsonRequest authRequest = VolleyRequestFactory.registerRequest(
                context,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("auth success, please retry");
                        originalRequest.deliverError(new VolleyError());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("Hachiko login error ", volleyError);
                    }
                }
        );
        HachikoApp.defaultRequestQueue().add(authRequest);
    }

    // TODO: リファクタリング
    // GoogleAuthActivtyからこぴぺ
    private static final String COM_GOOGLE = "com.google";
    private static void invalidateToken(Context context, GoogleAuthPreferences authPreferences) {
        HachikoLogger.debug("invalidate token");
        AccountManager accountManager = AccountManager.get(context);
        accountManager.invalidateAuthToken(COM_GOOGLE, authPreferences.getToken());
        authPreferences.setToken(null);
    }
}
