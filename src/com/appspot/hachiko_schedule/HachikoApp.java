package com.appspot.hachiko_schedule;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.appspot.hachiko_schedule.dev.FakeRequestQueue;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;

/**
 * アプリケーション全体で利用する管理するクラスなどを管理する
 */
public class HachikoApp extends Application {
    private static Context appContext;
    private static RequestQueue requestQueue;
    private static FakeRequestQueue fakeRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        fakeRequestQueue = new FakeRequestQueue(appContext);
        requestQueue = Volley.newRequestQueue(appContext);
        requestQueue.start();
    }

    public static RequestQueue defaultRequestQueue() {
        if (HachikoPreferences.getBooleanFromDefaultPref(
                appContext,
                HachikoPreferences.KEY_USE_FAKE_REQUEST_QUEUE,
                HachikoPreferences.USE_FAKE_REQUEST_QUEUE_DEFAULT)) {
            return fakeRequestQueue;
        }
        return requestQueue;
    }

    /**
     * @return own app version as registered obtained by {@link PackageManager}.
     */
    public static int getAppVersionCode() {
        try {
            PackageInfo packageInfo = appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


}
