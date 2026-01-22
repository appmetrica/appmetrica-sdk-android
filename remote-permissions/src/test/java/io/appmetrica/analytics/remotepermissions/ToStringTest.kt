package io.appmetrica.analytics.remotepermissions

import io.appmetrica.analytics.remotepermissions.internal.config.FeatureConfig
import io.appmetrica.analytics.testutils.BaseToStringTest
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class ToStringTest(
    clazz: Any?,
    actualValue: Any?,
    modifierPreconditions: Int,
    additionalDescription: String?
) : BaseToStringTest(clazz, actualValue, modifierPreconditions, additionalDescription) {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(FeatureConfig::class.java, FeatureConfig(setOf("first", "second")), 0, "")
        )
    }
}
