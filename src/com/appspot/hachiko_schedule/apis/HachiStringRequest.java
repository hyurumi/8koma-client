package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class HachiStringRequest extends StringRequest {
    private HachikoCookieManager cookieManager;

    public HachiStringRequest(Context context, String url, Response.Listener<String> listener,
                              Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        cookieManager = new HachikoCookieManager(context);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return cookieManager.addSessionCookie(super.getHeaders());
    }
}
