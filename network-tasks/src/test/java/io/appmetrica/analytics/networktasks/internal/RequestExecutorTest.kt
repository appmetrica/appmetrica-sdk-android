package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.network.internal.Call
import io.appmetrica.analytics.network.internal.NetworkClient
import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.network.internal.Response
import io.appmetrica.analytics.networktasks.impl.Constants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
    val newClientBuilder = MockedConstructionRule(NetworkClient.Builder::class.java) { mock, _ ->
        whenever(mock.withConnectTimeout(any())).thenReturn(mock)
        whenever(mock.withReadTimeout(any())).thenReturn(mock)
        whenever(mock.withUseCaches(any())).thenReturn(mock)
        whenever(mock.withInstanceFollowRedirects(any())).thenReturn(mock)
        whenever(mock.withMaxResponseSize(any())).thenReturn(mock)
        whenever(mock.withSslSocketFactory(anyOrNull())).thenReturn(mock)
        whenever(mock.build()).thenReturn(networkClient)
    }
    private val requestExecutor = CacheControlHttpsConnectionPerformer.RequestExecutor()

    @Test
    fun successfulExecutionFilled() {
        val prevEtag = "prev-etag"
        val url = "https://ya.ru"
        assertThat(requestExecutor.execute(prevEtag, url, sslSocketFactory)).isSameAs(response)
        with(newClientBuilder.constructionMock.constructed().first()) {
            verify(this).withInstanceFollowRedirects(true)
            verify(this).withConnectTimeout(Constants.Config.REQUEST_TIMEOUT)
            verify(this).withReadTimeout(Constants.Config.REQUEST_TIMEOUT)
            verify(this).withSslSocketFactory(sslSocketFactory)
            verify(this).build()
            verifyNoMoreInteractions(this)
        }
        ObjectPropertyAssertions(requestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", "GET")
            .checkField("headers", "getHeaders", mapOf("If-None-Match" to prevEtag))
            .checkField("body", "getBody", ByteArray(0))
            .checkAll()
    }

    @Test
    fun successfulExecutionNullable() {
        val url = "https://ya.ru"

        assertThat(requestExecutor.execute(null, url, null)).isSameAs(response)
        with(newClientBuilder.constructionMock.constructed().first()) {
            verify(this).withInstanceFollowRedirects(true)
            verify(this).withConnectTimeout(Constants.Config.REQUEST_TIMEOUT)
            verify(this).withReadTimeout(Constants.Config.REQUEST_TIMEOUT)
            verify(this).withSslSocketFactory(null)
            verify(this).build()
            verifyNoMoreInteractions(this)
        }
        ObjectPropertyAssertions(requestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", "GET")
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
