package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * GCMから送られてくるpushに対応する基底クラス
 */
abstract class GcmIntentHandlerBase<T> {
    private final Context context;
    private final HachikoNotificationManager notificationManager;

    protected GcmIntentHandlerBase(Context context) {
        this.context = context;
        this.notificationManager = new HachikoNotificationManager(context);
    }

    Context getContext() {
        return context;
    }

    void putNotification(String title, String message, PendingIntent intent) {
        notificationManager.putNotification(title, message, intent);
    }

    protected PendingIntent getActivityIntent(Intent intent) {
        return PendingIntent.getActivity(
                context, /* No request code*/ 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * pushのbodyに対して適切な処理を行う．pushのtagから適切なハンドラを選択し，bodyをよしなに型変換するまでは
     * {@link GcmIntentService}の責務
     */
    public abstract void handle(T body);

    protected void updateAttendanceInfo(long planId, long myHachikoId, JSONArray attendance)
            throws JSONException {
        PlansTableHelper tableHelper = new PlansTableHelper(context);
        Set<Long> positiveFriendIds = new HashSet<Long>();
        Set<Long> negativeFriendIds = new HashSet<Long>();
        for (int i = 0; i < attendance.length(); i++) {
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

                if (answer == CandidateDate.AnswerState.OK) {
                    positiveFriendIds.add(Long.parseLong(guestId));
                } else if (answer == CandidateDate.AnswerState.NG) {
                    negativeFriendIds.add(Long.parseLong(guestId));
                }
            }
            tableHelper.updateAnswers(planId, candidateId, positiveFriendIds, negativeFriendIds);
        }
    }
}
