package tk.hachikoma.data;

import java.util.Collection;
import java.util.List;

/**
 * 未確定の予定を表す
 */
public class UnfixedPlan extends Plan {
    private final Collection<String> potentialParticipants;
    private final List<CandidateDate> candidateDates;

    public UnfixedPlan(long planId, String title, long ownerId,
                       Collection<String> potentialParticipants,
                       List<CandidateDate> candidateDates) {
        super(planId, title, ownerId, false);
        this.potentialParticipants = potentialParticipants;
        this.candidateDates = candidateDates;
    }

    public Collection<String> getpotentialParticipants() {
        return potentialParticipants;
    }

    public List<CandidateDate> getCandidateDates() {
        return candidateDates;
    }
}
