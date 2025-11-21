package io.appmetrica.analytics.impl.profile.fpd

interface AttributeValueNormalizer {

    fun normalize(value: String): String?
}
