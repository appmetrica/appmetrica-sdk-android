package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.StateProvider

internal class ClidsStateProvider : StateProvider<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo> {

    override fun invoke(
        newChosen: ClidsInfo.Candidate,
        newCandidates: List<ClidsInfo.Candidate>
    ): ClidsInfo = ClidsInfo(newChosen, newCandidates)
}
