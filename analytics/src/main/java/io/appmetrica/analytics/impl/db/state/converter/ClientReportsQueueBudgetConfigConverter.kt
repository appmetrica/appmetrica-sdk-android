package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig

internal class ClientReportsQueueBudgetConfigConverter :
    ProtobufConverter<
        ClientReportsQueueBudgetConfig?,
        StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig
        > {

    override fun fromModel(
        value: ClientReportsQueueBudgetConfig?
    ): StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig {
        val nano = StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig()
        value?.let {
            nano.maxSizeMiB = it.maxSizeMiB
            nano.heapFraction = it.heapFraction
        }
        return nano
    }

    override fun toModel(
        value: StartupStateProtobuf.StartupState.ClientReportsQueueBudgetConfig
    ): ClientReportsQueueBudgetConfig? {
        // Invalid/corrupt stored proto -> null -> treat as never configured (DEFAULT at resolve).
        return ClientReportsQueueBudgetConfig.create(value.maxSizeMiB, value.heapFraction)
    }
}
