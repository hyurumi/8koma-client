package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import com.android.volley.*;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.apis.ssl.HachikoSSL;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * クッキー認証まわりのお世話をしてくれる {@link RequestQueue}.
 */
public class HachiRequestQueue extends RequestQueue {
    private static final String DEFAULT_CACHE_DIR = "volley";
    private boolean isReauthorizing;
    private Queue<Request> pendingRequestBeforeAuth = new LinkedList<Request>();
    private final Context context;

    public HachiRequestQueue(final Context context) {
        super(
                new DiskBasedCache(new File(context.getCacheDir(), DEFAULT_CACHE_DIR)),
                createNetwork(context));
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
            return new BasicNetwork(new HurlStack(null, HachikoSSL.getSocketFactory(context)));
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            return new BasicNetwork(new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));
        }
    }

    private boolean shouldDumpRequest(String url) {
        if (Constants.IS_DEVELOPER) {
            return true;
        }
        return Constants.IS_ALPHA_USER
                && (url.equals(HachikoAPI.User.REGISTER.getUrl())
                    || url.equals(HachikoAPI.User.IMPLICIT_LOGIN.getUrl())
                    || url.equals(HachikoAPI.User.REGISTER_GCM_ID.getUrl()));
    }

    @Override
    public Request add(Request request) {
        if (shouldDumpRequest(request.getUrl())) {
            HachikoLogger.dumpRequest(request);
        }
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

        reauthorizeIfNecessary(request);

        if (isReauthorizing) {
            pendingRequestBeforeAuth.add(request);
            return request;
        } else {
            return super.add(request);
        }
    }

    private synchronized void reauthorizeIfNecessary(Request request) {
        if (isReauthorizing || isAuthUrl(request.getUrl()) || !cookieWillExpireSoon()) {
            return;
        }

        isReauthorizing = true;
        HachikoLogger.info("session will expire in an hour, start re-authorization...");
        Request reauthRequest = new ImplicitLoginRequest(context, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                isReauthorizing = false;
                for (Request request: pendingRequestBeforeAuth) {
                    HachiRequestQueue.super.add(request);
                }
                pendingRequestBeforeAuth.clear();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                HachikoLogger.error("reauthorization error", volleyError);
                throw new IllegalStateException("Error reauthorization", volleyError);
            }
        });
        super.add(reauthRequest);
    }

    private boolean cookieWillExpireSoon() {
        long sessionExpiresMills = HachikoPreferences.getDefault(context).getLong(
                HachikoPreferences.KEY_SESSION_EXPIRES_MILLIS, 0L);
        long currentTimeMillis = new Date().getTime();
        long oneHour = 60 * 60 * 1000;
        return sessionExpiresMills - currentTimeMillis < oneHour;
    }

    private boolean isAuthUrl(String url) {
        return HachikoAPI.User.REGISTER.getUrl().equals(url) || HachikoAPI.User.IMPLICIT_LOGIN.getUrl().equals(url);
    }
}
