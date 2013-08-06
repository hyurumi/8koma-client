package com.appspot.hachiko_schedule;

import android.content.Context;
import com.appspot.hachiko_schedule.data.TimeWords;
import com.appspot.hachiko_schedule.data.Timeslot;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * This class suggests appropriate schedule based on user information.
 */
public class ScheduleSuggester {
    private static final Map<TimeWords, Integer> BEGINS_AT =
            new ImmutableMap.Builder<TimeWords, Integer>()
                    .put(TimeWords.MORNING, 9)
                    .put(TimeWords.AFTERNOON, 12)
                    .put(TimeWords.NIGHT, 18)
                    .build();
    private static final int NUM_OF_SUGGESTING = 3;
    private static final int NUM_OF_SPARE = 2;

    private Queue<Timeslot> spareTimeslots = new LinkedList<Timeslot>();
    private Set<Timeslot> inconvenientTimeslots = new HashSet<Timeslot>();
    private EventManager eventManager;

    public ScheduleSuggester(Context context) {
        eventManager = new EventManager(context);
    }

    public List<Timeslot> suggestTimeSlot(
            TimeWords timeWords, int DurationMin, int scheduleCanStartAfterDays) {
        // TODO: clever algorithm

        List<Timeslot> timeSlots = new ArrayList<Timeslot>();

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, BEGINS_AT.get(timeWords));
        start.set(Calendar.MINUTE, 0);
        start.add(Calendar.DAY_OF_MONTH, scheduleCanStartAfterDays);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MINUTE, DurationMin);
        Calendar originalStart = (Calendar) start.clone();
        Calendar originalEnd = (Calendar) end.clone();

        List<Timeslot> knownEvents = eventManager.queryAllForthcomingEvent();
        knownEvents.addAll(inconvenientTimeslots);

        fillTimeslots(NUM_OF_SUGGESTING, timeSlots, originalStart, originalEnd, start, end,
                knownEvents, timeWords);

        spareTimeslots.clear();
        fillTimeslots(NUM_OF_SPARE, spareTimeslots, originalStart, originalEnd, start, end,
                knownEvents, timeWords);
        return timeSlots;
    }

    /**
     * ユーザが「都合が悪い」とした時間帯を通知
     * いまのところは永続化しない (永続的にその時間が都合が悪いならカレンダーになにかしら予定が入るはず)
     */
    public void notifyInconvenientTimeslot(Timeslot timeslot) {
        inconvenientTimeslots.add(timeslot);
    }

    public Timeslot popNextRecommendedTimeslot() {
        if (spareTimeslots.isEmpty()) {
            return null;
        }
        return spareTimeslots.poll();
    }

    private void searchForNextAvailableTimeslot(
            Calendar start, Calendar end, Collection<Timeslot> knownEvents, int defaultStartTime) {
        while (canConflict(knownEvents, start, end)) {
            start.add(Calendar.HOUR, 1);
            end.add(Calendar.HOUR, 1);
            if (start.get(Calendar.HOUR) - defaultStartTime == 5) {
                start.set(Calendar.HOUR, start.get(Calendar.HOUR) - 5);
                end.set(Calendar.HOUR, end.get(Calendar.HOUR) - 5);
                start.add(Calendar.DAY_OF_MONTH, 1);
                end.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return;
    }

    private void addDayWithOriginalHour(
            Calendar originalStart, Calendar originalEnd, Calendar start, Calendar end) {
        start.add(Calendar.DAY_OF_MONTH, 1);
        end.add(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR, originalStart.get(Calendar.HOUR));
        end.set(Calendar.HOUR, originalEnd.get(Calendar.HOUR));
    }

    private void fillTimeslots(
            int numToFill, Collection<Timeslot> timeslots, Calendar originalStart, Calendar originalEnd,
            Calendar start, Calendar end, Collection<Timeslot> knownEvents, TimeWords timeWords) {
        for (int i = 0; i < numToFill; i++) {
            searchForNextAvailableTimeslot(start, end, knownEvents, BEGINS_AT.get(timeWords));
            timeslots.add(new Timeslot(start.getTime(), end.getTime(), false));
            addDayWithOriginalHour(originalStart, originalEnd, start, end);
        }
    }

    /**
     * @return どれかひとつでもconflictならtrue
     */
    private boolean canConflict(Collection<Timeslot> knownEvents, Calendar start, Calendar end) {
        for (Timeslot knownEvent: knownEvents) {
            if (canConflict(knownEvent, start, end)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 予定が時間的に重複しうるならtrue
     */
    private boolean canConflict(Timeslot timeslot, Calendar start, Calendar end) {
        Date startDate = timeslot.getStartDate();
        Date endDate = timeslot.getEndDate();
        if (timeslot.isAllDay()
                && (isSameDay(startDate, start) || isSameDay(startDate, end))) {
            return true;
        }
        if (endDate.compareTo(start.getTime()) < 0) {
            // はじまるより早く終わる
            return false;
        } else if (startDate.compareTo(end.getTime()) > 0) {
            // 終わってからはじまる
            return false;
        } else {
            return true;
        }
    }

    private boolean isSameDay(Date d, Calendar c) {
        return d.getDate() == c.get(Calendar.DAY_OF_MONTH);
    }
}
