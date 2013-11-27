package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * GCMから送られてくるpushに対応する基底クラス，いくつかのUtilityメソッドを提供する．
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
}
