package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.DistributionInfo
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.DistributionSourceProvider

internal data class ClidsInfo(
    override val chosen: Candidate,
    override val candidates: List<Candidate>
) : DistributionInfo<ClidsInfo.Candidate, ClidsInfo.Candidate> {

    internal data class Candidate(
        val clids: Map<String, String>?,
        override val source: DistributionSource
    ) : DistributionSourceProvider
}
