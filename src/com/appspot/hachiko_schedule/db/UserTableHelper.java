package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public void insertUser(String hachikoId, String displayName, String localContact,
                           String profilePicUri, String primaryEmail) {
        ContentValues values = new ContentValues();
        values.put(HACHIKO_ID, hachikoId);
        values.put(IS_HACHIKO_USER, 0);
        values.put(DISPLAY_NAME, displayName);
        values.put(PROFILE_PIC_URI, profilePicUri);
        values.put(LOCAL_CONTACT_ID, localContact);
        values.put(PRIMARY_EMAIL, primaryEmail);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(USER_TABLE_NAME, null, values);
        db.close();
    }

    public String getUserName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select " + HACHIKO_ID + " from " + USER_TABLE_NAME + ";", null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(HACHIKO_ID));
    }
}
