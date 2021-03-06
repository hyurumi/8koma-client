package tk.hachikoma.apis;

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
import tk.hachikoma.Constants;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.R;
import tk.hachikoma.apis.ssl.HachikoSSL;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.util.HachikoLogger;
import org.json.JSONObject;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * クッキー認証まわりのお世話をしてくれる {@link RequestQueue}.
 */
public class HachiRequestQueue extends RequestQueue {
    private static final String DEFAULT_CACHE_DIR = "volley";
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private boolean isReauthorizing;
    private Queue<Request> pendingRequestBeforeAuth = new LinkedList<Request>();
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
                                && !HachikoAPI.User.REGISTER.getUrl().equals(request.getUrl())
                                && !HachikoAPI.User.IMPLICIT_LOGIN.getUrl().equals(request.getUrl())) {
                            HachikoLogger.warn("auth error, try to login...");
                            loginAndRetry(context, request, error);
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
            SSLSocketFactory sslSocketFactory =  HachikoSSL.getSocketFactory(
                    context,
                    Constants.IS_DEVELOPER ? R.raw.debug: R.raw.release);
            return new BasicNetwork(new HurlStack(null, sslSocketFactory));
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

    private static void loginAndRetry(Context context, final Request originalRequest,
                                      final VolleyError originalError) {
        Request loginRequest = new ImplicitLoginRequest(context,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        // つらみ極まるがやむなし… #150 参照のこと
                        originalRequest.deliverError(originalError);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("failed to reauth", volleyError);
                        originalRequest.deliverError(volleyError);
                    }
                }
        );
        HachikoApp.defaultRequestQueue().add(loginRequest);
    }

    private boolean shouldDumpRequest(String url) {
        if (Constants.IS_DEVELOPER) {
            return true;
        }
        return Constants.IS_ALPHA_USER
                && !(url.equals(HachikoAPI.User.REGISTER.getUrl())
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
