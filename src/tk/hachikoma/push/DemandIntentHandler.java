package tk.hachikoma.push;

import android.app.PendingIntent;
import android.content.Context;
import tk.hachikoma.data.Plan;
import tk.hachikoma.db.PlansTableHelper;
import tk.hachikoma.db.UserTableHelper;
import tk.hachikoma.plans.PlanListActivity;
import com.google.common.collect.ImmutableList;

/**
 * 回答しろ！とおこられた
 */
public class DemandIntentHandler extends GcmIntentHandlerBase<String> {
    private final PlansTableHelper plansTableHelper;
    private final UserTableHelper userTableHelper;

    public DemandIntentHandler(Context context) {
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
                ownerName + "さんが回答を催促しています",
                pendingIntent);
    }
}
