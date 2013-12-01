package com.appspot.hachiko_schedule.plans;

import android.app.*;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.FetchPlansRequest;
import com.appspot.hachiko_schedule.friends.ChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;

import java.util.Calendar;
import java.util.Date;

public class PlanListActivity extends Activity{
    private boolean shouldBackToChooseGuestActivity;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentForSetup = new SetupManager(this).intentForRequiredSetupIfAny();
        if (intentForSetup != null) {
            startActivity(intentForSetup);
            finish();
            return;
        }

        handleIntent(getIntent());
        setContentView(R.layout.activity_event_list);
        progressDialog = new ProgressDialog(this);

        final Typeface fontForImage= Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<UnfixedGuestPlansFragment>(
                        this,
                        "f1",
                        UnfixedGuestPlansFragment.class
                ))
        );
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<UnfixedHostPlansFragment>(
                        this,
                        "f2",
                        UnfixedHostPlansFragment.class
                ))
        );
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<FixedPlansFragment>(
                        this,
                        "f3",
                        FixedPlansFragment.class
                ))
        );

        String[] tabNames = {getString(R.string.icon_unfixed_guest_plan),
                getString(R.string.icon_unfixed_host_plan),
                getString(R.string.icon_fixed_plan)};

        for(int i = 0; i<actionBar.getTabCount(); i++){
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.tab_title, null);

            TextView titleText = (TextView) customView.findViewById(R.id.action_custom_title);
            titleText.setText(tabNames[i]);
            titleText.setTypeface(fontForImage);

            actionBar.getTabAt(i).setCustomView(customView);
        }

        //予定がないときの表示用フォントを読み込む
        ((TextView)findViewById(R.id.angle_double_up)).setTypeface(fontForImage);

        //予定がないときのアニメーション
        AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(100);
        animation.setDuration(2000);
        (findViewById(R.id.angle_double_up)).startAnimation(animation);
        (findViewById(R.id.no_event_then_create_new)).startAnimation(animation);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        shouldBackToChooseGuestActivity
                = (intent != null && intent.getBooleanExtra(Constants.EXTRA_KEY_NEW_EVENT, false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_events:
                reloadPlans();
                return true;
            case R.id.action_create_event:
                startCreatingEvent();
                return true;
            case R.id.action_launch_calendar_app:
                launchCalendarApp();
                return true;
            case R.id.action_config:
                launchMenuActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startCreatingEvent() {
        Intent intent = new Intent(this, ChooseGuestActivity.class);
        startActivity(intent);
    }

    private void launchCalendarApp() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long time = calendar.getTime().getTime();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time").appendPath(Long.toString(time));
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        startActivity(intent);
    }

    private void launchMenuActivity() {
        Intent intent = new Intent(this, MainPreferenceActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && shouldBackToChooseGuestActivity) {
            Intent intent = new Intent(this, ChooseGuestActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void reloadPlans() {
        progressDialog.setMessage("サーバと通信中");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Request request =  new FetchPlansRequest(this,
                new FetchPlansRequest.PlansUpdateListener() {
                    @Override
                    public void onPlansUpdated() {
                        // TODO: update tab #168
                        progressDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        HachikoDialogs.showNetworkErrorDialog(PlanListActivity.this, volleyError);
                    }
                });
        HachikoApp.defaultRequestQueue().add(request);
    }

    private static class MainTabListener<T extends Fragment> implements ActionBar.TabListener{

        private Fragment fragment;
        private final Activity activity;
        private final String tag;
        private final Class<T> cls;

        public MainTabListener(
                Activity activity, String tag, Class<T> cls){
            this.activity = activity;
            this.tag = tag;
            this.cls = cls;
        }
        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if(fragment == null){
                fragment = Fragment.instantiate(activity, cls.getName());
                ft.add(android.R.id.content, fragment, tag);
            }
            else{
                ft.attach(fragment);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if(fragment != null){
                ft.detach(fragment);
            }
        }
    }
 }
