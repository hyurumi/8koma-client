package com.appspot.hachiko_schedule.apis;

import com.android.volley.Request;

public class FriendAPI {
    public static final HachikoAPI ADD_FRIENDS = new HachikoAPI(Request.Method.POST, "friends");
}
