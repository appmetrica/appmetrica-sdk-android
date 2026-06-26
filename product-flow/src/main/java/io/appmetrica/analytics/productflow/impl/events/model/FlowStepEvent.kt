package io.appmetrica.analytics.productflow.impl.events.model

import io.appmetrica.analytics.productflow.OfferPrice

internal class FlowStepEvent(
    val productId: String?,
    val productOfferId: String?,
    val stepType: String,
    val stepOption: String?,
    val price: OfferPrice?,
    val payload: Map<String, String>?
)
