package io.appmetrica.analytics.networkapi

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

class NetworkClientSettingsTest : CommonTest() {

    @Test
    fun builderWithDefaultValues() {
        val settings = NetworkClientSettings.Builder().build()

        ObjectPropertyAssertions(settings)
            .checkFieldIsNull("connectTimeout")
            .checkFieldIsNull("readTimeout")
            .checkFieldIsNull("callTimeout")
            .checkFieldIsNull("sslSocketFactory")
            .checkFieldIsNull("useCaches")
            .checkFieldIsNull("instanceFollowRedirects")
            .checkField("maxResponseSize", Int.MAX_VALUE)
            .checkFieldIsNull("collectMetrics")
            .checkAll()
    }

    @Test
    fun builderWithAllValues() {
        val sslSocketFactory = mock<SSLSocketFactory>()

        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(5000)
            .withReadTimeout(10000)
            .withCallTimeout(15, TimeUnit.SECONDS)
            .withSslSocketFactory(sslSocketFactory)
            .withUseCaches(true)
            .withInstanceFollowRedirects(false)
            .withMaxResponseSize(1024 * 1024)
            .withCollectMetrics(true)
            .build()

        ObjectPropertyAssertions(settings)
            .checkField("connectTimeout", 5000)
            .checkField("readTimeout", 10000)
            .checkField("callTimeout", 15000L)
            .checkField("sslSocketFactory", sslSocketFactory)
            .checkField("useCaches", true)
            .checkField("instanceFollowRedirects", false)
            .checkField("maxResponseSize", 1024 * 1024)
            .checkField("collectMetrics", true)
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
