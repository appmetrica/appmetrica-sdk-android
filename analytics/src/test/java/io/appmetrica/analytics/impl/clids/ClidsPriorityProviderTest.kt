package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ClidsPriorityProviderTest(
    private val oldData: ClidsInfo.Candidate,
    private val newData: ClidsInfo.Candidate,
    private val expected: Boolean,
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any?>> {
            val filledClids = mapOf("clid0" to "0")
            return listOf(
                // #0
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #4
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #8
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #12
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #16
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #20
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #24
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), false),
                // #28
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), false),
                // #32
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #36
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #40
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #44
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #48
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #52
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #56
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #60
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), false),
                // #64
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), false),
                // #68
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.APP), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #72
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #76
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #80
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #84
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #88
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #92
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #96
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), false),
                // #100
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), false),
                // #104
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), false),

                // #108
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #112
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #116
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(null, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #120
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), true),
                // #124
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), true),
                // #128
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.APP), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), true),
                arrayOf(ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), true),

                // #132
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(null, DistributionSource.SATELLITE), false),
                // #136
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(emptyMap(), DistributionSource.SATELLITE), false),
                // #140
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.UNDEFINED), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.APP), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), false),
                arrayOf(ClidsInfo.Candidate(filledClids, DistributionSource.RETAIL), ClidsInfo.Candidate(filledClids, DistributionSource.SATELLITE), false),
            )
        }
    }

    private val priorityProvider = ClidsPriorityProvider()

    @Test
    fun isNewDataMoreImportant() {
        assertThat(priorityProvider.isNewDataMoreImportant(newData, oldData)).isEqualTo(expected)
    }
}
