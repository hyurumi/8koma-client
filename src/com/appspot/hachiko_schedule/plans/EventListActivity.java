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
import android.widget.Toast;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.friends.NewEventChooseGuestActivity;
import com.appspot.hachiko_schedule.prefs.MainPreferenceActivity;
import com.appspot.hachiko_schedule.setup.SetupManager;
import com.google.common.collect.ImmutableList;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class EventListActivity extends Activity {

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
        ((ListView) findViewById(R.id.event_list)).setAdapter(new PlanAdapter(this,
                new UnfixedPlan[] {
                        new UnfixedPlan(
                                "Hachikoミーティング @ 7号館",
                                ImmutableList.of("古見元気", "臼山直人", "竹井悠人"),
                                ImmutableList.of(
                                        new CandidateDate("10/10 10:00 - 12:00", ImmutableList.of("古見元気", "臼山直人"), ImmutableList.of("竹井悠人")),
                                        new CandidateDate("10/11 12:00 - 14:00", ImmutableList.of("古見元気"), ImmutableList.of("臼山直人", "竹井悠人")),
                                        new CandidateDate("10/12 10:00 - 12:00", ImmutableList.of("古見元気"), ImmutableList.of("竹井悠人")))
                                ),
                        new UnfixedPlan(
                                "Hachiko開発",
                                ImmutableList.of("古見元気", "臼山直人", "竹井悠人", "古見元気", "臼山直人", "竹井悠人", "古見元気", "臼山直人", "竹井悠人"),
                                ImmutableList.of(
                                        new CandidateDate("10/10 10:00 - 12:00", ImmutableList.of("古見元気", "臼山直人"), ImmutableList.of("竹井悠人")),
                                        new CandidateDate("10/11 12:00 - 14:00", ImmutableList.of("古見元気"), ImmutableList.of("臼山直人", "竹井悠人")),
                                        new CandidateDate("10/12 10:00 - 12:00", ImmutableList.of("古見元気"), Collections.<String>emptyList()),
                                        new CandidateDate("10/10 10:00 - 12:00", ImmutableList.of("古見元気", "臼山直人"), ImmutableList.of("竹井悠人")),
                                        new CandidateDate("10/11 12:00 - 14:00", ImmutableList.of("古見元気"), ImmutableList.of("臼山直人", "竹井悠人")))
                        ),
                        new UnfixedPlan(
                                "Hachikoミーティング @ 7号館",
                                ImmutableList.of("古見元気", "臼山直人", "竹井悠人"),
                                ImmutableList.of(
                                        new CandidateDate("10/10 10:00 - 12:00", ImmutableList.of("古見元気", "臼山直人"), ImmutableList.of("竹井悠人")),
                                        new CandidateDate("10/11 12:00 - 14:00", ImmutableList.of("古見元気"), ImmutableList.of("臼山直人", "竹井悠人")),
                                        new CandidateDate("10/12 10:00 - 12:00", ImmutableList.of("古見元気"), Collections.<String>emptyList()),
                                        new CandidateDate("10/10 10:00 - 12:00", ImmutableList.of("古見元気", "臼山直人"), ImmutableList.of("竹井悠人")),
                                        new CandidateDate("10/11 12:00 - 14:00", ImmutableList.of("古見元気"), ImmutableList.of("臼山直人", "竹井悠人")))
                        )
                }));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkNewEvent(intent);
    }

    private void checkNewEvent(Intent intent) {
        if (intent.getBooleanExtra(Constants.EXTRA_KEY_NEW_EVENT, false)) {
            Toast.makeText(this,
                    "新しいイベントが登録されました！(データ記憶する部分はまだ作ってないので表示は変わりません...)",
                    Toast.LENGTH_SHORT).show();
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

    /**
     * 確定した予定一覧を表示する用のリストadapter
     */
    private static class PlanAdapter extends ArrayAdapter<UnfixedPlan> {
        private final UnfixedPlan[] plans;

        public PlanAdapter(
                Context context, UnfixedPlan[] unfixedPlans) {
            super(context, R.layout.unfixed_plan_view, unfixedPlans);
            this.plans = unfixedPlans;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new UnfixedPlanView(getContext());
            }
            ((UnfixedPlanView) convertView).setPlan(plans[position]);

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
