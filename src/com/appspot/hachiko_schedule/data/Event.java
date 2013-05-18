package com.appspot.hachiko_schedule.data;

import java.util.Date;

/**
 * Date class for single event.
 * Event has start/end date or its a all-day event.
 */
public class Event {
    private final Date startDate;
    private final Date endDate;
    private final boolean isAllDay;

    public Event(Date startDate, Date endDate, boolean isAllDay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isAllDay() {
        return isAllDay;
    }
}
