package com.appspot.hachiko_schedule.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * (8koma外で作られた)予定を表すデータクラス
 */
public class Event {
    private static final DateFormat START_DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm");
    private static final DateFormat END_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private final String title;
    private final Date start;
    private final Date end;

    public Event(String title, Date start, Date end) {
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public String getTitle() {
        return title;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return START_DATE_FORMAT.format(start) + " - " + END_DATE_FORMAT.format(end) + ": " + title;
    }
}
