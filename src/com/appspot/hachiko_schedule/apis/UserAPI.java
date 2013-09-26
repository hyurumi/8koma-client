package com.appspot.hachiko_schedule.apis;

import com.android.volley.Request;

public class UserAPI {
    public static final HachikoAPI REGISTER = new HachikoAPI(Request.Method.POST, "user");
    public static final HachikoAPI REGISTER_GCM_ID = new HachikoAPI(Request.Method.POST, "gcminfo");
}
