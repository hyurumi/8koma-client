package com.appspot.hachiko_schedule.plans;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.friends.NewEventChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.setup.SetupManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PlanListActivity extends Activity {
    private boolean shouldBackToChooseGuestActivity;

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
        PlansTableHelper plansTableHelper = new PlansTableHelper(this);

        ListView eventList = ((ListView) findViewById(R.id.event_list));
        List<Plan> unfixedPlans = plansTableHelper.queryUnfixedPlans();
        eventList.setAdapter(new PlanAdapter(this, unfixedPlans.toArray(new Plan[0])));
        findViewById(R.id.view_for_no_event).setVisibility(
                unfixedPlans.size() == 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.no_event_create_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreatingEvent();
            }
        });
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
        Intent intent = new Intent(this, NewEventChooseGuestActivity.class);
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
            Intent intent = new Intent(this, NewEventChooseGuestActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 確定した予定一覧を表示する用のリストadapter
     */
    private static class PlanAdapter extends ArrayAdapter<Plan> {
        private final Plan[] plans;

        public PlanAdapter(
                Context context, Plan[] unfixedPlans) {
            super(context, R.layout.unfixed_guest_plan_view, unfixedPlans);
            this.plans = unfixedPlans;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (plans[position].isHost()) {
                convertView = new UnfixedHostPlanView(getContext());
                ((UnfixedHostPlanView) convertView).setPlan((UnfixedPlan)plans[position]);
            } else {
                convertView = new UnfixedGuestPlanView(getContext());
                ((UnfixedGuestPlanView) convertView).setPlan((UnfixedPlan)plans[position]);
            }
            return convertView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
}
