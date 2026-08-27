package io.appmetrica.analytics.coreapi

import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.gradle.androidtestutils.tostring.BaseToStringTest
import io.appmetrica.gradle.androidtestutils.tostring.BaseToStringTest.Companion.toTestCase
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
            PermissionState("permission.name", true).toTestCase()
        )
    }
}
