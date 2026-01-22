package io.appmetrica.analytics.impl.profile.fpd

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class EmailNormalizerTest(
    private val value: String,
    private val expected: String?
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun data() = EmailNormalizerTest::class.java.classLoader!!
            .getResourceAsStream("email_normalizer_test.json")
            .readAllBytes()
            .let { bytes ->
                val jsonArray = JSONArray(String(bytes))
                (0 until jsonArray.length()).map { jsonArray.getJSONObject(it) }
            }
            .mapNotNull { jsonObject ->
                if (jsonObject.optBoolean("skip", false)) {
                    return@mapNotNull null
                }

                if (!jsonObject.getBoolean("isValid")) {
                    return@mapNotNull arrayOf(jsonObject.getString("initial"), null)
                }

                return@mapNotNull arrayOf(
                    jsonObject.getString("initial"),
                    jsonObject.getString("normalized")
                )
            }
    }

    private val normalizer = EmailNormalizer()

    @Test
    fun normalize() {
        assertThat(normalizer.normalize(value)).isEqualTo(expected)
    }
}
