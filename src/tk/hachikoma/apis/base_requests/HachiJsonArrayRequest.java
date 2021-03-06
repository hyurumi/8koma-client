package tk.hachikoma.apis.base_requests;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import tk.hachikoma.apis.HachikoCookieManager;
import tk.hachikoma.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HachiJsonArrayRequest extends JsonRequest<JSONArray> {

    private HachikoCookieManager cookieManager;

    public HachiJsonArrayRequest(
            Context context, int method, String url, JSONObject params,
            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        this(context, method, url, params == null ? null : params.toString(), listener, errorListener);
    }

    public HachiJsonArrayRequest(
            Context context, int method, String url, JSONArray params,
            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        this(context, method, url, params == null ? null : params.toString(), listener, errorListener);
    }

    public HachiJsonArrayRequest(
            Context context, int method, String url, String params,
            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, params, listener, errorListener);
        cookieManager = new HachikoCookieManager(context);

        HachikoLogger.debug("request body");
        HachikoLogger.debug(params == null ? null : params.toString());
    }

    public HachiJsonArrayRequest(
            Context context, int method, String url,
            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        this(context, method, url, (String) null, listener, errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONArray(jsonString),
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
