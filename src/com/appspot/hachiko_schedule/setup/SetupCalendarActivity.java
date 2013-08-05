package com.appspot.hachiko_schedule.setup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.appspot.hachiko_schedule.EventManager;
import com.appspot.hachiko_schedule.MainActivity;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CalendarIdentifier;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kazuki Nishiura
 */
public class SetupCalendarActivity extends Activity {

    private ListView calendarList;
    private List<CalendarIdentifier> calendars;
    private static final Function<CalendarIdentifier, String> EXTRACT_DISPLAY_NAME
            = new Function<CalendarIdentifier, String>() {
                @Override
                public String apply(CalendarIdentifier calendarIdentifier) {
                    return calendarIdentifier.getDisplayName();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_calendar);

        setCalendarList();
        
        findViewById(R.id.setup_calendar_complete_button)
                .setOnClickListener(new RegisterCalendarsAndTransit());
    }

    private void setCalendarList() {
        // メモ: 当初は律儀にLoading Indicatorを出していたが，
        // カレンダー一覧を取得するくらいのことは十分速く行えた (自分の環境では数ミリ秒)
        EventManager eventManager = new EventManager(this);
        calendars = eventManager.getCalenders();
        calendarList = (ListView) findViewById(R.id.setup_calendar_list);
        calendarList.setItemsCanFocus(false);
        calendarList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        calendarList.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                Lists.transform(calendars, EXTRACT_DISPLAY_NAME)));
    }

    private void transitToNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class RegisterCalendarsAndTransit implements View.OnClickListener {
        
        @Override
        public void onClick(View v) {
            Set<String> calendarsToUse = new HashSet<String>();
            Set<String> calendarsNotToUse = new HashSet<String>();
            for (int i = 0; i < calendars.size(); i++) {
                String str = calendars.get(i).encode();
                if (calendarList.isItemChecked(i)) {
                    calendarsToUse.add(str);
                } else {
                    calendarsNotToUse.add(str);
                }
            }
            HachikoPreferences.getDefaultEditor(SetupCalendarActivity.this)
                    .putStringSet(HachikoPreferences.KEY_CALENDARS_TO_USE, calendarsToUse)
                    .putStringSet(HachikoPreferences.KEY_CALENDARS_NOT_TO_USE, calendarsNotToUse)
                    .putBoolean(HachikoPreferences.KEY_IS_CALENDAR_SETUP, true)
                    .commit();

            transitToNextActivity();
            finish();
        }
    }
}
