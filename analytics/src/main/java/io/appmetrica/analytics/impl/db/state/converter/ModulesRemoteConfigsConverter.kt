package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf

internal class ModulesRemoteConfigsConverter :
    Converter<Map<String, Any?>, Array<StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry>> {

    private val modulesRemoteConfigsExtension = GlobalServiceLocator.getInstance().modulesController

    override fun fromModel(
        value: Map<String, Any?>
    ): Array<StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry> {
        val moduleRemoteConfigControllers = modulesRemoteConfigsExtension.collectRemoteConfigControllers()
        return value.mapNotNull { (identifier, controller) ->
            moduleRemoteConfigControllers[identifier]?.let { converter ->
                controller?.let { value ->
                    StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry().apply {
                        this.key = identifier
                        this.value = converter.fromModel(value)
                    }
                }
            }
        }.toTypedArray()
    }

    override fun toModel(value: Array<StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry>): Map<String, Any?> {
        val moduleRemoteConfigControllers = modulesRemoteConfigsExtension.collectRemoteConfigControllers()
        return value.mapNotNull { protoEntry ->
            moduleRemoteConfigControllers[protoEntry.key]?.let { converter ->
                protoEntry.key to converter.toModel(protoEntry.value)
            }
        }.toMap()
    }
}
