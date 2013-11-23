package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.appspot.hachiko_schedule.data.FriendGroup;
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

    // 直接の友人ではないが，同じ予定に招待されている人の表示名を格納するテーブル
    private static final String NON_FRIEND_NAME_TABLE_NAME = "non_friend_names";

    private static final String GROUP_TABLE_NAME = "groups";
    private static final String GROUP_ID = "group_id";
    private static final String GROUP_NAME = "group_name";
    private static final String FRIEND_IDS_COMMA_SEPARATED = "friend_ids";
    private static final String GROUP_ICON_URI = "group_icon_uri";

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
        String createGroupTable = new SQLiteCreateTableBuilder(GROUP_TABLE_NAME)
                .addColumn(GROUP_ID, SQLiteType.INTEGER, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(GROUP_NAME, SQLiteType.TEXT)
                .addColumn(FRIEND_IDS_COMMA_SEPARATED, SQLiteType.TEXT)
                .addColumn(GROUP_ICON_URI, SQLiteType.TEXT)
                .toString();
        database.execSQL(createUsersTable);
        database.execSQL(createNonFriendNamesTable);
        database.execSQL(createGroupTable);
    }

    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public UserTableHelper(Context context) {
        dbHelper = new HachikoDBOpenHelper(context, null);
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

    public FriendItem queryUser(long hachikoid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(USER_TABLE_NAME, null, HACHIKO_ID + "==?", new String[] {
                Long.toString(hachikoid)
        }, null, null, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        String uriString = c.getString(c.getColumnIndex(PROFILE_PIC_URI));
        FriendItem item = new FriendItem(hachikoid,
                c.getString(c.getColumnIndex(DISPLAY_NAME)),
                uriString == null ? null : Uri.parse(uriString),
                c.getString(c.getColumnIndex(PRIMARY_EMAIL)));
        c.close();
        return item;
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

    public boolean registerFriendAsHachikoUser(long hachikoId) {
        SQLiteDatabase db = getWritableUserDB();
        ContentValues values = new ContentValues();
        values.put(IS_HACHIKO_USER, true);
        return db.update(USER_TABLE_NAME, values, HACHIKO_ID + "==" + hachikoId, null) > 0;
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
     * @return 作成されたrowのID
     */
    public long createGroup(String groupName, Collection<Long> userIds, String groupIconUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GROUP_NAME, groupName);
        values.put(FRIEND_IDS_COMMA_SEPARATED, Joiner.on(",").join(userIds));
        values.put(GROUP_ICON_URI, groupIconUri);
        long id = db.insert(GROUP_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public List<FriendGroup> getListOfGroups() {
        List entries = new ArrayList<FriendGroup>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(GROUP_TABLE_NAME, null, null, null, null, null, null);
        int idIndex = cursor.getColumnIndex(GROUP_ID);
        int nameIndex = cursor.getColumnIndex(GROUP_NAME);
        int thumbnailIndex = cursor.getColumnIndex(GROUP_ICON_URI);
        int friendIdsIndex = cursor.getColumnIndex(FRIEND_IDS_COMMA_SEPARATED);
        if (!cursor.moveToFirst()) {
            return Collections.EMPTY_LIST;
        }
        do {
            String uriString = cursor.getString(thumbnailIndex);
            String[] friendIds = cursor.getString(friendIdsIndex).split(",");
            Set<FriendItem> friends = new HashSet<FriendItem>();
            for (String friendId: friendIds) {
                FriendItem friend = queryUser(Long.parseLong(friendId));
                if (friend != null) {
                    friends.add(friend);
                }
            }
            entries.add(new FriendGroup(
                    cursor.getInt(idIndex),
                    cursor.getString(nameIndex),
                    uriString == null ? null : Uri.parse(uriString),
                    friends));
        } while(cursor.moveToNext());
        cursor.close();
        return entries;
    }

    public int deleteGroup(int groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(GROUP_TABLE_NAME, GROUP_ID + "==" + groupId, null);
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
