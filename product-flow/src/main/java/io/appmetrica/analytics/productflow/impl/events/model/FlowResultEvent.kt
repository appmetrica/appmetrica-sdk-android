package io.appmetrica.analytics.productflow.impl.events.model

import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.ProductFlowStatus

internal class FlowResultEvent(
    val status: ProductFlowStatus,
    val productId: String?,
    val productOfferId: String?,
    val price: OfferPrice?,
    val payload: Map<String, String>?
)
