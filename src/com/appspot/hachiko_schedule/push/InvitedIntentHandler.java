package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.apis.HachiJsonObjectRequest;
import com.appspot.hachiko_schedule.apis.HachiRequestQueue;
import com.appspot.hachiko_schedule.apis.HachikoAPI;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.appspot.hachiko_schedule.util.JSONUtils;
import com.google.common.base.Joiner;
import org.json.JSONArray;
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
            long ownerIds = body.getLong("owner");
            List<Long> friendIds = JSONUtils.toList(body.getJSONArray("friendsId"));
            if (!friendIds.contains(ownerIds)) {
                friendIds.add(ownerIds);
            }
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
            Long planId = body.getLong("planId");
            String title = body.getString("title");
            List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
            JSONArray array = body.getJSONArray("candidates");
            for (int i = 0; i < array.length(); i++) {
                JSONObject candidateDateJson = array.getJSONObject(i);
                JSONObject timeRange = candidateDateJson.getJSONObject("time");
                candidateDates.add(new CandidateDate(
                        candidateDateJson.getInt("id"),
                        DateUtils.parseISO8601(timeRange.getString("start")),
                        DateUtils.parseISO8601(timeRange.getString("end")),
                        CandidateDate.AnswerState.NEUTRAL));
            }
            PlansTableHelper tableHelper = new PlansTableHelper(getContext());
            long hostId = Long.parseLong(body.getString("owner"));
            long myHachikoId = HachikoPreferences.getMyHachikoId(getContext());
            if (myHachikoId != hostId) {
                tableHelper.insertNewPlan(
                        planId, title, hostId, false, friendIds, candidateDates);
            } else {
                for (CandidateDate candidateDate: candidateDates) {
                    tableHelper.updateAnswerIdByStartDate(
                            planId, candidateDate.getStartDate(), candidateDate.getAnswerId());
                }
            }
            updateAttendanceInfo(planId, myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(getContext(), PlanListActivity.class));
            if (myHachikoId != hostId) {
                putNotification("イベントへの招待が届きました", title, pendingIntent);
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
