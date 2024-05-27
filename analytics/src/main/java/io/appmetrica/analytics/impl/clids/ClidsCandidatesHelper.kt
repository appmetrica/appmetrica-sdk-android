package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.UpdatedCandidatesProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val TAG = "[ClidsCandidatesHelper]"

internal class ClidsCandidatesHelper : UpdatedCandidatesProvider<ClidsInfo.Candidate, ClidsInfo.Candidate> {

    override fun invoke(
        oldCandidates: List<ClidsInfo.Candidate>,
        newCandidate: ClidsInfo.Candidate
    ): List<ClidsInfo.Candidate>? {
        return if (oldCandidates.any { it.source == newCandidate.source }) {
            if (newCandidate.source == DistributionSource.APP) {
                oldCandidates.filterNot { it.source == DistributionSource.APP } + newCandidate
            } else {
                null
            }
        } else {
            oldCandidates + newCandidate
        }.also {
            DebugLogger.info(
                TAG,
                "Get updated candidates from $oldCandidates  and $newCandidate. Result is $it"
            )
        }
    }
}
