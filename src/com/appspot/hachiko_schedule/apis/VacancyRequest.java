package com.appspot.hachiko_schedule.apis;

import android.content.Context;
import com.android.volley.Response;
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
                HachikoAPI.BASE + "vacancy",
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
                for (Hours range: parameter.preferredTimeRanges) {
                    JSONObject window = new JSONObject();
                    day.set(Calendar.HOUR_OF_DAY, range.start);
                    window.put("start", DateUtils.formatAsISO8601(day));
                    day.set(Calendar.HOUR_OF_DAY, range.end);
                    window.put("end", DateUtils.formatAsISO8601(day));
                    windows.put(window);
                }
                day.add(Calendar.DAY_OF_YEAR, 1);
            }
            param.put("windows", windows);
        } catch (JSONException e) {
            HachikoLogger.error("Parameter error in vacancy" + param, e);
            return param;
        }
        return param;
    }

    public static class Param {
        private final List<Long> friendIds;
        private final List<Hours> preferredTimeRanges;
        private final Calendar startDay;
        private final Calendar endDay;
        private final int durationMin;

        public Param(List<Long> friendIds, List<Hours> preferredTimeRanges, Calendar startDay,
                     Calendar endDay, int durationMin) {
            this.friendIds = friendIds;
            this.preferredTimeRanges = preferredTimeRanges;
            this.startDay = startDay;
            this.endDay = endDay;
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
                    && durationMin == opp.durationMin;
        }
    }

    public static class Hours {
        public int start, end;
        public Hours(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Hours)) {
                return false;
            }
            Hours opp = (Hours) o;
            return start == opp.start && end == opp.end;
        }
    }
}
