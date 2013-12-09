package tk.hachikoma.plans;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import tk.hachikoma.Constants;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.R;
import tk.hachikoma.apis.FetchPlansRequest;
import tk.hachikoma.friends.ChooseGuestActivity;
import tk.hachikoma.prefs.MainPreferenceActivity;
import tk.hachikoma.setup.SetupManager;
import tk.hachikoma.ui.HachikoDialogs;

import java.util.Calendar;
import java.util.Date;

public class PlanListActivity extends Activity{
    public static final String INTENT_KEY_TAB_NAME = "tab_name";
    public static final String EXTRA_UPDATE_PLAN_MESSAGE = "update_plan_message";
    public static final String TAB_NAME_UNFIXED_GUEST = "unfixed_guest_plan";
    public static final String TAB_NAME_UNFIXED_HOST = "unfixed_host_plan";
    public static final String TAB_NAME_FIXED = "fixed_plan";
    private final String[] tabNames = new String[] {
            TAB_NAME_UNFIXED_GUEST,
            TAB_NAME_UNFIXED_HOST,
            TAB_NAME_FIXED
    };

    public static final String BROADCAST_UPDATE_PLAN = "update_plan";
    private BroadcastReceiver planUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String tabName = intent.getStringExtra(INTENT_KEY_TAB_NAME);
            int currentIndex = getActionBar().getSelectedNavigationIndex();
            if (0 <= currentIndex && currentIndex < tabNames.length &&
                    tabNames[currentIndex].equals(tabName)) {
                ((PlanFragmentBase) getFragmentManager().findFragmentByTag(tabName))
                        .queryAndUpdatePlans();
                String msg = intent.getStringExtra(EXTRA_UPDATE_PLAN_MESSAGE);
                if (msg != null) {
                    Toast.makeText(PlanListActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
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

        setContentView(R.layout.activity_event_list);
        progressDialog = new ProgressDialog(this);

        final Typeface fontForImage= Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<UnfixedGuestPlansFragment>(
                        this,
                        TAB_NAME_UNFIXED_GUEST,
                        UnfixedGuestPlansFragment.class
                ))
        );
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<UnfixedHostPlansFragment>(
                        this,
                        TAB_NAME_UNFIXED_HOST,
                        UnfixedHostPlansFragment.class
                ))
        );
        actionBar.addTab(actionBar
                .newTab()
                .setTabListener(new MainTabListener<FixedPlansFragment>(
                        this,
                        TAB_NAME_FIXED,
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

        handleIntent(getIntent());    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                planUpdateReceiver, new IntentFilter(BROADCAST_UPDATE_PLAN));
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(planUpdateReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        shouldBackToChooseGuestActivity
                = intent.getBooleanExtra(Constants.EXTRA_KEY_NEW_EVENT, false);
        String tabName = intent.getStringExtra(INTENT_KEY_TAB_NAME);
        for (int i = 0; i < tabNames.length; i++) {
            if (tabNames[i].equals(tabName)) {
                getActionBar().setSelectedNavigationItem(i);
                return;
            }
        }
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
                        int selectedIndex = getActionBar().getSelectedNavigationIndex();
                        if (0 <= selectedIndex && selectedIndex < tabNames.length) {
                            String tabName = tabNames[selectedIndex];
                            ((PlanFragmentBase) getFragmentManager().findFragmentByTag(tabName))
                                    .queryAndUpdatePlans();
                        }
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

    public static Intent getIntentForUnfixedGuest(Context context) {
        return new Intent(context, PlanListActivity.class).putExtra(
                INTENT_KEY_TAB_NAME, TAB_NAME_UNFIXED_GUEST);
    }

    public static Intent getIntentForUnfixedHost(Context context) {
        return new Intent(context, PlanListActivity.class).putExtra(
                INTENT_KEY_TAB_NAME, TAB_NAME_UNFIXED_HOST);
    }

    /**
     * 予定一覧の再描画をリクエストするBroadcastを投げる．
     * @param tabName 以下のいずれか
     *                {@link PlanListActivity.TAB_NAME_FIXED}
     *                {@link PlanListActivity.TAB_NAME_UNFIXED_GUEST}
     *                {@link PlanListActivity.TAB_NAME_UNFIXED_HOST}
     */
    public static void sendBroadcastForUpdatePlan(Context context, String tabName, String msg) {
        Intent broadcastIntent = new Intent(BROADCAST_UPDATE_PLAN);
        broadcastIntent.putExtra(INTENT_KEY_TAB_NAME, tabName);
        if (msg != null) {
            broadcastIntent.putExtra(EXTRA_UPDATE_PLAN_MESSAGE, msg);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    private static class MainTabListener<T extends PlanFragmentBase> implements ActionBar.TabListener{

        private PlanFragmentBase fragment;
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
                fragment = (PlanFragmentBase) Fragment.instantiate(activity, cls.getName());
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
