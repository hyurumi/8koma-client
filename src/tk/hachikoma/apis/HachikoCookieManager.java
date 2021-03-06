package tk.hachikoma.apis;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.NetworkResponse;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.util.DateUtils;
import tk.hachikoma.util.HachikoLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HachikoCookieManager {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_KEY = ".ASPXAUTH";
    private final Context context;

    public HachikoCookieManager(Context context) {
        this.context = context;
    }

    /**
     * @return レスポンスに含まれるCookieを取得し保存できたかどうか
     */
    public boolean saveSessionCookie(NetworkResponse networkResponse) {
        Map<String, String> headers = networkResponse.headers;
        HachikoLogger.debugDeloperOnly(headers);
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_KEY)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            HachikoLogger.debugDeloperOnly(cookie);
            String[] splitCookie = cookie.split(";");
            String[] splitSessionId = splitCookie[0].split("=");
            SharedPreferences.Editor prefEditor
                    = HachikoPreferences.getDefaultEditor(context);
            prefEditor.putString(
                    HachikoPreferences.KEY_SESSION_KEY, splitSessionId[1]);
            HachikoLogger.debugDeloperOnly(splitSessionId[1]);
            if (cookie.contains("expires=")) {
                String expires = cookie.split("expires=")[1].split(";", 2)[0];
                prefEditor.putLong(HachikoPreferences.KEY_SESSION_EXPIRES_MILLIS,
                        DateUtils.parseFullDate(expires).getTime());
                HachikoLogger.debug("cookie stored");
                HachikoLogger.debug(expires);
            }
            prefEditor.commit();
            return true;
        }
        return false;
    }

    public void invalidateSessionCookie() {
        HachikoPreferences.getDefaultEditor(context)
                .remove(HachikoPreferences.KEY_SESSION_KEY)
                .remove(HachikoPreferences.KEY_SESSION_EXPIRES_MILLIS)
                .commit();
    }

    /**
     * @return セッションIDを付与して返す
     */
    public Map<String, String> addSessionCookie(Map<String, String> headers) {
        String sessionId = HachikoPreferences.getDefault(context)
                .getString(HachikoPreferences.KEY_SESSION_KEY, "");
        if (sessionId.length() == 0) {
            HachikoLogger.error("Session isn't available");
            return headers;
        }
        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(SESSION_KEY);
        builder.append("=");
        builder.append(sessionId);
        if (headers.containsKey(COOKIE_KEY)) {
            builder.append("; ");
            builder.append(headers.get(COOKIE_KEY));
        }
        headers.put(COOKIE_KEY, builder.toString());
        return headers;
    }
}
