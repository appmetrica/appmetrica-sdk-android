package io.appmetrica.analytics.coreapi.identifiers

import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class AppSetIdScopeTest(
    private val scope: AppSetIdScope,
    private val expectedString: String
) : CommonTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf<Any?>(AppSetIdScope.UNKNOWN, ""),
                arrayOf<Any?>(AppSetIdScope.APP, "app"),
                arrayOf<Any?>(AppSetIdScope.DEVELOPER, "developer"),
            ).also { assertThat(it.size).isEqualTo(AppSetIdScope.values().size) }
        }
    }

    @Test
    fun hasExpectedValue() {
        assertThat(scope.value).isEqualTo(expectedString)
    }
}
