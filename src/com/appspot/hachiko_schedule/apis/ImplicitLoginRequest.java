package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Hachikoサーバが自動で生成したパスワードを使って，暗黙のうちになされるログイン
 */
public class ImplicitLoginRequest extends JsonObjectRequest {
    private final HachikoCookieManager hachikoCookieManager;

    public ImplicitLoginRequest(Context context,
                           Response.Listener<JSONObject> listener,
                           Response.ErrorListener errorListener) {
        super(HachikoAPI.User.IMPLICIT_LOGIN.getMethod(), HachikoAPI.User.IMPLICIT_LOGIN.getUrl(),
                constructParam(context),
                listener, errorListener);
        hachikoCookieManager = new HachikoCookieManager(context);
    }

    private static JSONObject constructParam(Context context) {
        SharedPreferences pref = HachikoPreferences.getDefault(context);
        long hachikoId = pref.getLong(HachikoPreferences.KEY_MY_HACHIKO_ID, -1);
        String pass = pref.getString(HachikoPreferences.KEY_HACHIKO_INTERNAL_PASSWORD, "");
        JSONObject json = new JSONObject();
        try {
            json.put("id", hachikoId);
            json.put("pass", pass);
        } catch (JSONException e) {
            HachikoLogger.error("invalid json", e);
        }
        return json;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        hachikoCookieManager.saveSessionCookie(response);
        return super.parseNetworkResponse(response);
    }
}
