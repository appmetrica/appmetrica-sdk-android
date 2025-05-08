package io.appmetrica.analytics.networktasks.internal

import android.net.Uri
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkTaskForSendingDataParamsAppenderTest : CommonTest() {

    private val uriBuilder = Uri.Builder().query("https://ya.ru")
    private val requestBodyEncrypter = mock<RequestBodyEncrypter>()
    private val appender =
        NetworkTaskForSendingDataParamsAppender(
            requestBodyEncrypter
        )

    @Test
    fun appendEncryptedDataAes() {
        stubbing(requestBodyEncrypter) {
            on { this.encryptionMode } doReturn RequestBodyEncryptionMode.AES_RSA
        }
        appender.appendEncryptedData(uriBuilder)
        assertThat(uriBuilder.build().getQueryParameter(CommonUrlParts.ENCRYPTED_REQUEST)).isEqualTo("1")
    }

    @Test
    fun appendEncryptedDataNone() {
        stubbing(requestBodyEncrypter) {
            on { this.encryptionMode } doReturn RequestBodyEncryptionMode.NONE
        }
        appender.appendEncryptedData(uriBuilder)
        assertThat(uriBuilder.build().queryParameterNames).doesNotContain(CommonUrlParts.ENCRYPTED_REQUEST)
    }
}
