package tk.hachikoma.apis.base_requests;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.apis.HachikoCookieManager;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * リクエストをJSONで送って，戻り値を生textでもらう
 */
public class JSONStringRequest extends JsonRequest<String> {
    private final HachikoCookieManager cookieManager;

    public JSONStringRequest(Context context, int method, String url, JSONObject jsonRequest,
                             Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest == null ? null : jsonRequest.toString(), listener, errorListener);
        cookieManager = new HachikoCookieManager(context);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (HachikoAPI.User.REGISTER.getUrl().equals(getUrl())) {
            return super.getHeaders();
        }
        return cookieManager.addSessionCookie(super.getHeaders());
    }
}
