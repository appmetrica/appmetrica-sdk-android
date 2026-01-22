package io.appmetrica.analytics.billing.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.internal.config.BillingConfig
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock

internal class RemoteBillingConfigProtoConverterTest : CommonTest() {

    private val billingConfig: BillingConfig = mock()
    private val billingConfigProto: BillingConfigProto = mock()
    private val billingConfigProtoConverter: BillingConfigProtoConverter = mock {
        on { fromModel(billingConfig) }.thenReturn(billingConfigProto)
        on { toModel(billingConfigProto) }.thenReturn(billingConfig)
    }

    private val converter = RemoteBillingConfigProtoConverter(
        billingConfigProtoConverter
    )

    @Test
    fun fromModel() {
        val value = RemoteBillingConfig(
            enabled = true,
            config = billingConfig
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkField("config", billingConfigProto)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = RemoteBillingConfigProto().also {
            it.enabled = true
            it.config = billingConfigProto
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkField("config", billingConfig)
            .checkAll()
    }
}
