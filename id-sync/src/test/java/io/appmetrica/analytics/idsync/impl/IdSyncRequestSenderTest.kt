package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
class IdSyncRequestSenderTest : CommonTest() {

    private val url = "Some url"
    private val firstHeaderKey = "first header key"
    private val firstHeaderValue = "first header value"
    private val secondHeaderKey = "second header key"
    private val secondHeaderFirstValue = "second header first value"
    private val secondHeaderSecondValue = "second header second value"
    private val requestHeaders = mapOf(
        firstHeaderKey to listOf(firstHeaderValue),
        secondHeaderKey to listOf(secondHeaderFirstValue, secondHeaderSecondValue)
    )
    private val requestType = "Request type"
    private val validResponseCodes = listOf(200)

    private val requestConfig: RequestConfig = mock {
        on { type } doReturn requestType
        on { url } doReturn url
        on { headers } doReturn requestHeaders
        on { validResponseCodes } doReturn validResponseCodes
    }

    private val sslSocketFactory: SSLSocketFactory = mock()
    private val sslSocketFactoryProvider: SslSocketFactoryProvider = mock {
        on { sslSocketFactory }.thenReturn(sslSocketFactory)
    }
    private val requestCallback: IdSyncRequestCallback = mock()

    private val responseIsComplete = true
    private val responseCode = 200
    private val responseUrl = "Response url"
    private val responseData = "Response body".toByteArray()
    private val responseErrorData = "Response error body".toByteArray()
    private val responseHeaders = mapOf("Response header key" to listOf("Response header value"))

    private val response: Response = mock {
        on { isCompleted } doReturn responseIsComplete
        on { code } doReturn responseCode
        on { url } doReturn responseUrl
        on { responseData } doReturn responseData
        on { errorData } doReturn responseErrorData
        on { headers } doReturn responseHeaders
    }

    private val networkRequestCaptor = argumentCaptor<Request>()
    private val requestResultCaptor = argumentCaptor<RequestResult>()

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
        on { build() }.thenReturn(networkClient) doReturn networkClient
    }

    private val networkClientBuilder by networkClientBuilderRule

    private val sender by setUp { IdSyncRequestSender(sslSocketFactoryProvider, requestCallback) }

    @Test
    fun `sendRequest network client configuration`() {
        sender.sendRequest(requestConfig)

        verify(networkClientBuilder).withSslSocketFactory(sslSocketFactory)
        verify(networkClientBuilder).withUseCaches(false)
        verify(networkClientBuilder).withInstanceFollowRedirects(true)
        verify(networkClientBuilder).withMaxResponseSize(100 * 1024)
    }

    @Test
    fun `sendRequest request creation`() {
        sender.sendRequest(requestConfig)
        verify(networkClient).newCall(networkRequestCaptor.capture())
        assertThat(networkRequestCaptor.allValues.size).isEqualTo(1)

        ObjectPropertyAssertions(networkRequestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField(
                "headers",
                mapOf(
                    firstHeaderKey to firstHeaderValue,
                    secondHeaderKey to "$secondHeaderFirstValue, $secondHeaderSecondValue"
                )
            )
            .checkField("method", "GET")
            .checkField("body", ByteArray(0))
            .checkAll()
    }

    @Test
    fun `sendRequest request callback`() {
        sender.sendRequest(requestConfig)
        verify(requestCallback).onResult(requestResultCaptor.capture(), any())

        ObjectPropertyAssertions(requestResultCaptor.firstValue)
            .checkField("type", requestType)
            .checkField("isCompleted", responseIsComplete)
            .checkField("url", responseUrl)
            .checkField("responseCodeIsValid", true)
            .checkField("responseCode", responseCode)
            .checkField("responseBody", responseData)
            .checkField("responseHeaders", responseHeaders)
            .checkAll()
    }

    @Test
    fun `sendRequest callback with error response`() {
        whenever(response.responseData).doReturn(ByteArray(0))
        whenever(response.errorData).doReturn(responseErrorData)

        sender.sendRequest(requestConfig)
        verify(requestCallback).onResult(requestResultCaptor.capture(), any())

        ObjectPropertyAssertions(requestResultCaptor.firstValue)
            .checkField("type", requestType)
            .checkField("isCompleted", responseIsComplete)
            .checkField("url", responseUrl)
            .checkField("responseCodeIsValid", true)
            .checkField("responseCode", responseCode)
            .checkField("responseBody", responseErrorData)
            .checkField("responseHeaders", responseHeaders)
            .checkAll()
    }

    @Test
    fun `sendRequest callback with invalid response code`() {
        whenever(response.code).doReturn(404)

        sender.sendRequest(requestConfig)
        verify(requestCallback).onResult(requestResultCaptor.capture(), any())

        ObjectPropertyAssertions(requestResultCaptor.firstValue)
            .checkField("type", requestType)
            .checkField("isCompleted", responseIsComplete)
            .checkField("url", responseUrl)
            .checkField("responseCodeIsValid", false)
            .checkField("responseCode", 404)
            .checkField("responseBody", responseData)
            .checkField("responseHeaders", responseHeaders)
            .checkAll()
    }
}
