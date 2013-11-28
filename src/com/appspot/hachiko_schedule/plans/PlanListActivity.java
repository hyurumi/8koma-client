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
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.FetchPlansRequest;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.apis.base_requests.JSONStringRequest;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.FixedPlan;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.friends.ChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.appspot.hachiko_schedule.setup.WalkthroughFragment0;
import com.appspot.hachiko_schedule.setup.WalkthroughFragment1;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PlanListActivity extends Activity implements UnfixedHostPlanView.OnConfirmListener {
    private boolean shouldBackToChooseGuestActivity;
    private ProgressDialog progressDialog;
    private PlanAdapter planAdapter;
    private PlansTableHelper plansTableHelper;
    private ListView eventList;

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
        plansTableHelper = new PlansTableHelper(this);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar
                .newTab()
                .setText("呼ばれた予定")
                .setTabListener(new MainTabListener<UnfixedGuestPlansFragment>(
                        this,
                        "f1",
                        UnfixedGuestPlansFragment.class
                ))
        );
        actionBar.addTab(actionBar
                .newTab()
                .setText("呼んだ予定")
                .setTabListener(new MainTabListener<UnfixedHostPlansFragment>(
                        this,
                        "f1",
                        UnfixedHostPlansFragment.class
                ))
        );

        //予定がないときの表示用フォントを読み込む
        eventList = ((ListView) findViewById(R.id.event_list));
        Typeface fontForArrow= Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        ((TextView)findViewById(R.id.angle_double_up)).setTypeface(fontForArrow);

        //予定がないときのアニメーション
        AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(100);
        animation.setDuration(2000);
        (findViewById(R.id.angle_double_up)).startAnimation(animation);
        (findViewById(R.id.no_event_then_create_new)).startAnimation(animation);
    }


    @Override
    protected void onResume() {
        queryAndUpdatePlans();
        super.onResume();
    }

    private void queryAndUpdatePlans() {
        List<Plan> plans = plansTableHelper.queryPlans();
        planAdapter = new PlanAdapter(this, plans, this);
        eventList.setAdapter(planAdapter);
        findViewById(R.id.view_for_no_event).setVisibility(
                plans.size() == 0 ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onConfirm(final UnfixedPlan unfixedPlan, final CandidateDate candidateDate) {
        int answerId = candidateDate.getAnswerId();
        progressDialog.setMessage("予定を確定中");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        HachikoLogger.debug("confirm: " + HachikoAPI.Plan.CONFIRM.getUrl() + answerId);
        Request request = new JSONStringRequest(PlanListActivity.this,
                HachikoAPI.Plan.CONFIRM.getMethod(),
                HachikoAPI.Plan.CONFIRM.getUrl() + answerId,
                null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("Confirmed");
                        FixedPlan fixedPlan = new FixedPlan(
                                unfixedPlan.getPlanId(), unfixedPlan.getTitle(),
                                HachikoPreferences.getMyHachikoId(PlanListActivity.this),
                                candidateDate);
                        plansTableHelper.confirmCandidateDate(
                                unfixedPlan.getPlanId(), candidateDate.getAnswerId());
                        planAdapter.updatePlan(unfixedPlan, fixedPlan);
                        progressDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        HachikoDialogs.showNetworkErrorDialog(
                                PlanListActivity.this, volleyError);
                        HachikoLogger.error("confirm fail", volleyError);
                    }
                });
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
        HachikoApp.defaultRequestQueue().add(request);
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
                        queryAndUpdatePlans();
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
