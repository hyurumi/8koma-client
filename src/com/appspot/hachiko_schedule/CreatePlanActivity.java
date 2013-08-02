package com.appspot.hachiko_schedule;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.data.Friend;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.NotImplementedActivity;
import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * {@link Activity} for creating new plan.
 */
public class CreatePlanActivity extends Activity {
    private GridView eventIcons;
    private Spinner dayAfterSpinner;
    private Spinner timeWordsSpinner;
    private Spinner durationSpinner;
    private ViewGroup schedulesContainer;
    private Button inviteButton;
    private ScheduleSuggester scheduleSuggester = new ScheduleSuggester();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);
        eventIcons = (GridView) findViewById(R.id.event_icon_list);
        eventIcons.setAdapter(new EventIconsAdapter());
        dayAfterSpinner = (Spinner) findViewById(R.id.days_after_spinner);
        timeWordsSpinner = (Spinner) findViewById(R.id.time_words_spinner);
        durationSpinner = (Spinner) findViewById(R.id.duration_spinner);
        schedulesContainer = (ViewGroup) findViewById(R.id.schedules);
        inviteButton = (Button) findViewById(R.id.invite_button);

        dayAfterSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());
        timeWordsSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());
        durationSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());

        Parcelable[] friends =
                getIntent().getParcelableArrayExtra(Constants.EXTRA_KEY_FRIENDS);
        Preconditions.checkNotNull(friends);
        Preconditions.checkState(friends.length != 0);
        showFriendsName(friends);

        debugQueryEvents();

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(NotImplementedActivity.getIntentWithMessage(
                        CreatePlanActivity.this, "確認ページをつくる"));
            }
        });
    }

    private void showFriendsName(Parcelable[] friends) {
        StringBuilder friendsNameToInvite = new StringBuilder();
        for (Parcelable friend: friends) {
            friendsNameToInvite.append(((Friend) friend).getName()).append(" ");
        }
        ((TextView) findViewById(R.id.friends_name_to_invite))
                .setText(friendsNameToInvite.toString());
    }

    private void debugQueryEvents() {
        List<Timeslot> events = new EventManager(this).queryAllForecomingEvent();
        HachikoLogger.debug(events.size() + " events are registered");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        for (Timeslot event: events) {
            HachikoLogger.debug(dateFormat.format(event.getStartDate())
                    + "-" + timeFormat.format(event.getEndDate()));
        }
    }

    private class OnSpinnerItemSelectedListener
            implements Spinner.OnItemSelectedListener {
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
            for (Timeslot schedule: schedules) {
                TextView scheduleView = new TextView(CreatePlanActivity.this);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
                scheduleView.setText(dateFormat.format(schedule.getStartDate())
                        + " - " + dateFormat.format(schedule.getEndDate()));
                schedulesContainer.addView(scheduleView);
            }
            schedulesContainer.setVisibility(View.VISIBLE);
            inviteButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }

    private final class EventIconsAdapter extends BaseAdapter {
        private final int[] EVENT_ICONS = new int[] {
                R.drawable.ic_beer, R.drawable.ic_business, R.drawable.ic_cafe, R.drawable.ic_movie,
                R.drawable.ic_school, R.drawable.ic_shopping};

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
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(CreatePlanActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(EVENT_ICONS[position]);
            return imageView;
        }
    }
}
