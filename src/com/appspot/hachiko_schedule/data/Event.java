package com.appspot.hachiko_schedule.data;

/**
 * @author Kazuki Nishiura
 */
public abstract class Event {
    private String title;
    private EventCategory category;
    private int durationInMinutes;

    protected Event(String title, EventCategory category, int durationInMinutes) {
        this.title = title;
        this.category = category;
        this.durationInMinutes = durationInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public EventCategory getCategory() {
        return category;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }
}
