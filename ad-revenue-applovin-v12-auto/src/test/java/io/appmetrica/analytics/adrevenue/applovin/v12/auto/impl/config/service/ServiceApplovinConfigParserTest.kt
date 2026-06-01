package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.converter.ApplovinConfigProtoConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.parser.ApplovinConfigJsonParser
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ServiceApplovinConfigParserTest : CommonTest() {

    private val rawData = JSONObject()

    private val proto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also {
        it.enabled = false
    }
    private val config = ServiceApplovinConfig(enabled = false)

    private val jsonParser: ApplovinConfigJsonParser = mock {
        on { parse(rawData) } doReturn proto
    }
    private val protoConverter: ApplovinConfigProtoConverter = mock {
        on { toModel(proto) } doReturn config
    }

    private val parser = ServiceApplovinConfigParser(
        protoConverter = protoConverter,
        jsonParser = jsonParser,
    )

    @Test
    fun parseDelegatesToJsonParserAndProtoConverter() {
        val result = parser.parse(rawData)

        verify(jsonParser).parse(rawData)
        verify(protoConverter).toModel(proto)
        assertThat(result.config.enabled).isFalse()
    }

    @Test
    fun parseEnabled() {
        val enabledProto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also {
            it.enabled = true
        }
        val enabledConfig = ServiceApplovinConfig(enabled = true)
        whenever(jsonParser.parse(rawData)).thenReturn(enabledProto)
        whenever(protoConverter.toModel(enabledProto)).thenReturn(enabledConfig)

        val result = parser.parse(rawData)

        assertThat(result.config.enabled).isTrue()
    }
}
