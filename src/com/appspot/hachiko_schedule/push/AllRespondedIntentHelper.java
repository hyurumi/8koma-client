package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.plans.PlanUpdateHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * allResponded
 */
public class AllRespondedIntentHelper extends GcmIntentHandlerBase<JSONObject> {
    public AllRespondedIntentHelper(Context context) {
        super(context);
    }

    @Override
    public void handle(JSONObject body) {
        try {
            String title = body.getString("title");
            Long planId = body.getLong("planId");
            long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
            PlanUpdateHelper.updateAttendanceInfo(
                    getContext(), planId, myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent = getActivityIntent(
                    PlanListActivity.getIntentForUnfixedHost(getContext()));
            putNotification("参加者から返答が届きました", title, pendingIntent);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }
}
