package io.appmetrica.analytics.billing.impl.config.service

import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.config.service.converter.BillingConfigProtoConverter
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideBillingConfig
import io.appmetrica.analytics.billing.impl.config.service.parser.BillingConfigJsonParser
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideBillingConfigParserTest : CommonTest() {

    private val rawData = JSONObject()
    private val billingConfigProto: BillingConfigProto = mock()
    private val billingConfig: ServiceSideBillingConfig = mock()

    private val protoConverter: BillingConfigProtoConverter = mock {
        on { toModel(billingConfigProto) } doReturn billingConfig
    }
    private val jsonParser: BillingConfigJsonParser = mock {
        on { parse(rawData) } doReturn billingConfigProto
    }

    @get:Rule
    val remoteConfigJsonUtils = staticRule<RemoteConfigJsonUtils> {
        on {
            RemoteConfigJsonUtils.extractFeature(
                rawData,
                "auto_inapp_collecting",
                true
            )
        } doReturn true
    }

    private val parser = ServiceSideBillingConfigParser(
        converter = protoConverter,
        parser = jsonParser
    )

    @Test
    fun parse() {
        val wrapper = parser.parse(rawData)
        assertThat(wrapper.config.enabled).isTrue()
        assertThat(wrapper.config.config).isSameAs(billingConfig)
    }
}
