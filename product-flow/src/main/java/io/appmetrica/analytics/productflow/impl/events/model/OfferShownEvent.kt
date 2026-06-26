package io.appmetrica.analytics.productflow.impl.events.model

import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.OfferReferrer

internal class OfferShownEvent(
    val productOfferId: String,
    val offerType: String,
    val productId: String?,
    val benefitType: String?,
    val price: OfferPrice?,
    val payload: Map<String, String>?,
    val referrer: OfferReferrer?
)
