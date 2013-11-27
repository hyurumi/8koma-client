package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.Response;
import com.appspot.hachiko_schedule.apis.base_requests.HachiJsonArrayRequest;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.plans.PlanUpdateHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchPlansRequest extends HachiJsonArrayRequest {
    public interface PlansUpdateListener {
        public void onPlansUpdated();
    }

    public FetchPlansRequest(Context context, PlansUpdateListener listener, Response.ErrorListener errorListener) {
        super(context,
                HachikoAPI.Plan.GET_PLANS.getMethod(),
                HachikoAPI.Plan.GET_PLANS.getUrl(),
                (String) null,
                new UpdatePlanTableListener(context, listener),
                errorListener);
    }

    private static class UpdatePlanTableListener implements Response.Listener<JSONArray> {
        private final PlansUpdateListener listener;
        private final PlansTableHelper plansTableHelper;
        private final Context context;

        private UpdatePlanTableListener(Context context, PlansUpdateListener listener) {
            this.context = context;
            plansTableHelper = new PlansTableHelper(context);
            this.listener = listener;
        }

        @Override
        public void onResponse(JSONArray plans) {
            for (int i = 0; i < plans.length(); i++) {
                try {
                    JSONObject json = plans.getJSONObject(i);
                    Plan plan = PlanResponseParser.parsePlan(json);
                    long myId = HachikoPreferences.getMyHachikoId(context);
                    if (!plansTableHelper.planExists(plan.getPlanId())) {
                        plansTableHelper.insertNewPlan(
                                plan,
                                myId,
                                PlanResponseParser.parseFriendIds(json, true),
                                PlanResponseParser.parseCandidateDates(json, CandidateDate.AnswerState.NEUTRAL));
                    }
                    PlanUpdateHelper.updateAttendanceInfo(
                            context, plan.getPlanId(), myId, json.getJSONArray("attendance"));
                    if (plan.isFixed() && !plansTableHelper.isFixed(plan.getPlanId())) {
                        long answerId = json.getLong("confirmed");
                        plansTableHelper.confirmCandidateDate(plan.getPlanId(), answerId);
                    }
                } catch (JSONException e) {
                    HachikoLogger.error(plans.toString(), e);
                }
            }
            if (listener != null) {
                listener.onPlansUpdated();
            }
        }
    }
}
