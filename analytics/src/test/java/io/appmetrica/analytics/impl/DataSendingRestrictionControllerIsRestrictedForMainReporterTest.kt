package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class DataSendingRestrictionControllerIsRestrictedForMainReporterTest(
    @Suppress("unused") private val description: String,
    private val expected: Boolean,
    private val configure: (DataSendingRestrictionControllerImpl) -> Unit
) : CommonTest() {

    companion object {
        private const val API_KEY = "api key"

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            testCase("Initial state", expectedValue = false) {},
            testCase("Enabled for main", expectedValue = false) { controller ->
                controller.setEnabledFromMainReporter(true)
            },
            testCase("Disabled for main", expectedValue = true) { controller ->
                controller.setEnabledFromMainReporter(false)
            },
            testCase("Enabled for reporter", expectedValue = false) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, true)
            },
            testCase("Disabled for reporter", false) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, false)
            },
            testCase("Disabled for main and enabled for reporter", expectedValue = true) { controller ->
                controller.setEnabledFromMainReporter(false)
                controller.setEnabledFromSharedReporter(API_KEY, true)
            },
            testCase("Enabled for main and disabled for reporter", expectedValue = false) { controller ->
                controller.setEnabledFromMainReporter(true)
                controller.setEnabledFromSharedReporter(API_KEY, false)
            },
        )

        private fun testCase(
            description: String,
            expectedValue: Boolean,
            configure: (DataSendingRestrictionControllerImpl) -> Unit,
        ): Array<Any> {
            return arrayOf(description, expectedValue, configure)
        }
    }

    private val controller by setUp { DataSendingRestrictionControllerImpl(EmptyStorage()) }

    @Test
    fun isRestrictedForMainReporter() {
        configure(controller)
        assertThat(controller.isRestrictedForMainReporter).isEqualTo(expected)
    }

    private class EmptyStorage : DataSendingRestrictionControllerImpl.Storage {
        override fun storeRestrictionFromMainReporter(value: Boolean) {
        }

        override fun readRestrictionFromMainReporter(): Boolean? {
            return null
        }
    }
}
