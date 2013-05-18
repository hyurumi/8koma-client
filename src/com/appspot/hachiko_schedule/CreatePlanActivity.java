package com.appspot.hachiko_schedule;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import com.appspot.hachiko_schedule.data.Event;
import com.appspot.hachiko_schedule.data.Friend;
import com.appspot.hachiko_schedule.util.HachikoLogger;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * {@link Activity} for creating new plan.
 */
public class CreatePlanActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        ((Button) findViewById(R.id.ok_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatePlanActivity.this.finish();
            }
        });

        Parcelable[] friends = getIntent().getParcelableArrayExtra(Constants.EXTRA_KEY_FRIENDS);
        HachikoLogger.debug(friends.toString());
        for (Parcelable friend: friends) {
            HachikoLogger.debug(((Friend) friend).getName());
        }
        debugQueryEvents();
    }

    private void debugQueryEvents() {
        List<Event> events = new EventManager(this).queryAllForecomingEvent();
        HachikoLogger.debug(events.size() + " events are registered");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        for (Event event: events) {
            HachikoLogger.debug(dateFormat.format(event.getStartDate())
                    + "-" + timeFormat.format(event.getEndDate()));
        }
    }
}
