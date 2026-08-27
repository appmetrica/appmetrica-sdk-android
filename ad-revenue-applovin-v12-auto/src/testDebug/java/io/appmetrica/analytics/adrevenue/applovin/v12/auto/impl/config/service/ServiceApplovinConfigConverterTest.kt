package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ServiceApplovinConfigConverterTest : CommonTest() {

    private val converter = ServiceApplovinConfigConverter()

    @Test
    fun roundtripEnabled() {
        val wrapper = ServiceApplovinConfigWrapper(ServiceApplovinConfig(enabled = true))
        val bytes = converter.fromModel(wrapper)
        val result = converter.toModel(bytes)

        assertThat(result.config.enabled).isTrue()
    }

    @Test
    fun roundtripDisabled() {
        val wrapper = ServiceApplovinConfigWrapper(ServiceApplovinConfig(enabled = false))
        val bytes = converter.fromModel(wrapper)
        val result = converter.toModel(bytes)

        assertThat(result.config.enabled).isFalse()
    }
}
