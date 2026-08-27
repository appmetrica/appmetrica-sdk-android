package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.converter

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ApplovinConfigProtoConverterTest : CommonTest() {

    private val converter = ApplovinConfigProtoConverter()

    @Test
    fun fromModelEnabled() {
        val config = ServiceApplovinConfig(enabled = true)
        val proto = converter.fromModel(config)

        assertThat(proto.enabled).isTrue()
    }

    @Test
    fun fromModelDisabled() {
        val config = ServiceApplovinConfig(enabled = false)
        val proto = converter.fromModel(config)

        assertThat(proto.enabled).isFalse()
    }

    @Test
    fun toModelEnabled() {
        val proto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also {
            it.enabled = true
        }
        val config = converter.toModel(proto)

        assertThat(config.enabled).isTrue()
    }

    @Test
    fun toModelDisabled() {
        val proto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also {
            it.enabled = false
        }
        val config = converter.toModel(proto)

        assertThat(config.enabled).isFalse()
    }

    @Test
    fun defaultProtoHasEnabledTrue() {
        // Verify the default (no value written to proto) uses default=true
        val proto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig()

        assertThat(proto.enabled).isTrue()
    }
}
