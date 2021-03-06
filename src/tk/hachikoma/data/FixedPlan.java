package tk.hachikoma.data;

import android.content.Context;

public class FixedPlan extends Plan {
    private final CandidateDate date;

    public FixedPlan(long planId, String title, long ownerId, CandidateDate date) {
        super(planId, title, ownerId, true);
        this.date = date;
    }

    public String getDateText() {
        return date.getDateText();
    }

    public String getPositiveFriendNames(Context context) {
        return date.getPositiveFriendNames(context, getOwnerId());
    }
}
