package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class PreloadInfoCandidatesHelperTest(
    private val stateFromDisk: PreloadInfoData,
    private val oldCandidates: List<PreloadInfoData.Candidate>,
    private val newCandidate: PreloadInfoState,
    private val expected: List<PreloadInfoData.Candidate>?,
    private val description: String
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{4}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            val preloadInfoDataWithApp = PreloadInfoData(
                PreloadInfoState("999", JSONObject(), true, false, DistributionSource.SATELLITE),
                listOf(
                    PreloadInfoData.Candidate("999", JSONObject(), DistributionSource.RETAIL),
                    PreloadInfoData.Candidate("999", JSONObject(), DistributionSource.APP),
                    PreloadInfoData.Candidate("999", JSONObject(), DistributionSource.SATELLITE)
                )
            )
            val preloadInfoDataWithoutApp = PreloadInfoData(
                PreloadInfoState("999", JSONObject(), true, false, DistributionSource.SATELLITE),
                listOf(
                    PreloadInfoData.Candidate("999", JSONObject(), DistributionSource.RETAIL),
                    PreloadInfoData.Candidate("999", JSONObject(), DistributionSource.SATELLITE)
                )
            )
            val firstRetailCandidate =
                PreloadInfoData.Candidate("333", JSONObject().put("key3", "value3"), DistributionSource.RETAIL)
            val secondRetailCandidate =
                PreloadInfoData.Candidate("3332", JSONObject().put("key32", "value32"), DistributionSource.RETAIL)
            val secondRetailState = PreloadInfoState(
                secondRetailCandidate.trackingId,
                secondRetailCandidate.additionalParams,
                true,
                false,
                secondRetailCandidate.source
            )
            val firstSatelliteCandidate =
                PreloadInfoData.Candidate("444", JSONObject().put("key4", "value4"), DistributionSource.SATELLITE)
            val secondSatelliteCandidate =
                PreloadInfoData.Candidate("4442", JSONObject().put("key42", "value42"), DistributionSource.SATELLITE)
            val secondSatelliteState = PreloadInfoState(
                secondSatelliteCandidate.trackingId,
                secondSatelliteCandidate.additionalParams,
                true,
                false,
                secondSatelliteCandidate.source
            )
            val firstAppCandidate =
                PreloadInfoData.Candidate("111", JSONObject().put("key", "value"), DistributionSource.APP)
            val firstAppState = PreloadInfoState(
                firstAppCandidate.trackingId,
                firstAppCandidate.additionalParams,
                true,
                false,
                firstAppCandidate.source
            )
            val secondAppCandidate =
                PreloadInfoData.Candidate("222", JSONObject().put("key2", "value2"), DistributionSource.APP)
            val secondAppState = PreloadInfoState(
                secondAppCandidate.trackingId,
                secondAppCandidate.additionalParams,
                true,
                false,
                secondAppCandidate.source
            )
            return listOf(
                arrayOf(
                    preloadInfoDataWithoutApp,
                    emptyList<PreloadInfoData.Candidate>(),
                    firstAppState,
                    listOf(firstAppCandidate),
                    "Adds APP to empty list, no initial app"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    emptyList<PreloadInfoData.Candidate>(),
                    firstAppState,
                    listOf(firstAppCandidate),
                    "Adds APP to empty list, has initial app"
                ),
                arrayOf(
                    preloadInfoDataWithoutApp,
                    listOf(firstAppCandidate),
                    secondAppState,
                    listOf(firstAppCandidate, secondAppCandidate),
                    "Adds APP to list only with APP, no initial app"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstAppCandidate),
                    secondAppState,
                    null,
                    "Does not APP to list only with APP, has initial app"
                ),
                arrayOf(
                    preloadInfoDataWithoutApp,
                    listOf(firstRetailCandidate, firstAppCandidate, firstSatelliteCandidate),
                    secondAppState,
                    listOf(firstRetailCandidate, firstAppCandidate, firstSatelliteCandidate, secondAppCandidate),
                    "Adds APP to list with APP, no initial app"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstRetailCandidate, firstAppCandidate, firstSatelliteCandidate),
                    secondAppState,
                    null,
                    "Does not add APP to list with APP, has initial app"
                ),
                arrayOf(
                    preloadInfoDataWithoutApp,
                    listOf(firstRetailCandidate, firstSatelliteCandidate),
                    secondAppState,
                    listOf(firstRetailCandidate, firstSatelliteCandidate, secondAppCandidate),
                    "Adds APP to list without APP, no initial app"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstRetailCandidate, firstSatelliteCandidate),
                    secondAppState,
                    listOf(firstRetailCandidate, firstSatelliteCandidate, secondAppCandidate),
                    "Adds APP to list without APP, has initial app"
                ),

                arrayOf(
                    preloadInfoDataWithApp,
                    emptyList<PreloadInfoData.Candidate>(),
                    secondRetailState,
                    listOf(secondRetailCandidate),
                    "Adds RETAIL to empty list"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstRetailCandidate),
                    secondRetailState,
                    null,
                    "Does not add RETAIL to list only with RETAIL"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstRetailCandidate, firstAppCandidate, firstSatelliteCandidate),
                    secondRetailState,
                    null,
                    "Does not add RETAIL to list with RETAIL"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstAppCandidate, firstSatelliteCandidate),
                    secondRetailState,
                    listOf(firstAppCandidate, firstSatelliteCandidate, secondRetailCandidate),
                    "Adds RETAIL to list without RETAIL"
                ),

                arrayOf(
                    preloadInfoDataWithApp,
                    emptyList<PreloadInfoData.Candidate>(),
                    secondSatelliteState,
                    listOf(secondSatelliteCandidate),
                    "Adds SATELLITE to empty list"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstSatelliteCandidate),
                    secondSatelliteState,
                    null,
                    "Does not add SATELLITE to list only with SATELLITE"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstRetailCandidate, firstAppCandidate, firstSatelliteCandidate),
                    secondSatelliteState,
                    null,
                    "Does not add SATELLITE to list with SATELLITE"
                ),
                arrayOf(
                    preloadInfoDataWithApp,
                    listOf(firstAppCandidate, firstRetailCandidate),
                    secondSatelliteState,
                    listOf(firstAppCandidate, firstRetailCandidate, secondSatelliteCandidate),
                    "Adds SATELLITE to list without SATELLITE"
                ),
            )
        }
    }

    private lateinit var candidatesHelper: PreloadInfoCandidatesHelper

    @Before
    fun setUp() {
        candidatesHelper = PreloadInfoCandidatesHelper(stateFromDisk)
    }

    @Test
    fun getUpdatedCandidates() {
        val oldCandidatesCopy = ArrayList(oldCandidates)
        val actual = candidatesHelper(oldCandidates, newCandidate)
        if (expected == null) {
            assertThat(actual).isNull()
        } else {
            assertThat(actual).usingRecursiveFieldByFieldElementComparator().isEqualTo(expected)
        }
        assertThat(oldCandidates).isEqualTo(oldCandidatesCopy)
    }
}
