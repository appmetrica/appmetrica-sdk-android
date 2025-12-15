package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.network.internal.NetworkClientBuilder
import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.networkapi.Response
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder
import io.appmetrica.analytics.networktasks.internal.UnderlyingNetworkTask
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.AbstractMap
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
class NetworkTaskPerformingStrategyTest : CommonTest() {

    private val url = "https://ya.ru"
    private val userAgent = "custom user agent"
    private var requestHeaders = mapOf(
        "header1" to listOf("value11", "value12"),
        "header2" to listOf("value21"),
        "header3" to emptyList()
    )
    private var responseHeaders = mapOf(
        "header4" to listOf("value41", "value42"),
        "header5" to listOf("value51"),
        "header6" to emptyList()
    )
    private val requestDataHolder = mock<RequestDataHolder> {
        on { this.headers } doReturn requestHeaders
    }
    private val responseDataHolder = mock<ResponseDataHolder> {
        on { this.responseData } doReturn ByteArray(0)
        on { this.responseHeaders } doReturn responseHeaders
    }
    private val response = mock<Response> {
        on { this.responseData } doReturn ByteArray(0)
        on { this.headers } doReturn responseHeaders
    }
    private val call = mock<Call> {
        on { this.execute() } doReturn response
    }
    private val sslSocketFactory = mock<SSLSocketFactory>()
    private val networkTask = mock<NetworkTask> {
        on { this.requestDataHolder } doReturn requestDataHolder
        on { this.responseDataHolder } doReturn responseDataHolder
        on { this.onPerformRequest() } doReturn true
        on { this.url } doReturn url
        on { this.sslSocketFactory } doReturn sslSocketFactory
        on { this.userAgent } doReturn userAgent
    }
    private val requestCaptor = argumentCaptor<Request>()
    private val networkClient = mock<NetworkClient> {
        on { this.newCall(requestCaptor.capture()) } doReturn call
    }
    private val strategy = NetworkTaskPerformingStrategy()

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
            whenever(mock.build()).thenReturn(networkClientSettings)
        }

    @Test
    fun `performRequest() for null host`() {
        val fullUrlFormer = mock<FullUrlFormer<Any>> {
            on { allHosts } doReturn null
        }
        val underlyingNetworkTask = mock<UnderlyingNetworkTask> {
            on { getFullUrlFormer() } doReturn fullUrlFormer
        }
        whenever(networkTask.underlyingTask).thenReturn(underlyingNetworkTask)
        whenever(networkTask.url).thenReturn(null)
        assertThat(strategy.performRequest(networkTask)).isFalse()
        assertThat(newClientBuilder.constructionMock.constructed()).isEmpty()
        assertThat(requestCaptor.allValues).isEmpty()
    }

    @Test
    fun commonRequestOperations() {
        strategy.performRequest(networkTask)
        verify(networkTask).onPerformRequest()
        val expectedHeaders = mapOf(
            "header1" to "value11,value12",
            "header2" to "value21",
            "header3" to "",
            Constants.Headers.ACCEPT to Constants.Config.TYPE_JSON,
            Constants.Headers.USER_AGENT to userAgent
        )
        ObjectPropertyAssertions(requestCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("headers", "getHeaders", expectedHeaders)
            .checkField("method", "getMethod", Request.Method.GET)
            .checkField("body", "getBody", ByteArray(0))
            .checkAll()
        verify(newClientBuilder.constructionMock.constructed()[0]).withSettings(networkClientSettings)
    }

    @Test
    fun postMethod() {
        val sendTimestamp = 12345654321L
        val sendTimezone = 50000
        val postData = "data".toByteArray()
        stubbing(requestDataHolder) {
            on { this.method } doReturn NetworkTask.Method.POST
            on { this.postData } doReturn postData
            on { this.sendTimestamp } doReturn sendTimestamp
            on { this.sendTimezoneSec } doReturn sendTimezone
        }
        strategy.performRequest(networkTask)
        assertThat(requestCaptor.firstValue.headers).contains(
            AbstractMap.SimpleEntry(Constants.Headers.SEND_TIMESTAMP, "12345654"),
            AbstractMap.SimpleEntry(Constants.Headers.SEND_TIMEZONE, "50000")
        )
        assertThat(requestCaptor.firstValue.method).isEqualTo(Request.Method.POST)
    }

    @Test
    fun getMethod() {
        val postData = "data".toByteArray()
        stubbing(requestDataHolder) {
            on { this.method } doReturn NetworkTask.Method.GET
            on { this.postData } doReturn postData
        }
        strategy.performRequest(networkTask)
        assertThat(requestCaptor.firstValue.headers.keys).doesNotContain(
            Constants.Headers.SEND_TIMESTAMP,
            Constants.Headers.SEND_TIMEZONE
        )
        assertThat(requestCaptor.firstValue.method).isEqualTo(Request.Method.GET)
    }

    @Test
    fun postMethodNullTimes() {
        val postData = "data".toByteArray()
        stubbing(requestDataHolder) {
            on { this.method } doReturn NetworkTask.Method.POST
            on { this.postData } doReturn postData
            on { this.sendTimestamp } doReturn null
            on { this.sendTimezoneSec } doReturn null
        }
        strategy.performRequest(networkTask)
        assertThat(requestCaptor.firstValue.headers.keys).doesNotContain(
            Constants.Headers.SEND_TIMESTAMP,
            Constants.Headers.SEND_TIMEZONE
        )
        assertThat(requestCaptor.firstValue.method).isEqualTo(Request.Method.POST)
    }

    @Test
    fun nullPostData() {
        stubbing(requestDataHolder) {
            on { this.method } doReturn NetworkTask.Method.POST
            on { this.postData } doReturn null
        }
        strategy.performRequest(networkTask)
        assertThat(requestCaptor.firstValue.headers.keys).doesNotContain(
            Constants.Headers.SEND_TIMESTAMP,
            Constants.Headers.SEND_TIMEZONE
        )
        assertThat(requestCaptor.firstValue.method).isEqualTo(Request.Method.GET)
    }

    @Test
    fun emptyPostData() {
        stubbing(requestDataHolder) {
            on { this.method } doReturn NetworkTask.Method.POST
            on { this.postData } doReturn ByteArray(0)
        }
        strategy.performRequest(networkTask)
        assertThat(requestCaptor.firstValue.headers.keys).doesNotContain(
            Constants.Headers.SEND_TIMESTAMP,
            Constants.Headers.SEND_TIMEZONE
        )
        assertThat(requestCaptor.firstValue.method).isEqualTo(Request.Method.GET)
    }

    @Test
    fun validResponse() {
        val code = 555
        stubbing(response) {
            on { this.isCompleted } doReturn true
            on { this.code } doReturn code
            on { this.responseData } doReturn "response".toByteArray()
        }
        stubbing(responseDataHolder) {
            on { this.isValidResponse } doReturn true
        }
        strategy.performRequest(networkTask)
        verify(responseDataHolder).responseCode = code
        verify(responseDataHolder).responseHeaders = responseHeaders
        verify(networkTask).onRequestComplete()
    }

    @Test
    fun invalidResponse() {
        val code = 555
        stubbing(response) {
            on { this.code } doReturn code
        }
        stubbing(responseDataHolder) {
            on { this.isValidResponse } doReturn false
        }
        strategy.performRequest(networkTask)
        verify(responseDataHolder).responseCode = code
        verify(responseDataHolder).responseHeaders = responseHeaders
        verify(responseDataHolder, never()).responseData = anyOrNull()
    }

    @Test
    fun onPerformRequestReturnsFalse() {
        stubbing(networkTask) {
            on { onPerformRequest() } doReturn false
        }
        assertThat(strategy.performRequest(networkTask)).isFalse
        verify(networkTask).onRequestError(null)
    }

    @Test
    fun hasException() {
        val exception = RuntimeException()
        stubbing(response) {
            on { this.exception } doReturn exception
            on { this.isCompleted } doReturn false
        }
        assertThat(strategy.performRequest(networkTask)).isFalse
        verify(networkTask).onRequestError(exception)
    }

    @Test
    fun requestCompleteUnsuccessfully() {
        stubbing(networkTask) {
            on { this.onRequestComplete() } doReturn false
        }
        assertThat(strategy.performRequest(networkTask)).isFalse
    }

    @Test
    fun requestCompleteSuccessfully() {
        stubbing(response) {
            on { this.isCompleted } doReturn true
        }
        stubbing(networkTask) {
            on { this.onRequestComplete() } doReturn true
        }
        assertThat(strategy.performRequest(networkTask)).isTrue
    }
}
