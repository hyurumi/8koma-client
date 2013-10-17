package com.appspot.hachiko_schedule.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class GoogleAuthPreferences {
    private static final String ACCOUNT_NAME = "account_name";
    private static final String KEY_AUTH_CODE = "authCode";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences preferences;

    public GoogleAuthPreferences(Context context) {
        preferences = context.getSharedPreferences(
                HachikoPreferences.PREFERENCES_PREFIX + "auth",
                Context.MODE_PRIVATE);
    }

    public void setAccountName(String name) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCOUNT_NAME, name);
        editor.commit();
    }

    public void setAuthCode(String authCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_AUTH_CODE, authCode);
        editor.commit();
    }

    public String getAccountName() {
        return preferences.getString(ACCOUNT_NAME, null);
    }

    public String getAuthCode() {
        return preferences.getString(KEY_AUTH_CODE, null);
    }

    public boolean isAuthSetuped() {
        return getAccountName() != null && getAuthCode() != null;
    }
}
