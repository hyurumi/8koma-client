package com.appspot.hachiko_schedule.apis;

/**
 * Class that represents information about certain API.
 */
public class HachikoAPI {
    public static final String BASE = "http://hachiko.yutopio.net/api/";

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
}
