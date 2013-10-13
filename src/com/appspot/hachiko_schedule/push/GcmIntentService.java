package com.appspot.hachiko_schedule.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.plans.PlanListActivity;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * GCMを通じて送られてきたデータに応じて適切な処理をする
 */
public class GcmIntentService extends IntentService {
    // TODO: provide different value for different kind of message.
    public static final int NOTIFICATION_ID = 100;
    private static final String TAG_INVITE = "invited";
    private static final String TAG_ALL_RESPONDED = "allResponded";
    private static final String TAG_CONFIRMED = "confirmed";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        HachikoLogger.debug("Handle gcm intent");
        String tag = extras.getString("tag");
        String body = extras.getString("body");
        HachikoLogger.debug("Notification received: ", tag);
        HachikoLogger.debug(body);
        if (tag == null) {
            reportUnknownTypeNotification(intent, tag);
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

        try {
            if (tag.equals(TAG_INVITE)) {
                sendInviteNotification(new JSONObject(body));
            } else if (tag.equals(TAG_ALL_RESPONDED)) {
                sendRespondedNotification(new JSONObject(body));
            } else if (tag.equals(TAG_CONFIRMED)) {
                sendEventRegisteredNotification(new JSONObject(body));
            }
        } catch (JSONException e) {
            HachikoLogger.error("error parsing request " + body, e);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendInviteNotification(JSONObject body) {
        try {
            Long planId = body.getLong("planId");
            String title = body.getString("title");
            List<String> friendIds = new ArrayList<String>();
            JSONArray array = body.getJSONArray("friendsId");
            for (int i = 0; i < array.length(); i++) {
                friendIds.add(array.getString(i));
            }
            List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
            array = body.getJSONArray("candidates");
            for (int i = 0; i < array.length(); i++) {
                JSONObject candidateDateJson = array.getJSONObject(i);
                JSONObject timeRange = candidateDateJson.getJSONObject("time");
                candidateDates.add(new CandidateDate(
                        candidateDateJson.getInt("id"),
                        DateUtils.parseISO8601(timeRange.getString("start")),
                        DateUtils.parseISO8601(timeRange.getString("end")),
                        CandidateDate.AnswerState.NEUTRAL));
            }
            PlansTableHelper tableHelper = new PlansTableHelper(this);
            String hostId = body.getString("owner");
            String myHachikoId = HachikoPreferences.getDefault(this)
                    .getString(HachikoPreferences.KEY_MY_HACHIKO_ID, "");
            boolean isHost = myHachikoId.equals(hostId);
            if (!isHost) {
                tableHelper.insertNewPlan(planId, title, isHost, friendIds, candidateDates);
            } else {
                for (CandidateDate candidateDate: candidateDates) {
                    tableHelper.updateAnswerIdByStartDate(
                            planId, candidateDate.getStartDate(), candidateDate.getAnswerId());
                }
            }
            updateAttendanceInfo(planId, myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(this, PlanListActivity.class));
            if (!isHost) {
                sendNotification("イベントへの招待が届きました", title, pendingIntent);
            }
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }

    private void sendRespondedNotification(JSONObject body) {
        try {
            String title = body.getString("title");
            Long planId = body.getLong("planId");
            String myHachikoId = HachikoPreferences.getDefault(this)
                    .getString(HachikoPreferences.KEY_MY_HACHIKO_ID, "");
            updateAttendanceInfo(planId, myHachikoId, body.getJSONArray("attendance"));
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(this, PlanListActivity.class));
            sendNotification("参加者から返答が届きました", title, pendingIntent);
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }

    private void sendEventRegisteredNotification(JSONObject body) {
        try {
            String planId = body.getString("planId");
            PlansTableHelper plansTableHelper = new PlansTableHelper(this);
            String title = plansTableHelper.queryTitle(planId);
            Date startDate
                    = DateUtils.parseISO8601(body.getJSONObject("timeRange").getString("start"));
            sendNotification("予定がカレンダーに登録されました", title, calendarIntent(startDate));
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        }
    }

    // TODO: ICS以前の後方互換怪しいのであとでしらべる #1
    private PendingIntent calendarIntent(Date date) {
        Uri uriCalendar = Uri.parse(
                "content://com.android.calendar/time/" + Long.toString(date.getTime()));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriCalendar);
        return getActivityIntent(intent);
    }

    private void updateAttendanceInfo(long planId, String myHachikoId, JSONArray attendance)
            throws JSONException {
        PlansTableHelper tableHelper = new PlansTableHelper(this);
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
                if (guestId.equals(myHachikoId)) {
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

    private PendingIntent getActivityIntent(Intent intent) {
        return PendingIntent.getActivity(this, /* No request code*/ 0, intent,
                Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void sendNotification(String title, String msg, PendingIntent contentIntent) {
        NotificationManager notificationManager
                = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
               new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText(msg);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }
        Notification notification = builder.build();
        // DEFAULT_LIGHTはNoLightの可能性があるのでここでライトを上書き
        notification.ledARGB = 0xff0000cc;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void reportUnknownTypeNotification(Intent intent, String tag) {
        HachikoLogger.error("Unknown Intent type" + intent + tag);
        if (Constants.IS_DEVELOPER) {
            sendNotification("Unknown intent type", intent.getExtras().toString(), null);
        }
    }
}
