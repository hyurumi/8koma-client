package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appspot.hachiko_schedule.util.JSONUtils;
import org.json.JSONObject;

/**
 * 初回登録時に使われるリクエスト
 */
public class RegisterRequest extends JsonObjectRequest {
    private final HachikoCookieManager hachikoCookieManager;

    public RegisterRequest(Context context, String authCode,
                           Response.Listener<JSONObject> listener,
                           Response.ErrorListener errorListener) {
        super(UserAPI.REGISTER.getMethod(), UserAPI.REGISTER.getUrl(),
                JSONUtils.jsonObject("auth", authCode),
                listener, errorListener);
        hachikoCookieManager = new HachikoCookieManager(context);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        hachikoCookieManager.saveSessionCookie(response);
        return super.parseNetworkResponse(response);
    }
}
