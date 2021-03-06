package tk.hachikoma;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.volley.RequestQueue;
import tk.hachikoma.apis.HachiRequestQueue;
import tk.hachikoma.dev.FakeRequestQueue;
import tk.hachikoma.prefs.HachikoPreferences;
import com.deploygate.sdk.DeployGate;
import tk.hachikoma.util.IntegerUtils;

/**
 * アプリケーション全体で利用する管理するクラスなどを管理する
 */
public class HachikoApp extends Application {
    private static Context appContext;
    private static RequestQueue requestQueue;
    private static FakeRequestQueue fakeRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        requestQueue = new HachiRequestQueue(appContext);
        requestQueue.start();

        initPrefs();
        DeployGate.install(this);
    }

    public static RequestQueue defaultRequestQueue() {
        if (HachikoPreferences.getBooleanFromDefaultPref(
                appContext,
                HachikoPreferences.KEY_USE_FAKE_REQUEST_QUEUE,
                HachikoPreferences.USE_FAKE_REQUEST_QUEUE_DEFAULT)) {
            if (fakeRequestQueue == null) {
                fakeRequestQueue = new FakeRequestQueue(appContext);
            }
            return fakeRequestQueue;
        }
        return requestQueue;
    }

    /**
     * @return own app version as registered obtained by {@link PackageManager}.
     */
    public static int getAppVersionCode() {
        try {
            PackageInfo packageInfo = appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void initPrefs() {
        SharedPreferences.Editor preferencesEditor = HachikoPreferences.getDefaultEditor(appContext);
        int numOfCandidateDates = IntegerUtils.parseIntWithDefault(
                HachikoPreferences.getDefault(appContext).getString(
                        HachikoPreferences.KEY_NUMBER_OF_CANDIDATE_DATES,
                        Integer.toString(HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_DEFAULT)),
                HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_DEFAULT);
        if (numOfCandidateDates < 0 || HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_MAX < numOfCandidateDates) {
            preferencesEditor.putString(
                    HachikoPreferences.KEY_NUMBER_OF_CANDIDATE_DATES,
                    Integer.toString(HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_DEFAULT));
        }
        preferencesEditor.commit();
    }
}
