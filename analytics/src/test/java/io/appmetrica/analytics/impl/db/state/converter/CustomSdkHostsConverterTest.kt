package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CustomSdkHostsConverterTest : CommonTest() {

    private val converter = CustomSdkHostsConverter()

    @Test
    fun toProtoEmpty() {
        assertThat(converter.fromModel(emptyMap())).isEmpty()
    }

    @Test
    fun toProtoFilled() {
        val model = mapOf("am" to listOf("host1", "host2"), "ads" to listOf("host3"))
        val result = converter.fromModel(model)
        assertThat(result.size).isEqualTo(2)
        ProtoObjectPropertyAssertions(result[0])
            .checkField("key", "am")
            .checkField("hosts", arrayOf("host1", "host2"))
            .checkAll()
        ProtoObjectPropertyAssertions(result[1])
            .checkField("key", "ads")
            .checkField("hosts", arrayOf("host3"))
            .checkAll()
    }

    @Test
    fun toModelEmpty() {
        assertThat(converter.toModel(emptyArray())).isEmpty()
    }

    @Test
    fun toModelFilled() {
        val proto = Array(2) { StartupStateProtobuf.StartupState.CustomSdkHostsPair() }
        proto[0].key = "am"
        proto[0].hosts = arrayOf("host1", "host2")
        proto[1].key = "ads"
        proto[1].hosts = arrayOf("host3")
        val result = converter.toModel(proto)
        assertThat(result).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "am" to listOf("host1", "host2"),
                "ads" to listOf("host3")
            )
        )
    }
}
