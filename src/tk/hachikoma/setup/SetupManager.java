package tk.hachikoma.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import tk.hachikoma.prefs.GoogleAuthPreferences;
import tk.hachikoma.prefs.HachikoPreferences;

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
        Intent intent = null;
        if (!googleAuthPreferences.isAuthSetuped() || !HachikoPreferences.hasHachikoId(context)) {
            intent = new Intent(context, GoogleAuthActivity.class);
        } else if (!prefs.getBoolean(HachikoPreferences.KEY_IS_LOCAL_USER_TABLE_SETUP, false)) {
            intent = new Intent(context, SetupUserTableActivity.class);
        }

// TODO: uncomment out this: #115
//        else if (!prefs.getBoolean(
//                HachikoPreferences.KEY_IS_CALENDAR_SETUP,
//                HachikoPreferences.IS_CALENDAR_SETUP_DEFAULT)) {
//            return new Intent(context, SetupCalendarActivity.class);
//        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        return intent;
    }
}
