package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OptionalBoolConverterTest : CommonTest() {

    private val undefinedValue = 21736
    private val falseValue = 777
    private val trueValue = 444555

    private val converter = OptionalBoolConverter(undefinedValue, falseValue, trueValue)

    @Test
    fun nullToProto() {
        assertThat(converter.toProto(null)).isEqualTo(undefinedValue)
    }

    @Test
    fun falseToProto() {
        assertThat(converter.toProto(false)).isEqualTo(falseValue)
    }

    @Test
    fun trueToProto() {
        assertThat(converter.toProto(true)).isEqualTo(trueValue)
    }

    @Test
    fun undefinedToModel() {
        assertThat(converter.toModel(undefinedValue)).isNull()
    }

    @Test
    fun unknownToModel() {
        assertThat(converter.toModel(999)).isNull()
    }

    @Test
    fun falseToModel() {
        assertThat(converter.toModel(falseValue)).isFalse
    }

    @Test
    fun trueToModel() {
        assertThat(converter.toModel(trueValue)).isTrue
    }

}
