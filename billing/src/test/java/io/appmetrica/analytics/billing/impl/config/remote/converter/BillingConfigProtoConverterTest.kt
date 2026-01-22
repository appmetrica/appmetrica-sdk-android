package io.appmetrica.analytics.billing.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.internal.config.BillingConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

internal class BillingConfigProtoConverterTest : CommonTest() {

    private val converter = BillingConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = BillingConfig(
            sendFrequencySeconds = 42,
            firstCollectingInappMaxAgeSeconds = 4242,
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("sendFrequencySeconds", 42)
            .checkField("firstCollectingInappMaxAgeSeconds", 4242)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = BillingConfigProto().also {
            it.sendFrequencySeconds = 42
            it.firstCollectingInappMaxAgeSeconds = 4242
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("sendFrequencySeconds", 42)
            .checkField("firstCollectingInappMaxAgeSeconds", 4242)
            .checkAll()
    }
}
