package com.appspot.hachiko_schedule.prefs;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.ImplicitLoginRequest;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.dev.SQLDumpActivity;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONObject;

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

        // TODO: カレンダーまわりの設定を復活させる #115関係
        setupDebugPrefs();
        setPreferenceScreen(screen);
    }

    private void setupDebugPrefs() {
        if (!Constants.IS_DEVELOPER) {
            return;
        }

        CheckBoxPreference useFakeHttpStack = new CheckBoxPreference(this);
        useFakeHttpStack.setTitle("ダミーの通信を利用");
        useFakeHttpStack.setSummary("FakeHttpRequestクラスを利用して偽の通信結果を返す");
        useFakeHttpStack.setDefaultValue(HachikoPreferences.USE_FAKE_REQUEST_QUEUE_DEFAULT);
        useFakeHttpStack.setKey(HachikoPreferences.KEY_USE_FAKE_REQUEST_QUEUE);

        Preference showDb = new Preference(this);
        showDb.setTitle("データベースの中身を確認");
        showDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MainPreferenceActivity.this, SQLDumpActivity.class);
                startActivity(intent);
                return true;
            }
        });

        Preference reauth = new Preference(this);
        reauth.setTitle("再認証");
        reauth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final ProgressDialog progressDialog = new ProgressDialog(MainPreferenceActivity.this);
                progressDialog.setMessage("リクエスト中");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                Request request = new ImplicitLoginRequest(MainPreferenceActivity.this,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject object) {
                                progressDialog.hide();
                                Toast.makeText(MainPreferenceActivity.this, "Success: " + object,
                                        Toast.LENGTH_LONG);
                                HachikoLogger.debug(object);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                HachikoDialogs.showNetworkErrorDialog(MainPreferenceActivity.this,
                                        volleyError, "login");
                                HachikoLogger.error("failed to login", volleyError);
                            }
                        });
                HachikoApp.defaultRequestQueue().add(request);
                return true;
            }
        });

        Preference deletePlans = new Preference(this);
        deletePlans.setTitle("予定一覧画面の予定を削除");
        deletePlans.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PlansTableHelper plansTableHelper = new PlansTableHelper(MainPreferenceActivity.this);
                plansTableHelper.debugDeletePlansAndCandidateDates();
                Toast.makeText(MainPreferenceActivity.this, "データが削除されました", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference superLongTimeout = new CheckBoxPreference(this);
        superLongTimeout.setKey(HachikoPreferences.KEY_USE_SUPER_LONG_LIFE_REQUEST);
        superLongTimeout.setTitle("通信タイムアウト時間を長く");

        Preference confirmVersion = new Preference(this);
        confirmVersion.setTitle("ビルド情報を確認");
        confirmVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String info = "HachikoID: " + HachikoPreferences
                        .getDefault(MainPreferenceActivity.this)
                        .getLong(HachikoPreferences.KEY_MY_HACHIKO_ID, -1) + "\n"
                        + "Build Date: " + getBuildTimeString() + "\n"
                        + "Latest commit: " + getCommitInfo() + "\n"
                        + "Gcm Info: "
                        + HachikoPreferences.getDefault(MainPreferenceActivity.this)
                        .getString(HachikoPreferences.KEY_GCM_REGISTRATION_ID, "Not found") + "\n"
                        + "Google Auth: "
                        + new GoogleAuthPreferences(MainPreferenceActivity.this).getAccountName();
                new AlertDialog.Builder(MainPreferenceActivity.this)
                        .setMessage(info)
                        .show();
                HachikoLogger.debug(info);
                return true;
            }
        });

        newPreferenceCategory(
                "デバッグ", useFakeHttpStack, showDb, reauth, deletePlans, superLongTimeout, confirmVersion);
    }

    private String getBuildTimeString() {
        try{
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            return new SimpleDateFormat("yyyy MM/dd HH:mm").format(new java.util.Date(time));
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
