package com.appspot.hachiko_schedule.friends;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.dev.FakeContactManager;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to get data from system contacts.
 */
public class ContactManager {

    private static final String EXCLUDE_PATTERN = "^[a-zA-Z0-9_\\+@\\/\\(\\)\\-\\.\\s]+$";

    private Context context;

    protected ContactManager(Context context) {
        this.context = context;
    }

    public static ContactManager getInstance(Context context) {
        if (HachikoPreferences.getDefault(context).getBoolean(
                HachikoPreferences.KEY_USE_FAKE_CONTACT,
                HachikoPreferences.USE_FAKE_CONTACT_DEFAULT)) {
            return new FakeContactManager(context);
        }
        return new ContactManager(context);
    }

    public List<FriendItem> getListOfContactEntries() {
        List entries = new ArrayList<FriendItem>();
        Cursor cursor = queryAllContacts();
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbnailIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        while( cursor.moveToNext()){
            String displayName = cursor.getString(nameIndex);
            if (displayName.matches(EXCLUDE_PATTERN)) {
                continue;
            }
            String uriString = cursor.getString(thumbnailIndex);
            entries.add(
                    new FriendItem(cursor.getString(nameIndex),
                            uriString == null ? null : Uri.parse(uriString)));
        }
        cursor.close();
        return entries;
    }

    /**
     * @return cursor that point the first element or null if no element found.
     */
    private Cursor queryAllContacts() {
        Cursor c = context.getContentResolver().query(
                Contacts.CONTENT_URI,
                new String[] {
                        ContactsContract.Contacts._ID,
                        Contacts.DISPLAY_NAME,
                        Contacts.HAS_PHONE_NUMBER,
                        Contacts.PHOTO_THUMBNAIL_URI
                },
                null,
                null,
                Contacts.DISPLAY_NAME);
        if (c.moveToFirst()) {
            return c;
        }
        return null;
    }
}
