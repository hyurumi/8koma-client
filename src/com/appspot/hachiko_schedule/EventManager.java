package com.appspot.hachiko_schedule;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import com.appspot.hachiko_schedule.data.Timeslot;

import java.util.*;

/**
 * Utility class to retrieve events data from calendar.
 */
public class EventManager {
    private Context context;

    public EventManager(Context context) {
        this.context = context;
    }

    /**
     * @return List of events contains all events which start in the future,
     * ordered
     */
    public List<Timeslot> queryAllForecomingEvent() {
        final String[] EVENT_PROJECTION = {Events.DTSTART, Events.DTEND, Events.ALL_DAY};
        final int PROJECTION_DATE_START_INDEX = 0;
        final int PROJECTION_DATE_END_INDEX = 1;
        final int PROJECTION_IS_ALL_DAY_INDEX = 2;
        String selection = "(" + Events.DTSTART + " > ?)";

        Cursor cursor = context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                selection,
                new String[] {Long.toString(new Date().getTime())},
                Events.DTSTART);

        List<Timeslot> events = new ArrayList<Timeslot>();
        if (!cursor.moveToFirst()) {
            return events;
        }

        while (cursor.moveToNext()) {
            events.add(new Timeslot(
                    new Date(cursor.getLong(PROJECTION_DATE_START_INDEX)),
                    new Date(cursor.getLong(PROJECTION_DATE_END_INDEX)),
                    cursor.getInt(PROJECTION_IS_ALL_DAY_INDEX) == 1));
        }
        return events;
    }
}
