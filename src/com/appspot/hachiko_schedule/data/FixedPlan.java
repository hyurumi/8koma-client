package com.appspot.hachiko_schedule.data;

import android.content.Context;

public class FixedPlan extends Plan {
    private final CandidateDate date;

    public FixedPlan(long planId, String title, boolean isFixed, CandidateDate date) {
        super(planId, title, true, isFixed);
        this.date = date;
    }

    public String getDateText() {
        return date.getDateText();
    }

    public String getPositiveFriendNames(Context context) {
        return date.getPositiveFriendNames(context);
    }
}
