package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ClientReportsQueueBudgetConfigConverterTest : CommonTest() {

    private val converter = ClientReportsQueueBudgetConfigConverter()
    private val maxSizeMiB = 8L
    private val heapFraction = 0.25

    @Test
    fun fromModel() {
        val model = ClientReportsQueueBudgetConfig.create(maxSizeMiB, heapFraction)!!
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("maxSizeMiB", maxSizeMiB)
            .checkField("heapFraction", heapFraction)
            .checkAll()
    }

    @Test
    fun fromModelForNull() {
        val proto = converter.fromModel(null)
        ProtoObjectPropertyAssertions(proto)
            .checkField("maxSizeMiB", 16L)
            .checkField("heapFraction", 0.125)
            .checkAll()
    }

    @Test
    fun toModel() {
        val proto = StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig()
        proto.maxSizeMiB = maxSizeMiB
        proto.heapFraction = heapFraction
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkField("maxSizeMiB", maxSizeMiB)
            .checkField("heapFraction", heapFraction)
            .checkAll()
    }

    @Test
    fun toModelInvalidReturnsNull() {
        val proto = StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig()
        proto.maxSizeMiB = -1
        proto.heapFraction = heapFraction
        assertThat(converter.toModel(proto)).isNull()
    }

    @Test
    fun toModelHeapFractionAboveOneReturnsNull() {
        val proto = StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig()
        proto.maxSizeMiB = maxSizeMiB
        proto.heapFraction = 2.0
        assertThat(converter.toModel(proto)).isNull()
    }
}
