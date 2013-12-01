package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.google.common.collect.ImmutableList;

/**
 * 回答しろ！とおこられた
 */
public class RemindIntentHandler extends GcmIntentHandlerBase<String> {
    private final PlansTableHelper plansTableHelper;
    private final UserTableHelper userTableHelper;

    public RemindIntentHandler(Context context) {
        super(context);
        plansTableHelper = new PlansTableHelper(context);
        userTableHelper = new UserTableHelper(context);
    }

    @Override
    public void handle(String planId) {
        Plan plan = plansTableHelper.queryPlan(Long.parseLong(planId));
        PendingIntent pendingIntent = getActivityIntent(
                PlanListActivity.getIntentForUnfixedGuest(getContext()));
        String ownerName
                = userTableHelper.getIdToNameMap(ImmutableList.of(plan.getOwnerId())).get(plan.getOwnerId());
        putNotification(
                "「" + plan.getTitle() + "」に回答",
                ownerName + "さんが回答をリクエストしています",
                pendingIntent);
    }
}
