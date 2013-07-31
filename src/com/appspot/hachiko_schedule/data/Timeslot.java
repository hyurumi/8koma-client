package com.appspot.hachiko_schedule.data;

import java.util.Date;
import java.util.List;

/**
 * あるタイムスロット(何時から何時),あるいは終日の時間幅を示すデータクラス
 */
public class Timeslot {
    private final Date startDate;
    private final Date endDate;
    private final boolean isAllDay;

    public Timeslot(Date startDate, Date endDate, boolean isAllDay) {
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
