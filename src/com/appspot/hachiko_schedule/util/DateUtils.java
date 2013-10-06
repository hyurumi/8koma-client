package com.appspot.hachiko_schedule.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    private static final SimpleDateFormat START_DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat END_HOUR_FORMAT = new SimpleDateFormat("HH:mm");

    /**
     * 動作確認用などに，現在時間から指定した日時あとのDateオブジェクトを返す
     */
    public static Date dateAfterDaysAndHoursFromNow(int days, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.add(Calendar.HOUR, hours);
        return new Date(calendar.getTimeInMillis());
    }

    public static String parseAsISO8601(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    public static String timeslotString(Date startDate, Date endDate) {
        return new StringBuilder().append(START_DATE_FORMAT.format(startDate))
                .append(" - ")
                .append(END_HOUR_FORMAT.format(endDate))
                .toString();
    }
}
