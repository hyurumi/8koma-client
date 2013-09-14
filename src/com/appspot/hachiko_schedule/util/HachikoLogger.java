package com.appspot.hachiko_schedule.util;

import android.util.Log;

import static com.appspot.hachiko_schedule.Constants.IS_DEVELOPER;

/**
 * Wrapper class of {@link Log}
 */
public class HachikoLogger {
    private static final String DEFAULT_TAG = "HachikoApp";

    static public int info(String msg) {
        return Log.i(DEFAULT_TAG, msg);
    }

    static public int verbose(String msg) {
        return Log.v(DEFAULT_TAG, msg);
    }

    static public int error(String msg, Throwable throwable) {
        return Log.e(DEFAULT_TAG, msg, throwable);
    }

    static public int debug(String msg) {
        if (!IS_DEVELOPER) {
            return 0;
        }
        return Log.d(DEFAULT_TAG, msg);
    }

    static public int debug(Object... objects) {
        if (!IS_DEVELOPER) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj: objects) {
            builder.append(obj).append(' ');
        }
        return Log.d(DEFAULT_TAG, builder.toString());
    }

    static public int debugWithSeparator(char ch, Object... objects) {
        if (!IS_DEVELOPER) {
            return 0;
        }
        return debugWithSeparator(Character.toString(ch), objects);
    }

    static public int debugWithSeparator(CharSequence separator, Object... objects) {
        if (!IS_DEVELOPER) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj: objects) {
            builder.append(obj).append(' ').append(separator);
        }
        return Log.d(DEFAULT_TAG, builder.toString());
    }
}
