package com.appspot.hachiko_schedule.prefs;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        if (Constants.IS_DEVELOPER) {
            CheckBoxPreference useDummyContact = new CheckBoxPreference(this);
            useDummyContact.setTitle("ダミーの電話帳データを使う");
            useDummyContact.setKey(HachikoPreferences.KEY_USE_FAKE_CONTACT);
            newPreferenceCategory("コンタクト設定", useDummyContact);
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

        Preference confirmVersion = new Preference(this);
        confirmVersion.setTitle("ビルド情報を確認");
        confirmVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(MainPreferenceActivity.this)
                        .setMessage("Build Date: " + getBuildTimeString() + "\n"
                                + "Latest commit: " + getCommitInfo()
                                + "Gcm Info:"
                                + HachikoPreferences.getDefault(MainPreferenceActivity.this)
                                .getString(HachikoPreferences.KEY_GCM_REGISTRATION_ID, "Not found"))
                        .show();
                return true;
            }
        });
        PreferenceCategory category = newPreferenceCategory("デバッグ");
        category.addPreference(restartHachiko);
        category.addPreference(confirmVersion);
    }

    private String getBuildTimeString() {
        try{
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            return new SimpleDateFormat("yyyy mm/dd HH:mm").format(new java.util.Date(time));
        }catch(Exception e){
            return "unknown";
        }
    }

    private String getCommitInfo() {
        Properties prop = new Properties();
        String fileName = "commit.properties";
        try {
            InputStream fileStream = getAssets().open(fileName);
            prop.load(fileStream);
            fileStream.close();
        } catch (FileNotFoundException e) {
            return "(現状自動ビルド時のみ対応)";
        } catch (IOException e) {
            return "(取得に失敗)";
        }
        HachikoLogger.debug(prop);
        HachikoLogger.debug(prop.stringPropertyNames());
        return prop.getProperty("commit");
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
