package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.gradle.testutils.CommonTest
import okhttp3.Protocol
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

@SuppressWarnings("RobolectricUsage")
@RunWith(RobolectricTestRunner::class)
internal class OkHttpClientFactoryTest : CommonTest() {

    private val factory = OkHttpClientFactory()

    @Before
    fun setUp() {
        OkHttpClientFactory.clearClientsCache()
    }

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
    fun createOkHttpClientWithCallTimeout() {
        val settings = NetworkClientSettings.Builder()
            .withCallTimeout(5, TimeUnit.SECONDS)
            .build()

        val client = factory.createOkHttpClient(settings)

        assertThat(client.callTimeoutMillis).isEqualTo(5000)
    }

    @Test
    fun createMultipleClientsWithSameSettings() {
        val settings = NetworkClientSettings.Builder()
            .withConnectTimeout(1000)
            .build()
        val equalSettings = NetworkClientSettings.Builder()
            .withConnectTimeout(1000)
            .build()

        val client1 = factory.createOkHttpClient(settings)
        val client2 = factory.createOkHttpClient(settings)
        val client3 = factory.createOkHttpClient(equalSettings)
        val clientFromOtherFactory = OkHttpClientFactory().createOkHttpClient(settings)

        SoftAssertions().apply {
            assertThat(client1).`as`("same settings object").isSameAs(client2)
            assertThat(client1).`as`("equal settings").isSameAs(client3)
            assertThat(client1).`as`("shared across factory instances").isSameAs(clientFromOtherFactory)
            assertThat(client1.connectTimeoutMillis).`as`("timeout").isEqualTo(1000)
            assertAll()
        }
    }

    @Test
    fun createClientsWithDifferentSettings() {
        val settings1 = NetworkClientSettings.Builder()
            .withConnectTimeout(1000)
            .build()
        val settings2 = NetworkClientSettings.Builder()
            .withConnectTimeout(2000)
            .build()

        val client1 = factory.createOkHttpClient(settings1)
        val client2 = factory.createOkHttpClient(settings2)

        SoftAssertions().apply {
            assertThat(client1).`as`("different clients").isNotSameAs(client2)
            assertThat(client1.connectionPool).`as`("shared connectionPool").isSameAs(client2.connectionPool)
            assertThat(client1.dispatcher).`as`("shared dispatcher").isSameAs(client2.dispatcher)
            assertThat(client1.connectTimeoutMillis).isEqualTo(1000)
            assertThat(client2.connectTimeoutMillis).isEqualTo(2000)
            assertAll()
        }
    }

    @Test
    fun clientsCacheIsLruWithLimitedSize() {
        val maxSize = OkHttpClientFactory.MAX_CACHED_CLIENTS
        val settings = (1..maxSize + 1).map { index ->
            NetworkClientSettings.Builder()
                .withConnectTimeout(20_000 + index)
                .build()
        }

        val clientsAtCapacity = settings.take(maxSize).map { factory.createOkHttpClient(it) }

        SoftAssertions().apply {
            settings.take(maxSize).zip(clientsAtCapacity).forEachIndexed { index, (entrySettings, client) ->
                assertThat(factory.createOkHttpClient(entrySettings))
                    .`as`("retained at capacity #$index")
                    .isSameAs(client)
            }
            assertAll()
        }

        // After the loop above access order is settings[0]=LRU … settings[maxSize-1]=MRU.
        factory.createOkHttpClient(settings[maxSize])

        SoftAssertions().apply {
            (1 until maxSize).forEach { index ->
                assertThat(factory.createOkHttpClient(settings[index]))
                    .`as`("non-LRU entry kept after overflow #$index")
                    .isSameAs(clientsAtCapacity[index])
            }

            val rebuiltLru = factory.createOkHttpClient(settings[0])
            assertThat(rebuiltLru)
                .`as`("LRU entry is evicted on overflow")
                .isNotSameAs(clientsAtCapacity[0])
            assertThat(factory.createOkHttpClient(settings[0]))
                .`as`("rebuilt entry is cached again")
                .isSameAs(rebuiltLru)
            assertAll()
        }

        OkHttpClientFactory.clearClientsCache()

        val refillClients = settings.take(maxSize).map { factory.createOkHttpClient(it) }
        // Make settings[0] MRU so overflow evicts settings[1] instead.
        factory.createOkHttpClient(settings[0])
        factory.createOkHttpClient(settings[maxSize])

        SoftAssertions().apply {
            assertThat(factory.createOkHttpClient(settings[0]))
                .`as`("touched entry survives overflow")
                .isSameAs(refillClients[0])
            assertThat(factory.createOkHttpClient(settings[1]))
                .`as`("untouched entry is evicted after touch of former LRU")
                .isNotSameAs(refillClients[1])
            assertAll()
        }
    }
}
