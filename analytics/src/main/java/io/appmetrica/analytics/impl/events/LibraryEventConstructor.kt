package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.adrevenue.AdRevenuePayloadEnricher
import io.appmetrica.analytics.impl.adrevenue.NativeLayerPayloadEnricher
import io.appmetrica.analytics.impl.adrevenue.SupportedAdNetworksPayloadEnricher
import io.appmetrica.analytics.impl.adrevenue.YandexSourcePayloadEnricher
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

class LibraryEventConstructor {

    private val systemEventName = "appmetrica_system_event_42"
    private val nullPlaceholder = "null"

    private var attributesEnrichers: List<AdRevenuePayloadEnricher>? = null

    fun constructEvent(
        sender: String?,
        event: String?,
        payload: String?
    ): ModuleEvent {
        val attributes = mutableMapOf(
            "sender" to (sender ?: nullPlaceholder),
            "event" to (event ?: nullPlaceholder),
            "payload" to (payload ?: nullPlaceholder),
        )

        return ModuleEvent.newBuilder(EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .withName(systemEventName)
            .withAttributes(enrichAttributes(attributes))
            .build()
    }

    private fun enrichAttributes(attributes: MutableMap<String, String>): Map<String, String> {
        peekEnrichers()?.forEach { it.enrich(attributes) }
        return attributes
    }

    @Synchronized
    private fun peekEnrichers(): List<AdRevenuePayloadEnricher>? {
        if (attributesEnrichers == null) {
            ClientServiceLocator.getInstance().contextAppearedListener.peekContext()?.let {
                attributesEnrichers = listOf(
                    NativeLayerPayloadEnricher(),
                    SupportedAdNetworksPayloadEnricher(it),
                    YandexSourcePayloadEnricher()
                )
            }
        }
        return attributesEnrichers
    }
}
