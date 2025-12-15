package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.testutils.CommonTest
import okhttp3.Protocol
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock
import javax.net.ssl.SSLSocketFactory

class OkHttpClientFactoryTest : CommonTest() {

    private val factory = OkHttpClientFactory()

    @Test
    fun createOkHttpClientWithDefaultSettings() {
        val settings = NetworkClientSettings.Builder().build()

        val client = factory.createOkHttpClient(settings)

        SoftAssertions().apply {
            assertThat(client).`as`("client").isNotNull()
            assertThat(client.protocols).`as`("protocols").containsExactly(Protocol.HTTP_2, Protocol.HTTP_1_1)
            assertThat(client.connectTimeoutMillis).`as`("connectTimeout").isEqualTo(10000)
            assertThat(client.readTimeoutMillis).`as`("readTimeout").isEqualTo(10000)
            assertAll()
        }
    }

    @Test
    fun createOkHttpClientWithTimeouts() {
        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(3000)
            .withReadTimeout(7000)
            .build()

        val client = factory.createOkHttpClient(settings)

        SoftAssertions().apply {
            assertThat(client.connectTimeoutMillis).`as`("connectTimeout").isEqualTo(3000)
            assertThat(client.readTimeoutMillis).`as`("readTimeout").isEqualTo(7000)
            assertAll()
        }
    }

    @Test
    fun createOkHttpClientWithFollowRedirects() {
        val settingsTrue = NetworkClientSettings.Builder()
            .withInstanceFollowRedirects(true)
            .build()
        val settingsFalse = NetworkClientSettings.Builder()
            .withInstanceFollowRedirects(false)
            .build()

        val clientTrue = factory.createOkHttpClient(settingsTrue)
        val clientFalse = factory.createOkHttpClient(settingsFalse)

        assertThat(clientTrue.followRedirects).isTrue()
        assertThat(clientFalse.followRedirects).isFalse()
    }

    @Test
    fun createOkHttpClientWithUseCaches() {
        val settings = NetworkClientSettings.Builder()
            .withUseCaches(false)
            .build()

        val client = factory.createOkHttpClient(settings)

        assertThat(client.cache).isNull()
    }

    @Test
    fun createOkHttpClientWithSslSocketFactory() {
        val sslSocketFactory = mock<SSLSocketFactory>()
        val settings = NetworkClientSettings.Builder()
            .withSslSocketFactory(sslSocketFactory)
            .build()

        val client = factory.createOkHttpClient(settings)

        assertThat(client.sslSocketFactory).isEqualTo(sslSocketFactory)
    }

    @Test
    fun createOkHttpClientWithAllSettings() {
        val sslSocketFactory = mock<SSLSocketFactory>()
        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(2000)
            .withReadTimeout(4000)
            .withInstanceFollowRedirects(true)
            .withUseCaches(false)
            .withSslSocketFactory(sslSocketFactory)
            .build()

        val client = factory.createOkHttpClient(settings)

        SoftAssertions().apply {
            assertThat(client.connectTimeoutMillis).`as`("connectTimeout").isEqualTo(2000)
            assertThat(client.readTimeoutMillis).`as`("readTimeout").isEqualTo(4000)
            assertThat(client.followRedirects).`as`("followRedirects").isTrue()
            assertThat(client.cache).`as`("cache").isNull()
            assertThat(client.sslSocketFactory).`as`("sslSocketFactory").isEqualTo(sslSocketFactory)
            assertThat(client.protocols).`as`("protocols").containsExactly(Protocol.HTTP_2, Protocol.HTTP_1_1)
            assertAll()
        }
    }

    @Test
    fun createMultipleClientsWithSameSettings() {
        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(1000)
            .build()

        val client1 = factory.createOkHttpClient(settings)
        val client2 = factory.createOkHttpClient(settings)

        SoftAssertions().apply {
            assertThat(client1).`as`("not same instance").isNotSameAs(client2)
            assertThat(client1.connectTimeoutMillis).`as`("same timeout").isEqualTo(client2.connectTimeoutMillis)
            assertAll()
        }
    }
}
