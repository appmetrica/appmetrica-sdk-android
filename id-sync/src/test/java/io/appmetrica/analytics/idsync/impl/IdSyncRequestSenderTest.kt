package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.network.internal.NetworkClientBuilder
import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.networkapi.Response
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
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
internal class IdSyncRequestSenderTest : CommonTest() {

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
    private val responseHeaders = mapOf(
        "Response header key" to listOf("Response header value")
    )

    private val response: Response = mock {
        on { isCompleted } doReturn responseIsComplete
        on { code } doReturn responseCode
        on { url } doReturn responseUrl
        on { responseData } doReturn responseData
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
    val networkClientBuilderRule = constructionRule<NetworkClientBuilder> {
        on { withSettings(networkClientSettings) } doReturn this.mock
        on { build() }.thenReturn(networkClient) doReturn networkClient
    }

    private val networkClientSettings: NetworkClientSettings = mock()

    @get:Rule
    val networkClientSettingsBuilderRule =
        MockedConstructionRule(NetworkClientSettings.Builder::class.java) { mock, _ ->
            whenever(mock.withSslSocketFactory(sslSocketFactory)).thenReturn(mock)
            whenever(mock.withUseCaches(false)).thenReturn(mock)
            whenever(mock.withInstanceFollowRedirects(true)).thenReturn(mock)
            whenever(mock.withMaxResponseSize(100 * 1024)).thenReturn(mock)
            whenever(mock.build()).thenReturn(networkClientSettings)
        }

    private val networkClientBuilder by networkClientBuilderRule

    private val sender by setUp { IdSyncRequestSender(sslSocketFactoryProvider, requestCallback) }

    @Test
    fun `sendRequest network client configuration`() {
        sender.sendRequest(requestConfig)

        verify(networkClientBuilder).withSettings(networkClientSettings)
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
            .checkField("method", Request.Method.GET)
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
