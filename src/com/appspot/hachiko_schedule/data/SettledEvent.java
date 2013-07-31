package com.appspot.hachiko_schedule.data;

import java.util.Date;
import java.util.List;

/**
 * 確定したイベント
 */
public class SettledEvent {
    private String title;
    private EventCategory category;
    private List<String> participants;
    private Date when;
    private int durationInMinutes;

    public SettledEvent(String title, EventCategory category, List<String> participants, Date when,
                        int durationInMinutes) {
        this.title = title;
        this.category = category;
        this.participants = participants;
        this.when = when;
        this.durationInMinutes = durationInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public EventCategory getCategory() {
        return category;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public Date getWhen() {
        return when;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }
}
