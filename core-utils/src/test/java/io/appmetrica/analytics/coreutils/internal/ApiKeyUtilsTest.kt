package io.appmetrica.analytics.coreutils.internal

import io.appmetrica.analytics.coreutils.internal.ApiKeyUtils.createPartialApiKey
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Arrays

@RunWith(ParameterizedRobolectricTestRunner::class)
class ApiKeyUtilsTest(
    private val fullApiKey: String?,
    private val partialApiKey: String
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "class = {0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            return Arrays.asList(
                arrayOf("5052c3cc-39a4-4dbc-92d1-89ebc17c0fa9", "5052c3cc-xxxx-xxxx-xxxx-xxxxxxxx0fa9"),
                arrayOf("123456", StringUtils.UNDEFINED),
                arrayOf("", StringUtils.UNDEFINED),
                arrayOf(null, StringUtils.UNDEFINED)
            )
        }
    }

    @Test
    fun createPartialApiKey() {
        Assertions.assertThat(createPartialApiKey(fullApiKey)).isEqualTo(partialApiKey)
    }
}
