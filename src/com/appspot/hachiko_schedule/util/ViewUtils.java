package com.appspot.hachiko_schedule.util;

import android.view.View;

public class ViewUtils {

    /**
     * (SomeView) view.findViewById(id)のショートカットメソッド
     */
    static public <T extends View> T findViewById(View view, int id) {
        return (T) view.findViewById(id);
    }
}
