package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Random

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class StartupUnitStartupDidNotOverrideClidsTest(
    private val newClidsFromResponse: Map<String, String>?,
    private val oldClidsFromResponse: Map<String, String>?,
    private val chosenResponseClids: String?
) : StartupUnitBaseTest() {
    private val chosenForRequestClids = mapOf("clid333" to "333")

    private val collectingFlags: CollectingFlags = mock()

    private val parsedResult: StartupResult = mock {
        on { encodedClids } doReturn StartupUtils.encodeClids(newClidsFromResponse)
        on { collectionFlags } doReturn collectingFlags
    }

    @Before
    fun setUp() {
        super.setup()
        whenever(startupRequestConfig.chosenClids)
            .thenReturn(ClidsInfo.Candidate(chosenForRequestClids, DistributionSource.APP))
        whenever(startupConfigurationHolder.startupState).thenReturn(
            TestUtils.createDefaultStartupStateBuilder()
                .withEncodedClidsFromResponse(StartupUtils.encodeClids(oldClidsFromResponse))
                .build()
        )
    }

    @Test
    fun startupDidNotOverrideClids() {
        val result = Random().nextBoolean()
        whenever(
            clidsStateChecker.doRequestClidsMatchResponseClids(
                chosenForRequestClids,
                chosenResponseClids
            )
        ).thenReturn(result)
        val startupState = startupUnit.parseStartupResult(parsedResult, startupRequestConfig, 0L)
        assertThat(startupState.startupDidNotOverrideClids).isEqualTo(result)
        verify(clidsStateChecker)
            .doRequestClidsMatchResponseClids(chosenForRequestClids, chosenResponseClids)
    }

    companion object {
        private val EMPTY_MAP: Map<String, String> = HashMap()

        private val MAP_WITH_VALID_ITEMS_1 = mapOf("clid0" to "0", "clid1" to "1")
        private val MAP_WITH_VALID_ITEMS_1_STRING: String = StartupUtils.encodeClids(MAP_WITH_VALID_ITEMS_1)

        private val MAP_WITH_VALID_ITEMS_2 = mapOf("clid1" to "1", "clid2" to "2")

        private val MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID = mapOf("clid1" to "1", "clid2" to "not_a_number")

        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf(null, null, null),
                arrayOf(null, EMPTY_MAP, null),
                arrayOf(null, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(null, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null),
                arrayOf(EMPTY_MAP, null, null),
                arrayOf(EMPTY_MAP, EMPTY_MAP, null),
                arrayOf(EMPTY_MAP, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(EMPTY_MAP, MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null),
                arrayOf(MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, null, null),
                arrayOf(MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID, EMPTY_MAP, null),
                arrayOf(
                    MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID,
                    MAP_WITH_VALID_ITEMS_1,
                    MAP_WITH_VALID_ITEMS_1_STRING
                ),
                arrayOf(
                    MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID,
                    MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID,
                    null
                ),
                arrayOf(MAP_WITH_VALID_ITEMS_1, null, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(MAP_WITH_VALID_ITEMS_1, EMPTY_MAP, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(MAP_WITH_VALID_ITEMS_1, MAP_WITH_VALID_ITEMS_2, MAP_WITH_VALID_ITEMS_1_STRING),
                arrayOf(
                    MAP_WITH_VALID_ITEMS_1,
                    MAP_WITH_MULTIPLE_ITEMS_INCLUDING_INVALID,
                    MAP_WITH_VALID_ITEMS_1_STRING
                )
            )
        }
    }
}
