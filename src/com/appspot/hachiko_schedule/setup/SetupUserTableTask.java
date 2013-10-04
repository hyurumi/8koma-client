package com.appspot.hachiko_schedule.setup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.friends.ContactManager;

import java.util.List;

public class SetupUserTableTask extends AsyncTask<Void, Void, String> {

    private final Context context;
    public SetupUserTableTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        List<FriendItem> friends = ContactManager.getInstance(context).getListOfContactEntries();
        setupLocalTable(friends);
        // TODO: 通信
        return null;
    }

    private void setupLocalTable(List<FriendItem> friends) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        SQLiteDatabase db = userTableHelper.getWritableUserDB();
        for (FriendItem friend: friends) {
            Uri photoUri = friend.getPhotoUri();
            userTableHelper.insertUserToDb(db, friend.getDisplayName(),
                    friend.getLocalContactId(), photoUri == null ? null : photoUri.toString(),
                    friend.getEmailAddress());
        }
        db.close();
    }
}
