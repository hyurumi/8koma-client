package com.appspot.hachiko_schedule.prefs;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
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
public class MainPreferenceActivity extends Activity {
    private static SettingsFragment fragment;
    private static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();


        // TODO: カレンダーまわりの設定を復活させる #115関係
    }

    private static void setDebugPrefs() {

        PreferenceCategory category = new PreferenceCategory(fragment.getActivity());
        category.setTitle("デバッグ");
        fragment.getPreferenceScreen().addPreference(category);

        CheckBoxPreference useFakeHttpStack = new CheckBoxPreference(fragment.getActivity());
        useFakeHttpStack.setTitle("ダミーの通信を利用");
        useFakeHttpStack.setSummary("FakeHttpRequestクラスを利用して偽の通信結果を返す");
        useFakeHttpStack.setDefaultValue(HachikoPreferences.USE_FAKE_REQUEST_QUEUE_DEFAULT);
        useFakeHttpStack.setKey(HachikoPreferences.KEY_USE_FAKE_REQUEST_QUEUE);
        category.addPreference(useFakeHttpStack);

        Preference showDb = new Preference(fragment.getActivity());
        showDb.setTitle("データベースの中身を確認");
        showDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(fragment.getActivity(), SQLDumpActivity.class);
                fragment.getActivity().startActivity(intent);
                return true;
            }
        });
        category.addPreference(showDb);

        Preference deletePlans = new Preference(fragment.getActivity());
        deletePlans.setTitle("予定一覧画面の予定を削除");
        deletePlans.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PlansTableHelper plansTableHelper = new PlansTableHelper(fragment.getActivity());
                plansTableHelper.debugDeletePlansAndCandidateDates();
                Toast.makeText(fragment.getActivity(), "データが削除されました", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        category.addPreference(deletePlans);

        Preference superLongTimeout = new CheckBoxPreference(fragment.getActivity());
        superLongTimeout.setKey(HachikoPreferences.KEY_USE_SUPER_LONG_LIFE_REQUEST);
        superLongTimeout.setTitle("通信タイムアウト時間を長く");
        category.addPreference(superLongTimeout);
    }

    private static Preference reauthPreference() {
        Preference reauth = new Preference(fragment.getActivity());
        reauth.setTitle("再認証");
        reauth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final ProgressDialog progressDialog = new ProgressDialog(fragment.getActivity());
                progressDialog.setMessage("リクエスト中");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                Request request = new ImplicitLoginRequest(fragment.getActivity(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject object) {
                                progressDialog.hide();
                                Toast.makeText(fragment.getActivity(), "Success: " + object,
                                        Toast.LENGTH_LONG);
                                HachikoLogger.debug(object);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                HachikoDialogs.showNetworkErrorDialog(fragment.getActivity(),
                                        volleyError, "login");
                                HachikoLogger.error("failed to login", volleyError);
                            }
                        });
                HachikoApp.defaultRequestQueue().add(request);
                return true;
            }
        });
        return reauth;
    }

    private static void confirmVersionPreference() {
        Preference confirmVersion = fragment.findPreference("confirm_version");
        confirmVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String gcmInfo = "Gcm Info: "
                        + HachikoPreferences.getDefault(fragment.getActivity())
                        .getString(HachikoPreferences.KEY_GCM_REGISTRATION_ID, "Not found") + "\n";
                String info = "HachikoID: " + HachikoPreferences
                        .getDefault(fragment.getActivity())
                        .getLong(HachikoPreferences.KEY_MY_HACHIKO_ID, -1) + "\n"
                        + "Build Date: " + getBuildTimeString() + "\n"
                        + "Latest commit: " + getCommitInfo() + "\n"
                        + (Constants.IS_DEVELOPER ? gcmInfo : "")
                        + "Google Auth: "
                        + new GoogleAuthPreferences(fragment.getActivity()).getAccountName();
                new AlertDialog.Builder(fragment.getActivity())
                        .setMessage(info)
                        .show();
                HachikoLogger.debug(info);
                return true;
            }
        });
    }

    private static void sendFeedbackPreference() {
        Preference sendFeedback = fragment.getPreferenceScreen().findPreference("send_feedback");
        sendFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"app8koma+support@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "ハチコマフィードバック");
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n"
                        + "-----------------------\n"
                        + "Build ID: " + getCommitInfo() + "\n"
                        + "端末情報: " + Build.PRODUCT + "(" + Build.VERSION.RELEASE + ")\n"
                        + "UserID: "
                        + HachikoPreferences.getMyHachikoId(fragment.getActivity()) + "\n");
                HachikoLogger.debug("Feedback request from "
                        + HachikoPreferences.getMyHachikoId(fragment.getActivity())
                        + "\nBuild ID: " + getCommitInfo() + "\n");
                fragment.getActivity().startActivity(intent);
                return true;
            }
        });
    }

    private static void timerangePreference(){
        prefs = HachikoPreferences.getDefault(fragment.getActivity());
        fragment.getPreferenceScreen().findPreference(HachikoPreferences.KEY_TIMERANGE_ASA).setSummary(
                prefs.getString(HachikoPreferences.KEY_TIMERANGE_ASA, HachikoPreferences.DEFAULT_TIMERANGE_ASA));
        fragment.getPreferenceScreen().findPreference(HachikoPreferences.KEY_TIMERANGE_HIRU).setSummary(
                prefs.getString(HachikoPreferences.KEY_TIMERANGE_HIRU,HachikoPreferences.DEFAULT_TIMERANGE_HIRU));
        fragment.getPreferenceScreen().findPreference(HachikoPreferences.KEY_TIMERANGE_YU).setSummary(
                prefs.getString(HachikoPreferences.KEY_TIMERANGE_YU,HachikoPreferences.DEFAULT_TIMERANGE_YU));
        fragment.getPreferenceScreen().findPreference(HachikoPreferences.KEY_TIMERANGE_YORU).setSummary(
                prefs.getString(HachikoPreferences.KEY_TIMERANGE_YORU,HachikoPreferences.DEFAULT_TIMERANGE_YORU));
    }

    private static String getBuildTimeString() {
        try{
            ApplicationInfo ai = fragment.getActivity().getPackageManager().getApplicationInfo(fragment.getActivity().getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            return new SimpleDateFormat("yyyy MM/dd HH:mm").format(new java.util.Date(time));
        }catch(Exception e){
            return "unknown";
        }
    }

    private static String getCommitInfo() {
        Properties prop = new Properties();
        String fileName = "commit.properties";
        try {
            InputStream fileStream = fragment.getActivity().getApplicationContext().getAssets().open(fileName);
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

    public static class SettingsFragment extends PreferenceFragment{



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(HachikoPreferences.getPreferencesName());
            addPreferencesFromResource(R.xml.preferences);

            PreferenceScreen screen = this.getPreferenceScreen();

            // Load the preferences from an XML resource
            if (Constants.IS_ALPHA_USER) {
                ((PreferenceCategory)(screen.findPreference("category_others"))).addPreference(reauthPreference());
            }


            if (Constants.IS_DEVELOPER) {
                setDebugPrefs();
            }

            sendFeedbackPreference();
            confirmVersionPreference();
            timerangePreference();



        }



    }
}
