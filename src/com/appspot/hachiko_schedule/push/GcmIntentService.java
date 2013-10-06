package com.appspot.hachiko_schedule.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GCMを通じて送られてきたデータに応じて適切な処理をする
 */
public class GcmIntentService extends IntentService {
    // TODO: provide different value for different kind of message.
    public static final int NOTIFICATION_ID = 100;
    private static final String TAG_INVITE = "invite";

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
            }
        } catch (JSONException e) {
            HachikoLogger.error("error parsing request " + body, e);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendInviteNotification(JSONObject body) {
        try {
            String title = body.getString("title");
            String hostId = body.getString("hostId");
            List<String> friendIds = new ArrayList<String>();
            JSONArray array = body.getJSONArray("friendsId");
            for (int i = 0; i < array.length(); i++) {
                friendIds.add(array.getString(i));
            }
            List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
            array = body.getJSONArray("candidates");
            for (int i = 0; i < array.length(); i++) {
                JSONObject candidateDateJson = array.getJSONObject(i);
                JSONObject timeRange = candidateDateJson.getJSONObject("timeRange");
                candidateDates.add(new CandidateDate(
                        candidateDateJson.getInt("candId"),
                        DateUtils.parseISO8601(timeRange.getString("start")),
                        DateUtils.parseISO8601(timeRange.getString("end"))));
            }
            PlansTableHelper tableHelper = new PlansTableHelper(this);
            String myHachikoId = HachikoPreferences.getDefault(this)
                    .getString(HachikoPreferences.KEY_MY_HACHIKO_ID, "");
            boolean isHost = myHachikoId.equals(hostId);
            tableHelper.insertNewPlan(title, isHost, friendIds, candidateDates);
            PendingIntent pendingIntent
                    = getActivityIntent(new Intent(this, PlanListActivity.class));
            if (isHost) {
                sendNotification(title + "が作成されました", "詳細を見るにはクリック", pendingIntent);
            } else {
                sendNotification("イベントへの招待が届きました", title, pendingIntent);
            }
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
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
