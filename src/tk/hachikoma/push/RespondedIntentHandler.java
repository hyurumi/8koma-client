package tk.hachikoma.push;

import android.app.PendingIntent;
import android.content.Context;
import tk.hachikoma.plans.PlanListActivity;
import tk.hachikoma.plans.PlanUpdateHelper;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.util.HachikoLogger;
import tk.hachikoma.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class RespondedIntentHandler extends GcmIntentHandlerBase<JSONObject> {
    public RespondedIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void handle(JSONObject body) {
        try {
            String title = body.getString("title");
            Long planId = body.getLong("planId");
            long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
            PlanUpdateHelper.updateAttendanceInfo(
                    getContext(), planId,
                    JSONUtils.toList(body.getJSONArray("friendsId")),
                    myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent = getActivityIntent(
                    PlanListActivity.getIntentForUnfixedHost(getContext()));
            PlanListActivity.sendBroadcastForUpdatePlan(getContext(),
                    PlanListActivity.TAB_NAME_UNFIXED_HOST, title + "への回答が更新されました");
            putNotification(title + "に新しい回答が追加されました", "", pendingIntent);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }
}
