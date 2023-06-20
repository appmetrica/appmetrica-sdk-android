package io.appmetrica.analytics.coreutils.internal.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class UtilityServiceLocatorGetterTest(val description: String, private val getter: (UtilityServiceLocator) -> Any) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("First execution service", { service: UtilityServiceLocator -> service.firstExecutionService }),
            arrayOf("Activation barrier", { service: UtilityServiceLocator -> service.activationBarrier })
        )

    }

    @Test
    fun getService() {
        val first = getter.invoke(UtilityServiceLocator.instance)
        val second = getter.invoke(UtilityServiceLocator.instance)
        assertThat(first)
            .isNotNull()
            .isSameAs(second)
    }

}
