package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdsIdentifiersResult
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
class AdsIdentifiersFromStartupParamsItemStatusTest(
    private val input: StartupParamsItemStatus,
    private val expected: AdsIdentifiersResult.Details
) {

    companion object {

        @JvmStatic
        @Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(StartupParamsItemStatus.OK, AdsIdentifiersResult.Details.OK),
            arrayOf(StartupParamsItemStatus.NETWORK_ERROR, AdsIdentifiersResult.Details.NO_STARTUP),
            arrayOf(StartupParamsItemStatus.FEATURE_DISABLED, AdsIdentifiersResult.Details.FEATURE_DISABLED),
            arrayOf(
                StartupParamsItemStatus.PROVIDER_UNAVAILABLE,
                AdsIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE
            ),
            arrayOf(StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER, AdsIdentifiersResult.Details.INVALID_ADV_ID),
            arrayOf(StartupParamsItemStatus.UNKNOWN_ERROR, AdsIdentifiersResult.Details.INTERNAL_ERROR)
        )
    }

    private val startupParamsItem = mock<StartupParamsItem> {
        on { status } doReturn input
    }
    private lateinit var converter: AdsIdentifiersFromStartupParamsItemConverter

    @Before
    fun setUp() {
        converter = AdsIdentifiersFromStartupParamsItemConverter()
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
