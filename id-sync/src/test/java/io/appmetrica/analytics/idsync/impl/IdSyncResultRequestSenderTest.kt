package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceNetworkContext
import io.appmetrica.analytics.network.internal.Call
import io.appmetrica.analytics.network.internal.NetworkClient
import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.network.internal.Response
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
class IdSyncResultRequestSenderTest : CommonTest() {

    private val url = "https://example.com/report"
    private val value = "test_value"

    private val sslSocketFactory: SSLSocketFactory = mock()
    private val sslSocketFactoryProvider: SslSocketFactoryProvider = mock {
        on { sslSocketFactory } doReturn sslSocketFactory
    }

    private val networkContext: ServiceNetworkContext = mock {
        on { sslSocketFactoryProvider } doReturn sslSocketFactoryProvider
    }

    private val serviceContext: ServiceContext = mock {
        on { networkContext } doReturn networkContext
    }

    private val response: Response = mock {
        on { isCompleted } doReturn true
        on { code } doReturn 200
    }

    private val networkRequestCaptor = argumentCaptor<Request>()

    private val networkCall: Call = mock {
        on { execute() } doReturn response
    }

    private val networkClient: NetworkClient = mock {
        on { newCall(any()) } doReturn networkCall
    }

    @get:Rule
    val networkClientBuilderRule = constructionRule<NetworkClient.Builder> {
        on { withSslSocketFactory(any()) } doReturn this.mock
        on { withUseCaches(any()) } doReturn this.mock
        on { withInstanceFollowRedirects(any()) } doReturn this.mock
        on { withMaxResponseSize(any()) } doReturn this.mock
        on { build() } doReturn networkClient
    }

    private val networkClientBuilder by networkClientBuilderRule

    private val sender by setUp { IdSyncResultRequestSender(serviceContext) }

    @Test
    fun `sendRequest uses serviceContext networkContext`() {
        sender.sendRequest(url, value)

        verify(serviceContext).networkContext
        verify(networkContext).sslSocketFactoryProvider
        verify(sslSocketFactoryProvider).sslSocketFactory
    }

    @Test
    fun `sendRequest network client configuration`() {
        sender.sendRequest(url, value)

        verify(networkClientBuilder).withSslSocketFactory(sslSocketFactory)
        verify(networkClientBuilder).withUseCaches(false)
        verify(networkClientBuilder).withInstanceFollowRedirects(true)
        verify(networkClientBuilder).withMaxResponseSize(10 * 1024)
    }

    @Test
    fun `sendRequest request creation with POST method`() {
        sender.sendRequest(url, value)

        verify(networkClient).newCall(networkRequestCaptor.capture())
        assertThat(networkRequestCaptor.allValues.size).isEqualTo(1)

        val request = networkRequestCaptor.firstValue
        assertThat(request.url).isEqualTo(url)
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.body).isEqualTo(value.toByteArray(Charsets.UTF_8))
    }

    @Test
    fun `sendRequest adds Content-Type header`() {
        sender.sendRequest(url, value)

        verify(networkClient).newCall(networkRequestCaptor.capture())

        val request = networkRequestCaptor.firstValue
        assertThat(request.headers).containsEntry("Content-Type", "application/json")
    }

    @Test
    fun `sendRequest returns true when response is completed with 200 code`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(200)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns false when response is not completed`() {
        whenever(response.isCompleted).thenReturn(false)
        whenever(response.code).thenReturn(200)

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest returns true when response code is 404`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(404)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns true when response code is 400`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(400)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns true when response code is 403`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(403)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns true when response code is 422`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(422)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns true when response code is 499`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(499)

        val result = sender.sendRequest(url, value)

        assertThat(result).isTrue()
    }

    @Test
    fun `sendRequest returns false when response code is 399`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(399)

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest returns false when response code is 500`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(500)

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest returns false when response code is 201`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(201)

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest returns false when response code is 502`() {
        whenever(response.isCompleted).thenReturn(true)
        whenever(response.code).thenReturn(502)

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest returns false when exception is thrown`() {
        whenever(networkCall.execute()).doThrow(RuntimeException("Network error"))

        val result = sender.sendRequest(url, value)

        assertThat(result).isFalse()
    }

    @Test
    fun `sendRequest handles empty value`() {
        val emptyValue = ""

        val result = sender.sendRequest(url, emptyValue)

        verify(networkClient).newCall(networkRequestCaptor.capture())
        val request = networkRequestCaptor.firstValue
        assertThat(request.body).isEqualTo(emptyValue.toByteArray(Charsets.UTF_8))
    }

    @Test
    fun `sendRequest handles URL with query parameters`() {
        val urlWithParams = "https://example.com/report?param1=value1&param2=value2"

        sender.sendRequest(urlWithParams, value)

        verify(networkClient).newCall(networkRequestCaptor.capture())
        val request = networkRequestCaptor.firstValue
        assertThat(request.url).isEqualTo(urlWithParams)
    }
}
