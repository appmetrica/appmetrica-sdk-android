package io.appmetrica.analytics.adrevenue.other.impl.config.service.converter

import io.appmetrica.analytics.adrevenue.other.impl.AdRevenueOtherConfigProto
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class AdRevenueOtherConfigProtoConverterTest : CommonTest() {

    private val converter = AdRevenueOtherConfigProtoConverter()

    @Test
    fun fromModel() {
        val config = ServiceSideAdRevenueOtherConfig(enabled = true, includeSource = true)
        val proto = converter.fromModel(config)

        assertThat(proto.enabled).isTrue()
        assertThat(proto.includeSource).isTrue()
    }

    @Test
    fun fromModelDefaults() {
        val config = ServiceSideAdRevenueOtherConfig(enabled = false, includeSource = false)
        val proto = converter.fromModel(config)

        assertThat(proto.enabled).isFalse()
        assertThat(proto.includeSource).isFalse()
    }

    @Test
    fun toModel() {
        val proto = AdRevenueOtherConfigProto().also {
            it.enabled = true
            it.includeSource = true
        }
        val config = converter.toModel(proto)

        assertThat(config.enabled).isTrue()
        assertThat(config.includeSource).isTrue()
    }

    @Test
    fun toModelDefaults() {
        val proto = AdRevenueOtherConfigProto()
        val config = converter.toModel(proto)

        assertThat(config.enabled).isFalse()
        assertThat(config.includeSource).isFalse()
    }
}
