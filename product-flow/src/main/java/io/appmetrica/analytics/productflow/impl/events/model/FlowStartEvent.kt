package io.appmetrica.analytics.productflow.impl.events.model

import io.appmetrica.analytics.productflow.OfferPrice

internal class FlowStartEvent(
    val productId: String,
    val productOfferId: String?,
    val price: OfferPrice?,
    val payload: Map<String, String>?
)
