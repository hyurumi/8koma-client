package com.appspot.hachiko_schedule.data;

import java.util.Date;
import java.util.List;

/**
 * 確定したイベント
 */
public class SettledEvent extends Event {
    private List<String> participants;
    private Date when;

    public SettledEvent(String title, EventCategory category, List<String> participants,
                        Date when, int durationInMinutes) {
        super(title, category, durationInMinutes);
        this.participants = participants;
        this.when = when;
    }

    public Date getWhen() {
        return when;
    }

    public List<String> getParticipants() {
        return participants;
    }
}
