package com.appspot.hachiko_schedule.data;

import com.google.common.collect.Multimap;

import java.util.Date;

/**
 * 未確定のイベント
 */
public class UnsettledEvent extends Event {
    private Multimap<Date, String> dayToParticipants;

    public UnsettledEvent(String title, EventCategory category, int durationInMinutes,
                          Multimap<Date, String> dayToParticipants) {
        super(title, category,  durationInMinutes);
        this.dayToParticipants = dayToParticipants;
    }

    public Multimap<Date, String> getDayToParticipants() {
        return dayToParticipants;
    }
}
