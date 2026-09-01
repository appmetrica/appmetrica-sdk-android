package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent

internal object ServiceEventToCoreServiceEventConverter {

    fun convert(source: ServiceEvent): CoreServiceEvent {
        return CoreServiceEvent()
            .apply {
                type = source.type
                customType = source.customType
                name = source.name
                if (source.valueBytes == null) {
                    source.value?.let { value = it }
                }
                source.valueBytes?.let { valueBytes = it }
                valueProtocolVersion = source.valueProtocolVersion
                bytesTruncated = source.bytesTruncated
                extras = HashMap(source.extras)
            }
    }

    fun convert(source: ServiceEvent, prototype: CoreServiceEvent): CoreServiceEvent {
        return CoreServiceEvent
            .formReportCopyingMetadata(prototype)
            .apply {
                type = source.type
                customType = source.customType
                name = source.name
                source.value?.let { value = it }
                source.valueBytes?.let { valueBytes = it }
                valueProtocolVersion = source.valueProtocolVersion
                bytesTruncated = source.bytesTruncated
            }
    }
}
