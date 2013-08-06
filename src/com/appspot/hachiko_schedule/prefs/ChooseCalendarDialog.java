package com.appspot.hachiko_schedule.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.CalendarIdentifier;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * @author Kazuki Nishiura
 */
public class ChooseCalendarDialog extends DialogFragment {

    private List<String> calendarIds;
    private String[] calendarNames;
    private boolean[] use;
    private static final Function<String, String> EXTRACT_DISPLAY_NAME
            = new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return CalendarIdentifier.decode(s).getDisplayName();
                }
            };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        readPrefs();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.prefs_set_calendars_dialog)
                .setMultiChoiceItems(calendarNames, use, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        use[which] = isChecked;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        persistToPrefs();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        return builder.create();
    }

    private void readPrefs() {
        SharedPreferences prefs = HachikoPreferences.getDefault(getActivity());
        Set<String> calendarsToUse = prefs.getStringSet(
                HachikoPreferences.KEY_CALENDARS_TO_USE, HachikoPreferences.CALENDARS_TO_USE_DEFAULT);
        Set<String> calendarsNotToUse = prefs.getStringSet(
                HachikoPreferences.KEY_CALENDARS_NOT_TO_USE, HachikoPreferences.CALENDARS_NOT_TO_USE_DEFAULT);
        calendarIds = calendarsSetToSortedList(calendarsToUse, calendarsNotToUse);
        calendarNames = new String[calendarIds.size()];
        Lists.transform(calendarIds, EXTRACT_DISPLAY_NAME).toArray(calendarNames);
        use = new boolean[calendarIds.size()];
        for (int i = 0; i < calendarIds.size(); i++) {
            use[i] = calendarsToUse.contains(calendarIds.get(i));
        }
    }

    private List<String> calendarsSetToSortedList(
            Set<String> calendarsToUse, Set<String> calendarsNotToUse) {
        List<String> calendarList = new ArrayList<String>(calendarsToUse);
        calendarList.addAll(calendarsNotToUse);
        Collections.sort(calendarList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return (int) (CalendarIdentifier.decode(lhs).getId()
                        - CalendarIdentifier.decode(rhs).getId());
            }
        });
        return calendarList;
    }

    private void persistToPrefs() {
        Set<String> calendarsToUse = new HashSet<String>();
        Set<String> calendarsNotToUse = new HashSet<String>();
        for (int i = 0; i < calendarIds.size(); i++) {
            if (use[i]) {
                calendarsToUse.add(calendarIds.get(i));
            } else {
                calendarsNotToUse.add(calendarIds.get(i));
            }
        }

        HachikoPreferences.getDefaultEditor(getActivity())
                .putStringSet(HachikoPreferences.KEY_CALENDARS_TO_USE, calendarsToUse)
                .putStringSet(HachikoPreferences.KEY_CALENDARS_NOT_TO_USE, calendarsNotToUse)
                .commit();
    }
}
