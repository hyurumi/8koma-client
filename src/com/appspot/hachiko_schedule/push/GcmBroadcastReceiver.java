package com.appspot.hachiko_schedule.push;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * GCMからのpushを受けて{@link GcmIntentService}を起動するためのBroadcastReceiver.
 * {@link WakefulBroadcastReceiver} "passes off the work of processing the GCM message to a Service
 * (typically an IntentService), while ensuring that the device does not go back to sleep in the
 * transition"らしい．
 * {@see http://developer.android.com/google/gcm/client.html}
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName componentName
                = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
