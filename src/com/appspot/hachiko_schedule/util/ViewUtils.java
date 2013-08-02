package com.appspot.hachiko_schedule.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    /**
     * (SomeView) view.findViewById(id)のショートカットメソッド
     */
    static public <T extends View> T findViewById(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * ((ViewGroup) view.getParent()).removeView(view)のショートカットメソッド
     */
    static public void removeView(View view) {
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
