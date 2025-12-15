package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.network.internal.NetworkClientBuilder
import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.networkapi.Response
import io.appmetrica.analytics.networktasks.impl.Constants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
class RequestExecutorTest : CommonTest() {

    private val sslSocketFactory = mock<SSLSocketFactory>()
    private val response = mock<Response>()
    private val call = mock<Call> {
        on { execute() } doReturn response
    }
    private val requestCaptor = argumentCaptor<Request>()
    private val networkClient = mock<NetworkClient> {
        on { newCall(requestCaptor.capture()) } doReturn call
    }

    @get:Rule
    val newClientBuilder = MockedConstructionRule(NetworkClientBuilder::class.java) { mock, _ ->
        whenever(mock.withSettings(networkClientSettings)).thenReturn(mock)
        whenever(mock.build()).thenReturn(networkClient)
    }

    private val networkClientSettings: NetworkClientSettings = mock()

    @get:Rule
    val networkClientSettingsBuilderRule =
        MockedConstructionRule(NetworkClientSettings.Builder::class.java) { mock, _ ->
            whenever(mock.withConnectTimeout(Constants.Config.REQUEST_TIMEOUT)).thenReturn(mock)
            whenever(mock.withReadTimeout(Constants.Config.REQUEST_TIMEOUT)).thenReturn(mock)
            whenever(mock.withSslSocketFactory(sslSocketFactory)).thenReturn(mock)
            whenever(mock.withSslSocketFactory(null)).thenReturn(mock)
            whenever(mock.withInstanceFollowRedirects(true)).thenReturn(mock)
            whenever(mock.build()).thenReturn(networkClientSettings)
        }

    private val requestExecutor = CacheControlHttpsConnectionPerformer.RequestExecutor()

    @Test
    fun successfulExecutionFilled() {
        val prevEtag = "prev-etag"
        val url = "https://ya.ru"
        assertThat(requestExecutor.execute(prevEtag, url, sslSocketFactory)).isSameAs(response)
        with(newClientBuilder.constructionMock.constructed().first()) {
            verify(this).withSettings(networkClientSettings)
            verify(this).build()
            verifyNoMoreInteractions(this)
        }
        ObjectPropertyAssertions(requestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", Request.Method.GET)
            .checkField("headers", "getHeaders", mapOf("If-None-Match" to prevEtag))
            .checkField("body", "getBody", ByteArray(0))
            .checkAll()
    }

    @Test
    fun successfulExecutionNullable() {
        val url = "https://ya.ru"

        assertThat(requestExecutor.execute(null, url, null)).isSameAs(response)
        with(newClientBuilder.constructionMock.constructed().first()) {
            verify(this).withSettings(networkClientSettings)
            verify(this).build()
            verifyNoMoreInteractions(this)
        }
        ObjectPropertyAssertions(requestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", Request.Method.GET)
            .checkField("headers", "getHeaders", emptyMap<String, String>())
            .checkField("body", "getBody", ByteArray(0))
            .checkAll()
    }

    @Test
    fun emptyEtag() {
        val url = "https://ya.ru"
        requestExecutor.execute("", url, sslSocketFactory)
        assertThat(requestCaptor.firstValue.headers).isEmpty()
    }
}
