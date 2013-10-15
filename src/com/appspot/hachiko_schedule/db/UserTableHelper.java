package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;

import java.util.*;

/**
 * Usersテーブルまわりのヘルパ.
 */
public class UserTableHelper {
    private static final String USER_TABLE_NAME = "users";
    private static final String HACHIKO_ID = "hachiko_id";
    private static final String IS_HACHIKO_USER = "is_haciko_user";
    private static final String DISPLAY_NAME = "display_name";
    private static final String LOCAL_CONTACT_ID = "local_contact_id";
    private static final String PROFILE_PIC_URI = "profile_pic_uri";
    private static final String PRIMARY_EMAIL = "primary_email";

    private static final String NON_FRIEND_NAME_TABLE_NAME = "non_friend_names";

    private HachikoDBOpenHelper dbHelper;

    public static void onCreateDatabase(SQLiteDatabase database) {
        String createUsersTable = new SQLiteCreateTableBuilder(USER_TABLE_NAME)
                .addColumn(HACHIKO_ID, SQLiteType.TEXT, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(IS_HACHIKO_USER, SQLiteType.INTEGER)
                .addColumn(DISPLAY_NAME, SQLiteType.TEXT, SQLiteConstraint.NOT_NULL)
                .addColumn(PROFILE_PIC_URI, SQLiteType.TEXT)
                .addColumn(LOCAL_CONTACT_ID, SQLiteType.INTEGER)
                .addColumn(PRIMARY_EMAIL, SQLiteType.TEXT)
                .toString();
        String createNonFriendNamesTable = new SQLiteCreateTableBuilder(NON_FRIEND_NAME_TABLE_NAME)
                .addColumn(HACHIKO_ID, SQLiteType.TEXT, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(DISPLAY_NAME, SQLiteType.TEXT)
                .toString();
        database.execSQL(createUsersTable);
        database.execSQL(createNonFriendNamesTable);
    }

    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public UserTableHelper(Context context) {
        dbHelper = new HachikoDBOpenHelper(context, null);
    }

    public void insertUser(String displayName, long localContact, String profilePicUri,
                           String primaryEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        insertUserToDb(db, displayName, localContact, profilePicUri, primaryEmail);
        db.close();
    }

    public SQLiteDatabase getWritableUserDB() {
        return dbHelper.getWritableDatabase();
    }

    public void insertUserToDb(SQLiteDatabase db, String displayName, long localContact,
                               String profilePicUri, String primaryEmail) {
        ContentValues values = new ContentValues();
        values.put(IS_HACHIKO_USER, 0);
        values.put(DISPLAY_NAME, displayName);
        values.put(PROFILE_PIC_URI, profilePicUri);
        values.put(LOCAL_CONTACT_ID, localContact);
        values.put(PRIMARY_EMAIL, primaryEmail);
        db.insert(USER_TABLE_NAME, null, values);
        HachikoLogger.debug("insert", localContact, primaryEmail);
    }

    public void updateHachikoId(SQLiteDatabase db, long hachikoId, long localContactId, boolean isHachikoUser) {
        ContentValues values = new ContentValues();
        values.put(HACHIKO_ID, hachikoId);
        values.put(IS_HACHIKO_USER, isHachikoUser ? 1 : 0);
        db.update(USER_TABLE_NAME, values, LOCAL_CONTACT_ID + "=?", new String[]{Long.toString(localContactId)});
    }

    public boolean isHachikoUser(long localContactId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + IS_HACHIKO_USER + " from " + USER_TABLE_NAME + " where "
                + LOCAL_CONTACT_ID + "=" + localContactId + " LIMIT 1;", null);
        if (!c.moveToFirst()) {
            c.close();
            return false;
        }
        boolean isHachikoUser = c.getInt(c.getColumnIndex(IS_HACHIKO_USER)) == 1;
        c.close();
        return isHachikoUser;
    }

    public String queryPrimaryEmail(long localContactId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + '*' + " from " + USER_TABLE_NAME + " where "
                + LOCAL_CONTACT_ID + "=" + localContactId, null);
        if (!c.moveToFirst()) {
            c.close();
            return "";
        }
        String email = c.getString(c.getColumnIndex(PRIMARY_EMAIL));
        c.close();
        return email;
    }

    public long getHachikoId(long localContactId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + HACHIKO_ID + " from " + USER_TABLE_NAME + " where "
                + LOCAL_CONTACT_ID + "=" + localContactId + " LIMIT 1;", null);
        if (!c.moveToFirst()) {
            c.close();
            return 0L;
        }
        long hachikoId = c.getLong(c.getColumnIndex(HACHIKO_ID));
        c.close();
        return hachikoId;
    }

    public String getUserName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + HACHIKO_ID + " from " + USER_TABLE_NAME + ";", null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(HACHIKO_ID));
    }

    public Map<Long, String> getIdToNameMap(Collection<Long> hachikoIds) {
        if (hachikoIds.size() == 0) {
            return Collections.emptyMap();
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<Long, String> names = new HashMap<Long, String>();
        queryAndPutNames(db, names, USER_TABLE_NAME, hachikoIds);
        if (names.size() == hachikoIds.size()) {
            return names;
        }
        Set<Long> unresolvedIds = new HashSet<Long>();
        for (Long id: hachikoIds) {
            if (!names.containsKey(id)) {
                unresolvedIds.add(id);
            }
        }
        queryAndPutNames(db, names, NON_FRIEND_NAME_TABLE_NAME, unresolvedIds);
        return names;
    }

    public Collection<String> getFriendsNameForHachikoIds(Collection<Long> hachikoIds) {
        return getIdToNameMap(hachikoIds).values();
    }

    private void queryAndPutNames(SQLiteDatabase db, Map<Long, String> names,
                                  String tableNameToQuery, Collection<Long> idsToQuery) {
        Cursor c = db.query(tableNameToQuery, new String[]{HACHIKO_ID, DISPLAY_NAME},
                HACHIKO_ID + " in (" + Joiner.on(",").join(idsToQuery) + ")", null,
                null, null, null);
        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(HACHIKO_ID);
            int displayNameIndex = c.getColumnIndex(DISPLAY_NAME);
            do {
                names.put(c.getLong(idIndex), c.getString(displayNameIndex));
            } while (c.moveToNext());
        }
    }

    /**
     * 直接の友達ではない相手の表示名(サーバから取得した)を保持する
     * @param names HachikoIdと表示名の組
     */
    public void persistNonFriendName(Map<Long, String> names) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Map.Entry<Long, String> name: names.entrySet()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(HACHIKO_ID, name.getKey());
            contentValues.put(DISPLAY_NAME, name.getValue());
            db.insert(NON_FRIEND_NAME_TABLE_NAME, null, contentValues);
        }
        db.close();
    }

    public List<FriendItem> getListOfContactEntries() {
        List entries = new ArrayList<FriendItem>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + LOCAL_CONTACT_ID + "," + DISPLAY_NAME + ","
                + PROFILE_PIC_URI + "," + PRIMARY_EMAIL + " from " + USER_TABLE_NAME +";", null);

        int idIndex = cursor.getColumnIndex(LOCAL_CONTACT_ID);
        int nameIndex = cursor.getColumnIndex(DISPLAY_NAME);
        int thumbnailIndex = cursor.getColumnIndex(PROFILE_PIC_URI);
        int emailIndex = cursor.getColumnIndex(PRIMARY_EMAIL);
        if (!cursor.moveToFirst()) {
            return Collections.EMPTY_LIST;
        }
        do {
            String displayName = cursor.getString(nameIndex);
            String uriString = cursor.getString(thumbnailIndex);
            String email = cursor.getString(emailIndex);

            entries.add(new FriendItem(
                    cursor.getLong(idIndex),
                    displayName,
                    uriString == null ? null : Uri.parse(uriString),
                    email));
        } while(cursor.moveToNext());
        cursor.close();
        return entries;
    }

    /**
     * テーブルの内容をログにはく，デバッグ用
     */
    public void dumpAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + '*' + " from " + USER_TABLE_NAME + ";", null);
        if (!c.moveToFirst()) {
            HachikoLogger.debug("No entry in " + USER_TABLE_NAME);
            return;
        }
        do {
            HachikoLogger.debug(c.getString(c.getColumnIndex(HACHIKO_ID)) + "|"
                    + c.getString(c.getColumnIndex(PRIMARY_EMAIL)));
        } while (c.moveToNext());
    }
}
