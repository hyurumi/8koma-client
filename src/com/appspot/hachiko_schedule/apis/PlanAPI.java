package com.appspot.hachiko_schedule.apis;

import com.android.volley.Request;

public class PlanAPI {
    public static final HachikoAPI NEW_PLAN = new HachikoAPI(Request.Method.POST, "plans");
}
