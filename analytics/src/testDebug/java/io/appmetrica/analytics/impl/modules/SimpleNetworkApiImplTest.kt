package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.network.http.ModuleSslSocketFactoryProvider
import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SimpleNetworkApiImplTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val cacheControlHttpsConnectionPerformerMockedConstructionRule =
        constructionRule<CacheControlHttpsConnectionPerformer>()

    @get:Rule
    val cacheControlConnectionHttpsClientImplMockedConstructionRule =
        constructionRule<CacheControlConnectionHttpsClientImpl>()

    private val client: NetworkClientWithCacheControl = mock()
    private val configuration = SslConfiguration(mock(), mock())
    private val anotherConfiguration = SslConfiguration(mock(), mock())
    private val provider: ModuleSslSocketFactoryProvider = mock {
        on { sslConfiguration } doReturn configuration
    }
    private val url = "Url"

    private val simpleNetworkApiImpl by setUp { SimpleNetworkApiImpl() }

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().sslSocketFactoryProvider).thenReturn(provider)
    }

    @Test
    fun `request uses one complete snapshot`() {
        whenever(provider.sslConfiguration).thenReturn(configuration, anotherConfiguration)

        simpleNetworkApiImpl.performRequestWithCacheControl(url, client)

        assertThat(cacheControlHttpsConnectionPerformerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(configuration.sslSocketFactory, configuration.trustManager)
        verify(provider).sslConfiguration
    }

    @Test
    fun performRequestWithCacheControl() {
        val configurations = listOf(configuration, SslConfiguration.EMPTY)
        configurations.forEachIndexed { index, configuration ->
            whenever(provider.sslConfiguration).thenReturn(configuration)

            simpleNetworkApiImpl.performRequestWithCacheControl(url, client)

            verify(cacheControlHttpsConnectionPerformerMockedConstructionRule.constructionMock.constructed()[index])
                .performConnection(
                    url,
                    cacheControlConnectionHttpsClientImplMockedConstructionRule.constructionMock.constructed()[index]
                )
            verify(provider, times(index + 1)).sslConfiguration
        }

        assertThat(cacheControlHttpsConnectionPerformerMockedConstructionRule.constructionMock.constructed())
            .hasSize(2)
        assertThat(cacheControlHttpsConnectionPerformerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                configuration.sslSocketFactory,
                configuration.trustManager,
                null,
                null,
            )

        assertThat(cacheControlConnectionHttpsClientImplMockedConstructionRule.constructionMock.constructed())
            .hasSize(2)
        assertThat(cacheControlConnectionHttpsClientImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(client, client)
    }
}
