package com.appspot.hachiko_schedule.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.appspot.hachiko_schedule.data.CandidateDate;
import com.appspot.hachiko_schedule.data.FixedPlan;
import com.appspot.hachiko_schedule.data.Plan;
import com.appspot.hachiko_schedule.data.UnfixedPlan;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import java.util.*;

public class PlansTableHelper {
    private static final String PLAN_TABLE_NAME = "plans";
    private static final String PLAN_ID = "plan_id";
    private static final String TITLE = "title";
    private static final String OWNER_ID = "owner_id";
    private static final String IS_HOST = "is_host";
    private static final String IS_FIXED = "is_fixed";
    private static final String FRIEND_IDS = "friend_ids";
    private static final String CREATED_AT = "created_at";

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
                .addColumn(OWNER_ID, SQLiteType.INTEGER)
                .addColumn(IS_HOST, SQLiteType.INTEGER)
                .addColumn(IS_FIXED, SQLiteType.INTEGER)
                .addColumn(FRIEND_IDS, SQLiteType.TEXT)
                .addColumn(CREATED_AT, SQLiteType.INTEGER)
                .toString();
        String createCanidateDatesTable = new SQLiteCreateTableBuilder(CANDIDATE_DATE_TABLE_NAME)
                .addColumn(ANSWER_ID, SQLiteType.INTEGER, SQLiteConstraint.PRIMARY_KEY)
                .addColumn(PLAN_ID, SQLiteType.INTEGER)
                .addColumn(START_AT_MILLIS, SQLiteType.INTEGER)
                .addColumn(END_AT_MILLIS, SQLiteType.INTEGER)
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

    public long insertNewPlan(Plan plan, long myId, List<Long> friendIds, List<CandidateDate> dates) {
        return insertNewPlan(plan.getPlanId(),
                plan.getTitle(),
                plan.getOwnerId(),
                plan.getOwnerId() == myId,
                friendIds,
                dates);
    }

    /**
     * (Unfixedな)予定を追加する
     */
    public long insertNewPlan(long planId, String title, long ownerId, boolean isHost,
                              List<Long> friendIds, List<CandidateDate> dates) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues planValue = new ContentValues();
        planValue.put(PLAN_ID, planId);
        planValue.put(TITLE, title);
        planValue.put(OWNER_ID, ownerId);
        planValue.put(IS_HOST, isHost ? 1 : 0);
        planValue.put(IS_FIXED, 0);
        planValue.put(FRIEND_IDS, Joiner.on(",").join(friendIds));
        planValue.put(CREATED_AT, new Date().getTime());
        db.insert(PLAN_TABLE_NAME, null, planValue);
        for (CandidateDate date: dates) {
            insertCandidateDate(db, planId, date);
        }
        db.close();
        HachikoLogger.debug("insert plan:", planValue);
        return planId;
    }

    private void insertCandidateDate(SQLiteDatabase db, long planId, CandidateDate candidateDate) {
        ContentValues values = new ContentValues();
        values.put(PLAN_ID, planId);
        if (candidateDate.getAnswerId() > 0) {
            values.put(ANSWER_ID, candidateDate.getAnswerId());
        }
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
    public List<Plan> queryUnfixedPlans() {
        return queryPlans(true);
    }

    /**
     * @return すべての予定を取得
     */
    // TODO: 全部クエリするのではなくselectFromとかnumDataみたいな引数を指定できるように
    public List<Plan> queryPlans() {
        return queryPlans(false);
    }

    /**
     * @return すべての予定を取得
     */
    // TODO: 全部クエリするのではなくselectFromとかnumDataみたいな引数を指定できるように
    public List<Plan> queryPlans(boolean onlyUnfixed) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + PLAN_TABLE_NAME +
                (onlyUnfixed ? (" WHERE " + IS_FIXED + " == 0") : "")
                + " ORDER BY "
                + CREATED_AT + " DESC;", null);
        if (!c.moveToFirst()) {
            c.close();
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<Plan>();
        do {
            long planId = c.getLong(c.getColumnIndex(PLAN_ID));
            Collection<Long> participantIds = Collections2.transform(
                    Arrays.asList(c.getString(c.getColumnIndex(FRIEND_IDS)).split(",")),
                    new Function<String, Long>() {
                        @Override
                        public Long apply(String val) {
                            return Long.parseLong(val);
                        }
                    });
            String title = c.getString(c.getColumnIndex(TITLE));
            long ownerId = c.getLong(c.getColumnIndex(OWNER_ID));
            if (participantIds.contains(ownerId)) {
                participantIds = new ArrayList<Long>(participantIds);
                participantIds.remove(ownerId);
            }
            boolean isFixed = c.getInt(c.getColumnIndex(IS_FIXED)) == 1;
            if (isFixed) {
                plans.add(new FixedPlan(
                        planId, title, ownerId, queryCandidateDates(db, planId).get(0)));
            } else {
                plans.add(new UnfixedPlan(
                        planId,
                        title,
                        ownerId,
                        userTableHelper.getFriendsNameForHachikoIds(participantIds),
                        queryCandidateDates(db, planId)
                ));
            }
        } while (c.moveToNext());
        db.close();
        return plans;
    }

    private List<CandidateDate> queryCandidateDates(SQLiteDatabase db, long planId) {
        Cursor c = db.query(
                CANDIDATE_DATE_TABLE_NAME, null, PLAN_ID + "==" + planId, null, null, null, null);
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
                    CandidateDate.AnswerState.fromInt(c.getInt(c.getColumnIndex(ANSWER_STATE))),
                    stringArrayToList(c.getString(c.getColumnIndex(POSITIVE_MEMBER_IDS))),
                    stringArrayToList(c.getString(c.getColumnIndex(NEGETIVE_MEMBER_IDS)))
            ));
        } while (c.moveToNext());
        return candidateDates;
    }

    public void updateOwnAnswer(long planId, long answerId, CandidateDate.AnswerState state) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANSWER_STATE, state.toInt());
        db.update(CANDIDATE_DATE_TABLE_NAME, values, PLAN_ID + "==? AND " + ANSWER_ID + "==?",
                new String[]{Long.toString(planId), Long.toString(answerId)});
        db.close();
    }

    public void updateAnswerIdByStartDate(long planId, Date startDate, long answerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANSWER_ID, answerId);
        db.update(CANDIDATE_DATE_TABLE_NAME, values, PLAN_ID + "==? AND " + START_AT_MILLIS + "==?",
                new String[]{Long.toString(planId), Long.toString(startDate.getTime())});
        db.close();
    }

    public void updateAnswers(long planId, long answerId, Set<Long> positiveFriendIds,
                             Set<Long> negativeFriendIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POSITIVE_MEMBER_IDS, positiveFriendIds.toString());
        values.put(NEGETIVE_MEMBER_IDS, negativeFriendIds.toString());
        db.update(CANDIDATE_DATE_TABLE_NAME, values, PLAN_ID + "==? AND " + ANSWER_ID + "==?",
                new String[]{Long.toString(planId), Long.toString(answerId)});
        db.close();
    }

    public void confirmCandidateDate(long planId, long answerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CANDIDATE_DATE_TABLE_NAME, PLAN_ID + "==? AND " + ANSWER_ID + "!=?",
                new String[]{Long.toString(planId), Long.toString(answerId)});
        ContentValues values = new ContentValues();
        values.put(IS_FIXED, 1);
        db.update(PLAN_TABLE_NAME, values, PLAN_ID + "==?", new String[] {Long.toString(planId)});
        db.close();
    }

    public String queryTitle(long planId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(false, PLAN_TABLE_NAME, new String[]{TITLE}, PLAN_ID + "==" + planId,
                null, null, null, null, "1");
        String ret = null;
        if (c.moveToFirst()) {
            ret = c.getString(c.getColumnIndex(TITLE));
        }
        db.close();
        return ret;
    }

    public boolean planExists(long planId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(false, PLAN_TABLE_NAME, new String[]{PLAN_ID}, PLAN_ID + "==" + planId,
                null, null, null, null, "1");
        boolean ret = c.moveToFirst();
        db.close();
        return ret;
    }

    public boolean isFixed(long planId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(false, PLAN_TABLE_NAME, new String[]{IS_FIXED}, PLAN_ID + "==" + planId,
                null, null, null, null, "1");
        boolean ret = false;
        if (!c.moveToFirst()) {
            ret = c.getInt(c.getColumnIndex(IS_FIXED)) != 0;
        }
        db.close();
        return ret;
    }

    /**
     * デバッグ要
     */
    public void debugDeletePlansAndCandidateDates() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PLAN_TABLE_NAME, "1", new String[]{});
        db.delete(CANDIDATE_DATE_TABLE_NAME, "1", new String[]{});
        db.close();
    }

    private List<Long> stringArrayToList(String str) {
        if (str == null || str.length() == 0 || "[]".equals(str)) {
            return Collections.emptyList();
        }

        if (str.startsWith("[")) {
            str = str.substring(1, str.length() - 1);
        }
        String[] strs = str.split(",");
        List<Long> ret = new ArrayList<Long>();
        for (int i = 0; i < strs.length; i++) {
            ret.add(Long.parseLong(strs[i].trim()));
        }
        return ret;
    }
}
