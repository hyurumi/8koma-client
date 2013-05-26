package com.appspot.hachiko_schedule;

import com.appspot.hachiko_schedule.data.Event;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * This class suggests appropriate schedule based on user information.
 */
public class ScheduleSuggester {
    public List<Event> suggestTimeSlot(
            TimeWords timeWords, int DurationMin, int scheduleCanStartAfterDays) {
        // TODO: consider calendar
        // TODO: clever algorithm
        Map<TimeWords, Integer> beginAt =
                new ImmutableMap.Builder<TimeWords, Integer>()
                        .put(TimeWords.MORNING, 9)
                        .put(TimeWords.AFTERNOON, 12)
                        .put(TimeWords.NIGHT, 18)
                        .build();
        List<Event> timeSlots = new ArrayList<Event>();

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, beginAt.get(timeWords));
        start.set(Calendar.MINUTE, 0);
        start.add(Calendar.DAY_OF_MONTH, scheduleCanStartAfterDays);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MINUTE, DurationMin);

        for (int i = 0; i < 3; i++) {
            timeSlots.add(new Event(start.getTime(), end.getTime(), false));
            start.add(Calendar.DAY_OF_MONTH, 1);
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        return timeSlots;
    }
}
