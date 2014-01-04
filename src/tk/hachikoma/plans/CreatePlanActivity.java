package tk.hachikoma.plans;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import tk.hachikoma.Constants;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.R;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.apis.NewPlanRequest;
import tk.hachikoma.apis.PlanResponseParser;
import tk.hachikoma.apis.VacancyRequest;
import tk.hachikoma.data.CandidateDate;
import tk.hachikoma.data.FriendIdentifier;
import tk.hachikoma.data.TimeRange;
import tk.hachikoma.data.Timeslot;
import tk.hachikoma.db.PlansTableHelper;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.ui.HachikoDialogs;
import tk.hachikoma.ui.SwipeToDismissTouchListener;
import tk.hachikoma.util.DateUtils;
import tk.hachikoma.util.HachikoLogger;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tk.hachikoma.util.IntegerUtils;

import java.text.SimpleDateFormat;
import java.util.*;

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
    private TimeRange asa, hiru, yugata, yoru;
    private List<String> durationOptions;
    private ArrayAdapter durationAdapter;
    private int durationMin = 30;
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

        durationOptions = Lists.newArrayList(getResources().getStringArray(R.array.duration));
        durationOptions.add("それ以上");
        durationAdapter = new ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, durationOptions);
        durationSpinner.setAdapter(durationAdapter);
        durationSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                synchronized (durationAdapter) {
                    durationOptions.remove(durationOptions.size() - 1);
                    durationOptions.add("それ以上");
                    durationAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        setFriends();
        initDateRange();
        durationSpinner.setOnItemSelectedListener(new OnDurationSelectedListener());
        confirmButton.setOnClickListener(new ConfirmButtonListener());
        startDateSpinner.setOnItemSelectedListener(new OnStartDateSelectedListener());
        endDateSpinner.setOnItemSelectedListener(new OnEndDateSelectedListener());
        morningButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        afternoonButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        eveningButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
        nightButton.setOnCheckedChangeListener(new OnTimeRangePreferenceSelectedListener());
     }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = HachikoPreferences.getDefault(this);
        asa = new TimeRange(pref.getString(
                HachikoPreferences.KEY_TIMERANGE_ASA,
                HachikoPreferences.DEFAULT_TIMERANGE_ASA));
        hiru = new TimeRange(pref.getString(
                HachikoPreferences.KEY_TIMERANGE_HIRU,
                HachikoPreferences.DEFAULT_TIMERANGE_HIRU));
        yugata = new TimeRange(pref.getString(
                HachikoPreferences.KEY_TIMERANGE_YU,
                HachikoPreferences.DEFAULT_TIMERANGE_YU));
        yoru = new TimeRange(pref.getString(
                HachikoPreferences.KEY_TIMERANGE_YORU,
                HachikoPreferences.DEFAULT_TIMERANGE_YORU));
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
        final String title = getEventTitle();
        Request request = new NewPlanRequest(this,
                title,
                Arrays.asList(friendIds),
                suggestingTimeslots,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        long planId;
                        List<CandidateDate> candidateDates;
                        String token;
                        try {
                            planId = json.getLong("planId");
                            HachikoLogger.debug("plan successfully created", planId, json);
                            candidateDates = PlanResponseParser.parseCandidateDates(
                                    json, CandidateDate.AnswerState.OK);
                            token = json.getString("token");
                        } catch (JSONException e) {
                            HachikoLogger.error("JSON parse error/Never happen", e);
                            return;
                        }
                        PlansTableHelper plansTableHelper
                                = new PlansTableHelper(CreatePlanActivity.this);
                        plansTableHelper.insertNewPlan(planId, title,
                                HachikoPreferences.getMyHachikoId(CreatePlanActivity.this),
                                /* you are host */ true, Arrays.<Long>asList(friendIds),
                                candidateDates);
                        Intent intent =
                                PlanListActivity.getIntentForUnfixedHost(CreatePlanActivity.this);
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(Constants.EXTRA_KEY_NEW_EVENT, true);
                        intent.putExtra(
                                PlanListActivity.EXTRA_SHARE_PLAN_URL,
                                HachikoAPI.Plan.shareUrl(token));
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
        request.setRetryPolicy(HachikoAPI.RETRY_POLICY_LONG);
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void showFriendsName(Parcelable[] friends) {
        StringBuilder friendsNameToInvite = new StringBuilder();
        for (Parcelable friend: friends) {
            friendsNameToInvite.append(((FriendIdentifier) friend).getName()).append(", ");
        }
        friendsNameToInvite.deleteCharAt(friendsNameToInvite.length()-1);
        friendsNameToInvite.deleteCharAt(friendsNameToInvite.length()-1);
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
                            synchronized (this) {
                                if (view.getParent() != null) {
                                    ((ViewGroup) view.getParent()).removeView(view);
                                }
                            }
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

    private List<TimeRange> getPreferredTimeRange() {
        return getPreferredTimeRange(
                asa, morningButton.isChecked(), hiru, afternoonButton.isChecked(),
                yugata, eveningButton.isChecked(), yoru, nightButton.isChecked());
    }

    /**
     * @return ユーザの望む時間帯を，オーバーラップしない形にして返す.
     * 例えば [10:00 - 13:00, 12:00 - 14:00, 15:00 - 16:00]
     *    => [10:00 - 14:00, 15:00 - 16:00]みたいな処理
     */
    @VisibleForTesting
    static protected List<TimeRange> getPreferredTimeRange(
            TimeRange asa, boolean useAsa, TimeRange hiru, boolean useHiru,
            TimeRange yugata, boolean useYugata, TimeRange yoru, boolean useYoru) {
        List<TimeRange> result = new ArrayList<TimeRange>();
        TimeRange range = null;
        TimeRange[] ranges = new TimeRange[] {asa, hiru, yugata, yoru};
        boolean[] use = new boolean[] {useAsa, useHiru, useYugata, useYoru, false};
        for (int i = 0; i < 5; i++) {
            if (use[i]) {
                if (range == null) {
                    range = ranges[i];
                } else if (range.endsAfterStartOf(ranges[i])) {
                    range = range.merge(ranges[i]);
                } else {
                    result.add(range);
                    range = ranges[i];
                }
            } else if (range != null) {
                result.add(range);
                range = null;
            }
        }
        return result;
    }

    private synchronized void suggestNewCandidates() {
        List<TimeRange> preferredTimeRange = getPreferredTimeRange();
        Calendar startDay = (Calendar) startDateSpinner.getSelectedItem();
        Calendar endDay = (Calendar) endDateSpinner.getSelectedItem();
        if (preferredTimeRange.size() == 0 || endDay.before(startDay)) {
            HachikoLogger.debug("ignore invalid date or time input");
            return;
        }
        int numOfCandidateDates = IntegerUtils.parseIntWithDefault(
                HachikoPreferences.getDefault(this).getString(
                        HachikoPreferences.KEY_NUMBER_OF_CANDIDATE_DATES,
                        Integer.toString(HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_DEFAULT)),
                HachikoPreferences.NUMBER_OF_CANDIDATE_DATES_DEFAULT);
        VacancyRequest.Param param = new VacancyRequest.Param(
                Arrays.asList(friendIds), preferredTimeRange, startDay, endDay,
                dateRangeRadioGroup.getCheckedRadioButtonId() == R.id.date_range_asap,
                durationMin,
                numOfCandidateDates
        );
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
        HachikoApp.defaultRequestQueue().cancelAll(HachikoAPI.TAG_VACANCY_REQUEST);
        vacancyRequest.setTag(HachikoAPI.TAG_VACANCY_REQUEST);
        HachikoApp.defaultRequestQueue().add(vacancyRequest);
        lastRequestedVacancyParam = param;
    }

    private synchronized void clearTimeSlots() {
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

    private class OnDurationSelectedListener extends DefaultSpinnerItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == durationOptions.size() - 1) {
                durationMin = 3 * 60;
                TimePickerDialog durationPicker = new TimePickerDialog(
                        CreatePlanActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        durationMin = hourOfDay * 60 + minute;
                    }
                }, 3, 0, true) {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        super.onTimeChanged(view, hourOfDay, minute);
                        durationMin = hourOfDay * 60 + minute;
                        setTitle("予定の長さ");
                    }
                };
                durationPicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        synchronized (durationAdapter) {
                            durationOptions.remove(durationOptions.size() - 1);
                            durationOptions.add(
                                    (durationMin < 60 ? "" : ((durationMin / 60) + "時間"))
                                            + (durationMin % 60) + "分");
                            durationAdapter.notifyDataSetChanged();
                        }
                        suggestNewCandidates();
                    }
                });
                durationPicker.setTitle("予定の長さ");
                durationPicker.show();
            } else {
                durationMin = TEXT_TO_MIN.get(durationSpinner.getSelectedItem());
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
