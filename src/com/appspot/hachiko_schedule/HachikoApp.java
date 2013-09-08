package com.appspot.hachiko_schedule;

import android.app.Application;
import android.content.Context;
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
    private static FakeRequestQueue fakeRequestQueue = new FakeRequestQueue();

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        requestQueue = Volley.newRequestQueue(appContext);
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
}
