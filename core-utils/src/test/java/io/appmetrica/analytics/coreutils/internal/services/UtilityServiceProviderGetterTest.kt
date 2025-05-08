package io.appmetrica.analytics.coreutils.internal.services

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class UtilityServiceProviderGetterTest(
    val description: String,
    private val getter: (UtilityServiceProvider) -> Any
) : CommonTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("First execution service", { service: UtilityServiceProvider -> service.firstExecutionService }),
            arrayOf("Activation barrier", { service: UtilityServiceProvider -> service.activationBarrier })
        )
    }

    private val utilityServiceProvider: UtilityServiceProvider by setUp { UtilityServiceProvider() }

    @Test
    fun getService() {
        val first = getter.invoke(utilityServiceProvider)
        val second = getter.invoke(utilityServiceProvider)
        assertThat(first)
            .isNotNull()
            .isSameAs(second)
    }
}
