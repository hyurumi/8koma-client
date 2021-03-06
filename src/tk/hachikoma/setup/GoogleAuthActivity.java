package tk.hachikoma.setup;

import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import tk.hachikoma.Constants;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.R;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.apis.RegisterRequest;
import tk.hachikoma.prefs.GoogleAuthPreferences;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.ui.HachikoDialogs;
import tk.hachikoma.util.HachikoLogger;
import tk.hachikoma.ui.ViewIndicator;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class GoogleAuthActivity extends SetupBaseActivity {
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

    private GoogleAuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ViewPager viewPager;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_auth);

        viewPager = (ViewPager) findViewById(R.id.walkthrough_viewpager);
        viewPager.setAdapter(
                new MyFragmentStatePagerAdapter(
                        getSupportFragmentManager()));
        ViewIndicator indicator = (ViewIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        indicator.setPosition(0);

        authPreferences  = new GoogleAuthPreferences(this);
        if (authPreferences.isAuthSetuped()
                && HachikoPreferences.hasHachikoId(GoogleAuthActivity.this)) {
            transitToNextActivity();
        }

        findViewById(R.id.start_auth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuth();
            }
        });
    }

    private void startAuth() {
        if (authPreferences.isAuthSetuped()) {
            if (!HachikoPreferences.hasHachikoId(GoogleAuthActivity.this)) {
                sendRegisterRequest(authPreferences.getAuthCode());
            } else {
                transitToNextActivity();
            }
        } else {
            chooseAccount();
        }
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
                if (Constants.IS_DEVELOPER) {
                    HachikoLogger.debug("AuthCode obtained: ", authCode);
                }
            }
        };
        task.execute();
    }

    private void sendRegisterRequest(String authCode) {
        HachikoLogger.debug("register request");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("ハチコマサーバと通信中...");
            }
        });
        JsonRequest request = new RegisterRequest(
                this,
                authCode,
                new RegisterRequest.ResponseListener() {
                    @Override
                    public void onResponse(long id, String pass) {
                        HachikoLogger.debug("Registration completed: ", id);
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
                        hideProgressDialog();
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
                        HachikoLogger.error("Hachikoma registration error ", volleyError);
                    }
                }
        );
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void invalidateToken() {
        HachikoLogger.debug("invalidate token");
        AccountManager accountManager = AccountManager.get(this);
        GoogleAuthUtil.invalidateToken(this, authPreferences.getAccountName());
        accountManager.invalidateAuthToken(COM_GOOGLE, authPreferences.getAccountName());
        authPreferences.setAuthCode(null);
    }

    public class MyFragmentStatePagerAdapter
            extends FragmentStatePagerAdapter {

        public MyFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            switch(i){
                case 0:
                    return new WalkthroughFragment0();
                case 1:
                    return new WalkthroughFragment1();
                case 2:
                    return new WalkthroughFragment2();
                default:
                    return new WalkthroughFragment3();
            }

        }

        @Override
        public int getCount() {
            return 4;
        }


    }
}
