package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.InternalEvents

internal object UrgentEvents {

    @JvmStatic
    val urgentEventTypes: Set<Int> = setOf(
        InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId,
        InternalEvents.EVENT_TYPE_APP_UPDATE.typeId,
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId,
        InternalEvents.EVENT_TYPE_INIT.typeId,
        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId,
        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId,
    )

    @JvmStatic
    fun isUrgent(eventType: Int): Boolean = eventType in urgentEventTypes
}
