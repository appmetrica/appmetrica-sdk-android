package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.ExternalAttributionConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

internal class ExternalAttributionConfigConverterTest : CommonTest() {

    private val collectingInterval: Long = 444333
    private var converter = ExternalAttributionConfigConverter()

    @Test
    fun fromModel() {
        val model = ExternalAttributionConfig(collectingInterval)
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("collectingInterval", collectingInterval)
            .checkAll()
    }

    @Test
    fun fromModelForNull() {
        val proto = converter.fromModel(null)
        ProtoObjectPropertyAssertions(proto)
            .checkField("collectingInterval", 864000000L)
            .checkAll()
    }

    @Test
    fun toModel() {
        val proto = StartupStateProtobuf.StartupState.ExternalAttributionConfig()
        proto.collectingInterval = collectingInterval
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkField("collectingInterval", collectingInterval)
            .checkAll()
    }
}
