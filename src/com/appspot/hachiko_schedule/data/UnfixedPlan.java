package com.appspot.hachiko_schedule.data;

import java.util.Collection;
import java.util.List;

/**
 * 未確定の予定を表すデータクラス
 */
public class UnfixedPlan {
    private final long planId;
    private final String title;
    private final boolean isHost;
    private final Collection<String> potentialParticipants;
    private final List<CandidateDate> candidateDates;

    public UnfixedPlan(long planId, String title, boolean isHost,
                       Collection<String> potentialParticipants,
                       List<CandidateDate> candidateDates) {
        this.planId = planId;
        this.title = title;
        this.isHost = isHost;
        this.potentialParticipants = potentialParticipants;
        this.candidateDates = candidateDates;
    }

    public long getPlanId() {
        return planId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHost() {
        return isHost;
    }

    public Collection<String> getpotentialParticipants() {
        return potentialParticipants;
    }

    public List<CandidateDate> getCandidateDates() {
        return candidateDates;
    }
}
