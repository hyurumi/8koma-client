package com.appspot.hachiko_schedule.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;

/**
 * アプリの初期設定用ヘルパークラス
 */
public class SetupManager {
    private final Context context;

    public SetupManager(Context context) {
        this.context = context;
    }

    /**
     * @return まだ処理されてない初期設定があれば，それに関連するIntentを返す．なければnull.
     */
    public Intent intentForRequiredSetupIfAny() {
        SharedPreferences prefs = HachikoPreferences.getDefault(context);
        GoogleAuthPreferences googleAuthPreferences = new GoogleAuthPreferences(context);
        String myHachikoId = prefs.getString(HachikoPreferences.KEY_MY_HACHIKO_ID, "");
        if (!googleAuthPreferences.isAuthSetuped() || myHachikoId.equals("")) {
            return new Intent(context, GoogleAuthActivity.class);
        } else if (!prefs.getBoolean(
                HachikoPreferences.KEY_IS_CALENDAR_SETUP,
                HachikoPreferences.IS_CALENDAR_SETUP_DEFAULT)) {
            return new Intent(context, SetupCalendarActivity.class);
        }

        return null;
    }
}
