package com.appspot.hachiko_schedule.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.appspot.hachiko_schedule.db.PlansTableHelper;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * confirmed
 */
public class ConfirmedIntentHelper extends GcmIntentHandlerBase<JSONObject> {
    public ConfirmedIntentHelper(Context context) {
        super(context);
    }

    @Override
    public void handle(JSONObject body) {
        String title = "";
        Date startDate = null;
        try {
            long planId = body.getLong("planId");
            long answerId = body.getLong("id");
            PlansTableHelper plansTableHelper = new PlansTableHelper(getContext());
            title = plansTableHelper.queryPlan(planId).getTitle();
            plansTableHelper.confirmCandidateDate(planId, answerId);
            startDate = DateUtils.parseISO8601(body.getJSONObject("time").getString("start"));
        } catch (JSONException e) {
            HachikoLogger.error(body.toString(), e);
        } finally {
            putNotification("予定がカレンダーに登録されました", title,
                    calendarIntent(startDate == null ? new Date() : startDate));
        }
    }

    // TODO: ICS以前の後方互換怪しいのであとでしらべる #1
    private PendingIntent calendarIntent(Date date) {
        Uri uriCalendar = Uri.parse(
                "content://com.android.calendar/time/" + Long.toString(date.getTime()));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriCalendar);
        return getActivityIntent(intent);
    }

}
