package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.appspot.hachiko_schedule.MainActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_login);
        indicatorView = findViewById(R.id.setup_fetching_status_indicator);
        loginButton = findViewById(R.id.setup_login_button);
        afterLoginOptionView = findViewById(R.id.setup_after_login);

        View afterLoggedInButton = afterLoginOptionView.findViewById(R.id.setup_login_done_button);
        afterLoggedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                HachikoPreferences.getDefaultEditor(LoginActivity.this)
                        .putBoolean(HachikoPreferences.KEY_FB_LOGGED_IN, true)
                        .commit();
                startActivity(intent);
            }
        });

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
        indicatorView.setVisibility(View.GONE);
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        afterLoginOptionView.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
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
