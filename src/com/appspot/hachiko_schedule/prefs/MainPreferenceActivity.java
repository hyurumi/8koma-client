package com.appspot.hachiko_schedule.prefs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;

/**
 * メイン画面から飛べる設定
 */
public class MainPreferenceActivity extends PreferenceActivity {
    private SharedPreferences prefs;
    private PreferenceScreen screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = HachikoPreferences.getDefault(MainPreferenceActivity.this);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(HachikoPreferences.getPreferencesName());
        screen = preferenceManager.createPreferenceScreen(this);

        setupCalendarPrefs();
        setupContactPrefs();
        setupNetworkPrefs();
        setupDebugPrefs();
        setPreferenceScreen(screen);
    }

    private void setupCalendarPrefs() {
        Preference calendarsToUse = new Preference(this);
        calendarsToUse.setTitle(R.string.prefs_set_calendars);
        calendarsToUse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showCalendarChooseDialog();
                return false;
            }
        });
        final PreferenceCategory calendarPrefs = newPreferenceCategory("カレンダー設定", calendarsToUse);

        if (Constants.IS_DEVELOPER && prefs.getBoolean(
                HachikoPreferences.KEY_IS_CALENDAR_SETUP, HachikoPreferences.IS_CALENDAR_SETUP_DEFAULT)) {
            final Preference resetCalendarSetup = new Preference(this);
            resetCalendarSetup.setTitle("カレンダー設定をリセット (次回起動時に再設定を行います)");
            resetCalendarSetup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    HachikoPreferences.getDefaultEditor(MainPreferenceActivity.this)
                            .putStringSet(HachikoPreferences.KEY_CALENDARS_TO_USE,
                                    HachikoPreferences.CALENDARS_TO_USE_DEFAULT)
                            .putStringSet(HachikoPreferences.KEY_CALENDARS_NOT_TO_USE,
                                    HachikoPreferences.CALENDARS_NOT_TO_USE_DEFAULT)
                            .putBoolean(HachikoPreferences.KEY_IS_CALENDAR_SETUP, false)
                            .commit();

                    Toast.makeText(
                            MainPreferenceActivity.this,
                            "OK, アプリを再起動してください",
                            Toast.LENGTH_SHORT).show();
                    calendarPrefs.removePreference(resetCalendarSetup);
                    return false;
                }
            });
            calendarPrefs.addPreference(resetCalendarSetup);
        }
    }

    private void setupContactPrefs() {
        CheckBoxPreference useFbContact = new CheckBoxPreference(this);
        useFbContact.setTitle("Facebookの連絡帳データを利用する");
        useFbContact.setSummary("チェックされてない場合は端末の電話帳データを利用");
        useFbContact.setDefaultValue(HachikoPreferences.USE_FAKE_CONTACT_DEFAULT);
        useFbContact.setKey(HachikoPreferences.KEY_USE_FB_CONTACT);
        PreferenceCategory contactPrefs = newPreferenceCategory("コンタクト設定", useFbContact);

        if (Constants.IS_DEVELOPER) {
            CheckBoxPreference useDummyContact = new CheckBoxPreference(this);
            useDummyContact.setTitle("ダミーの電話帳データを使う");
            useDummyContact.setKey(HachikoPreferences.KEY_USE_FAKE_CONTACT);
            contactPrefs.addPreference(useDummyContact);
        }
    }

    private void setupNetworkPrefs() {
        if (!Constants.IS_DEVELOPER) {
            return;
        }

        CheckBoxPreference useFakeHttpStack = new CheckBoxPreference(this);
        useFakeHttpStack.setTitle("ダミーの通信を利用");
        useFakeHttpStack.setSummary("FakeHttpRequestクラスを利用して偽の通信結果を返す");
        useFakeHttpStack.setDefaultValue(HachikoPreferences.USE_FAKE_REQUEST_QUEUE_DEFAULT);
        useFakeHttpStack.setKey(HachikoPreferences.KEY_USE_FAKE_REQUEST_QUEUE);
        newPreferenceCategory("ネットワーク", useFakeHttpStack);
    }

    private void setupDebugPrefs() {
        if (!Constants.IS_DEVELOPER) {
            return;
        }

        Preference restartHachiko = new Preference(this);
        restartHachiko.setTitle("Hachikoを再起動");
        restartHachiko.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
        });
        newPreferenceCategory("デバッグ", restartHachiko);
    }

    private PreferenceCategory newPreferenceCategory(String title, Preference... preferences) {
        PreferenceCategory category = new PreferenceCategory(this);
        screen.addPreference(category);
        category.setTitle(title);
        for (Preference preference: preferences) {
            category.addPreference(preference);
        }
        return category;
    }

    private void showCalendarChooseDialog() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            transaction.remove(prev);
        }
        transaction.addToBackStack(null);

        DialogFragment dialog = new ChooseCalendarDialog();
        dialog.show(transaction, "dialog");
    }
}
