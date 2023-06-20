package io.appmetrica.analytics.coreutils.internal.parsing

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class RemoteConfigJsonUtilsExtractMillisOrDefaultTest(
    private val input: Any?,
    private val timeUnit: TimeUnit,
    private val default: Long,
    private val expected: Long
) {

    companion object {
        private const val inputLong = 100200L
        private const val defaultLong = 200500L

        @Parameterized.Parameters(name = "Input: `{0}`; unit: {1}; default: {2} -> {3}")
        @JvmStatic
        fun data(): List<Array<Any?>> = listOf(
            arrayOf(inputLong, TimeUnit.MILLISECONDS, defaultLong, inputLong),
            arrayOf("wrongValue", TimeUnit.MILLISECONDS, defaultLong, defaultLong),
            arrayOf(null, TimeUnit.MILLISECONDS, defaultLong, defaultLong),

            arrayOf(inputLong, TimeUnit.SECONDS, defaultLong, TimeUnit.SECONDS.toMillis(inputLong)),
            arrayOf("wrongValue", TimeUnit.SECONDS, defaultLong, defaultLong),
            arrayOf(null, TimeUnit.SECONDS, defaultLong, defaultLong),
        )
    }

    @Test
    fun test() {
        val key = "some key"
        val inputJson = JSONObject().put(key, input)
        assertThat(RemoteConfigJsonUtils.extractMillisOrDefault(inputJson, key, timeUnit, default))
            .isEqualTo(expected)
    }
}
