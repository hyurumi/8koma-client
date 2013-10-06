package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;

import java.util.*;

public class PlansTableHelper {
    private static final String PLAN_TABLE_NAME = "plans";
    private static final String PLAN_ID = "plan_id";
    private static final String TITLE = "title";
    private static final String IS_HOST = "is_host";
    private static final String IS_FIXED = "is_fixed";
    private static final String FRIEND_IDS = "friend_ids";

    private static final String CANDIDATE_DATE_TABLE_NAME = "candiate_dates";
    private static final String START_AT_MILLIS = "start_at_millis";
    private static final String END_AT_MILLIS = "end_at_millis";
    private static final String ANSWER_ID = "answer_id";
    private static final String ANSWER_STATE = "answer_state";
    private static final String POSITIVE_MEMBER_IDS = "positive_member_ids";
    private static final String NEGETIVE_MEMBER_IDS = "negative_member_ids";

    private HachikoDBOpenHelper dbHelper;
    private UserTableHelper userTableHelper;

    public static void onCreateDatabase(SQLiteDatabase database) {
        String createUsersTable = new SQLiteCreateTableBuilder(PLAN_TABLE_NAME)
                .addColumn(PLAN_ID, SQLiteType.INTEGER, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(TITLE, SQLiteType.TEXT)
                .addColumn(IS_HOST, SQLiteType.INTEGER)
                .addColumn(IS_FIXED, SQLiteType.INTEGER)
                .addColumn(FRIEND_IDS, SQLiteType.TEXT)
                .toString();
        String createCanidateDatesTable = new SQLiteCreateTableBuilder(CANDIDATE_DATE_TABLE_NAME)
                .addColumn(PLAN_ID, SQLiteType.INTEGER)
                .addColumn(START_AT_MILLIS, SQLiteType.INTEGER)
                .addColumn(END_AT_MILLIS, SQLiteType.INTEGER)
                .addColumn(ANSWER_ID, SQLiteType.INTEGER)
                .addColumn(ANSWER_STATE, SQLiteType.INTEGER)
                .addColumn(POSITIVE_MEMBER_IDS, SQLiteType.TEXT)
                .addColumn(NEGETIVE_MEMBER_IDS, SQLiteType.TEXT)
                .toString();
        database.execSQL(createUsersTable);
        database.execSQL(createCanidateDatesTable);
    }

    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public PlansTableHelper(Context context) {
        dbHelper = new HachikoDBOpenHelper(context, null);
        userTableHelper = new UserTableHelper(context);
    }

    /**
     * (Unfixedな)予定を追加する
     */
    public void insertNewPlan(
            String title, boolean isHost, List<String> frinedIds, List<CandidateDate> dates) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues planValue = new ContentValues();
        planValue.put(TITLE, title);
        planValue.put(IS_HOST, isHost ? 1 : 0);
        planValue.put(IS_FIXED, 0);
        planValue.put(FRIEND_IDS, Joiner.on(",").join(frinedIds));
        long planId = db.insert(PLAN_TABLE_NAME, null, planValue);
        for (CandidateDate date: dates) {
            insertCandidateDate(db, planId, date);
        }
        db.close();
        HachikoLogger.debug("insert plan:", planValue);
    }

    private void insertCandidateDate(SQLiteDatabase db, long planId, CandidateDate candidateDate) {
        ContentValues values = new ContentValues();
        values.put(PLAN_ID, planId);
        values.put(ANSWER_ID, candidateDate.getAnswerId());
        values.put(ANSWER_STATE, candidateDate.getMyAnswerState().toInt());
        values.put(START_AT_MILLIS, candidateDate.getStartDate().getTime());
        values.put(END_AT_MILLIS, candidateDate.getEndDate().getTime());
        db.insert(CANDIDATE_DATE_TABLE_NAME, null, values);
        HachikoLogger.debug("insert date:", values);
    }

    /**
     * @return すべての未確定な予定を取得
     */
    // TODO: 全部クエリするのではなくselectFromとかnumDataみたいな引数を指定できるように
    public List<UnfixedPlan> queryUnfixedPlans() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + PLAN_TABLE_NAME + " WHERE " + IS_FIXED + " == 0;", null);
        if (!c.moveToFirst()) {
            c.close();
            return Collections.emptyList();
        }
        List<UnfixedPlan> unfixedPlans = new ArrayList<UnfixedPlan>();
        do {
            long planId = c.getLong(c.getColumnIndex(PLAN_ID));
            String participantIds = c.getString(c.getColumnIndex(FRIEND_IDS));
            HachikoLogger.debug(participantIds);
            unfixedPlans.add(new UnfixedPlan(
                    planId,
                    c.getString(c.getColumnIndex(TITLE)),
                    userTableHelper.getFriendsNameForHachikoIds(
                            Arrays.asList(participantIds.split(","))),
                    queryCandidateDates(db, planId)
            ));
        } while (c.moveToNext());
        db.close();
        return unfixedPlans;
    }

    private List<CandidateDate> queryCandidateDates(SQLiteDatabase db, long planId) {
        Cursor c = db.rawQuery("select " + ANSWER_ID + "," + START_AT_MILLIS + "," + END_AT_MILLIS
                + "," + ANSWER_STATE + " from " + CANDIDATE_DATE_TABLE_NAME + " WHERE " + PLAN_ID
                + " == " + planId + ";",
                null);
        if (!c.moveToFirst()) {
            c.close();
            HachikoLogger.debug("no dates for plan", planId);
            return Collections.emptyList();
        }
        List<CandidateDate> candidateDates = new ArrayList<CandidateDate>();
        do {
            candidateDates.add(new CandidateDate(
                    c.getInt(c.getColumnIndex(ANSWER_ID)),
                    new Date(c.getLong(c.getColumnIndex(START_AT_MILLIS))),
                    new Date(c.getLong(c.getColumnIndex(END_AT_MILLIS))),
                    CandidateDate.AnswerState.fromInt(c.getInt(c.getColumnIndex(ANSWER_STATE)))
            ));
        } while (c.moveToNext());
        return candidateDates;
    }

    public void updateAnswer(long planId, long answerId, CandidateDate.AnswerState state) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANSWER_STATE, state.toInt());
        db.update(CANDIDATE_DATE_TABLE_NAME, values, PLAN_ID + "==? AND " + ANSWER_ID + "==?",
                new String[]{Long.toString(planId), Long.toString(answerId)});
        db.close();
    }
}
