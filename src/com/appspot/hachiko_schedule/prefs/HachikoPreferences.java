package com.appspot.hachiko_schedule.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

/**
 * {@link SharedPreferences}のラッパと，keyに使う定数たち
 */
public class HachikoPreferences {
    protected static final String PREFERENCES_PREFIX = "Hachiko";
    private static final String PREFERENCES_NAME = "Hachiko";

    public static SharedPreferences getDefault(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getDefaultEditor(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
    }

    public static boolean getBooleanFromDefaultPref(
            Context context, String key, boolean defaultValue) {
        return getDefault(context).getBoolean(key, defaultValue);
    }

    public static String getPreferencesName() {
        return PREFERENCES_NAME;
    }

    public static boolean hasHachikoId(Context context) {
        return getDefault(context).contains(HachikoPreferences.KEY_MY_HACHIKO_ID);
    }

    /**
     * @return 自身のHachikoId
     * @throws IllegalStateException HachikoIDが存在しないとき
     */
    public static long getMyHachikoId(Context context) {
        long ret =  getDefault(context).getLong(HachikoPreferences.KEY_MY_HACHIKO_ID, -1);
        if (ret == -1) {
            throw new IllegalStateException("Hachiko ID is not found on your device");
        }
        return ret;
    }

    public static final String KEY_APP_VERSION = "app_version";
    public static final int APP_VERSION_DEFAULT = 0;
    public static final String KEY_IS_CALENDAR_SETUP = "is_calendar_setup";
    public static final boolean IS_CALENDAR_SETUP_DEFAULT = false;
    public static final String KEY_IS_LOCAL_USER_TABLE_SETUP = "is_local_user_table_setup";
    public static final boolean IS_LOCAL_USER_TABLE_SETUP_DEFAULT = false;
    public static final String KEY_CALENDARS_TO_USE = "calendars_to_use";
    public static final Set<String> CALENDARS_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_CALENDARS_NOT_TO_USE = "calendars_not_to_use";
    public static final Set<String> CALENDARS_NOT_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_SESSION_KEY = "session_key";
    public static final String KEY_SESSION_EXPIRES_MILLIS = "session_expires";
    public static final String KEY_MY_HACHIKO_ID = "my_hachiko_id";
    public static final String KEY_HACHIKO_INTERNAL_PASSWORD = "hachiko_internal_password";

    public static final String KEY_TIMERANGE_ASA = "timerange_asa";
    public static final String DEFAULT_TIMERANGE_ASA = "05:00-09:00";
    public static final String KEY_TIMERANGE_HIRU = "timerange_hiru";
    public static final String DEFAULT_TIMERANGE_HIRU = "11:00-15:00";
    public static final String KEY_TIMERANGE_YU = "timerange_yu";
    public static final String DEFAULT_TIMERANGE_YU = "15:00-19:00";
    public static final String KEY_TIMERANGE_YORU = "timerange_yoru";
    public static final String DEFAULT_TIMERANGE_YORU = "18:00-23:59";

    // Debugまわり
    public static final String KEY_USE_SUPER_LONG_LIFE_REQUEST = "use_super_long_life_request";
    public static final boolean USE_SUPER_LONG_LIFE_REQUEST = false;
    public static final String KEY_USE_FAKE_REQUEST_QUEUE = "use_fake_request_queue";
    public static final boolean USE_FAKE_REQUEST_QUEUE_DEFAULT = false;

    public static final String KEY_GCM_REGISTRATION_ID = "gcm_registration_id";
    public static final String GCM_REGISTRATION_ID_DEFAULT = "";
}
