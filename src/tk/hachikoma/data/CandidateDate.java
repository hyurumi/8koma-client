package tk.hachikoma.data;

import android.content.Context;
import tk.hachikoma.R;
import tk.hachikoma.db.UserTableHelper;
import tk.hachikoma.util.DateUtils;
import com.google.common.base.Joiner;

import java.util.*;

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

    public String getPositiveFriendNames(Context context, long exceptId) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        List<Long> friendIds = positiveFriendIds;
        if (exceptId > 0 && positiveFriendIds.contains(exceptId)) {
            friendIds = new ArrayList<Long>(positiveFriendIds);
            friendIds.remove(exceptId);
        }
        Collection<String> hachikoIds = userTableHelper.getFriendsNameForHachikoIds(friendIds);
        return Joiner.on(", ").join(hachikoIds);
    }
    public int getPositiveFriendsNum(long ownerId) {
        return positiveFriendIds.size() - (positiveFriendIds.contains(ownerId) ? 1 : 0);
    }
    public int getPositiveFriendsNumWithSelf() {
        return positiveFriendIds.size() + (myAnswerState == AnswerState.OK ? 1 : 0);
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
        OK(R.color.ok), NEUTRAL(R.color.tentative), NG(R.color.ng);

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
