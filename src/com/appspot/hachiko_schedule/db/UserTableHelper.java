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
    private static final String PHONE_TABLE_NAME = "phones";
    private static final String EMAIL_TABLE_NAME = "emails";
    private static final String HACHIKO_ID = "hachiko_id";
    private static final String DISPLAY_NAME = "display_name";
    private static final String FB_ID = "fb_id";
    private static final String LOCAL_CONTACT_ID = "local_contact_id";
    private static final String PROFILE_PIC = "profile_pic";
    private static final String PHONE = "phone_no";
    private static final String EMAIL = "address";

    private HachikoDBOpenHelper dbHelper;

    public static void onCreateDatabase(SQLiteDatabase database) {
        String createUsersTable = new SQLiteCreateTableBuilder(USER_TABLE_NAME)
                .addColumn(HACHIKO_ID, SQLiteType.TEXT, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(DISPLAY_NAME, SQLiteType.TEXT, SQLiteConstraint.NOT_NULL)
                .addColumn(PROFILE_PIC, SQLiteType.BLOB)
                .addColumn(FB_ID, SQLiteType.INTEGER)
                .addColumn(LOCAL_CONTACT_ID, SQLiteType.INTEGER)
                .toString();
        String createPhonesTable = new SQLiteCreateTableBuilder(PHONE_TABLE_NAME)
                .addColumn(HACHIKO_ID, SQLiteType.TEXT, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(PHONE, SQLiteType.TEXT)
                .toString();
        String createEmailTable = new SQLiteCreateTableBuilder(EMAIL_TABLE_NAME)
                .addColumn(HACHIKO_ID, SQLiteType.TEXT, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(EMAIL, SQLiteType.TEXT)
                .toString();
        database.execSQL(createUsersTable + createPhonesTable + createEmailTable);
    }

    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public UserTableHelper(Context context) {
        dbHelper = new HachikoDBOpenHelper(context, null);
    }

    public void insertUser(
            String hachikoId, String displayName, String fbId, String localContact,
            String phone, String email) {
        ContentValues values = new ContentValues();
        values.put(HACHIKO_ID, hachikoId);
        values.put(DISPLAY_NAME, displayName);
        values.put(FB_ID, fbId);
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
