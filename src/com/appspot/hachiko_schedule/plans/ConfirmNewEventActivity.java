package com.appspot.hachiko_schedule.plans;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.Timeslot;

import java.text.SimpleDateFormat;

public class ConfirmNewEventActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_event);
        setTitle(R.string.confirm_new_event_text);
        Intent intent = getIntent();
        final Parcelable[] friends = intent.getParcelableArrayExtra(Constants.EXTRA_KEY_FRIENDS);
        final Parcelable[] timeslots =
                intent.getParcelableArrayExtra(Constants.EXTRA_KEY_EVENT_CANDIDATE_SCHEDULES);
        showFriendsName(friends);
        ((TextView) findViewById(R.id.confirm_event_event_title))
                .setText(intent.getStringExtra(Constants.EXTRA_KEY_EVENT_TITLE));
        showTimeslots(timeslots);

        ((Button) findViewById(R.id.confirm_new_event_back)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        ((Button) findViewById(R.id.confirm_new_event_ok)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ConfirmNewEventActivity.this, EventListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.EXTRA_KEY_NEW_EVENT, true);
                        startActivity(intent);
                    }
                }
        );
    }

    // TODO: fix. 以下20行くらいCreatePlanActivityからのひどいコピペ
    private void showFriendsName(Parcelable[] friends) {
        StringBuilder friendsNameToInvite = new StringBuilder();
        for (Parcelable friend: friends) {
            friendsNameToInvite.append(((FriendIdentifier) friend).getName()).append(" ");
        }
        ((TextView) findViewById(R.id.friends_name_to_invite))
                .setText(friendsNameToInvite.toString());
    }

    private void showTimeslots(Parcelable[] timeslots) {
        ViewGroup container = (ViewGroup) findViewById(R.id.schedules);
        for (Parcelable timeslot: timeslots) {
            addNewScheduleTextView(container, (Timeslot) timeslot);
        }
    }

    private View addNewScheduleTextView(ViewGroup container, Timeslot schedule) {
        TextView scheduleView = new TextView(this);
        SimpleDateFormat startDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat endDateFormat = new SimpleDateFormat("HH:mm");
        scheduleView.setText(startDateFormat.format(schedule.getStartDate())
                + " - " + endDateFormat.format(schedule.getEndDate()));

        // TODO: DPIを考慮した実装 (setPaddingの引数はpx指定)
        scheduleView.setPadding(0, 3, 4, 7);
        scheduleView.setBackgroundColor(Color.WHITE);
        container.addView(scheduleView);
        return scheduleView;
    }
}
