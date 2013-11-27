package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.plans.PlanUpdateHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
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
                    getContext(), planId, myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(getContext(), PlanListActivity.class));
            putNotification(title + "に新しい回答が追加されました", "", pendingIntent);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }
}
