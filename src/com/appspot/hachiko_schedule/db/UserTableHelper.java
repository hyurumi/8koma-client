package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.appspot.hachiko_schedule.util.HachikoLogger;

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
        database.execSQL(createUsersTable);
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
