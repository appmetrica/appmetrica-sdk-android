package io.appmetrica.analytics.networkapi

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import javax.net.ssl.SSLSocketFactory

class NetworkClientSettingsTest : CommonTest() {

    @Test
    fun builderWithDefaultValues() {
        val settings = NetworkClientSettings.Builder().build()

        ObjectPropertyAssertions(settings)
            .checkFieldIsNull("connectTimeout")
            .checkFieldIsNull("readTimeout")
            .checkFieldIsNull("sslSocketFactory")
            .checkFieldIsNull("useCaches")
            .checkFieldIsNull("instanceFollowRedirects")
            .checkField("maxResponseSize", Int.MAX_VALUE)
            .checkAll()
    }

    @Test
    fun builderWithAllValues() {
        val sslSocketFactory = mock<SSLSocketFactory>()

        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(5000)
            .withReadTimeout(10000)
            .withSslSocketFactory(sslSocketFactory)
            .withUseCaches(true)
            .withInstanceFollowRedirects(false)
            .withMaxResponseSize(1024 * 1024)
            .build()

        ObjectPropertyAssertions(settings)
            .checkField("connectTimeout", 5000)
            .checkField("readTimeout", 10000)
            .checkField("sslSocketFactory", sslSocketFactory)
            .checkField("useCaches", true)
            .checkField("instanceFollowRedirects", false)
            .checkField("maxResponseSize", 1024 * 1024)
            .checkAll()
    }

    @Test
    fun builderWithSslSocketFactoryNull() {
        val settings = NetworkClientSettings.Builder()
            .withSslSocketFactory(null)
            .build()

        assertThat(settings.sslSocketFactory).isNull()
    }
}
