package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

internal class ClidsStateProviderTest : CommonTest() {

    private val stateProvider = ClidsStateProvider()

    @Test
    fun createState() {
        val chosen = ClidsInfo.Candidate(mapOf("clid0" to "0"), DistributionSource.SATELLITE)
        val candidates = listOf(
            ClidsInfo.Candidate(mapOf("clid1" to "2"), DistributionSource.RETAIL),
            ClidsInfo.Candidate(mapOf("clid2" to "3"), DistributionSource.APP)
        )
        ObjectPropertyAssertions(stateProvider(chosen, candidates))
            .checkField("chosen", "getChosen", chosen)
            .checkField("candidates", "getCandidates", candidates)
            .checkAll()
    }
}
