package io.appmetrica.analytics.impl.crash.jvm.converter

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class PlatformConverterTest(
    private val input: String,
    private val expected: String
) : CommonTest() {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("native", "JVM"),
            arrayOf("unity", "unity"),
            arrayOf("some stuff", "some stuff"),
        )
    }

    private val converter = PlatformConverter()

    @Test
    fun convert() {
        assertThat(converter.fromModel(input)).isEqualTo(expected.toByteArray())
    }
}
