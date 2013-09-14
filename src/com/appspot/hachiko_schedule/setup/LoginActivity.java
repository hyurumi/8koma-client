package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.MainActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.UserAPI;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.JSONUtils;
import com.appspot.hachiko_schedule.util.LocalProfileHelper;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import org.json.JSONException;
import org.json.JSONObject;

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
            Request.newMeRequest(session ,new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, com.facebook.Response response) {
                    sendUserInfoForRegistration(Long.parseLong(user.getId()));
                }
            }).executeAsync();
        } else {
            indicatorView.setVisibility(View.GONE);
        }
    }

    private void sendUserInfoForRegistration(long fbId) {
        registerRequestSent = true;
        JSONObject params = new JSONObject();
        LocalProfileHelper localProfileHelper = new LocalProfileHelper(this);
        try {
            JSONUtils.putOrIgnoreNull(params, "fbid", fbId);
            JSONUtils.putOrIgnoreNull(params, "name", localProfileHelper.getDisplayName());
            JSONUtils.putOrIgnoreNull(params, "phone", localProfileHelper.getMyOwnPhoneNumber());
            JSONUtils.putOrIgnoreNull(params, "email", localProfileHelper.getMyOwnEmail());
        } catch (JSONException e) {
            HachikoLogger.error("JSON exception", e);
        }
        JsonObjectRequest request = new JsonObjectRequest(
                UserAPI.REGISTER.getMethod(),
                UserAPI.REGISTER.getUrl(),
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        HachikoLogger.debug("会員登録成功");
                        HachikoLogger.debug(jsonObject);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        HachikoPreferences.getDefaultEditor(LoginActivity.this)
                                .putBoolean(HachikoPreferences.KEY_FB_LOGGED_IN, true)
                                .commit();
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
