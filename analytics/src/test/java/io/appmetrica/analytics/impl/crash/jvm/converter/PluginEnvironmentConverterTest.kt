package io.appmetrica.analytics.impl.crash.jvm.converter

import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PluginEnvironmentConverterTest : CommonTest() {

    private val converter = PluginEnvironmentConverter()

    @Test
    fun toProtoFilled() {
        val key1 = "key 1"
        val value1 = "value 1"
        val key2 = "key 2"
        val value2 = "value 2"
        val input = mapOf(key1 to value1, key2 to value2)
        val result = converter.fromModel(input)
        assertThat(result).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                CrashAndroid.BytesPair().apply {
                    key = key1.toByteArray()
                    value = value1.toByteArray()
                },
                CrashAndroid.BytesPair().apply {
                    key = key2.toByteArray()
                    value = value2.toByteArray()
                }
            )
    }

    @Test
    fun toProtoEmpty() {
        assertThat(converter.fromModel(emptyMap())).isNotNull.isEmpty()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun toModel() {
        converter.toModel(emptyArray())
    }
}
