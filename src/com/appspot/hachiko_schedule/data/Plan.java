package com.appspot.hachiko_schedule.data;

import android.content.Context;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/**
 * 予定を表すデータクラス
 */
public class Plan {
    private final long planId;
    private final String title;
    private final boolean isFixed;
    private final long ownerId;

    public Plan(long planId, String title, long ownerId, boolean isFixed) {
        this.planId = planId;
        this.title = title;
        this.isFixed = isFixed;
        this.ownerId = ownerId;
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

    public String getOwnerName(Context context) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        return Iterables.get(
                userTableHelper.getFriendsNameForHachikoIds(Arrays.asList(new Long[]{ownerId})),
                0);
    }

    public boolean isHost(Context context) {
        return HachikoPreferences.getMyHachikoId(context) == ownerId;
    }
}
