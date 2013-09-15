package com.appspot.hachiko_schedule.push;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.util.HachikoLogger;

/**
 * GCMを通じて送られてきたデータに応じて適切な処理をする
 */
public class GcmIntentService extends IntentService {
    // TODO: provide different value for different kind of message.
    public static final int NOTIFICATION_ID = 100;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        HachikoLogger.debug("Handle gcm intent");
        sendNotification(extras.getString("tag") + "|" + extras.getString("body"));
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        NotificationManager notificationManager
                = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
               new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification Test")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
