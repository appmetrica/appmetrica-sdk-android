package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.io.Compressor
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import java.io.IOException

class SendingDataTaskHelperTest {

    private val requestBodyEncrypter = mock<RequestBodyEncrypter>()
    private val compressor = mock<Compressor>()
    private val timeProvider = mock<TimeProvider>()
    private val requestDataHolder = mock<RequestDataHolder>()
    private val responseDataHolder = mock<ResponseDataHolder>()
    private val postData = "post data".toByteArray()
    private val responseHandler = mock<NetworkResponseHandler<DefaultResponseParser.Response>>()
    private val helper =
        SendingDataTaskHelper(
            requestBodyEncrypter,
            compressor,
            timeProvider,
            requestDataHolder,
            responseDataHolder,
            responseHandler
        )

    @Test
    fun onPerformRequest() {
        val currentTime = 777888L
        stubbing(timeProvider) {
            on { this.currentTimeMillis() } doReturn currentTime
        }
        helper.onPerformRequest()
        verify(requestDataHolder).applySendTime(currentTime)
    }

    @Test
    fun isResponseValidNullResponse() {
        stubbing(responseHandler) {
            on { this.handle(responseDataHolder) } doReturn null
        }
        assertThat(helper.isResponseValid).isFalse
    }

    @Test
    fun isResponseValidNotAccepted() {
        stubbing(responseHandler) {
            on { this.handle(responseDataHolder) } doReturn DefaultResponseParser.Response("not accepted")
        }
        assertThat(helper.isResponseValid).isFalse
    }

    @Test
    fun isResponseValidAccepted() {
        stubbing(responseHandler) {
            on { this.handle(responseDataHolder) } doReturn DefaultResponseParser.Response("accepted")
        }
        assertThat(helper.isResponseValid).isTrue
    }

    @Test
    fun prepareAndSetPostDataCompressedBytesAreNull() {
        stubbing(compressor) {
            on { this.compress(postData) } doReturn null
        }
        assertThat(helper.prepareAndSetPostData(postData)).isFalse
        verifyNoInteractions(requestBodyEncrypter, requestDataHolder)
    }

    @Test
    fun prepareAndSetPostDataEncryptedBytesAreNull() {
        val compressed = "compressed".toByteArray()
        stubbing(compressor) {
            on { this.compress(postData) } doReturn compressed
        }
        stubbing(requestBodyEncrypter) {
            on { this.encrypt(compressed) } doReturn null
        }
        assertThat(helper.prepareAndSetPostData(postData)).isFalse
        verifyNoInteractions(requestDataHolder)
    }

    @Test
    fun prepareAndSetPostDataException() {
        stubbing(compressor) {
            on { this.compress(postData) } doThrow IOException()
        }
        assertThat(helper.prepareAndSetPostData(postData)).isFalse
        verifyNoInteractions(requestDataHolder)
    }

    @Test
    fun prepareAndSetPostDataSuccess() {
        val encrypted = "encrypted".toByteArray()
        val compressed = "compressed".toByteArray()
        stubbing(compressor) {
            on { this.compress(postData) } doReturn compressed
        }
        stubbing(requestBodyEncrypter) {
            on { this.encrypt(compressed) } doReturn encrypted
        }
        assertThat(helper.prepareAndSetPostData(postData)).isTrue
        verify(requestDataHolder).postData = encrypted
    }
}
