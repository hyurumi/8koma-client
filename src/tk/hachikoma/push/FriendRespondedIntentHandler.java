package tk.hachikoma.push;

import android.content.Context;
import tk.hachikoma.plans.PlanUpdateHelper;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.util.HachikoLogger;
import tk.hachikoma.util.JSONUtils;
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
                    getContext(), planId,
                    JSONUtils.toList(body.getJSONArray("friendsId")),
                    myHachikoId, body.getJSONArray("attendance"));
            HachikoLogger.debug("Updated friend response for ", planId);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }
}
