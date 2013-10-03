package com.appspot.hachiko_schedule.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link org.json.JSONObject} とか {@link org.json.JSONArray} とかまわりの便利メソッド
 */
public class JSONUtils {
    public static JSONObject putOrIgnoreNull(JSONObject obj, String key, Object value)
            throws JSONException {
        if (key != null && value != null) {
            obj.put(key, value);
        }
        return obj;
    }

    public static JSONObject jsonObject(String... keyAndvalues) {
        if (keyAndvalues.length % 2 != 0) {
            throw new IllegalStateException("偶数個のパラメータが渡されるべき");
        }

        JSONObject object = new JSONObject();
        for (int i = 0; i + 1 < keyAndvalues.length; i+=2) {
            try {
                object.put(keyAndvalues[i], keyAndvalues[i + 1]);
            } catch (JSONException e) {
                throw new IllegalStateException("" + i, e);
            }
        }
        return object;
    }
}
