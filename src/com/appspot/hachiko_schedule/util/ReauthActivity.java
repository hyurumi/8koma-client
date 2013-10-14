package com.appspot.hachiko_schedule.util;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.HachikoCookieManager;
import com.appspot.hachiko_schedule.apis.VolleyRequestFactory;
import com.appspot.hachiko_schedule.friends.NewEventChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Google/Hachikoサーバ両者に対して再認証を行うための{@link Activity}. 認証時点でActivity遷移が発生してしまう，
 * かつそれなりに時間もかかってしまうので，他の手段が模索されるべきな気がする． #73
 */
public class ReauthActivity extends Activity {
    private static final int CHOOSE_ACCOUNT_RESULT_CODE = 1001;
    private static final int REQUEST_AUTH_RESULT_CODE = 1002;
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String OWN_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String EMAIL_SCOPE = "https://mail.google.com/";
    private static final String SCOPE = "oauth2:" + PROFILE_SCOPE + " " + OWN_EMAIL_SCOPE + " "
            + CALENDAR_SCOPE + " " + EMAIL_SCOPE;
    private static final String COM_GOOGLE = "com.google";

    private ProgressDialog progressDialog;
    private GoogleAuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPreferences  = new GoogleAuthPreferences(this);
        invalidateToken();
        progressDialog = new ProgressDialog(this);
        new HachikoCookieManager(this).invalidateSessionCookie();
        HachikoLogger.debug("choose account");
        chooseAccount();
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
        showProgressDialog("認証を行っています...");
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    token = GoogleAuthUtil.getToken(
                            ReauthActivity.this, authPreferences.getAccountName(), SCOPE);
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

    private void showProgressDialog(String msg) {
        progressDialog.setMessage("認証を行っています...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void sendRegisterRequest(String gmail, String authToken) {
        JSONObject params = JSONUtils.jsonObject("gmail", gmail, "google_token", authToken);
        HachikoLogger.debug("register request");
        HachikoLogger.debug(gmail, authToken);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("Hachikoサーバと通信中...");
            }
        });
        JsonRequest request = VolleyRequestFactory.registerRequest(
                this,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("Registration/login completed: ", s);
                        HachikoPreferences.getDefaultEditor(getApplicationContext())
                                .putString(HachikoPreferences.KEY_MY_HACHIKO_ID, s)
                                .commit();
                        transitToNextActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        HachikoDialogs
                                .networkErrorDialogBuilder(ReauthActivity.this, volleyError)
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
