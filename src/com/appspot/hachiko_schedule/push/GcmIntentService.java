package com.appspot.hachiko_schedule.push;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.appspot.hachiko_schedule.Constants;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GCMを通じて送られてきたデータに応じて適切な処理をする
 */
public class GcmIntentService extends IntentService {
    private static final String TAG_INVITE = "invited";
    private static final String TAG_ALL_RESPONDED = "allResponded";
    private static final String TAG_CONFIRMED = "confirmed";
    private static final String TAG_RESPONDED = "responded";
    private static final String TAG_UPDATE_AFTER_ALL_RESPONSE = "updateAfterAllResponse";

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
        HachikoNotificationManager notificationManager = new HachikoNotificationManager(this);
        if (tag == null) {
            reportUnknownTypeNotification(notificationManager, intent, tag);
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

        try {
            if (tag.equals(TAG_INVITE)) {
                new InvitedIntentHandler(this).handle(new JSONObject(body));
            } else if (tag.equals(TAG_ALL_RESPONDED)) {
                new AllRespondedIntentHelper(this).handle(new JSONObject(body));
            } else if (tag.equals(TAG_CONFIRMED)) {
                new ConfirmedIntentHelper(this).handle(new JSONObject(body));
            } else if (tag.equals(TAG_RESPONDED)) {
                new RespondedIntentHandler(this).handle(new JSONObject(body));
            } else if (tag.equals(TAG_UPDATE_AFTER_ALL_RESPONSE)) {
                new RespondedIntentHandler(this).handle(new JSONObject(body));
            } else {
                reportUnknownTypeNotification(notificationManager, intent, tag);
            }
        } catch (JSONException e) {
            HachikoLogger.error("error parsing request " + body, e);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void reportUnknownTypeNotification(
            HachikoNotificationManager notificationManager, Intent intent, String tag) {
        HachikoLogger.error("Unknown Intent type" + intent + tag);
        if (Constants.IS_ALPHA_USER) {
            notificationManager.putNotification(
                    "Unknown intent type", intent.getExtras().toString(), null);
        }
    }
}
