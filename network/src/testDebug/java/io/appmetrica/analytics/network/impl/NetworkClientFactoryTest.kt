package io.appmetrica.analytics.network.impl

import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.network.internal.NetworkClientServiceLocator
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NetworkClientFactoryTest : CommonTest() {

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
    fun `createNetworkClient uses OkHttp builder when custom is not configured`() {
        val expectedClient: NetworkClient = mock()
        val okHttpBuilder = TestNetworkClientBuilder(expectedClient)

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(OKHTTP_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isSameAs(expectedClient)
        assertThat(okHttpBuilder.appliedSettings).isSameAs(settings)
        verifyBuilderLoaded(OKHTTP_BUILDER_CLASS)
        verifyBuildersLoadedTimes(1)
    }

    @Test
    fun `createNetworkClient uses custom builder when configured in metadata`() {
        metaData.putString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY, CUSTOM_BUILDER_CLASS)
        val expectedClient: NetworkClient = mock()
        val customBuilder = TestNetworkClientBuilder(expectedClient)

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(CUSTOM_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(customBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isSameAs(expectedClient)
        assertThat(customBuilder.appliedSettings).isSameAs(settings)
        verifyBuilderLoaded(CUSTOM_BUILDER_CLASS)
        verifyBuilderNeverLoaded(OKHTTP_BUILDER_CLASS)
    }

    @Test
    fun `createNetworkClient falls back to OkHttp when custom builder is unavailable`() {
        metaData.putString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY, CUSTOM_BUILDER_CLASS)

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(CUSTOM_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(null)

        val expectedClient: NetworkClient = mock()
        val okHttpBuilder = TestNetworkClientBuilder(expectedClient)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(OKHTTP_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isSameAs(expectedClient)
        assertThat(okHttpBuilder.appliedSettings).isSameAs(settings)
        verifyBuilderLoaded(CUSTOM_BUILDER_CLASS)
        verifyBuilderLoaded(OKHTTP_BUILDER_CLASS)
    }

    @Test
    fun `createNetworkClient falls back to OkHttp when custom builder class name is empty`() {
        metaData.putString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY, "")

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(""),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(null)

        val expectedClient: NetworkClient = mock()
        val okHttpBuilder = TestNetworkClientBuilder(expectedClient)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(OKHTTP_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isSameAs(expectedClient)
        verifyBuilderLoaded("")
        verifyBuilderLoaded(OKHTTP_BUILDER_CLASS)
    }

    @Test
    fun `createNetworkClient uses DummyNetworkClient when all builders fail`() {
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                any<String>(),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(null)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isInstanceOf(DummyNetworkClient::class.java)
        assertThat(client.settings).isSameAs(settings)
    }

    @Test
    fun `createNetworkClient without metadata skips custom builder`() {
        whenever(locator.applicationMetaData).thenReturn(null)

        val expectedClient: NetworkClient = mock()
        val okHttpBuilder = TestNetworkClientBuilder(expectedClient)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                eq(OKHTTP_BUILDER_CLASS),
                eq(NetworkClient.Builder::class.java)
            )
        ).thenReturn(okHttpBuilder)

        val client = NetworkClientFactory.createNetworkClient(settings)

        assertThat(client).isSameAs(expectedClient)
        assertThat(okHttpBuilder.appliedSettings).isSameAs(settings)
        verifyBuilderLoaded(OKHTTP_BUILDER_CLASS)
        verifyBuildersLoadedTimes(1)
    }

    private fun verifyBuilderLoaded(className: String) {
        reflectionUtilsRule.staticMock.verify {
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                className,
                NetworkClient.Builder::class.java
            )
        }
    }

    private fun verifyBuilderNeverLoaded(className: String) {
        reflectionUtilsRule.staticMock.verify(
            {
                ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                    className,
                    NetworkClient.Builder::class.java
                )
            },
            Mockito.never()
        )
    }

    private fun verifyBuildersLoadedTimes(times: Int) {
        reflectionUtilsRule.staticMock.verify(
            {
                ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                    any(),
                    eq(NetworkClient.Builder::class.java)
                )
            },
            Mockito.times(times)
        )
    }

    private class TestNetworkClientBuilder(
        private val client: NetworkClient,
    ) : NetworkClient.Builder() {

        val appliedSettings: NetworkClientSettings
            get() = settings

        override fun build(): NetworkClient = client
    }

    private companion object {
        const val OKHTTP_BUILDER_CLASS =
            "io.appmetrica.analytics.networkokhttp.internal.OkHttpNetworkClientBuilder"
        const val CUSTOM_BUILDER_CLASS = "com.custom.CustomBuilder"
    }
}
