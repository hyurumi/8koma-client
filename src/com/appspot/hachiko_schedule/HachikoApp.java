package com.appspot.hachiko_schedule;

import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * アプリケーション全体で利用する管理するクラスなどを管理する
 */
public class HachikoApp extends Application {
    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static RequestQueue defaultRequestQueue() {
        return requestQueue;
    }
}
