package io.appmetrica.analytics.network.internal

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import org.junit.Test
import org.mockito.kotlin.mock
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

class NetworkClientTest {

    @Test
    fun createFilledObject() {
        val connectTimeout = 8127368
        val readTimeout = 777888
        val maxResponseSize = 121212
        val useCaches = Random.nextBoolean()
        val followRedirects = Random.nextBoolean()
        val sslSocketFactory = mock<SSLSocketFactory>()
        val client = NetworkClient.Builder()
            .withConnectTimeout(connectTimeout)
            .withReadTimeout(readTimeout)
            .withMaxResponseSize(maxResponseSize)
            .withUseCaches(useCaches)
            .withInstanceFollowRedirects(followRedirects)
            .withSslSocketFactory(sslSocketFactory)
            .build()
        ObjectPropertyAssertions(client)
            .withPrivateFields(true)
            .checkField("connectTimeout", "getConnectTimeout", connectTimeout)
            .checkField("readTimeout", "getReadTimeout", readTimeout)
            .checkField("sslSocketFactory", "getSslSocketFactory", sslSocketFactory)
            .checkField("useCaches", "getUseCaches", useCaches)
            .checkField("instanceFollowRedirects", "getInstanceFollowRedirects", followRedirects)
            .checkField("maxResponseSize", "getMaxResponseSize", maxResponseSize)
            .checkAll()
    }

    @Test
    fun createEmptyObject() {
        val client = NetworkClient.Builder().build()
        ObjectPropertyAssertions(client)
            .withPrivateFields(true)
            .checkFieldIsNull("connectTimeout", "getConnectTimeout")
            .checkFieldIsNull("readTimeout", "getReadTimeout")
            .checkFieldIsNull("sslSocketFactory", "getSslSocketFactory")
            .checkFieldIsNull("useCaches", "getUseCaches")
            .checkFieldIsNull("instanceFollowRedirects", "getInstanceFollowRedirects")
            .checkField("maxResponseSize", "getMaxResponseSize", Int.MAX_VALUE)
            .checkAll()
    }
}
