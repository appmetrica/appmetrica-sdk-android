package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsItemStatusAdapterTest(
    private val input: IdentifierStatus,
    private val expected: StartupParamsItemStatus
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any?>> {
            val data: Collection<Array<Any?>> = listOf(
                arrayOf(IdentifierStatus.OK, StartupParamsItemStatus.OK),
                arrayOf(IdentifierStatus.FEATURE_DISABLED, StartupParamsItemStatus.FEATURE_DISABLED),
                arrayOf(IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, StartupParamsItemStatus.PROVIDER_UNAVAILABLE),
                arrayOf(IdentifierStatus.INVALID_ADV_ID, StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER),
                arrayOf(IdentifierStatus.UNKNOWN, StartupParamsItemStatus.UNKNOWN_ERROR),
                arrayOf(IdentifierStatus.FORBIDDEN_BY_CLIENT_CONFIG, StartupParamsItemStatus.FORBIDDEN_BY_CLIENT_CONFIG)
            )

            assertThat(data.size).isEqualTo(IdentifierStatus.values().size)
            assertThat(data.size).isEqualTo(StartupParamsItemStatus.values().size)

            return data
        }
    }

    private lateinit var startupParamItemStatusAdapter: StartupParamItemStatusAdapter

    @Before
    fun setUp() {
        startupParamItemStatusAdapter = StartupParamItemStatusAdapter()
    }

    @Test
    fun adapt() {
        assertThat(startupParamItemStatusAdapter.adapt(input)).isEqualTo(expected)
    }
}
