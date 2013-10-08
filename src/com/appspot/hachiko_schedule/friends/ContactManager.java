package com.appspot.hachiko_schedule.friends;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.dev.FakeContactManager;
import com.appspot.hachiko_schedule.prefs.HachikoPreferences;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds;

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
        Cursor cursor = queryAllContacts(Contacts.DISPLAY_NAME);
        int idIndex = cursor.getColumnIndex(Contacts._ID);
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbnailIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        if (!cursor.moveToFirst()) {
            return Collections.EMPTY_LIST;
        }
        do {
            String displayName = cursor.getString(nameIndex);
            if (displayName.matches(EXCLUDE_PATTERN)) {
                continue;
            }
            String uriString = cursor.getString(thumbnailIndex);
            String email = queryPrimaryEmailAddress(cursor.getString(
                    cursor.getColumnIndex(Contacts._ID)));
            if (email == null) {
                continue;
            }

            entries.add(new FriendItem(
                    cursor.getLong(idIndex),
                    cursor.getString(nameIndex),
                    uriString == null ? null : Uri.parse(uriString),
                    email));
        } while(cursor.moveToNext());
        cursor.close();
        return entries;
    }

    /**
     * @return cursor that point the first element or null if no element found.
     */
    private Cursor queryAllContacts(String sortOrder) {
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
                sortOrder);
        if (c.moveToFirst()) {
            return c;
        }
        return null;
    }

    private Cursor queryAllContacts() {
        return queryAllContacts(null);
    }

    /**
     * @return 友人(name, phone, emailをkeyとする{@link JSONObject})を要素とする{@link JSONArray}.
     */
    public JSONArray getAllFriendsAsJSON() {
        Cursor c = queryAllContacts();
        if (c == null) {
            return null;
        }

        int nameIndex = c.getColumnIndexOrThrow(Contacts.DISPLAY_NAME);
        int hasPhoneIndex = c.getColumnIndexOrThrow(Contacts.HAS_PHONE_NUMBER);
        int idIndex = c.getColumnIndexOrThrow(Contacts._ID);
        JSONArray friends = new JSONArray();
        do {
            String name = c.getString(nameIndex);
            if (Integer.parseInt(c.getString(hasPhoneIndex)) == 0) {
                continue;
            }
            String userId = c.getString(idIndex);
            String primaryPhoneNumber = queryPrimaryPhoneNumber(userId);
            if (primaryPhoneNumber == null) {
                continue;
            }
            String email = queryPrimaryEmailAddress(userId);
            if (email == null) {
                continue;
            }
            try {
                JSONObject friend = new JSONObject();
                friend.put("name", name);
                friend.put("phone", primaryPhoneNumber);
                friend.put("email", email);
                friends.put(friend);
            } catch (JSONException e) {
                HachikoLogger.error("Unexpected JSON exception: ", e);
            }
        } while (c.moveToNext());
        return friends;
    }

    private String queryPrimaryPhoneNumber(String userId) {
        return queryPrimaryData(
                CommonDataKinds.Phone.CONTENT_URI,
                CommonDataKinds.Phone.NUMBER,
                CommonDataKinds.Phone.CONTACT_ID,
                userId);
    }

    /**
     * '主要な'Eメールアドレスを引っ張る．主要とは，1) Gmailである，2) 1件めのデータである のいずれか
     */
    private String queryPrimaryEmailAddress(String userId) {
        Cursor cursor = context.getContentResolver().query(
                CommonDataKinds.Email.CONTENT_URI,
                new String[]{CommonDataKinds.Email.ADDRESS},
                CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{userId},
                null);
        String ret = null;
        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS));
            if (data != null) {
                if (ret == null) {
                    ret = data;
                } else if (data.contains("@gmail.com")) {
                    cursor.close();
                    return data;
                }
            }
        }
        cursor.close();
        return ret;
    }

    private String queryPrimaryData(Uri uri, String key, String leftOp, String userId) {
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{key},
                leftOp + " = ?",
                new String[]{userId},
                null);
        String data = null;
        while (cursor.moveToNext()) {
            data = cursor.getString(cursor.getColumnIndex(key));
            if (data != null) {
                cursor.close();
                break;
            }
        }
        cursor.close();
        return data;
    }
}
