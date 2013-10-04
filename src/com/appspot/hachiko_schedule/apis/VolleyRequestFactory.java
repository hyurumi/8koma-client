package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import org.json.JSONObject;

/**
 * Volleyの{@link Request}周りのヘルパ
 */
public class VolleyRequestFactory {
    static public JSONStringRequest registerRequest(
            final Context context, JSONObject params,
            Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        return new JSONStringRequest(
                context, UserAPI.REGISTER.getMethod(), UserAPI.REGISTER.getUrl(), params,
                responseListener, errorListener) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                new HachikoCookieManager(context).saveSessionCookie(response);
                return super.parseNetworkResponse(response);
            }
        };
    }
}
