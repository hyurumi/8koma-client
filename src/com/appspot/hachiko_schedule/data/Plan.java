package com.appspot.hachiko_schedule.data;

/**
 * 予定を表すデータクラス
 */
public class Plan {
    private final long planId;
    private final String title;
    private final boolean isFixed;
    private final boolean isHost;

    public Plan(long planId, String title, boolean isHost, boolean isFixed) {
        this.planId = planId;
        this.title = title;
        this.isFixed = isFixed;
        this.isHost = isHost;
    }

    public long getPlanId() {
        return planId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean isHost() {
        return isHost;
    }
}
