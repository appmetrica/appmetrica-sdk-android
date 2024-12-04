package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class AdvIdentifiersFromStartupParamsItemStatusTest(
    private val input: StartupParamsItemStatus,
    private val expected: AdvIdentifiersResult.Details
) {

    companion object {

        @JvmStatic
        @Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any?>> {
            val data: Collection<Array<Any?>> = listOf(
                arrayOf(StartupParamsItemStatus.OK, AdvIdentifiersResult.Details.OK),
                arrayOf(StartupParamsItemStatus.NETWORK_ERROR, AdvIdentifiersResult.Details.NO_STARTUP),
                arrayOf(StartupParamsItemStatus.FEATURE_DISABLED, AdvIdentifiersResult.Details.FEATURE_DISABLED),
                arrayOf(
                    StartupParamsItemStatus.PROVIDER_UNAVAILABLE,
                    AdvIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE
                ),
                arrayOf(StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER, AdvIdentifiersResult.Details.INVALID_ADV_ID),
                arrayOf(StartupParamsItemStatus.UNKNOWN_ERROR, AdvIdentifiersResult.Details.INTERNAL_ERROR),
                arrayOf(
                    StartupParamsItemStatus.FORBIDDEN_BY_CLIENT_CONFIG,
                    AdvIdentifiersResult.Details.FORBIDDEN_BY_CLIENT_CONFIG
                ),
            )

            assertThat(data.size).isEqualTo(StartupParamsItemStatus.values().size)
            assertThat(data.size).isEqualTo(AdvIdentifiersResult.Details.values().size)

            return data
        }
    }

    private val startupParamsItem = mock<StartupParamsItem> {
        on { status } doReturn input
    }
    private lateinit var converter: AdvIdentifiersFromStartupParamsItemConverter

    @Before
    fun setUp() {
        converter = AdvIdentifiersFromStartupParamsItemConverter()
    }

    @Test
    fun convert() {
        assertThat(
            converter.convert(
                startupParamsItem,
                null,
                null
            ).googleAdvId.details
        ).isEqualTo(expected)
    }
}
