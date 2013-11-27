package com.appspot.hachiko_schedule.apis;

import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * サーバーからやってくるPlanまわりの型をパースする
 */
public class PlanResponseParser {
    public static Plan parsePlan(JSONObject plan) throws JSONException {
        return new Plan(
                plan.getLong("planId"),
                plan.getString("title"),
                plan.getLong("owner"),
                !plan.isNull("confirmed")
        );
    }

    public static List<CandidateDate> parseCandidateDates(
            JSONObject plan, CandidateDate.AnswerState myState) throws JSONException {
        List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
        JSONArray candidatesJson = plan.getJSONArray("candidates");
        for (int i = 0; i < candidatesJson.length(); i++) {
            JSONObject candidateJson = candidatesJson.getJSONObject(i);
            JSONObject timeRange = candidateJson.getJSONObject("time");
            candidateDates.add(new CandidateDate(
                    candidateJson.getInt("id"),
                    DateUtils.parseISO8601(timeRange.getString("start")),
                    DateUtils.parseISO8601(timeRange.getString("end")),
                    myState
            ));
        }
        return candidateDates;
    }

    public static List<Long> parseFriendIds(JSONObject plan, boolean includeOwner) throws JSONException {
        List<Long> friendIds = JSONUtils.toList(plan.getJSONArray("friendsId"));
        long ownerId = plan.getLong("owner");
        if (includeOwner && !friendIds.contains(ownerId)) {
            friendIds.add(ownerId);
        }
        return friendIds;
    }
}
