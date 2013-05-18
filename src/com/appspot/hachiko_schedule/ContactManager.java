package com.appspot.hachiko_schedule;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utility class to get data from system contacts.
 */
public class ContactManager {

    private Context context;

    public ContactManager(Context context) {
        this.context = context;
    }

    /**
     * Query all friends info from contacts and set it to given adapter.
     */
    public void queryAllFriends(ArrayAdapter<String> adapter) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[] {
                        Contacts.DISPLAY_NAME,
                        CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME,
                        CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
                        CommonDataKinds.Email.ADDRESS
                },
                ContactsContract.Data.MIMETYPE + "==\'vnd.android.cursor.item/name\'",
                null,
                CommonDataKinds.StructuredName.DISPLAY_NAME);

        if(cursor.moveToFirst()){
            int displayNameIndex = cursor.getColumnIndex( Contacts.DISPLAY_NAME );

            while( cursor.moveToNext()){
                String displayName = cursor.getString(displayNameIndex);
                if (displayName.matches("^[a-zA-Z0-9_\\+@\\/\\(\\)\\-\\.\\s]+$")) {
                    continue;
                }

                adapter.add(cursor.getString(displayNameIndex));
            }
        }
        cursor.close();
    }
}
