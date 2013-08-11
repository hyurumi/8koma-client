package com.appspot.hachiko_schedule;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.data.EventCategory;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.TimeWords;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.ui.BorderedImageView;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.ui.SwipeToDismissTouchListener;
import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.appspot.hachiko_schedule.util.ViewUtils.removeView;

/**
 * {@link Activity} for creating new plan.
 */
public class CreatePlanActivity extends Activity {
    private GridView eventIcons;
    private Spinner dayAfterSpinner;
    private Spinner timeWordsSpinner;
    private Spinner durationSpinner;
    private ViewGroup schedulesContainer;
    private Set<Timeslot> suggestingTimeslots = new HashSet<Timeslot>();
    private int selectedEventId = -1;
    private Button confirmButton;
    private Parcelable[] friends;
    private ScheduleSuggester scheduleSuggester;
    private Map<View, Timeslot> viewToTimeslots = new HashMap<View, Timeslot>();
    private Handler hander = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        setTitle(R.string.create_event_detail_text);
        initEventIcons();

        scheduleSuggester = new ScheduleSuggester(this);
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

        debugQueryEvents();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitToConfirmView();
            }
        });

        initOtherOptions();
     }

    private void initEventIcons() {
        eventIcons = (GridView) findViewById(R.id.event_icon_list);
        eventIcons.setAdapter(new EventIconsAdapter());
        eventIcons.setOnItemClickListener(new EventIconClickListener());
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

    private void initOtherOptions() {
        findViewById(R.id.create_event_add_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        CreatePlanActivity.this,
                        "未実装: メモが追加出来るようになる予定",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        findViewById(R.id.create_event_add_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreatePlanActivity.this,
                        "未実装: 地図が追加出来るようになる予定",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });


        findViewById(R.id.create_event_add_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreatePlanActivity.this,
                        "未実装: ファイルが追加出来るようになる予定",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private View addNewScheduleTextView(Timeslot schedule) {
        TextView scheduleView = new TextView(CreatePlanActivity.this);
        SimpleDateFormat startDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat endDateFormat = new SimpleDateFormat("HH:mm");
        scheduleView.setText(startDateFormat.format(schedule.getStartDate())
                + " - " + endDateFormat.format(schedule.getEndDate()));
        viewToTimeslots.put(scheduleView, schedule);

        // TODO: DPIを考慮した実装 (setPaddingの引数はpx指定)
        scheduleView.setPadding(15, 7, 5, 7);
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

    private void transitToConfirmView() {
        Intent intent = new Intent(this, ConfirmNewEventActivity.class);
        intent.putExtra(Constants.EXTRA_KEY_FRIENDS, friends);
        intent.putExtra(Constants.EXTRA_KEY_EVENT_CANDIDATE_SCHEDULES,
                suggestingTimeslots.toArray(new Parcelable[0]));
        if (selectedEventId > 0) {
            intent.putExtra(Constants.EXTRA_KEY_EVENT_TYPE, selectedEventId);
        }
        startActivity(intent);
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

    private final class EventIconsAdapter extends BaseAdapter {
        // TODO: fix here
        // アイコン素材集めるのがめんどいので，アイコンが存在する6つだけ表示している
        private final int[] EVENT_ICONS = new int[6];
        {
            for (int i = 0; i < 6; i++) {
                EVENT_ICONS[i] = EventCategory.values()[i].getIconResourceId();
            }
        }

        public int getCount() {
            return EVENT_ICONS.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            BorderedImageView imageView;
            if (convertView == null) {
                imageView = new BorderedImageView(CreatePlanActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setBorderColor(Color.WHITE);
            } else {
                imageView = (BorderedImageView) convertView;
            }
            imageView.setImageResource(EVENT_ICONS[position]);
            return imageView;
        }
    }

    private class EventIconClickListener implements AdapterView.OnItemClickListener {
        private BorderedImageView selectedImageView;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (selectedImageView != null) {
                selectedImageView.setBorderColor(Color.WHITE);
            }
            selectedImageView = (BorderedImageView) view;
            selectedImageView.setBorderColor(Color.BLACK);
            selectedEventId = position;
        }
    }
}
