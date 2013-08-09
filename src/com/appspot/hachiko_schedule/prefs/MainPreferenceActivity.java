package com.appspot.hachiko_schedule.prefs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;

import java.util.Set;

/**
 * メイン画面から飛べる設定
 */
public class MainPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = HachikoPreferences.getDefault(MainPreferenceActivity.this);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(HachikoPreferences.getPreferencesName());
        PreferenceScreen screen = preferenceManager.createPreferenceScreen(this);

        setupCalendarPrefs(screen, prefs);
        setupContactPrefs(screen, prefs);
        setPreferenceScreen(screen);
    }

    private PreferenceCategory setupCalendarPrefs(
            PreferenceScreen screen, SharedPreferences prefs) {
        final PreferenceCategory calendar = new PreferenceCategory(this);
        screen.addPreference(calendar);
        calendar.setTitle("カレンダー設定");

        Preference calendarsToUse = new Preference(this);
        calendarsToUse.setTitle(R.string.prefs_set_calendars);
        calendarsToUse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showCalendarChooseDialog();
                return false;
            }
        });
        calendar.addPreference(calendarsToUse);

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
                    calendar.removePreference(resetCalendarSetup);
                    return false;
                }
            });
            calendar.addPreference(resetCalendarSetup);
        }
        return calendar;
    }

    private PreferenceCategory setupContactPrefs(
            PreferenceScreen screen, SharedPreferences prefs) {
        if (Constants.IS_DEVELOPER) {
            final PreferenceCategory contacts = new PreferenceCategory(this);
            screen.addPreference(contacts);
            contacts.setTitle("コンタクト設定");
            CheckBoxPreference useDummyContact = new CheckBoxPreference(this);
            useDummyContact.setTitle("ダミーの電話帳データを使う");
            useDummyContact.setKey(HachikoPreferences.KEY_USE_FAKE_CONTACT);
            contacts.addPreference(useDummyContact);
            return contacts;
        }
        return null;
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
