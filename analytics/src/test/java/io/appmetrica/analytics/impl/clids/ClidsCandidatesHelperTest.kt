package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ClidsCandidatesHelperTest(
    private val oldCandidates: List<ClidsInfo.Candidate>,
    private val newCandidate: ClidsInfo.Candidate,
    private val expected: List<ClidsInfo.Candidate>?
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any?>> {
            val firstAppCandidate = ClidsInfo.Candidate(emptyMap(), DistributionSource.APP)
            val secondAppCandidate = ClidsInfo.Candidate(mapOf("clid2" to "2"), DistributionSource.APP)
            val thirdAppCandidate = ClidsInfo.Candidate(mapOf("clid3" to "3"), DistributionSource.APP)
            val firstRetailCandidate = ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL)
            val secondRetailCandidate = ClidsInfo.Candidate(mapOf("clid2" to "2"), DistributionSource.RETAIL)
            val firstSatelliteCandidate = ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE)
            val secondSatelliteCandidate = ClidsInfo.Candidate(mapOf("clid2" to "2"), DistributionSource.SATELLITE)
            return listOf(
                // #0
                arrayOf(emptyList<ClidsInfo.Candidate>(), firstAppCandidate, listOf(firstAppCandidate)),
                arrayOf(listOf(firstRetailCandidate, firstSatelliteCandidate), firstAppCandidate, listOf(firstRetailCandidate, firstSatelliteCandidate, firstAppCandidate)),
                arrayOf(listOf(firstAppCandidate, firstRetailCandidate, firstSatelliteCandidate), secondAppCandidate, listOf(firstRetailCandidate, firstSatelliteCandidate, secondAppCandidate)),
                arrayOf(listOf(firstAppCandidate), secondAppCandidate, listOf(secondAppCandidate)),
                arrayOf(listOf(firstAppCandidate, secondAppCandidate), thirdAppCandidate, listOf(thirdAppCandidate)),

                // #5
                arrayOf(emptyList<ClidsInfo.Candidate>(), firstRetailCandidate, listOf(firstRetailCandidate)),
                arrayOf(listOf(firstAppCandidate, firstSatelliteCandidate), firstRetailCandidate, listOf(firstAppCandidate, firstSatelliteCandidate, firstRetailCandidate)),
                arrayOf(listOf(firstRetailCandidate), secondRetailCandidate, null),
                arrayOf(listOf(firstAppCandidate, firstRetailCandidate, firstSatelliteCandidate), secondRetailCandidate, null),
                arrayOf(emptyList<ClidsInfo.Candidate>(), firstSatelliteCandidate, listOf(firstSatelliteCandidate)),

                // #10
                arrayOf(listOf(firstAppCandidate, firstRetailCandidate), firstSatelliteCandidate, listOf(firstAppCandidate, firstRetailCandidate, firstSatelliteCandidate)),
                arrayOf(listOf(firstSatelliteCandidate), secondSatelliteCandidate, null),
                arrayOf(listOf(firstAppCandidate, firstRetailCandidate, firstSatelliteCandidate), secondSatelliteCandidate, null),
            )
        }
    }

    private val candidatesHelper = ClidsCandidatesHelper()

    @Test
    fun getUpdatedCandidates() {
        assertThat(candidatesHelper(oldCandidates, newCandidate)).isEqualTo(expected)
    }
}
