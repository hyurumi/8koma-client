package com.appspot.hachiko_schedule.push;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.UserAPI;
import com.appspot.hachiko_schedule.apis.VolleyRequestFactory;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;

public class GoogleCloudMessagingHelper {
    private final Activity activity;
    // TODO: 実環境に合わせてここを更新
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String SENDER_ID = "715637255810";

    public GoogleCloudMessagingHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        // http://developer.android.com/google/gcm/client.html からコピー
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil
                        .getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                HachikoLogger.error("Google Play Service is neither available nor recoverable");
                new AlertDialog.Builder(activity)
                        .setMessage("お使いの端末では一部機能がご利用できません．お手数ですが，Hachiko開発チームまで問い合わせをお願いします．")
                        .show();
            }
            return false;
        }
        return true;
    }

    public String getRegistrationId() {
        SharedPreferences pref = HachikoPreferences.getDefault(activity);
        String registrationId = pref.getString(
                HachikoPreferences.KEY_GCM_REGISTRATION_ID,
                HachikoPreferences.GCM_REGISTRATION_ID_DEFAULT);
        if (registrationId.isEmpty()) {
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID since the existing
        // regID is not guaranteed to work with the new app version.
        int registeredVersion = HachikoApp.getAppVersionCode();
        int currentVersion = pref.getInt(
                HachikoPreferences.KEY_APP_VERSION, HachikoPreferences.APP_VERSION_DEFAULT);
        if (registeredVersion != currentVersion) {
            HachikoLogger.info(
                    "Update from ", registeredVersion, " to ", currentVersion, " is detected.");
            return "";
        }
        return registrationId;
    }

    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    GoogleCloudMessaging googleCloudMessaging
                            = GoogleCloudMessaging.getInstance(activity);
                    String registrationId = googleCloudMessaging.register(SENDER_ID);
                    result = "device registered to GCM and ID is " + registrationId;
                    sendRegistrationIdToServer(registrationId);
                    storeRegistrationId(registrationId);
                } catch (IOException e) {
                    result = "Error :" + e.getMessage();
                    HachikoLogger.error("Fail to register GCM", e);
                }
                return result;
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToServer(String gcmRegistrationId) {
        HachikoLogger.debug(gcmRegistrationId);
        Map<String, String> params = ImmutableMap.of("registration_id", gcmRegistrationId);
        StringRequest request = VolleyRequestFactory.newStringRequest(
                UserAPI.REGISTER_GCM_ID,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("Registration ID successfully sent to server");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("GCM registration ID send error", volleyError);
                    }
                }
        );
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void storeRegistrationId(String registrationID) {
        SharedPreferences.Editor editor = HachikoPreferences.getDefaultEditor(activity);
        int appVersion = HachikoApp.getAppVersionCode();
        HachikoLogger.info("Register GCM and save registration ID on app version ", appVersion);
        editor.putInt(HachikoPreferences.KEY_APP_VERSION, appVersion)
                .putString(HachikoPreferences.KEY_GCM_REGISTRATION_ID, registrationID)
                .commit();
    }
}
