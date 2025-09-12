package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class DataSendingRestrictionControllerImplRestrictedForReporterTest(
    @Suppress("unused") private val description: String,
    private val expectedValue: Boolean,
    private val configure: (DataSendingRestrictionControllerImpl) -> Unit
) : CommonTest() {

    companion object {

        private const val API_KEY = "api key"
        private const val ONE_MORE_API_KEY = "another api key"

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            testCase("Initial state", expectedValue = false) {},
            testCase("Enabled for main reporter", expectedValue = false) { controller ->
                controller.setEnabledFromMainReporter(true)
            },
            testCase("Disabled for main reporter", expectedValue = true) { controller ->
                controller.setEnabledFromMainReporter(false)
            },
            testCase("Enabled for reporter", expectedValue = false) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, true)
            },
            testCase("Disabled for reporter", expectedValue = true) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, false)
            },
            testCase("Enabled for another reporter", expectedValue = false) { controller ->
                controller.setEnabledFromSharedReporter(ONE_MORE_API_KEY, true)
            },
            testCase("Disabled for another reporter", expectedValue = false) { controller ->
                controller.setEnabledFromSharedReporter(ONE_MORE_API_KEY, false)
            },
            testCase("Enabled for main and disabled for reporter", expectedValue = true) { controller ->
                controller.setEnabledFromMainReporter(true)
                controller.setEnabledFromSharedReporter(API_KEY, false)
            },
            testCase("Disabled for main and enabled for reporter", expectedValue = true) { controller ->
                controller.setEnabledFromMainReporter(false)
                controller.setEnabledFromSharedReporter(API_KEY, true)
            },
            testCase("Enabled for reporter and disabled for another", expectedValue = false) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, true)
                controller.setEnabledFromSharedReporter(ONE_MORE_API_KEY, false)
            },
            testCase("Disabled for reporter and enabled for another", expectedValue = true) { controller ->
                controller.setEnabledFromSharedReporter(API_KEY, false)
                controller.setEnabledFromSharedReporter(ONE_MORE_API_KEY, true)
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

    private val controller by setUp {
        DataSendingRestrictionControllerImpl(EmptyStorage())
    }

    @Test
    fun isRestrictedForReporter() {
        configure(controller)
        assertThat(controller.isRestrictedForReporter(API_KEY)).isEqualTo(expectedValue)
    }

    private class EmptyStorage : DataSendingRestrictionControllerImpl.Storage {
        override fun storeRestrictionFromMainReporter(value: Boolean) {
        }

        override fun readRestrictionFromMainReporter(): Boolean? {
            return null
        }
    }
}
