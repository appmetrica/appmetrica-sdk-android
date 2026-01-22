package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

internal class StartupUpdateConfigConverterTest : CommonTest() {

    private val intervalSeconds = 4357989
    private val converter = StartupUpdateConfigConverter()

    @Test
    fun toProto() {
        val model = StartupUpdateConfig(intervalSeconds)
        val result = converter.fromModel(model)
        ProtoObjectPropertyAssertions(result)
            .checkField("interval", intervalSeconds)
            .checkAll()
    }

    @Test
    fun toModelFilled() {
        val nano = StartupStateProtobuf.StartupState.StartupUpdateConfig().apply {
            interval = intervalSeconds
        }
        val result = converter.toModel(nano)
        ObjectPropertyAssertions(result)
            .checkField("intervalSeconds", "getIntervalSeconds", intervalSeconds)
            .checkAll()
    }

    @Test
    fun toModelDefault() {
        val nano = StartupStateProtobuf.StartupState.StartupUpdateConfig()
        val result = converter.toModel(nano)
        ObjectPropertyAssertions(result)
            .checkField("intervalSeconds", "getIntervalSeconds", 86400)
            .checkAll()
    }
}
