package io.appmetrica.analytics.impl.profile.fpd

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class Sha256ConverterTest : CommonTest() {

    private val normalizer = object : AttributeValueNormalizer {
        override fun normalize(value: String) = value
    }
    private val converter = Sha256Converter(normalizer)

    @Test
    fun convert() {
        val inputs = listOf(
            "input1",
            "input2"
        )
        val expected = listOf(
            "1ea06586b18e8fce1b923eff26fd8252f617f0efd4e49820e8e9bee0614e5792",
            "124d8541ff3d7a18b95432bdfbecd86816b86c8265bff44ef629765afb25f06b"
        )
        assertThat(converter.convert(inputs)).isEqualTo(expected)
    }
}
