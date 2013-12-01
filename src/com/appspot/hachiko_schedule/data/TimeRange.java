package com.appspot.hachiko_schedule.data;

/**
 * 05:30 - 09:10とかそういう，時間はばを表すやつ
 */
public class TimeRange {
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinutes;

    public TimeRange(String timeRangeString) {
        String[] timerange = timeRangeString.split(":|-");
        if (timerange.length != 4) {
            throw new IllegalStateException("Invalid format: " + timeRangeString);
        }
        init(Integer.parseInt(timerange[0].trim()),
                Integer.parseInt(timerange[1].trim()),
                Integer.parseInt(timerange[2].trim()),
                Integer.parseInt(timerange[3].trim()));
    }

    private void init(int startHour, int startMinute, int endHour, int endMinutes) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinutes = endMinutes;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinutes() {
        return endMinutes;
    }
}
