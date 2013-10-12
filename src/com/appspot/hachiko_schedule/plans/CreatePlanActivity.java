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
    private Spinner dayAfterSpinner;
    private Spinner timeWordsSpinner;
    private Spinner durationSpinner;
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
        dayAfterSpinner = (Spinner) findViewById(R.id.days_after_spinner);
        timeWordsSpinner = (Spinner) findViewById(R.id.time_words_spinner);
        durationSpinner = (Spinner) findViewById(R.id.duration_spinner);
        schedulesContainer = (ViewGroup) findViewById(R.id.schedules);
        confirmButton = (Button) findViewById(R.id.invite_button);

        dayAfterSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());
        timeWordsSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());
        durationSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());

        friends = getIntent().getParcelableArrayExtra(Constants.EXTRA_KEY_FRIENDS);
        Preconditions.checkNotNull(friends);
        Preconditions.checkState(friends.length != 0);
        showFriendsName(friends);
        friendIds = new Long[friends.length];
        for (int i = 0; i < friends.length; i++) {
            friendIds[i] = ((FriendIdentifier) friends[i]).getHachikoId();
        }

        debugQueryEvents();

        confirmButton.setOnClickListener(new View.OnClickListener() {
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
                content.append(eventTitleView.getText().toString())
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
        });
     }

    private void sendCreatePlanRequest() {
        JSONObject param = new JSONObject();
        final List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
        final String title = eventTitleView.getText().toString();
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.EXTRA_KEY_NEW_EVENT, true);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // TODO: 作った予定のキャンセルと，エラーメッセージ #53
                        progressDialog.hide();
                        new AlertDialog.Builder(CreatePlanActivity.this)
                                .setMessage("通信中にエラーが発生しました ("
                                        + volleyError.networkResponse.statusCode + ")")
                                .show();
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

    private void debugQueryEvents() {
        List<Timeslot> events = new EventManager(this).queryAllForthcomingEvent();
        HachikoLogger.debug(events.size() + " events are registered");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        for (Timeslot event: events) {
            HachikoLogger.debug(dateFormat.format(event.getStartDate())
                    + "-" + timeFormat.format(event.getEndDate()) + event.isAllDay());
        }
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

    private class OnSpinnerItemSelectedListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // reschedule.
            schedulesContainer.removeAllViews();
            int timeWordIndex = timeWordsSpinner.getSelectedItemPosition();
            String duration = (String) durationSpinner.getSelectedItem();
            String daysAfter = (String) dayAfterSpinner.getSelectedItem();
            if (timeWordIndex == AdapterView.INVALID_POSITION
                    || duration == null || daysAfter == null) {
                return;
            }

            List<Timeslot> schedules = scheduleSuggester.suggestTimeSlot(
                    TimeWords.values()[timeWordIndex],
                    Integer.parseInt(duration),
                    Integer.parseInt(daysAfter)
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
