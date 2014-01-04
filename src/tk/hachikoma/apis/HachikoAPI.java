package tk.hachikoma.apis;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import tk.hachikoma.Constants;

/**
 * Class that represents information about certain API.
 */
public class HachikoAPI {
    public static final String BASE = "https://"
            + (Constants.IS_DEVELOPER ? "8koma.yutopio.net": "8koma.cloudapp.net")
            + "/api/";
    public static final RetryPolicy RETRY_POLICY_LONG
            = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 4,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public static final int TAG_VACANCY_REQUEST = 1000;

    private final int method;
    private final String subUrl;

    protected HachikoAPI(int method, String subUrl) {
        this.method = method;
        this.subUrl = subUrl;
    }

    public int getMethod() {
        return method;
    }

    public String getUrl() {
        return BASE + subUrl;
    }

    public static String getUrl(String path) {
        return BASE + path;
    }

    public static class Friend {
        public static final HachikoAPI ADD_FRIENDS = new HachikoAPI(Request.Method.POST, "friends");
    }

    public static class Plan {
        public static final HachikoAPI CONFIRM = new HachikoAPI(Request.Method.POST, "confirm/");
        public static final HachikoAPI GET_PLANS = new HachikoAPI(Request.Method.GET, "plans");
        public static final HachikoAPI RESPOND = new HachikoAPI(Request.Method.POST, "respond");
        public static final HachikoAPI DEMAND = new HachikoAPI(Request.Method.POST, "demand/");
        public static final String shareUrl(String token) {
            return BASE.replace("/api/", "/") + "respond/" + token;
        };
    }

    public static class User {
        public static final HachikoAPI REGISTER = new HachikoAPI(Request.Method.POST, "user");
        public static final HachikoAPI IMPLICIT_LOGIN = new HachikoAPI(Request.Method.POST, "user");
        public static final HachikoAPI REGISTER_GCM_ID = new HachikoAPI(Request.Method.POST, "gcminfo");
        public static final HachikoAPI GET_NAMES = new HachikoAPI(Request.Method.GET, "users");
    }
}
