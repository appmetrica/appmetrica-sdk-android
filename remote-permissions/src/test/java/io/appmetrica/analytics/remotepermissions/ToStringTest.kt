package io.appmetrica.analytics.remotepermissions

import io.appmetrica.analytics.remotepermissions.internal.config.FeatureConfig
import io.appmetrica.analytics.testutils.BaseToStringTest
import io.appmetrica.analytics.testutils.BaseToStringTest.Companion.toTestCase
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ToStringTest(
    actualValue: Any?,
    modifierPreconditions: Int,
    excludedFields: Set<String>?,
    additionalDescription: String?
) : BaseToStringTest(
    actualValue,
    modifierPreconditions,
    excludedFields,
    additionalDescription
) {

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            FeatureConfig(setOf("first", "second")).toTestCase()
        )
    }
}
