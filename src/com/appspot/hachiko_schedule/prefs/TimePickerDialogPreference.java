package com.appspot.hachiko_schedule.prefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import com.appspot.hachiko_schedule.R;

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

        String[] timerange = getPersistedString("").split(":|-");
        if (timerange.length != 4){
            if (getKey().equals("timerange_asa")){
                timerange = HachikoPreferences.DEFAULT_TIMERANGE_ASA.split(":|-");
            }
            else if(getKey().equals("timerange_hiru")){
                timerange = HachikoPreferences.DEFAULT_TIMERANGE_HIRU.split(":|-");
            }
            else if (getKey().equals("timerange_yu")){
                timerange = HachikoPreferences.DEFAULT_TIMERANGE_YU.split(":|-");
            }
            else{
                timerange = HachikoPreferences.DEFAULT_TIMERANGE_YORU.split(":|-");
            }
        }

        timepicker_start.setCurrentHour(Integer.valueOf(timerange[0]));
        timepicker_start.setCurrentMinute(Integer.valueOf(timerange[1]));
        timepicker_end.setCurrentHour(Integer.valueOf(timerange[2]));
        timepicker_end.setCurrentMinute(Integer.valueOf(timerange[3]));
        return v;
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
