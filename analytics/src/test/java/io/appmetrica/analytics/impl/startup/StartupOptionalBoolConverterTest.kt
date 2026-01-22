package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class StartupOptionalBoolConverterTest : CommonTest() {

    private val converter = StartupOptionalBoolConverter()

    @Test
    fun toProtoNull() {
        assertThat(converter.toProto(null)).isEqualTo(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED)
    }

    @Test
    fun toProtoTrue() {
        assertThat(converter.toProto(true)).isEqualTo(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_TRUE)
    }

    @Test
    fun toProtoFalse() {
        assertThat(converter.toProto(false)).isEqualTo(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_FALSE)
    }

    @Test
    fun toModelUndefined() {
        assertThat(converter.toModel(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED)).isNull()
    }

    @Test
    fun toModelUnknown() {
        assertThat(converter.toModel(55)).isNull()
    }

    @Test
    fun toModelTrue() {
        assertThat(converter.toModel(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_TRUE)).isTrue
    }

    @Test
    fun toModelFalse() {
        assertThat(converter.toModel(StartupStateProtobuf.StartupState.OPTIONAL_BOOL_FALSE)).isFalse
    }
}
