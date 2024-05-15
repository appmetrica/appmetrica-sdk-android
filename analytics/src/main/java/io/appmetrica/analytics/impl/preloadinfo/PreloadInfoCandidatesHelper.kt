package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.UpdatedCandidatesProvider
import io.appmetrica.analytics.logger.internal.DebugLogger

private const val TAG = "[PreloadInfoCandidatesHelper]"

internal class PreloadInfoCandidatesHelper(stateFromDisk: PreloadInfoData) :
    UpdatedCandidatesProvider<PreloadInfoData.Candidate, PreloadInfoState> {

    private val isFirstLaunchWithPreloadInfoFromApp = stateFromDisk.candidates.none {
        it.source == DistributionSource.APP
    }

    override fun invoke(
        oldCandidates: List<PreloadInfoData.Candidate>,
        newCandidate: PreloadInfoState
    ): List<PreloadInfoData.Candidate>? {
        val newListCandidate = PreloadInfoData.Candidate(
            newCandidate.trackingId,
            newCandidate.additionalParameters,
            newCandidate.source
        )
        return if (oldCandidates.any { it.source == newCandidate.source }) {
            if (newListCandidate.source == DistributionSource.APP && isFirstLaunchWithPreloadInfoFromApp) {
                oldCandidates + newListCandidate
            } else {
                null
            }
        } else {
            oldCandidates + newListCandidate
        }.also {
            DebugLogger.info(
                TAG,
                "get updated candidates from $oldCandidates and $newCandidate. " +
                    "isFirstLaunchWithPreloadInfoFromApp  = $isFirstLaunchWithPreloadInfoFromApp. Result is $it"
            )
        }
    }
}
