package com.appspot.hachiko_schedule.plans;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.apis.HachiJsonObjectRequest;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.apis.PlanAPI;
import com.appspot.hachiko_schedule.apis.VacancyRequest;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.ui.HachikoDialogs;
import com.appspot.hachiko_schedule.ui.SwipeToDismissTouchListener;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.appspot.hachiko_schedule.apis.VacancyRequest.Hours;
import static com.appspot.hachiko_schedule.util.ViewUtils.removeView;

/**
 * {@link Activity} for creating new plan.
 */
public class CreatePlanActivity extends Activity {
    private static final String DEFAULT_EVENT_TITLE = "打ち合わせ";
    // TODO: もっとましな対応管理の仕方を考える
    private static final ImmutableMap<String, Integer> TEXT_TO_MIN
            = new ImmutableMap.Builder<String, Integer>()
            .put("30分", 30).put("1時間", 60).put("1時間半", 90)
            .put("2時間", 120).put("2時間半", 150).put("３時間", 180).build();
    // やがては設定可能になるべき，なのでメンバ変数
    private Hours morning = new Hours(8, 11);
    private Hours afternoon = new Hours(11, 16);
    private Hours evening = new Hours(16, 19);
    private Hours night = new Hours(18, 21);
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
    private Map<View, Timeslot> viewToTimeslots = new HashMap<View, Timeslot>();
    private Long[] friendIds;
    private ProgressBar loadingCandidateView;
    private ProgressDialog progressDialog;
    private VacancyRequest.Param lastRequestedVacancyParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        setTitle(R.string.create_event_detail_text);

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
        loadingCandidateView = (ProgressBar) findViewById(R.id.progress_loading_candidate);
        confirmButton = (Button) findViewById(R.id.invite_button);

        setFriends();
        initDateRange();
        durationSpinner.setOnItemSelectedListener(new DefaultSpinnerItemSelectedListener());
        confirmButton.setOnClickListener(new ConfirmButtonListener());
        startDateSpinner.setOnItemSelectedListener(new OnStartDateSelectedListener());
        endDateSpinner.setOnItemSelectedListener(new OnEndDateSelectedListener());
        morningButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        afternoonButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        eveningButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        nightButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
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
        List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
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
            JSONArray friendIdsJson = new JSONArray(Arrays.asList(friendIds));
            param.put("friendsId", friendIdsJson);
            param.put("candidates", dates);
            param.put("title", title);
        } catch (JSONException e) {
            HachikoLogger.error("JSON error/Never happen", e);
            return;
        }
        HachikoLogger.debug(param);
        Request request = new HachiJsonObjectRequest(this,
                PlanAPI.NEW_PLAN.getMethod(),
                PlanAPI.NEW_PLAN.getUrl(),
                param,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        long planId;
                        List<CandidateDate> candidateDates;
                        try {
                            planId = json.getLong("planId");
                            HachikoLogger.debug("plan successfully created", planId);
                            candidateDates = new ArrayList<CandidateDate>();
                            JSONArray candidatesJson = json.getJSONArray("candidates");
                            for (int i = 0; i < candidatesJson.length(); i++) {
                                JSONObject candidateJson = candidatesJson.getJSONObject(i);
                                JSONObject timeRange = candidateJson.getJSONObject("time");
                                candidateDates.add(new CandidateDate(
                                        candidateJson.getInt("id"),
                                        DateUtils.parseISO8601(timeRange.getString("start")),
                                        DateUtils.parseISO8601(timeRange.getString("end")),
                                        CandidateDate.AnswerState.OK
                                ));
                            }
                        } catch (JSONException e) {
                            HachikoLogger.error("JSON parse error/Never happen", e);
                            return;
                        }
                        PlansTableHelper plansTableHelper
                                = new PlansTableHelper(CreatePlanActivity.this);
                        plansTableHelper.insertNewPlan(planId, title,
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
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG_AND_RETRY);
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
                            HachikoLogger.debug("swipe end");
                        }
                    }
                }));
        schedulesContainer.addView(scheduleView);
        confirmButton.setEnabled(true);
        suggestingTimeslots.add(schedule);
        return scheduleView;
    }

    private List<Hours> getPreferredTimeRange() {
        List<Hours> result = new ArrayList<Hours>();
        boolean[] checked = new boolean[] {morningButton.isChecked(), afternoonButton.isChecked(),
                eveningButton.isChecked(), nightButton.isChecked(), false};
        Hours[] hours = new Hours[] {morning, afternoon, evening, night};
        boolean inTimeRange = false;
        int start = 0;
        for (int i = 0; i < 5; i++) {
            if (checked[i] && !inTimeRange) {
                start = hours[i].start;
                inTimeRange = true;
            } else if (!checked[i] && inTimeRange) {
                result.add(new Hours(start, hours[i - 1].end));
                inTimeRange = false;
            }
        }
        return result;
    }

    private synchronized void suggestNewCandidates() {
        List<Hours> preferredTimeRange = getPreferredTimeRange();
        Calendar startDay = (Calendar) startDateSpinner.getSelectedItem();
        Calendar endDay = (Calendar) endDateSpinner.getSelectedItem();
        final int durationMin = TEXT_TO_MIN.get(durationSpinner.getSelectedItem());
        if (preferredTimeRange.size() == 0 || endDay.before(startDay)) {
            HachikoLogger.debug("ignore invalid date or time input");
            return;
        }
        VacancyRequest.Param param = new VacancyRequest.Param(
                Arrays.asList(friendIds), preferredTimeRange, startDay, endDay, durationMin);
        clearTimeSlots();
        loadingCandidateView.setVisibility(View.VISIBLE);

        if (param.equals(lastRequestedVacancyParam)) {
            return;
        }
        Request vacancyRequest = new VacancyRequest(
                this, param,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray array) {
                        clearTimeSlots();
                        for (int i = 0; i < array.length(); i++) {
                            Date date;
                            try {
                                date = DateUtils.parseISO8601(array.getString(i));
                            } catch (JSONException e) {
                                HachikoLogger.error("Json parse error", e);
                                continue;
                            }
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cal.add(Calendar.MINUTE, durationMin);
                            addNewScheduleTextView(new Timeslot(date, cal.getTime(), false));
                        }
                        confirmButton.setEnabled(true);
                        loadingCandidateView.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoDialogs.showNetworkErrorDialog(CreatePlanActivity.this, volleyError);
                        HachikoLogger.error("err", volleyError);
                    }
                });
        HachikoApp.defaultRequestQueue().add(vacancyRequest);
        lastRequestedVacancyParam = param;
    }

    private void clearTimeSlots() {
        suggestingTimeslots.clear();
        schedulesContainer.removeAllViews();
        confirmButton.setEnabled(false);
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
            List<Timeslot> sortedTimeslot = new ArrayList<Timeslot>(suggestingTimeslots);
            Collections.sort(sortedTimeslot);
            for (Timeslot timeslot: sortedTimeslot) {
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

    private class OnTimeRangePreferenceSelectedListener
            implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            suggestNewCandidates();
        }
    }

    private class OnStartDateSelectedListener extends DefaultSpinnerItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (startDateSpinner.getAdapter().getCount() - 1 == position) {
                startDateSpinner.setSelection(position - 1);
            }
            if (startDateSpinner.getSelectedItemPosition()
                    > endDateSpinner.getSelectedItemPosition()) {
                endDateSpinner.setSelection(startDateSpinner.getSelectedItemPosition() + 1);
            }
            super.onItemSelected(parent, view, position, id);
        }
    }

    private class OnEndDateSelectedListener extends DefaultSpinnerItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                endDateSpinner.setSelection(1);
            }
            if (startDateSpinner.getSelectedItemPosition()
                    > endDateSpinner.getSelectedItemPosition()) {
                startDateSpinner.setSelection(endDateSpinner.getSelectedItemPosition() - 1);
            }
            super.onItemSelected(parent, view, position, id);
        }
    }

    private class DefaultSpinnerItemSelectedListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            suggestNewCandidates();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }
}
