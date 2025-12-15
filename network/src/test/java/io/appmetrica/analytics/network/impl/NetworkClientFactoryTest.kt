package io.appmetrica.analytics.network.impl

import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.network.internal.NetworkClientServiceLocator
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkClientFactoryTest : CommonTest() {

    private val metaData = Bundle()

    private val locator: NetworkClientServiceLocator = mock {
        on { applicationMetaData } doReturn metaData
    }
    private val settings: NetworkClientSettings = mock()

    @get:Rule
    val serviceLocatorRule = staticRule<NetworkClientServiceLocator>()

    @get:Rule
    val reflectionUtilsRule = staticRule<ReflectionUtils>()

    @Before
    fun setUp() {
        whenever(NetworkClientServiceLocator.getInstance()).thenReturn(locator)
    }

    @Test
    fun `createNetworkClient with all parameters configures builder correctly`() {
        val testBuilder = TestNetworkClientBuilder()

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq("io.appmetrica.analytics.networkokhttp.internal.OkHttpNetworkClientBuilder"),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(testBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isNotNull
    }

    @Test
    fun `createNetworkClient with custom builder uses custom implementation`() {
        metaData.putString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY, "com.custom.CustomBuilder")
        val customBuilder = TestNetworkClientBuilder()

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq("com.custom.CustomBuilder"),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(customBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isNotNull
    }

    @Test
    fun `createNetworkClient fallback mechanism tries builders sequentially`() {
        metaData.putString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY, "com.custom.CustomBuilder")

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq("com.custom.CustomBuilder"),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(null)

        val okHttpBuilder = TestNetworkClientBuilder()
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq("io.appmetrica.analytics.networkokhttp.internal.OkHttpNetworkClientBuilder"),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isNotNull
    }

    @Test
    fun `createNetworkClient uses DummyBuilder when all builders fail`() {
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                any<String>(),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(null)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isInstanceOf(DummyNetworkClient::class.java)
    }

    @Test
    fun `createNetworkClient without metadata skips custom builder`() {
        whenever(locator.applicationMetaData).thenReturn(null)

        val okHttpBuilder = TestNetworkClientBuilder()
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq("io.appmetrica.analytics.networkokhttp.internal.OkHttpNetworkClientBuilder"),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isNotNull
    }

    private class TestNetworkClientBuilder : NetworkClient.Builder() {

        override fun build(): NetworkClient = mock()
    }
}
