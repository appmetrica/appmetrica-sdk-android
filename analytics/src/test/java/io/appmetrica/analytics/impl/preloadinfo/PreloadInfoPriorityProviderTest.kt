package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class PreloadInfoPriorityProviderTest(
    private val oldWasSet: Boolean,
    private val oldSource: DistributionSource,
    private val newWasSet: Boolean,
    private val newSource: DistributionSource,
    private val expected: Boolean
) : CommonTest() {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // #0
                arrayOf(false, DistributionSource.UNDEFINED, false, DistributionSource.UNDEFINED, false),
                arrayOf(false, DistributionSource.UNDEFINED, false, DistributionSource.APP, false),
                arrayOf(false, DistributionSource.UNDEFINED, false, DistributionSource.SATELLITE, false),
                arrayOf(false, DistributionSource.UNDEFINED, false, DistributionSource.RETAIL, false),
                // #4
                arrayOf(false, DistributionSource.UNDEFINED, true, DistributionSource.UNDEFINED, true),
                arrayOf(false, DistributionSource.UNDEFINED, true, DistributionSource.APP, true),
                arrayOf(false, DistributionSource.UNDEFINED, true, DistributionSource.SATELLITE, true),
                arrayOf(false, DistributionSource.UNDEFINED, true, DistributionSource.RETAIL, true),

                // #8
                arrayOf(false, DistributionSource.APP, false, DistributionSource.UNDEFINED, false),
                arrayOf(false, DistributionSource.APP, false, DistributionSource.APP, false),
                arrayOf(false, DistributionSource.APP, false, DistributionSource.SATELLITE, false),
                arrayOf(false, DistributionSource.APP, false, DistributionSource.RETAIL, false),
                // #12
                arrayOf(false, DistributionSource.APP, true, DistributionSource.UNDEFINED, true),
                arrayOf(false, DistributionSource.APP, true, DistributionSource.APP, true),
                arrayOf(false, DistributionSource.APP, true, DistributionSource.SATELLITE, true),
                arrayOf(false, DistributionSource.APP, true, DistributionSource.RETAIL, true),

                // #16
                arrayOf(false, DistributionSource.SATELLITE, false, DistributionSource.UNDEFINED, false),
                arrayOf(false, DistributionSource.SATELLITE, false, DistributionSource.APP, false),
                arrayOf(false, DistributionSource.SATELLITE, false, DistributionSource.SATELLITE, false),
                arrayOf(false, DistributionSource.SATELLITE, false, DistributionSource.RETAIL, false),
                // #20
                arrayOf(false, DistributionSource.SATELLITE, true, DistributionSource.UNDEFINED, true),
                arrayOf(false, DistributionSource.SATELLITE, true, DistributionSource.APP, true),
                arrayOf(false, DistributionSource.SATELLITE, true, DistributionSource.SATELLITE, true),
                arrayOf(false, DistributionSource.SATELLITE, true, DistributionSource.RETAIL, true),

                // #24
                arrayOf(false, DistributionSource.RETAIL, false, DistributionSource.UNDEFINED, false),
                arrayOf(false, DistributionSource.RETAIL, false, DistributionSource.APP, false),
                arrayOf(false, DistributionSource.RETAIL, false, DistributionSource.SATELLITE, false),
                arrayOf(false, DistributionSource.RETAIL, false, DistributionSource.RETAIL, false),
                // #28
                arrayOf(false, DistributionSource.RETAIL, true, DistributionSource.UNDEFINED, true),
                arrayOf(false, DistributionSource.RETAIL, true, DistributionSource.APP, true),
                arrayOf(false, DistributionSource.RETAIL, true, DistributionSource.SATELLITE, true),
                arrayOf(false, DistributionSource.RETAIL, true, DistributionSource.RETAIL, true),

                // #32
                arrayOf(true, DistributionSource.UNDEFINED, false, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.UNDEFINED, false, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.UNDEFINED, false, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.UNDEFINED, false, DistributionSource.RETAIL, false),
                // #36
                arrayOf(true, DistributionSource.UNDEFINED, true, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.UNDEFINED, true, DistributionSource.APP, true),
                arrayOf(true, DistributionSource.UNDEFINED, true, DistributionSource.SATELLITE, true),
                arrayOf(true, DistributionSource.UNDEFINED, true, DistributionSource.RETAIL, true),

                // #40
                arrayOf(true, DistributionSource.APP, false, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.APP, false, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.APP, false, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.APP, false, DistributionSource.RETAIL, false),
                // #44
                arrayOf(true, DistributionSource.APP, true, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.APP, true, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.APP, true, DistributionSource.SATELLITE, true),
                arrayOf(true, DistributionSource.APP, true, DistributionSource.RETAIL, true),

                // #48
                arrayOf(true, DistributionSource.SATELLITE, false, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.SATELLITE, false, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.SATELLITE, false, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.SATELLITE, false, DistributionSource.RETAIL, false),
                // #52
                arrayOf(true, DistributionSource.SATELLITE, true, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.SATELLITE, true, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.SATELLITE, true, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.SATELLITE, true, DistributionSource.RETAIL, true),

                // #56
                arrayOf(true, DistributionSource.RETAIL, false, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.RETAIL, false, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.RETAIL, false, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.RETAIL, false, DistributionSource.RETAIL, false),
                // #60
                arrayOf(true, DistributionSource.RETAIL, true, DistributionSource.UNDEFINED, false),
                arrayOf(true, DistributionSource.RETAIL, true, DistributionSource.APP, false),
                arrayOf(true, DistributionSource.RETAIL, true, DistributionSource.SATELLITE, false),
                arrayOf(true, DistributionSource.RETAIL, true, DistributionSource.RETAIL, false),
            )
        }
    }

    private val priorityProvider = PreloadInfoPriorityProvider()

    @Test
    fun isNewDataMoreImportant() {
        val oldData = PreloadInfoState("111", JSONObject(), oldWasSet, false, oldSource)
        val newData = PreloadInfoState("222", JSONObject(), newWasSet, false, newSource)
        assertThat(priorityProvider.isNewDataMoreImportant(newData, oldData)).isEqualTo(expected)
    }
}
