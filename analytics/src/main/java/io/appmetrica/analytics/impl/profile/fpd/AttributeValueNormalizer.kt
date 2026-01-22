package io.appmetrica.analytics.impl.profile.fpd

internal interface AttributeValueNormalizer {

    fun normalize(value: String): String?
}
