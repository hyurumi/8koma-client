package com.appspot.hachiko_schedule.plans;

import android.content.Context;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class PlanUpdateHelper {
    public static void updateAttendanceInfo(
            Context context, long planId, List<Long> friendIds, long myHachikoId, JSONArray attendance)
            throws JSONException {
        PlansTableHelper tableHelper = new PlansTableHelper(context);
        Set<Long> respondedFriendIds = new HashSet<Long>();
        Set<Long> positiveFriendIds = new HashSet<Long>();
        Set<Long> negativeFriendIds = new HashSet<Long>();
        for (int i = 0; i < attendance.length(); i++) {
            positiveFriendIds.clear();
            negativeFriendIds.clear();
            JSONObject attendanceJson = attendance.getJSONObject(i);
            long candidateId = attendanceJson.getLong("candId");
            JSONObject answers = attendanceJson.getJSONObject("attendance");
            Iterator<String> guestIds = answers.keys();
            while (guestIds.hasNext()) {
                String guestId = guestIds.next();
                CandidateDate.AnswerState answer
                        = CandidateDate.AnswerState.fromString(answers.getString(guestId));
                if (Long.parseLong(guestId) == myHachikoId) {
                    tableHelper.updateOwnAnswer(planId, candidateId, answer);
                    continue;
                }
                respondedFriendIds.add(Long.parseLong(guestId));

                if (answer == CandidateDate.AnswerState.OK) {
                    positiveFriendIds.add(Long.parseLong(guestId));
                } else if (answer == CandidateDate.AnswerState.NG) {
                    negativeFriendIds.add(Long.parseLong(guestId));
                }
            }
            tableHelper.updateAnswers(planId, candidateId, positiveFriendIds, negativeFriendIds);
        }
        List<Long> unanswered = new ArrayList<Long>(friendIds);
        unanswered.removeAll(respondedFriendIds);
        unanswered.remove(myHachikoId);
        tableHelper.updateUnansweredFriendIds(planId, unanswered);
    }
}
