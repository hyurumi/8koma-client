package com.appspot.hachiko_schedule.setup;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.VolleyRequestFactory;
import com.appspot.hachiko_schedule.friends.NewEventChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.JSONUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import org.json.JSONObject;

import java.io.IOException;

public class GoogleAuthActivity extends Activity {
    private static final int CHOOSE_ACCOUNT_RESULT_CODE = 1001;
    private static final int REQUEST_AUTH_RESULT_CODE = 1002;
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String OWN_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String EMAIL_SCOPE = "https://mail.google.com/";
    private static final String SCOPE = "oauth2:" + PROFILE_SCOPE + " " + OWN_EMAIL_SCOPE + " "
            + CALENDAR_SCOPE + " " + EMAIL_SCOPE;
    private static final String COM_GOOGLE = "com.google";

    private GoogleAuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPreferences  = new GoogleAuthPreferences(this);
        if (authPreferences.isAuthSetuped()) {
            transitToNextActivity();
        } else {
            HachikoLogger.debug("choose account");
            chooseAccount();
        }
    }

    private void transitToNextActivity() {
        Intent intent = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intent == null) {
            intent = new Intent(this, NewEventChooseGuestActivity.class);
        }
        startActivity(intent);
    }

    private void chooseAccount() {
        // TODO: investigate https://github.com/frakbot/Android-AccountChooser for compatibility
        // with older devices
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[] { COM_GOOGLE }, false, null, null, null, null);
        HachikoLogger.debug("Choose account");
        startActivityForResult(intent, CHOOSE_ACCOUNT_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_ACCOUNT_RESULT_CODE:
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    authPreferences.setAccountName(accountName);

                    // invalidate old tokens which might be cached. we want a fresh
                    // one, which is guaranteed to work
                    invalidateToken();
                    requestToken();
                    break;
                case REQUEST_AUTH_RESULT_CODE:
                    requestToken();
                    break;
                default:
                    HachikoLogger.error("on result no action" + requestCode);
                    return;
            }
        } else {
            HachikoLogger.error("error result " +  requestCode + ", resultCode:" + requestCode);
        }
    }

    private void requestToken() {
        HachikoLogger.debug("Request token as " + authPreferences.getAccountName());
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    token = GoogleAuthUtil.getToken(
                            GoogleAuthActivity.this, authPreferences.getAccountName(), SCOPE);
                    authPreferences.setToken(token);
                    sendRegisterRequest(
                            authPreferences.getAccountName(), authPreferences.getToken());
                } catch (IOException transientEx) {
                    HachikoLogger.error("Google auth network error?", transientEx);
                } catch (UserRecoverableAuthException recoverableException) {
                    Intent recover = recoverableException.getIntent();
                    startActivityForResult(recover, REQUEST_AUTH_RESULT_CODE);
                } catch (GoogleAuthException authEx) {
                    // The call is not ever expected to succeed assuming you have already verified
                    // that Google Play services is installed.
                    HachikoLogger.error("auth exception", authEx);
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                HachikoLogger.debug("Token obtained: ", token);
            }

        };
        task.execute();
    }

    private void sendRegisterRequest(String gmail, String authToken) {
        JSONObject params = JSONUtils.jsonObject("gmail", gmail, "google_token", authToken);
        HachikoLogger.debug("register request");
        JsonRequest request = VolleyRequestFactory.registerRequest(
                this,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("Registration completed: ", s);
                        new SetupUserTableTask(getApplicationContext()).execute();
                        HachikoPreferences.getDefaultEditor(getApplicationContext())
                                .putString(HachikoPreferences.KEY_MY_HACHIKO_ID, s)
                                .commit();
                        transitToNextActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("Hachiko registration error ", volleyError);
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        HachikoApp.defaultRequestQueue().add(request);
    }


    /**
     * call this method if your token expired, or you want to request a new
     * token for whatever reason. call requestToken() again afterwards in order
     * to get a new token.
     */
    private void invalidateToken() {
        HachikoLogger.debug("invalidate token");
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken(COM_GOOGLE, authPreferences.getToken());
        authPreferences.setToken(null);
    }
}
