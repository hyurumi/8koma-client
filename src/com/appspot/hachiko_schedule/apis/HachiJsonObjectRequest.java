package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HachiJsonObjectRequest extends JsonRequest<JSONObject> {

    private HachikoCookieManager cookieManager;

    public HachiJsonObjectRequest(
            Context context, String url,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(context, Method.GET, url, null, listener, errorListener);
    }

    public HachiJsonObjectRequest(
            Context context, int method, String url, JSONObject params,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, params == null ? null : params.toString(), listener, errorListener);
        cookieManager = new HachikoCookieManager(context);

        HachikoLogger.debug("request", url);
        HachikoLogger.debug(params);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return cookieManager.addSessionCookie(super.getHeaders());
    }
}
