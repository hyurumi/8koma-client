package tk.hachikoma.apis;

import tk.hachikoma.data.TimeRange;
import tk.hachikoma.util.DateUtils;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class VacancyRequestTest {
    @Test
    public void constructParamsTimeRangeTest() throws Exception {
        Calendar[] starts = new Calendar[] {
                day(2013, 11, 10, 10, 20),
                day(2013, 11, 10, 15, 00),
                day(2013, 11, 11, 10, 20),
                day(2013, 11, 11, 15, 00)
        };
        Calendar[] ends = new Calendar[] {
                day(2013, 11, 10, 12, 15),
                day(2013, 11, 10, 17, 00),
                day(2013, 11, 11, 12, 15),
                day(2013, 11, 11, 17, 00)
        };
        JSONObject json = VacancyRequest.constructParams(new VacancyRequest.Param(
                Arrays.asList(new Long[]{1L, 2L}),
                Arrays.asList(new TimeRange[]{new TimeRange("10:20-12:15"), new TimeRange("15:00-17:00")}),
                day(2013, 11, 10),
                day(2013, 11, 11),
                false,
                60
        ));
        JSONArray windows = json.getJSONArray("windows");
        for (int i = 0; i < windows.length(); i++) {
            JSONObject window = windows.getJSONObject(i);
            assertEquals(DateUtils.formatAsISO8601(starts[i]), window.getString("start"));
            assertEquals(DateUtils.formatAsISO8601(ends[i]), window.getString("end"));
        }
    }

    @Test
    public void constructParamsTimeRangeTestAcrossDay() throws Exception {
        Calendar[] starts = new Calendar[] {
                day(2013, 11, 10, 10, 20),
                day(2013, 11, 10, 20, 30),
                day(2013, 11, 11, 10, 20),
                day(2013, 11, 11, 20, 30)
        };
        Calendar[] ends = new Calendar[] {
                day(2013, 11, 10, 12, 15),
                day(2013, 11, 11, 02, 45),
                day(2013, 11, 11, 12, 15),
                day(2013, 11, 12, 02, 45)
        };
        JSONObject json = VacancyRequest.constructParams(new VacancyRequest.Param(
                Arrays.asList(new Long[]{1L, 2L}),
                Arrays.asList(new TimeRange[]{new TimeRange("10:20-12:15"), new TimeRange("20:30-02:45")}),
                day(2013, 11, 10),
                day(2013, 11, 11),
                false,
                60
        ));
        JSONArray windows = json.getJSONArray("windows");
        for (int i = 0; i < windows.length(); i++) {
            JSONObject window = windows.getJSONObject(i);
            assertEquals(DateUtils.formatAsISO8601(starts[i]), window.getString("start"));
            assertEquals(DateUtils.formatAsISO8601(ends[i]), window.getString("end"));
        }
    }

    private Calendar day(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar;
    }

    private Calendar day(int year, int month, int day, int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(year, month, day, hour, min);
        return calendar;
    }
}
