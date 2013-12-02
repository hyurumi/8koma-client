package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.Response;
import com.appspot.hachiko_schedule.apis.base_requests.HachiJsonArrayRequest;
import com.appspot.hachiko_schedule.data.TimeRange;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * @author Kazuki Nishiura
 */
public class VacancyRequest extends HachiJsonArrayRequest {
    public VacancyRequest(Context context, Param param,
                          Response.Listener<JSONArray> listener,
                          Response.ErrorListener errorListener) {
        super(context,
                Method.POST,
                HachikoAPI.getUrl("vacancy"),
                constructParams(param).toString(),
                listener,
                errorListener);
    }

    private static JSONObject constructParams(Param parameter) {
        JSONObject param = new JSONObject();
        try {
            param.put("friends", new JSONArray(parameter.friendIds));
            param.put("durationMin", parameter.durationMin);
            JSONArray windows = new JSONArray();
            Calendar day = (Calendar) parameter.startDay.clone();
            day.set(Calendar.MILLISECOND, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MINUTE, 0);
            while (day.before(parameter.endDay)) {
                for (TimeRange range: parameter.preferredTimeRanges) {
                    JSONObject window = new JSONObject();
                    day.set(Calendar.HOUR_OF_DAY, range.getStartHour());
                    day.set(Calendar.MINUTE, range.getStartMinute());
                    window.put("start", DateUtils.formatAsISO8601(day));
                    if (range.acrossDay()) {
                        day.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    day.set(Calendar.HOUR_OF_DAY, range.getEndHour());
                    day.set(Calendar.MINUTE, range.getEndMinutes());
                    window.put("end", DateUtils.formatAsISO8601(day));
                    if (range.acrossDay()) {
                        day.add(Calendar.DAY_OF_YEAR, -1);
                    }
                    windows.put(window);
                }
                day.add(Calendar.DAY_OF_YEAR, 1);
            }
            param.put("windows", windows);
            param.put("asap", parameter.shouldAsap);
        } catch (JSONException e) {
            HachikoLogger.error("Parameter error in vacancy" + param, e);
            return param;
        }
        return param;
    }

    public static class Param {
        private final List<Long> friendIds;
        private final List<TimeRange> preferredTimeRanges;
        private final Calendar startDay;
        private final Calendar endDay;
        private final boolean shouldAsap;
        private final int durationMin;

        public Param(List<Long> friendIds, List<TimeRange> preferredTimeRanges, Calendar startDay,
                     Calendar endDay, boolean shouldAsap, int durationMin) {
            this.friendIds = friendIds;
            this.preferredTimeRanges = preferredTimeRanges;
            this.startDay = startDay;
            this.endDay = endDay;
            this.shouldAsap = shouldAsap;
            this.durationMin = durationMin;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Param)) {
                return false;
            }
            Param opp = (Param) o;
            return friendIds.equals(opp.friendIds)
                    && preferredTimeRanges.equals(opp.preferredTimeRanges)
                    && startDay.equals(opp.startDay)
                    && endDay.equals(opp.endDay)
                    && shouldAsap == opp.shouldAsap
                    && durationMin == opp.durationMin;
        }
    }
}
