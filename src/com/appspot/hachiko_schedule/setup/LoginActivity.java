package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.facebook.*;
import com.facebook.model.GraphUser;
import org.json.JSONObject;

/**
 * ログイン用Activity, 今のとこFacebookログインのみ対応(かつログイン強制)
 */
public class LoginActivity extends Activity {
    private View indicatorView;
    private View loginButton;
    private View afterLoginOptionView;
    private long myId;

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
                String url = "http://daisy-lab.sakura.ne.jp/fetch_friends.php?user_id=" + myId
                        + "&token=" + Session.getActiveSession().getAccessToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        url,
                        null,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                showDialog("success", jsonObject.toString());
                            }
                        },
                        new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                showDialog("error", volleyError.getMessage());
                            }
                        });
                RequestQueue queue = HachikoApp.defaultRequestQueue();
                queue.add(jsonObjectRequest);
                queue.start();
                HachikoLogger.debug("request sent to " + url);
                Toast.makeText(LoginActivity.this, "request sent to " + url, Toast.LENGTH_LONG).show();
            }
        });

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    private void showDialog(String title, String message) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        handleLoggedInStatus(Session.getActiveSession());
    }

    private void handleLoggedInStatus(Session session) {
        boolean loggedIn = session != null && session.isOpened();
        if (loggedIn) {
            Request.newMeRequest(session ,new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    myId = Long.parseLong(user.getId());
                    indicatorView.setVisibility(View.GONE);
                    afterLoginOptionView.setVisibility(View.VISIBLE);
                }
            }).executeAsync();
        }
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
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
