package com.appspot.hachiko_schedule.apis;

import com.android.volley.Request;

public class PlanAPI {
    public static final HachikoAPI CONFIRM = new HachikoAPI(Request.Method.POST, "confirm/");
    public static final HachikoAPI RESPOND = new HachikoAPI(Request.Method.POST, "respond");
}
