package com.appspot.hachiko_schedule.setup;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.apis.RegisterRequest;
import com.appspot.hachiko_schedule.friends.ChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
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
    private static final String HACHIKO_APP_ID
            = "715637255810-bqg5i0p4mdvi0kb5cdftof6voc10kft6.apps.googleusercontent.com";
    private static final String GOOGLE_PLUS = "https://www.googleapis.com/auth/plus.login";
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String OWN_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String EMAIL_SCOPE = "https://mail.google.com/";
    public static final String SCOPE_FOR_SERVER = "oauth2:server:client_id:" + HACHIKO_APP_ID
            + ":api_scope:" + GOOGLE_PLUS + " " + PROFILE_SCOPE + " " + OWN_EMAIL_SCOPE + " "
            + CALENDAR_SCOPE + " " + EMAIL_SCOPE;

    private static final String COM_GOOGLE = "com.google";

    private ProgressDialog progressDialog;
    private GoogleAuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPreferences  = new GoogleAuthPreferences(this);
        progressDialog = new ProgressDialog(this);
        if (authPreferences.isAuthSetuped()) {
            if (!HachikoPreferences.hasHachikoId(GoogleAuthActivity.this)) {
                sendRegisterRequest(authPreferences.getAuthCode());
            } else {
                transitToNextActivity();
            }
        } else {
            HachikoLogger.debug("choose account");

            chooseAccount();
        }
    }

    private void transitToNextActivity() {
        Intent intent = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intent == null) {
            intent = new Intent(this, ChooseGuestActivity.class);
        }
        startActivity(intent);
        finish();
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
                    invalidateToken();
                    requestAuthCode();
                    break;
                case REQUEST_AUTH_RESULT_CODE:
                    requestAuthCode();
                    break;
                default:
                    HachikoLogger.error("on result no action" + requestCode);
                    return;
            }
        } else {
            HachikoLogger.error("error result " +  requestCode + ", resultCode:" + requestCode);
        }
    }

    private void requestAuthCode() {
        HachikoLogger.debug("Request token as " + authPreferences.getAccountName());
        showProgressDialog("認証を行っています...");
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String authCode = null;
                try {
                    authCode = GoogleAuthUtil.getToken(
                            GoogleAuthActivity.this,
                            authPreferences.getAccountName(),
                            SCOPE_FOR_SERVER);
                    authPreferences.setAuthCode(authCode);
                    sendRegisterRequest(authCode);
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
                return authCode;
            }

            @Override
            protected void onPostExecute(String authCode) {
                HachikoLogger.debug("AuthCode obtained: ", authCode);
            }
        };
        task.execute();
    }

    private void showProgressDialog(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void sendRegisterRequest(String authCode) {
        JSONObject params = JSONUtils.jsonObject("authCode", authCode);
        HachikoLogger.debug("register request");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("Hachikoサーバと通信中...");
            }
        });
        JsonRequest request = new RegisterRequest(
                this,
                authCode,
                new RegisterRequest.ResponseListener() {
                    @Override
                    public void onResponse(long id, String pass) {
                        HachikoLogger.debug("Registration completed: ", id);
                        new SetupUserTableTask(getApplicationContext()).execute();
                        HachikoPreferences.getDefaultEditor(getApplicationContext())
                                .putLong(HachikoPreferences.KEY_MY_HACHIKO_ID, id)
                                .commit();
                        HachikoPreferences.getDefaultEditor(getApplicationContext())
                                .putString(HachikoPreferences.KEY_HACHIKO_INTERNAL_PASSWORD, pass)
                                .commit();
                        transitToNextActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        HachikoDialogs
                                .networkErrorDialogBuilder(GoogleAuthActivity.this, volleyError)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                        HachikoLogger.error("Hachiko registration error ", volleyError);
                    }
                }
        );
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void invalidateToken() {
        HachikoLogger.debug("invalidate token");
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken(COM_GOOGLE, authPreferences.getAccountName());
        authPreferences.setAuthCode(null);
    }
}
