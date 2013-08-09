package com.appspot.hachiko_schedule;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to get data from system contacts.
 */
public class ContactManager {

    private static final String EXCLUDE_PATTERN = "^[a-zA-Z0-9_\\+@\\/\\(\\)\\-\\.\\s]+$";

    private Context context;

    public ContactManager(Context context) {
        this.context = context;
    }

    public List<FriendListViewAdapter.Entry> getListOfContactEntries() {
        List entries = new ArrayList<FriendListViewAdapter.Entry>();
        Cursor cursor = queryAllFriends();
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbnailIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        while( cursor.moveToNext()){
            String displayName = cursor.getString(nameIndex);
            if (displayName.matches(EXCLUDE_PATTERN)) {
                continue;
            }
            String uriString = cursor.getString(thumbnailIndex);
            entries.add(
                    new FriendListViewAdapter.Entry(cursor.getString(nameIndex),
                            uriString == null ? null : Uri.parse(uriString)));
        }
        cursor.close();
        return entries;
    }

    /**
     * @return cursor that point the first element or null if no element found.
     */
    private Cursor queryAllFriends() {
        Cursor c = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[] {
                        Contacts.DISPLAY_NAME,
                        Contacts.PHOTO_THUMBNAIL_URI,
                        CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME,
                        CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
                        CommonDataKinds.Email.ADDRESS
                },
                ContactsContract.Data.MIMETYPE + "==\'vnd.android.cursor.item/name\'",
                null,
                CommonDataKinds.StructuredName.DISPLAY_NAME);
        if (c.moveToFirst()) {
            return c;
        }
        return null;
    }
}
