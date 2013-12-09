package tk.hachikoma.plans;

import tk.hachikoma.data.TimeRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CreatePlanActivityTest {
    @Test
    public void getPreferredTimeRangeNoOverwrap() throws Exception {
        List<TimeRange> ranges = CreatePlanActivity.getPreferredTimeRange(
                new TimeRange("09:00-12:00"), true,
                new TimeRange("12:00-15:00"), true,
                new TimeRange("16:00-18:00"), true,
                new TimeRange("18:00-20:00"), false);
        assertEquals(
                Arrays.asList(new TimeRange[]{
                        new TimeRange("09:00-12:00"),
                        new TimeRange("12:00-15:00"),
                        new TimeRange("16:00-18:00")}),
                ranges);
    }

    @Test
    public void getPreferredTimeRangeOverwrap() throws Exception {
        List<TimeRange> ranges = CreatePlanActivity.getPreferredTimeRange(
                new TimeRange("09:00-12:00"), false,
                new TimeRange("12:00-15:00"), true,
                new TimeRange("16:00-18:00"), true,
                new TimeRange("17:00-20:00"), true);
        assertEquals(
                Arrays.asList(new TimeRange[]{
                        new TimeRange("12:00-15:00"),
                        new TimeRange("16:00-20:00")}),
                ranges);
    }
}
