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
}
