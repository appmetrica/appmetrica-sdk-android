package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.network.internal.NetworkClient
import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.network.internal.Response
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.function.Predicate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class CallImplTest : CommonTest() {

    private val client = mock<NetworkClient>()
    private val request = mock<Request> {
        on { url } doReturn "some url"
    }
    private val connection = mock<HttpsURLConnection>()
    private val url = mock<URL> {
        on { openConnection() } doReturn connection
    }
    private val urlProvider = mock<UrlProvider> {
        on { createUrl(any()) } doReturn url
    }

    private val callImpl = CallImpl(client, request, urlProvider)

    @Test
    fun couldNotOpenConnection() {
        val exception = MalformedURLException()
        stubbing(url) {
            on { openConnection() } doThrow exception
        }
        val result = callImpl.execute()
        verifyNoInteractions(client, connection)
        checkResponse(result, false, 0, ByteArray(0), ByteArray(0), emptyMap(), exception, null)
    }

    @Test
    fun notHttpsConnection() {
        stubbing(url) {
            on { openConnection() } doReturn mock()
        }
        val result = callImpl.execute()
        verifyNoInteractions(client, connection)
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkField("completed", "isCompleted", false)
            .checkField("code", "getCode", 0)
            .checkField("responseData", "getResponseData", ByteArray(0))
            .checkField("errorData", "getErrorData", ByteArray(0))
            .checkField("headers", "getHeaders", emptyMap<String, List<String>>())
            .checkField("url", null as String?)
            .checkFieldMatchPredicate(
                "exception",
                "getException",
                Predicate<Throwable> {
                    try {
                        assertThat(it).isInstanceOf(IllegalArgumentException::class.java)
                        assertThat(it.message)
                            .isEqualTo("Connection created for some url does not represent https connection")
                        true
                    } catch (ex: Throwable) {
                        false
                    }
                }
            )
            .checkAll()
    }

    @Test
    fun connectionSetUpFilledGet() {
        val url = "some url"
        val method = "GET"
        val body = "some body".toByteArray()
        val headers = mapOf("key1" to "value1", "key2" to "value2, value3")
        val connectTimeout = 777
        val readTimeout = 888
        val sslSocketFactory = mock<SSLSocketFactory>()
        val useCaches = Random.nextBoolean()
        val followRedirects = Random.nextBoolean()
        stubbing(request) {
            on { this.url } doReturn url
            on { this.method } doReturn method
            on { this.body } doReturn body
            on { this.headers } doReturn headers
        }
        stubbing(client) {
            on { this.connectTimeout } doReturn connectTimeout
            on { this.readTimeout } doReturn readTimeout
            on { this.sslSocketFactory } doReturn sslSocketFactory
            on { this.useCaches } doReturn useCaches
            on { this.instanceFollowRedirects } doReturn followRedirects
        }
        callImpl.execute()
        verify(urlProvider).createUrl(url)
        verify(connection).requestMethod = method
        verify(connection, never()).outputStream
        verify(connection).addRequestProperty("key1", "value1")
        verify(connection).addRequestProperty("key2", "value2, value3")
        verify(connection).connectTimeout = connectTimeout
        verify(connection).readTimeout = readTimeout
        verify(connection).sslSocketFactory = sslSocketFactory
        verify(connection).useCaches = useCaches
        verify(connection).instanceFollowRedirects = followRedirects
    }

    @Test
    fun connectionSetUpEmptyGet() {
        val url = "some url"
        val method = "GET"
        stubbing(request) {
            on { this.url } doReturn url
            on { this.method } doReturn method
        }
        stubbing(client) {
            on { connectTimeout } doReturn null
            on { readTimeout } doReturn null
            on { useCaches } doReturn null
            on { instanceFollowRedirects } doReturn null
        }
        callImpl.execute()
        verify(urlProvider).createUrl(url)
        verify(connection).requestMethod = method
        verify(connection, never()).outputStream
        verify(connection, never()).addRequestProperty(any(), any())
        verify(connection, never()).connectTimeout = any()
        verify(connection, never()).readTimeout = any()
        verify(connection, never()).sslSocketFactory = any()
        verify(connection, never()).useCaches = any()
        verify(connection, never()).instanceFollowRedirects = any()
    }

    @Test
    fun connectionSetUpFilledPost() {
        val url = "some url"
        val method = "POST"
        val body = "some body".toByteArray()
        val headers = mapOf("key1" to "value1", "key2" to "value2, value3")
        val connectTimeout = 777
        val readTimeout = 888
        val sslSocketFactory = mock<SSLSocketFactory>()
        val useCaches = Random.nextBoolean()
        val followRedirects = Random.nextBoolean()
        val outputStream = mock<OutputStream>()
        stubbing(connection) {
            on { this.outputStream } doReturn outputStream
        }
        stubbing(request) {
            on { this.url } doReturn url
            on { this.method } doReturn method
            on { this.body } doReturn body
            on { this.headers } doReturn headers
        }
        stubbing(client) {
            on { this.connectTimeout } doReturn connectTimeout
            on { this.readTimeout } doReturn readTimeout
            on { this.sslSocketFactory } doReturn sslSocketFactory
            on { this.useCaches } doReturn useCaches
            on { this.instanceFollowRedirects } doReturn followRedirects
        }
        callImpl.execute()
        verify(urlProvider).createUrl(url)
        verify(connection).requestMethod = method
        verify(connection).addRequestProperty("key1", "value1")
        verify(connection).addRequestProperty("key2", "value2, value3")
        verify(connection).connectTimeout = connectTimeout
        verify(connection).readTimeout = readTimeout
        verify(connection).sslSocketFactory = sslSocketFactory
        verify(connection).useCaches = useCaches
        verify(connection).instanceFollowRedirects = followRedirects
        verify(connection).doOutput = true
        with(inOrder(outputStream)) {
            verify(outputStream).write(body)
            verify(outputStream).flush()
        }
    }

    @Test
    fun connectionSetUpEmptyPost() {
        val url = "some url"
        val method = "POST"
        val outputStream = mock<OutputStream>()
        stubbing(connection) {
            on { this.outputStream } doReturn outputStream
        }
        stubbing(request) {
            on { this.url } doReturn url
            on { this.method } doReturn method
            on { this.body } doReturn ByteArray(0)
        }
        stubbing(client) {
            on { this.connectTimeout } doReturn null
            on { this.readTimeout } doReturn null
            on { this.useCaches } doReturn null
            on { this.instanceFollowRedirects } doReturn null
        }
        callImpl.execute()
        verify(urlProvider).createUrl(url)
        verify(connection).requestMethod = method
        verify(connection, never()).addRequestProperty(any(), any())
        verify(connection, never()).connectTimeout = any()
        verify(connection, never()).readTimeout = any()
        verify(connection, never()).sslSocketFactory = any()
        verify(connection, never()).useCaches = any()
        verify(connection, never()).instanceFollowRedirects = any()
        verify(connection).doOutput = true
        with(inOrder(outputStream)) {
            verify(outputStream).write(ByteArray(0))
            verify(outputStream).flush()
        }
    }

    @Test
    fun responseCodeThrows() {
        val exception = IllegalStateException()
        stubbing(connection) {
            on { responseCode } doThrow exception
        }
        val response = callImpl.execute()
        checkResponse(response, false, 0, ByteArray(0), ByteArray(0), emptyMap(), exception, null)
        verify(connection).disconnect()
    }

    @Test
    fun emptyResponse() {
        val code = 310
        val requestUrl = URL("https", "some", "")
        stubbing(connection) {
            on { responseCode } doReturn code
            on { url } doReturn requestUrl
        }
        val response = callImpl.execute()
        checkResponse(response, true, code, ByteArray(0), ByteArray(0), emptyMap(), null, requestUrl.toString())
        verify(connection).disconnect()
    }

    @Test
    fun filledResponse() {
        val code = 310
        val responseData = "some response".toByteArray()
        val errorData = "some error".toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(responseData))
        val errorStream = BufferedInputStream(ByteArrayInputStream(errorData))
        val headers = mapOf("key1" to listOf("header1", "header2"), "key2" to listOf("header3", "header4"))
        val url = URL("https", "test.com", "")
        stubbing(connection) {
            on { responseCode } doReturn code
            on { this.inputStream } doReturn inputStream
            on { this.errorStream } doReturn errorStream
            on { this.headerFields } doReturn headers
            on { this.url } doReturn url
        }
        val response = callImpl.execute()
        checkResponse(response, true, code, responseData, errorData, headers, null, url.toString())
        verify(connection).disconnect()
    }

    @Test
    fun onlyMaxSizeOfResponseIsRead() {
        val code = 310
        val responseData = "a".repeat(8200).toByteArray()
        val errorData = "b".repeat(8200).toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(responseData))
        val errorStream = BufferedInputStream(ByteArrayInputStream(errorData))
        stubbing(connection) {
            on { responseCode } doReturn code
            on { this.inputStream } doReturn inputStream
            on { this.errorStream } doReturn errorStream
        }
        stubbing(client) {
            on { maxResponseSize } doReturn 8100
        }
        val response = callImpl.execute()
        assertThat(response.responseData).isEqualTo("a".repeat(8192).toByteArray())
        assertThat(response.errorData).isEqualTo("b".repeat(8192).toByteArray())
    }

    private fun checkResponse(
        actual: Response,
        completed: Boolean,
        responseCode: Int,
        responseData: ByteArray,
        errorData: ByteArray,
        headers: Map<String, List<String>>,
        exception: Throwable?,
        url: String?
    ) {
        ObjectPropertyAssertions(actual)
            .withPrivateFields(true)
            .checkField("completed", "isCompleted", completed)
            .checkField("code", "getCode", responseCode)
            .checkField("responseData", "getResponseData", responseData)
            .checkField("errorData", "getErrorData", errorData)
            .checkField("headers", "getHeaders", headers)
            .checkField("exception", "getException", exception)
            .checkField("url", "getUrl", url)
            .checkAll()
    }
}
