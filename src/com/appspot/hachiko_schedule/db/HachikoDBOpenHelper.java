package com.appspot.hachiko_schedule.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * アプリで利用するメインのローカルDBの生成のための{@link SQLiteOpenHelper}.
 */
public class HachikoDBOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    private static final String DB_NAME = "hachiko_main";

    public HachikoDBOpenHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        UserTableHelper.onCreateDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        UserTableHelper.onUpgradeDatabase(db, oldVersion, newVersion);
    }
}
