package io.appmetrica.analytics.certificates.impl.config.service

import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.certificates.impl.config.service.parser.NetworkCertificatesConfigJsonParser
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideNetworkCertificatesConfigParserTest : CommonTest() {

    private val rawData = JSONObject().put("key", "value")
    private val config = ServiceSideNetworkCertificatesConfig(enabled = false)
    private val jsonParser: NetworkCertificatesConfigJsonParser = mock {
        on { parse(rawData) } doReturn config
    }
    private val parser = ServiceSideNetworkCertificatesConfigParser(jsonParser)

    @Test
    fun `parse wraps json parser result`() {
        val wrapper = parser.parse(rawData)

        assertThat(wrapper.config).isSameAs(config)
    }
}
