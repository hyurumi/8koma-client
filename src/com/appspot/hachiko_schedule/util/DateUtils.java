package com.appspot.hachiko_schedule.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    private static final SimpleDateFormat START_DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat END_HOUR_FORMAT = new SimpleDateFormat("HH:mm");
    private static final String DATE_FORMAT_ISO8601_UTC = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String DATE_FORMAT_ISO8601_WITHOUT_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 動作確認用などに，現在時間から指定した日時あとのDateオブジェクトを返す
     */
    public static Date dateAfterDaysAndHoursFromNow(int days, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.add(Calendar.HOUR, hours);
        return new Date(calendar.getTimeInMillis());
    }

    public static String formatAsISO8601(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_ISO8601);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    public static String formatAsISO8601(Calendar calendar) {
        return formatAsISO8601(calendar.getTime());
    }

    public static Date parseISO8601(String iso8601) {
        try {
            if (iso8601.endsWith("Z")) {
                return new SimpleDateFormat(DATE_FORMAT_ISO8601_UTC).parse(iso8601);
            } else if (iso8601.length() == 19) {
                return new SimpleDateFormat(DATE_FORMAT_ISO8601_WITHOUT_TIMEZONE).parse(iso8601);
            }
            return new SimpleDateFormat(DATE_FORMAT_ISO8601).parse(iso8601);
        } catch (ParseException e) {
            HachikoLogger.error("Date parse error" + iso8601, e);
            return null;
        }
    }

    public static String timeslotString(Date startDate, Date endDate) {
        return new StringBuilder().append(START_DATE_FORMAT.format(startDate))
                .append(" - ")
                .append(END_HOUR_FORMAT.format(endDate))
                .toString();
    }
}
