package com.appspot.hachiko_schedule.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Long> toList(JSONArray array) throws JSONException {
        List<Long> retList = new ArrayList<Long>(array.length());
        for (int i = 0; i < array.length(); i++) {
            retList.add(array.getLong(i));
        }
        return retList;
    }
}
