package io.appmetrica.analytics.internal

import io.appmetrica.analytics.internal.CounterConfigurationReporterType.Companion.fromStringValue
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CounterConfigurationReporterTypeTest(
    private val reporterType: CounterConfigurationReporterType,
    private val stringValue: String
) : CommonTest() {

    @Test
    fun stringValue() {
        assertThat(reporterType.stringValue).isEqualTo(stringValue)
    }

    @Test
    fun fromString() {
        assertThat(fromStringValue(stringValue)).isEqualTo(reporterType)
    }

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(CounterConfigurationReporterType.COMMUTATION, "commutation"),
                arrayOf(CounterConfigurationReporterType.MAIN, "main"),
                arrayOf(CounterConfigurationReporterType.MANUAL, "manual"),
                arrayOf(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN, "self_diagnostic_main"),
                arrayOf(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL, "self_diagnostic_manual"),
                arrayOf(CounterConfigurationReporterType.SELF_SDK, "self_sdk"),
                arrayOf(CounterConfigurationReporterType.CRASH, "crash")
            )
        }
    }
}
