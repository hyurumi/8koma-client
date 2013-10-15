package com.appspot.hachiko_schedule.data;

import android.content.Context;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.util.DateUtils;
import com.google.common.base.Joiner;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 予定調整中の候補日に対応するデータクラス
 */
public class CandidateDate {
    private final int answerId;
    private final Date startDate;
    private final Date endDate;
    private AnswerState myAnswerState;
    private List<Long> positiveFriendIds;
    private List<Long> negativeFriendIds;

    public CandidateDate(int answerId, Date startDate, Date endDate, AnswerState myAnswerState) {
        this(answerId, startDate, endDate, myAnswerState, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public CandidateDate(int answerId, Date startDate, Date endDate, AnswerState myAnswerState,
                         List<Long> positiveFriendIds, List<Long> negativeFriendIds) {
        this.answerId = answerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.myAnswerState = myAnswerState;
        this.positiveFriendIds = positiveFriendIds;
        this.negativeFriendIds = negativeFriendIds;
    }

    public String getDateText() {
        return DateUtils.timeslotString(startDate, endDate);
    }

    public String getPositiveFriendNames(Context context) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        Collection<String> hachikoIds = userTableHelper.getFriendsNameForHachikoIds(positiveFriendIds);
        return Joiner.on(", ").join(hachikoIds);
    }
    public int getPositiveFriendsNum() {
        return positiveFriendIds.size();
    }

    public int getNegativeFriendsNum() {
        return negativeFriendIds.size();
    }

    public int getAnswerId() {
        return answerId;
    }

    public AnswerState getMyAnswerState() {
        return myAnswerState;
    }

    public void setMyAnswerState(AnswerState myAnswerState) {
        this.myAnswerState = myAnswerState;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public static enum AnswerState {
        OK(R.color.ok_green), NEUTRAL(R.color.neutral_yellow), NG(R.color.ng_red);

        private final int colorResource;
        private AnswerState(int colorResource) {
            this.colorResource = colorResource;
        }

        public static AnswerState fromInt(int i) {
            switch (i) {
                case 0:
                    return OK;
                case 1:
                    return NEUTRAL;
                case 2:
                    return NG;
            }
            return null;
        }

        public static AnswerState fromString(String str) {
            str = str.toLowerCase();
            if (str.equals("ok")) {
                return OK;
            } else if (str.equals("tentative")) {
                return NEUTRAL;
            } else if (str.equals("ng")) {
                return NG;
            }
            return NEUTRAL;
        }

        public int toInt() {
            switch (this) {
                case OK:
                    return 0;
                case NEUTRAL:
                    return 1;
                case NG:
                    return 2;
            }
            return -1;
        }

        public int getColorResource() {
            return colorResource;
        }


        @Override
        public String toString() {
            switch (this) {
                case OK:
                    return "ok";
                case NEUTRAL:
                    return "tentative";
                case NG:
                    return "ng";
            }
            return null;
        }
    }
}
