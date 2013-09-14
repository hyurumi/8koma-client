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
}
