package com.appspot.hachiko_schedule.data;

import java.util.Collection;
import java.util.List;

public class UnfixedPlan {
    private String title;
    private Collection<String> potentialCandidates;
    private List<CandidateDate> candidateDates;

    public UnfixedPlan(
            String title, Collection<String> potentialCandidates, List<CandidateDate> candidateDates) {
        this.title = title;
        this.potentialCandidates = potentialCandidates;
        this.candidateDates = candidateDates;
    }

    public String getTitle() {
        return title;
    }

    public Collection<String> getPotentialCandidates() {
        return potentialCandidates;
    }

    public List<CandidateDate> getCandidateDates() {
        return candidateDates;
    }
}
