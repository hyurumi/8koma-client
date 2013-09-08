package com.appspot.hachiko_schedule.plans;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import com.appspot.hachiko_schedule.data.CalendarIdentifier;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;

import java.util.*;

/**
 * Utility class to retrieve events data from calendar.
 */
public class EventManager {
    private Context context;
    private final ContentResolver contentResolver;

    public EventManager(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    /**
     * @return List of events contains all events which start in the future,
     * ordered
     */
    public List<Timeslot> queryAllForthcomingEvent() {
        final String[] EVENT_PROJECTION = {Events.DTSTART, Events.DTEND, Events.ALL_DAY};
        final int PROJECTION_DATE_START_INDEX = 0;
        final int PROJECTION_DATE_END_INDEX = 1;
        final int PROJECTION_IS_ALL_DAY_INDEX = 2;

        Set<String> calendars = HachikoPreferences.getDefault(context)
                .getStringSet(HachikoPreferences.KEY_CALENDARS_TO_USE, HachikoPreferences.CALENDARS_TO_USE_DEFAULT);
        StringBuilder selection = new StringBuilder();
        selection.append("(").append(Events.DTSTART).append(" > ?) AND (")
                .append(Events.CALENDAR_ID).append(" IN (");
        boolean first = true;
        for (String calendarIdentifier: calendars) {
            if (first) {
                first = false;
            } else {
                selection.append(',');
            }
            selection.append(CalendarIdentifier.decode(calendarIdentifier).getId());
        }
        selection.append("))");

        Cursor cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                selection.toString(),
                new String[]{Long.toString(new Date().getTime())},
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

    /**
     * 端末に登録されているカレンダーの一覧を返す (自分のカレンダーの他，自分のアカウントに紐付いている，
     * 日本の祝日，研究室カレンダーなど...）
     */
    public List<CalendarIdentifier> getCalenders() {
        final String[] projetion = new String[] {
                CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        int projectionIdIndex = 0;
        int projectionDisplayNameIndex = 1;

        List<CalendarIdentifier> calendars = new ArrayList<CalendarIdentifier>();
        Cursor cur = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, projetion, null, null, null);
        while (cur.moveToNext()) {
            long calID = cur.getLong(projectionIdIndex);
            String displayName = cur.getString(projectionDisplayNameIndex);
            calendars.add(new CalendarIdentifier(calID, displayName));
        }
        return calendars;
    }
}