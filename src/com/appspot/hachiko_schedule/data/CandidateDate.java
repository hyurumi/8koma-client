package com.appspot.hachiko_schedule.data;

import com.appspot.hachiko_schedule.util.DateUtils;

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
    private List<Long> positiveFriendIds;
    private List<Long> negativeFriendIds;

    public CandidateDate(int answerId, Date startDate, Date endDate) {
        this(answerId, startDate, endDate, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public CandidateDate(int answerId, Date startDate, Date endDate,
                         List<Long> positiveFriendIds, List<Long> negativeFriendIds) {
        this.answerId = answerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.positiveFriendIds = positiveFriendIds;
        this.negativeFriendIds = negativeFriendIds;
    }

    public String getDateText() {
        return DateUtils.timeslotString(startDate, endDate);
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

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
