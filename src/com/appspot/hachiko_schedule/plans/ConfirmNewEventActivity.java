package com.appspot.hachiko_schedule.plans;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.MainActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.EventCategory;
import com.appspot.hachiko_schedule.data.FriendIdentifier;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.util.GmailSendHelper;
import com.appspot.hachiko_schedule.util.HachikoLogger;

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
        int eventType = intent.getIntExtra(Constants.EXTRA_KEY_EVENT_TYPE, -1);
        showFriendsName(friends);
        showEventInfo(eventType);
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
                        sendInvitations(friends, timeslots);
                        Intent intent = new Intent(ConfirmNewEventActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.EXTRA_KEY_NEW_EVENT, true);
                        startActivity(intent);
                    }
                }
        );
    }

    private void sendInvitations(Parcelable[] friends, Parcelable[] timeslots) {
        for (Parcelable friendParcelable: friends) {
            FriendIdentifier friend = (FriendIdentifier) friendParcelable;
            if (friend.getEmail() != null) {
                sendEmailInvitationByEmail(friend, timeslots);
            } else {
                // TODO:
                HachikoLogger.debug("Not implemented: send invitation by hachiko server");
            }
        }
    }

    private void sendEmailInvitationByEmail(FriendIdentifier friend, Parcelable[] timeslots) {
        // TODO: テンプレートエンジンの利用を検討, MiniTemplatorとか。
        StringBuilder emailBuilder = new StringBuilder();
        emailBuilder.append(friend.getName()).append("さん、")
                .append(System.getProperty("line.separator"))
                .append("以下の時間帯で招待が届きました")
                .append(System.getProperty("line.separator"));
        for (Parcelable timeslotParcel: timeslots) {
            Timeslot timeslot = (Timeslot) timeslotParcel;
            emailBuilder.append(timeslot.getStartDate())
                    .append(" - ")
                    .append(timeslot.getEndDate())
                    .append(System.getProperty("line.separator"));
        }
        GmailSendHelper gmailSendHelper = new GmailSendHelper(this);
        HachikoLogger.debug("Email sent to ", friend.getEmail());
        gmailSendHelper.sendHtmlMailAsync(
                "Hachiko invitation", emailBuilder.toString(), friend.getEmail());
    }

    private void showEventInfo(int eventType) {
        if (eventType < 0) {
            return;
        }

        EventCategory eventCategory = EventCategory.values()[eventType];
        ((ImageView) findViewById(R.id.confirm_event_event_category_img))
                .setImageResource(eventCategory.getIconResourceId());
        ((TextView) findViewById(R.id.confirm_event_event_category_text))
                .setText(eventCategory.getSimpleDescription());
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
