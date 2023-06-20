package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.StateProvider

internal class PreloadInfoStateProvider : StateProvider<PreloadInfoData.Candidate, PreloadInfoState, PreloadInfoData> {

    override fun invoke(
        newChosen: PreloadInfoState,
        newCandidates: List<PreloadInfoData.Candidate>
    ): PreloadInfoData = PreloadInfoData(newChosen, newCandidates)
}
