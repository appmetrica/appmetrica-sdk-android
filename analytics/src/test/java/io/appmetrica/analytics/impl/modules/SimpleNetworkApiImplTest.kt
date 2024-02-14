package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SimpleNetworkApiImplTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val cacheControlHttpsConnectionPerformerMockedConstructionRule =
        constructionRule<CacheControlHttpsConnectionPerformer>()

    @get:Rule
    val cacheControlConnectionHttpsClientImplMockedConstructionRule =
        constructionRule<CacheControlConnectionHttpsClientImpl>()

    private val client: NetworkClientWithCacheControl = mock()
    private val url = "Url"

    private val simpleNetworkApiImpl by setUp { SimpleNetworkApiImpl() }

    @Test
    fun performRequestWithCacheControl() {
        simpleNetworkApiImpl.performRequestWithCacheControl(url, client)

        verify(cacheControlHttpsConnectionPerformerMockedConstructionRule.constructionMock.constructed().first())
            .performConnection(
                url,
                cacheControlConnectionHttpsClientImplMockedConstructionRule.constructionMock.constructed().first()
            )

        assertThat(cacheControlHttpsConnectionPerformerMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(cacheControlHttpsConnectionPerformerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(GlobalServiceLocator.getInstance().sslSocketFactoryProvider.sslSocketFactory)

        assertThat(cacheControlConnectionHttpsClientImplMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(cacheControlConnectionHttpsClientImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(client)
    }
}
