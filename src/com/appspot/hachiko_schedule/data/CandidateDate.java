package com.appspot.hachiko_schedule.data;

import java.util.Collection;

/**
 * 予定調整中の候補日に対応するデータクラス
 */
public class CandidateDate {
    private final String dateText;
    private Collection<String> positiveFriends;
    private Collection<String> negativeFriends;

    public CandidateDate(String dateText, Collection<String> positiveFriends,
                         Collection<String> negativeFriends) {
        this.dateText = dateText;
        this.positiveFriends = positiveFriends;
        this.negativeFriends = negativeFriends;
    }

    public String getDateText() {
        return dateText;
    }

    public Collection<String> getPositiveFriends() {
        return positiveFriends;
    }

    public Collection<String> getNegativeFriends() {
        return negativeFriends;
    }
}
