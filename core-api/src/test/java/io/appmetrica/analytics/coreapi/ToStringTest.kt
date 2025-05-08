package io.appmetrica.analytics.coreapi

import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.testutils.BaseToStringTest
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ToStringTest(
    clazz: Any?,
    actualValue: Any?,
    modifierPreconditions: Int,
    additionalDescription: String?
) : BaseToStringTest(
    clazz,
    actualValue,
    modifierPreconditions,
    additionalDescription
) {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(PermissionState::class.java, PermissionState("permission.name", true), 0, "")
        )
    }
}
