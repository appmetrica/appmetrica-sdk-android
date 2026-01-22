package io.appmetrica.analytics.billing.impl.config.remote

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.config.remote.converter.BillingConfigProtoConverter
import io.appmetrica.analytics.billing.impl.config.remote.parser.BillingConfigJsonParser
import io.appmetrica.analytics.billing.internal.config.BillingConfig
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class RemoteBillingConfigParserTest : CommonTest() {

    private val rawData = JSONObject()
    private val billingConfigProto: BillingConfigProto = mock()
    private val billingConfig: BillingConfig = mock()

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

    private val parser = RemoteBillingConfigParser(
        converter = protoConverter,
        parser = jsonParser
    )

    @Test
    fun parse() {
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("config", billingConfig)
            .checkAll()
    }
}
