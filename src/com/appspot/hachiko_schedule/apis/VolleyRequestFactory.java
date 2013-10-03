package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.common.base.Joiner;
import org.json.JSONObject;

import java.util.Map;

/**
 * Volleyの{@link Request}周りのヘルパ
 */
public class VolleyRequestFactory {
    static public StringRequest newStringRequest(HachikoAPI api, final Map<String, String> params,
                                                 Response.Listener<String> responseListener,
                                                 Response.ErrorListener errorListener) {
        switch (api.getMethod()) {
            case Request.Method.POST:
            case Request.Method.PUT:
                return new StringRequest(api.getMethod(), api.getUrl(), responseListener, errorListener) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return params;
                    }
                };
            case Request.Method.GET:
                return new StringRequest(
                        api.getMethod(),
                        api.getUrl() + "?" + Joiner.on("&").withKeyValueSeparator("=").join(params),
                        responseListener,
                        errorListener);
            default:
                throw new UnsupportedOperationException("Unknown request type " + api.getMethod());
        }
    }

    static public JSONStringRequest registerRequest(
            final Context context, JSONObject params,
            Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        return new JSONStringRequest(
                UserAPI.REGISTER.getMethod(), UserAPI.REGISTER.getUrl(), params,
                responseListener, errorListener) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                new HachikoCookieManager(context).saveSessionCookie(response);
                return super.parseNetworkResponse(response);
            }
        };
    }
}
