package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class RequestAttemptResultFromStringTest(
    private val inputValue: String,
    private val expectedValue: RequestAttemptResult
) : CommonTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data() = listOf(
            arrayOf("success", RequestAttemptResult.SUCCESS),
            arrayOf("failure", RequestAttemptResult.FAILURE),
            arrayOf("incompatible_precondition", RequestAttemptResult.INCOMPATIBLE_PRECONDITION),
            arrayOf("none", RequestAttemptResult.NONE),
            arrayOf("unknown", RequestAttemptResult.NONE)
        )
    }

    @Test
    fun fromString() {
        assertThat(RequestAttemptResult.fromString(inputValue)).isEqualTo(expectedValue)
    }
}
