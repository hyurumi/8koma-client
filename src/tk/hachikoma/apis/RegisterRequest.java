package tk.hachikoma.apis;

import android.content.Context;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import tk.hachikoma.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 初回登録時に使われるリクエスト
 */
public class RegisterRequest extends JsonObjectRequest {
    private final HachikoCookieManager hachikoCookieManager;

    public interface ResponseListener {
        public void onResponse(long hachikoId, String pass);
    }

    public RegisterRequest(Context context, String authCode,
                           ResponseListener listener,
                           Response.ErrorListener errorListener) {
        super(HachikoAPI.User.REGISTER.getMethod(), HachikoAPI.User.REGISTER.getUrl(),
                JSONUtils.jsonObject("auth", authCode),
                wrapResponseListener(listener), errorListener);
        hachikoCookieManager = new HachikoCookieManager(context);
    }

    private static Response.Listener<JSONObject> wrapResponseListener(
            final ResponseListener listener) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                try {
                    listener.onResponse(res.getLong("id"), res.getString("pass"));
                } catch (JSONException e) {
                    throw new IllegalStateException("Illegal JSON Response " + res, e);
                }
            }
        };
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        hachikoCookieManager.saveSessionCookie(response);
        return super.parseNetworkResponse(response);
    }
}
