package tk.hachikoma.prefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
import tk.hachikoma.R;
import tk.hachikoma.data.TimeRange;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 12/1/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimePickerDialogPreference extends DialogPreference {

    TimePicker timepicker_start;
    TimePicker timepicker_end;

    public TimePickerDialogPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public TimePickerDialogPreference(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_timepicker, null);
        timepicker_start = (TimePicker)v.findViewById(R.id.timepicker_start);
        timepicker_end = (TimePicker)v.findViewById(R.id.timepicker_end);
        timepicker_start.setIs24HourView(true);
        timepicker_end.setIs24HourView(true);

        TimeRange timeRange = new TimeRange(getPersistedString(getDefaultTimeRangeString()));

        timepicker_start.setCurrentHour(timeRange.getStartHour());
        timepicker_start.setCurrentMinute(timeRange.getStartMinute());
        timepicker_end.setCurrentHour(timeRange.getEndHour());
        timepicker_end.setCurrentMinute(timeRange.getEndMinutes());
        return v;
    }

    private String getDefaultTimeRangeString() {
        if (getKey().equals(HachikoPreferences.KEY_TIMERANGE_ASA)){
            return HachikoPreferences.DEFAULT_TIMERANGE_ASA;
        } else if(getKey().equals(HachikoPreferences.KEY_TIMERANGE_HIRU)){
            return HachikoPreferences.DEFAULT_TIMERANGE_HIRU;
        } else if (getKey().equals(HachikoPreferences.KEY_TIMERANGE_YU)){
            return HachikoPreferences.DEFAULT_TIMERANGE_YU;
        } else{
            return HachikoPreferences.DEFAULT_TIMERANGE_YORU;
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            String timerange = String.format("%02d:%02d-%02d:%02d",
                    this.timepicker_start.getCurrentHour(),
                    this.timepicker_start.getCurrentMinute(),
                    this.timepicker_end.getCurrentHour(),
                    this.timepicker_end.getCurrentMinute());
            persistString(timerange);
            this.setSummary(timerange);
        }
        super.onDialogClosed(positiveResult);
    }
}
