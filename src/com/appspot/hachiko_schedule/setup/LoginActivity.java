package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.MainActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.UserAPI;
import com.appspot.hachiko_schedule.apis.VolleyRequestFactory;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.LocalProfileHelper;
import com.appspot.hachiko_schedule.util.MapUtils;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * ログイン用Activity, 今のとこFacebookログインのみ対応(かつログイン強制)
 */
public class LoginActivity extends Activity {
    private View indicatorView;
    private View loginButton;
    private View afterLoginOptionView;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    handleLoggedInStatus(session);
                }
            };
    private boolean registerRequestSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_login);
        indicatorView = findViewById(R.id.setup_fetching_status_indicator);
        loginButton = findViewById(R.id.setup_login_button);
        afterLoginOptionView = findViewById(R.id.setup_after_login);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        handleLoggedInStatus(Session.getActiveSession());
    }

    private void handleLoggedInStatus(Session session) {
        boolean loggedIn = session != null && session.isOpened();
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        afterLoginOptionView.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (loggedIn && !registerRequestSent) {
            sendNewUserRegistrationRequest(session.getAccessToken());
        } else {
            indicatorView.setVisibility(View.GONE);
        }
    }

    private void sendNewUserRegistrationRequest(String fbToken) {
        registerRequestSent = true;
        Map<String, String> params = new HashMap<String, String>();
        LocalProfileHelper localProfileHelper = new LocalProfileHelper(this);
        MapUtils.putOrIgnoreNull(params, "UserName", localProfileHelper.getDisplayName());
        MapUtils.putOrIgnoreNull(params, "PhoneNumber", localProfileHelper.getMyOwnPhoneNumber());
        MapUtils.putOrIgnoreNull(params, "Email", localProfileHelper.getMyOwnEmail());
        MapUtils.putOrIgnoreNull(params, "FacebookToken", fbToken);
        StringRequest request = VolleyRequestFactory.newStringRequest(
                UserAPI.REGISTER,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String myHachikoId) {
                        HachikoLogger.debug("会員登録成功 ID: " + myHachikoId);
                        HachikoPreferences.getDefaultEditor(LoginActivity.this)
                                .putString(HachikoPreferences.KEY_MY_HACHIKO_ID, myHachikoId);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        HachikoPreferences.getDefaultEditor(LoginActivity.this)
                                .putBoolean(HachikoPreferences.KEY_FB_LOGGED_IN, true)
                                .commit();

                        // TODO: cookieに含まれる認証情報をよしなに処理
                        // TODO: 電話帳アップロード

                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("user registration communication error", volleyError);
                        new AlertDialog.Builder(LoginActivity.this)
                                .setMessage("通信に失敗しました")
                                .show();
                    }
                }
        );
        HachikoApp.defaultRequestQueue().add(request);
    }

    @Override
    public void onPause() {
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        uiHelper.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
