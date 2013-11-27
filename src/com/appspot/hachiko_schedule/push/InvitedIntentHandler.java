package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.apis.HachiRequestQueue;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.apis.PlanResponseParser;
import com.appspot.hachiko_schedule.apis.base_requests.HachiJsonObjectRequest;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.plans.PlanUpdateHelper;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * invited
 */
public class InvitedIntentHandler extends GcmIntentHandlerBase<JSONObject> {
    public InvitedIntentHandler(Context context) {
       super(context);
    }

    @Override
    public void handle(JSONObject body) {
        try {
            List<Long> friendIds = PlanResponseParser.parseFriendIds(body, true);
            if (!requestUnknownFriendInfo(body, friendIds)) {
                // もし上の条件がfalseなら，notificationは通信が終了したあと非同期で追加される．
                sendInviteNotification(body, friendIds);
            }
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }

    private void sendInviteNotification(JSONObject body, List<Long> friendIds) {
        try {
            List<CandidateDate> candidateDates = PlanResponseParser.parseCandidateDates(
                    body, CandidateDate.AnswerState.NEUTRAL);
            PlansTableHelper tableHelper = new PlansTableHelper(getContext());
            Plan plan = PlanResponseParser.parsePlan(body);
            long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
            if (myHachikoId != plan.getOwnerId()) {
                tableHelper.insertNewPlan(plan, myHachikoId, friendIds, candidateDates);
            } else {
                for (CandidateDate candidateDate: candidateDates) {
                    tableHelper.updateAnswerIdByStartDate(
                            plan.getPlanId(), candidateDate.getStartDate(), candidateDate.getAnswerId());
                }
            }
            PlanUpdateHelper.updateAttendanceInfo(
                    getContext(), plan.getPlanId(), myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(getContext(), PlanListActivity.class));
            if (myHachikoId != plan.getOwnerId()) {
                putNotification("イベントへの招待が届きました", plan.getTitle(), pendingIntent);
            }
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }

    /**
     * 渡されたIDの中に知らないものがあれば，サーバに問い合わせる
     *
     * @return 問い合わせが発生したかどうか
     */
    private boolean requestUnknownFriendInfo(final JSONObject body, final List<Long> friendIds) {
        UserTableHelper userTableHelper = new UserTableHelper(getContext());
        long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
        if (friendIds.contains(myHachikoId)) {
            friendIds.remove(myHachikoId);
        }
        Map<Long, String> idToName = userTableHelper.getIdToNameMap(friendIds);
        if (idToName.size() == friendIds.size()) {
            return false;
        }

        final List<Long> unknownIds = new ArrayList<Long>(friendIds.size() - idToName.size());
        for (Long id: friendIds) {
            if (!idToName.containsKey(id)) {
                unknownIds.add(id);
            }
        }
        String url = HachikoAPI.User.GET_NAMES.getUrl() + "?userIds=" + Joiner.on(",").join(unknownIds);
        HachikoLogger.debug("unknown friend ids:", unknownIds, "asking server..");
        HachikoLogger.debug(url);
        RequestQueue queue = new HachiRequestQueue(getContext());
        Request request = new HachiJsonObjectRequest(
                getContext(),
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject dictionary) {
                        try {
                            UserTableHelper userTableHelper
                                    = new UserTableHelper(getContext());
                            Iterator<String> userIds = dictionary.keys();
                            Map<Long, String> names = new HashMap<Long, String>();
                            while (userIds.hasNext()) {
                                String userId = userIds.next();
                                names.put(Long.parseLong(userId), dictionary.getString(userId));
                            }
                            HachikoLogger.debug("name resolved", names);
                            userTableHelper.persistNonFriendName(names);
                        } catch (JSONException e) {
                            HachikoLogger.error("json error " + dictionary, e);
                        } finally {
                            sendInviteNotification(body, friendIds);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("get non-friend name ", volleyError);
                        sendInviteNotification(body, friendIds);
                    }
                });
        queue.start();
        queue.add(request);
        return true;
    }

}
