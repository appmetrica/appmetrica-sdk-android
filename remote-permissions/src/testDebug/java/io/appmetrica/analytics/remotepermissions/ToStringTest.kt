package io.appmetrica.analytics.remotepermissions

import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig
import io.appmetrica.gradle.androidtestutils.tostring.BaseToStringTest
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
            ServiceSideRemotePermissionsConfig(setOf("first", "second")).toTestCase()
        )
    }
}
