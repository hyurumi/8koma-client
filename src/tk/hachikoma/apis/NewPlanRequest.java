package tk.hachikoma.apis;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.Response;
import tk.hachikoma.apis.base_requests.HachiJsonObjectRequest;
import tk.hachikoma.data.Timeslot;
import tk.hachikoma.util.DateUtils;
import tk.hachikoma.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

public class NewPlanRequest extends HachiJsonObjectRequest {
    public NewPlanRequest(Context context, String title, List<Long> friendIds,
                          Collection<Timeslot> timeslots,
                          Response.Listener<JSONObject> listener,
                          Response.ErrorListener errorListener) {
        super(context, Request.Method.POST, HachikoAPI.getUrl("plans"),
                constructParams(title, friendIds, timeslots)
                , listener, errorListener);
    }

    private static JSONObject constructParams(
            String title, List<Long> friendIds, Collection<Timeslot> timeslots) {
        JSONObject param = new JSONObject();
        try {
            JSONArray dates = new JSONArray();
            for (Timeslot timeslot: timeslots) {
                JSONObject candidateJson = new JSONObject();
                JSONObject timeslotJson = new JSONObject();
                timeslotJson.put("start", DateUtils.formatAsISO8601(timeslot.getStartDate()));
                timeslotJson.put("end", DateUtils.formatAsISO8601(timeslot.getEndDate()));
                candidateJson.put("time", timeslotJson);
                dates.put(candidateJson);
            }
            param.put("friendsId", new JSONArray(friendIds));
            param.put("candidates", dates);
            param.put("title", title);
            HachikoLogger.debug(param);
        } catch (JSONException e) {
            HachikoLogger.error("JSON error/Never happen", e);
        }
        return param;
    }
}
