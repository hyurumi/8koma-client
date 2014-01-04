package tk.hachikoma.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import tk.hachikoma.R;

class HachikoNotificationManager {
    // TODO: provide different value for different kind of message.
    public static final int NOTIFICATION_ID = 100;
    final Context context;

    public HachikoNotificationManager(Context context) {
        this.context = context;
    }

    protected void putNotification(String title, String msg, PendingIntent contentIntent) {
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo_launcher)
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
        notification.icon = R.drawable.logo_launcher;
        notification.ledARGB = 0xff0000cc;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
