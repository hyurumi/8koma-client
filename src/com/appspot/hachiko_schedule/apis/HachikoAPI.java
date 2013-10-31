package com.appspot.hachiko_schedule.apis;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;

/**
 * Class that represents information about certain API.
 */
public class HachikoAPI {
    public static final String BASE = "http://hachiko.yutopio.net/api/";
    public static final RetryPolicy RETRY_POLICY_LONG
            = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 4,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public static final int TAG_VACANCY_REQUEST = 1000;

    private final int method;
    private final String subUrl;

    protected HachikoAPI(int method, String subUrl) {
        this.method = method;
        this.subUrl = subUrl;
    }

    public int getMethod() {
        return method;
    }

    public String getUrl() {
        return BASE + subUrl;
    }

    public static String getUrl(String path) {
        return BASE + path;
    }
}
