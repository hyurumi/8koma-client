package com.appspot.hachiko_schedule.util;

import android.util.Log;

/**
 * Wrapper class of {@link Log}
 */
public class HachikoLogger {
    private static final String DEFAULT_TAG = "HachikoApp";

    static public int debug(String msg) {
        return Log.d(DEFAULT_TAG, msg);
    }

    static public int debug(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object obj: objects) {
            builder.append(obj).append(' ');
        }
        return Log.d(DEFAULT_TAG, builder.toString());
    }
}
