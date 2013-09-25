package com.appspot.hachiko_schedule.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

/**
 * {@link SharedPreferences}のラッパと，keyに使う定数たち
 */
public class HachikoPreferences {
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

    public static final String KEY_APP_VERSION = "app_version";
    public static final int APP_VERSION_DEFAULT = 0;
    public static final String KEY_IS_CALENDAR_SETUP = "is_calendar_setup";
    public static final boolean IS_CALENDAR_SETUP_DEFAULT = false;
    public static final String KEY_CALENDARS_TO_USE = "calendars_to_use";
    public static final Set<String> CALENDARS_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_CALENDARS_NOT_TO_USE = "calendars_not_to_use";
    public static final Set<String> CALENDARS_NOT_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_MY_HACHIKO_ID = "my_hachiko_id";
    public static final String KEY_USE_FB_CONTACT = "use_fb_contact";
    public static final boolean USE_FB_CONTACT_DEFAULT = true;
    public static final String KEY_USE_FAKE_CONTACT = "use_fake_contact";
    public static final boolean USE_FAKE_CONTACT_DEFAULT = false;
    public static final String KEY_USE_FAKE_REQUEST_QUEUE = "use_fake_request_queue";
    public static final boolean USE_FAKE_REQUEST_QUEUE_DEFAULT = true;

    public static final String KEY_GCM_REGISTRATION_ID = "gcm_registration_id";
    public static final String GCM_REGISTRATION_ID_DEFAULT = "";

    /**
     * 一度でもFBの認証をしたかどうか．実際のログイン状態とは必ずしも一致しない．
     */
    public static final String KEY_FB_LOGGED_IN = "fb_logged_in";
    public static final boolean FB_LOGGED_IN_DEFAULT = false;
}
