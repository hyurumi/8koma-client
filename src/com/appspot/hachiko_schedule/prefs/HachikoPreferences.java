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

    public static String getPreferencesName() {
        return PREFERENCES_NAME;
    }

    public static final String KEY_IS_CALENDAR_SETUP = "is_calendar_setup";
    public static final boolean IS_CALENDAR_SETUP_DEFAULT = false;
    public static final String KEY_CALENDARS_TO_USE = "calendars_to_use";
    public static final Set<String> CALENDARS_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_CALENDARS_NOT_TO_USE = "calendars_not_to_use";
    public static final Set<String> CALENDARS_NOT_TO_USE_DEFAULT = Collections.emptySet();
    public static final String KEY_USE_FAKE_CONTACT = "use_fake_contact";
    public static final boolean USE_FAKE_CONTACT_DEFAULT = false;
}
