package com.appspot.hachiko_schedule.push;

import android.content.Context;
import com.appspot.hachiko_schedule.plans.PlanUpdateHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 友人が予定に回答した
 */
public class FriendRespondedIntentHandler extends GcmIntentHandlerBase<JSONObject> {
    public FriendRespondedIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void handle(JSONObject body) {
        try {
            Long planId = body.getLong("planId");
            long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
            PlanUpdateHelper.updateAttendanceInfo(
                    getContext(), planId, myHachikoId, body.getJSONArray("attendance"));
            HachikoLogger.debug("Updated friend response for ", planId);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }
}
