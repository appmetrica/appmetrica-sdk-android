package io.appmetrica.analytics.billing.impl.config.service.converter

import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideBillingConfig
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test
import org.mockito.kotlin.mock

internal class RemoteBillingConfigProtoConverterTest : CommonTest() {

    private val billingConfig: ServiceSideBillingConfig = mock()
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
        val value = ServiceSideRemoteBillingConfig(
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
