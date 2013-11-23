package com.appspot.hachiko_schedule.push;

import android.content.Context;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.util.HachikoLogger;

/**
 * 知ってる人がHachikoを使い始めた
 */
public class JoinIntentHandler extends GcmIntentHandlerBase<String> {
    public JoinIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void handle(String body) {
        UserTableHelper tableHelper = new UserTableHelper(getContext());
        if (tableHelper.registerFriendAsHachikoUser(Long.parseLong(body))) {
            HachikoLogger.debug("Register new user successfully ", body);
        } else {
            HachikoLogger.debug("Tried to notify user " + body + " is registered");
        }
    }
}
