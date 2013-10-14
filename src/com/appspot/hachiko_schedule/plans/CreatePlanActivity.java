package com.appspot.hachiko_schedule.plans;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.JSONStringRequest;
import com.appspot.hachiko_schedule.apis.PlanAPI;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.TimeWords;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.ui.SwipeToDismissTouchListener;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Preconditions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.appspot.hachiko_schedule.util.ViewUtils.removeView;

/**
 * {@link Activity} for creating new plan.
 */
public class CreatePlanActivity extends Activity {
    private static final String DEFAULT_EVENT_TITLE = "打ち合わせ";
    private Spinner startDateSpinner;
    private Spinner endDateSpinner;
    private Spinner durationSpinner;
    private RadioGroup dateRangeRadioGroup;
    private ToggleButton morningButton;
    private ToggleButton afternoonButton;
    private ToggleButton eveningButton;
    private ToggleButton nightButton;
    private EditText eventTitleView;
    private ViewGroup schedulesContainer;
    private Set<Timeslot> suggestingTimeslots = new HashSet<Timeslot>();
    private Button confirmButton;
    private Parcelable[] friends;
    private ScheduleSuggester scheduleSuggester;
    private Map<View, Timeslot> viewToTimeslots = new HashMap<View, Timeslot>();
    private Handler hander = new Handler();
    private Long[] friendIds;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        setTitle(R.string.create_event_detail_text);

        scheduleSuggester = new ScheduleSuggester(this);
        eventTitleView = (EditText) findViewById(R.id.what_to_do);
        startDateSpinner = (Spinner) findViewById(R.id.start_date_spinner);
        endDateSpinner = (Spinner) findViewById(R.id.end_date_spinner);
        dateRangeRadioGroup = (RadioGroup) findViewById(R.id.date_range_selections);
        morningButton = (ToggleButton) findViewById(R.id.morning);
        afternoonButton = (ToggleButton) findViewById(R.id.afternoon);
        eveningButton = (ToggleButton) findViewById(R.id.evening);
        nightButton = (ToggleButton) findViewById(R.id.night);
        durationSpinner = (Spinner) findViewById(R.id.duration_spinner);
        schedulesContainer = (ViewGroup) findViewById(R.id.schedules);
        confirmButton = (Button) findViewById(R.id.invite_button);

        setFriends();
        initDateRange();
        durationSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());
        confirmButton.setOnClickListener(new ConfirmButtonListener());
     }

    private void setFriends() {
        friends = getIntent().getParcelableArrayExtra(Constants.EXTRA_KEY_FRIENDS);
        Preconditions.checkNotNull(friends);
        Preconditions.checkState(friends.length != 0);
        showFriendsName(friends);
        friendIds = new Long[friends.length];
        for (int i = 0; i < friends.length; i++) {
            friendIds[i] = ((FriendIdentifier) friends[i]).getHachikoId();
        }
    }

    private void initDateRange() {
        final Calendar today;
        Calendar nextMonday = null;
        Calendar nextFriday = null;
        today = Calendar.getInstance();
        final List<Calendar> continuesDays = new ArrayList<Calendar>();
        continuesDays.add(today);
        Calendar calendar = today;
        for (int i = 0; i < 21; i++) {
            calendar = (Calendar) calendar.clone();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            if (nextMonday == null && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                nextMonday = calendar;
            }
            if (nextFriday == null && nextMonday != null
                    && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                nextFriday = calendar;
            }
            continuesDays.add(calendar);
        }
        startDateSpinner.setAdapter(new CalendarArrayAdapter(continuesDays));
        endDateSpinner.setAdapter(new CalendarArrayAdapter(continuesDays));

        final Calendar nextMonday_ = nextMonday;
        final Calendar nextFriday_ = nextFriday;
        startDateSpinner.setSelection(continuesDays.indexOf(nextMonday_));
        endDateSpinner.setSelection(continuesDays.indexOf(nextFriday_));
        startDateSpinner.setEnabled(false);
        endDateSpinner.setEnabled(false);
        dateRangeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.date_range_nextweek:
                        startDateSpinner.setSelection(continuesDays.indexOf(nextMonday_));
                        endDateSpinner.setSelection(continuesDays.indexOf(nextFriday_));
                        startDateSpinner.setEnabled(false);
                        endDateSpinner.setEnabled(false);
                        return;
                    case R.id.date_range_asap:
                        startDateSpinner.setSelection(continuesDays.indexOf(today));
                        endDateSpinner.setSelection(continuesDays.indexOf(today) + 3);
                        startDateSpinner.setEnabled(false);
                        endDateSpinner.setEnabled(false);
                        return;
                    case R.id.date_range_manual:
                        startDateSpinner.setEnabled(true);
                        endDateSpinner.setEnabled(true);
                        return;
                }
            }
        });
    }

    private class CalendarArrayAdapter extends ArrayAdapter<Calendar> {
        private CalendarArrayAdapter(List<Calendar> calendars) {
            super(CreatePlanActivity.this, android.R.layout.simple_spinner_item, calendars);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return overrideTextOfView(super.getView(position, convertView, parent), position);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return overrideTextOfView(
                    super.getDropDownView(position, convertView, parent), position);
        }

        private View overrideTextOfView(View textView, int position) {
            Calendar item = getItem(position);
            ((TextView) textView).setText(DateFormat.format("MM / dd (E)", item));
            return textView;
        }
    }

    private void sendCreatePlanRequest() {
        JSONObject param = new JSONObject();
        final List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
        final String title = getEventTitle();
        try {
            JSONArray dates = new JSONArray();
            for (Timeslot timeslot: suggestingTimeslots) {
                JSONObject candidateJson = new JSONObject();
                JSONObject timeslotJson = new JSONObject();
                timeslotJson.put("start", DateUtils.formatAsISO8601(timeslot.getStartDate()));
                timeslotJson.put("end", DateUtils.formatAsISO8601(timeslot.getEndDate()));
                candidateJson.put("time", timeslotJson);
                dates.put(candidateJson);
                candidateDates.add(new CandidateDate(-1, timeslot.getStartDate(),
                        timeslot.getEndDate(), CandidateDate.AnswerState.NEUTRAL));
            }
            JSONArray friendIdsJson = new JSONArray();
            for (long friendId: friendIds) {
                friendIdsJson.put(friendId);
            }
            param.put("friendsId", friendIdsJson);
            param.put("candidates", dates);
            param.put("title", title);
        } catch (JSONException e) {
            HachikoLogger.error("JSON error/Never happen", e);
            return;
        }
        HachikoLogger.debug(param);
        Request request = new JSONStringRequest(this,
                PlanAPI.NEW_PLAN.getMethod(),
                PlanAPI.NEW_PLAN.getUrl(),
                param,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        HachikoLogger.debug("plan successfully created");
                        HachikoLogger.debug(s);
                        PlansTableHelper plansTableHelper
                                = new PlansTableHelper(CreatePlanActivity.this);
                        plansTableHelper.insertNewPlan(Long.parseLong(s), title,
                                /* you are host */ true, Arrays.<Long>asList(friendIds), candidateDates);
                        Intent intent = new Intent(CreatePlanActivity.this, PlanListActivity.class);
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(Constants.EXTRA_KEY_NEW_EVENT, true);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // TODO: 作った予定のキャンセル #53
                        progressDialog.hide();
                        HachikoDialogs.showNetworkErrorDialog(CreatePlanActivity.this, volleyError);
                        HachikoLogger.error("plan creation error", volleyError);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                /* num of retry */3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void showFriendsName(Parcelable[] friends) {
        StringBuilder friendsNameToInvite = new StringBuilder();
        for (Parcelable friend: friends) {
            friendsNameToInvite.append(((FriendIdentifier) friend).getName()).append(" ");
        }
        ((TextView) findViewById(R.id.friends_name_to_invite))
                .setText(friendsNameToInvite.toString());
    }

    private String getEventTitle() {
        String str = eventTitleView.getText().toString();
        return str.length() == 0 ? DEFAULT_EVENT_TITLE : str;
    }

    private View addNewScheduleTextView(Timeslot schedule) {
        TextView scheduleView = new TextView(CreatePlanActivity.this);
        SimpleDateFormat startDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat endDateFormat = new SimpleDateFormat("HH:mm");
        scheduleView.setText(startDateFormat.format(schedule.getStartDate())
                + " - " + endDateFormat.format(schedule.getEndDate()));
        viewToTimeslots.put(scheduleView, schedule);

        // TODO: DPIを考慮した実装 (setPaddingの引数はpx指定)
        scheduleView.setPadding(20, 15, 7, 15);
        scheduleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        scheduleView.setBackgroundColor(Color.WHITE);
        scheduleView.setOnTouchListener(new SwipeToDismissTouchListener(
                CreatePlanActivity.this,
                new SwipeToDismissTouchListener.SwipeAndDismissEventListenerAdapter() {
                    @Override
                    public void onSwipeEndAnimationEnd(View view, boolean removed) {
                        if (removed) {
                            Timeslot inconvenientTimeslot = viewToTimeslots.get(view);
                            suggestingTimeslots.remove(inconvenientTimeslot);
                            removeView(view);
                            if (suggestingTimeslots.isEmpty()) {
                                confirmButton.setEnabled(false);
                            }
                            scheduleSuggester.notifyInconvenientTimeslot(inconvenientTimeslot);
                            HachikoLogger.debug("swipe end");
                            tryToAddNewScheduleTextViewWithDelayAndAnimation(300);
                        }
                    }
                }));
        schedulesContainer.addView(scheduleView);
        confirmButton.setEnabled(true);
        suggestingTimeslots.add(schedule);
        return scheduleView;
    }

    private void tryToAddNewScheduleTextViewWithDelayAndAnimation(int delayInMillis) {
        hander.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        Timeslot nextRecommended = scheduleSuggester.popNextRecommendedTimeslot();
                        if (nextRecommended != null) {
                            View view = addNewScheduleTextView(nextRecommended);
                            AnimatorSet set = new AnimatorSet();
                            set.play(ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f))
                                    .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f));
                            set.setDuration(700);
                            set.start();
                        }
                    }
                },
                delayInMillis);
    }

    private class ConfirmButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            StringBuilder title = new StringBuilder();
            for (int i = 0; i < friends.length; i++) {
                title.append(((FriendIdentifier) friends[i]).getName());
                if (i != friends.length - 1) {
                    title.append(", ");
                } else {
                    title.append("に招待を送信する");
                }
            }
            StringBuilder content = new StringBuilder();
            content.append(getEventTitle())
                    .append(System.getProperty("line.separator"))
                    .append("候補日:")
                    .append(System.getProperty("line.separator"));
            for (Timeslot timeslot: suggestingTimeslots) {
                content.append(DateUtils.timeslotString(
                        timeslot.getStartDate(), timeslot.getEndDate()));
                content.append(System.getProperty("line.separator"));

            }
            AlertDialog dialog = new AlertDialog.Builder(CreatePlanActivity.this)
                    .setTitle(title.toString())
                    .setMessage(content.toString())
                    .setPositiveButton("送信", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendCreatePlanRequest();
                            progressDialog = new ProgressDialog(CreatePlanActivity.this);
                            progressDialog.setMessage("予定作成しています...");
                            progressDialog.setCancelable(false);
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                        }
                    }).create();
            dialog.show();
        }
    }

    private class OnSpinnerItemSelectedListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // reschedule.
            schedulesContainer.removeAllViews();

            List<Timeslot> schedules = scheduleSuggester.suggestTimeSlot(
                    TimeWords.MORNING,
                    // TODO: いったんダミー値を入れるようにしておくので、後で直す。
                    Integer.parseInt("30"),
                    Integer.parseInt("1")
            );
            suggestingTimeslots.clear();
            for (Timeslot schedule: schedules) {
                addNewScheduleTextView(schedule);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }
}
