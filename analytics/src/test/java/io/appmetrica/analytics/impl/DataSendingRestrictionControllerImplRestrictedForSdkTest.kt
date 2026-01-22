package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class DataSendingRestrictionControllerImplRestrictedForSdkTest(
    description: String?,
    private val mController: DataSendingRestrictionControllerImpl,
    private val expectedRestrictedForSdk: Boolean,
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> {
            val data = ArrayList<Array<Any>>()

            var controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromMainReporter(true)
            data.add(arrayOf("enabled in main reporter", controller, false))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromMainReporter(false)
            data.add(arrayOf("disabled in main reporter", controller, true))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromSharedReporter("1", true)
            data.add(arrayOf("enabled in all reporters", controller, false))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromSharedReporter("1", false)
            data.add(arrayOf("disabled in all reporters", controller, true))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromSharedReporter("1", false)
            controller.setEnabledFromSharedReporter("2", true)
            data.add(arrayOf("different in all reporters", controller, false))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            data.add(arrayOf("test no data", controller, true))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromMainReporterIfNotYet(true)
            data.add(arrayOf("enabled in main reporter if not yet", controller, false))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            controller.setEnabledFromMainReporterIfNotYet(false)
            data.add(arrayOf("disabled in main reporter if not yet", controller, true))

            controller = DataSendingRestrictionControllerImpl(EmptyStorage())
            data.add(arrayOf("test no data", controller, true))

            return data
        }
    }

    private class EmptyStorage : DataSendingRestrictionControllerImpl.Storage {
        override fun storeRestrictionFromMainReporter(value: Boolean) {
        }

        override fun readRestrictionFromMainReporter(): Boolean? {
            return null
        }
    }

    @Test
    fun isRestrictedForSdk() {
        assertThat(mController.isRestrictedForSdk).isEqualTo(expectedRestrictedForSdk)
    }
}
