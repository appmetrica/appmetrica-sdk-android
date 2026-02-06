package io.appmetrica.analytics.impl.utils.encryption

import android.util.Base64
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.testutils.RandomStringGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class AESEventEncrypterEncrypterParametrizedTest(
    private val input: String?,
    private val expectedEncryptedData: String?,
    private val expectedDecryptedData: ByteArray,
    @Suppress("unused") inputCaption: String
) : AESEventEncrypterTestBase() {
    private lateinit var aesEncrypter: AESEncrypter
    private lateinit var counterReport: CounterReport
    private lateinit var encryptedCounterReport: EncryptedCounterReport
    private var encryptedValue: String? = null

    @Before
    override fun setUp() {
        super.setUp()
        aesEncrypter = createAESEncrypterMock()
        aesEventEncrypter = AESEventEncrypter(aesEncrypter)
        counterReport = CounterReport()
        counterReport.value = input
        encryptedCounterReport = aesEventEncrypter.encrypt(counterReport)
        encryptedValue = encryptedCounterReport.mCounterReport.value
    }

    @Test
    fun encryptedValue() {
        val expectedValue = expectedEncryptedData?.let {
            Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
        } ?: expectedEncryptedData

        assertThat(encryptedValue).isEqualTo(expectedValue)
    }

    @Test
    fun decryptedValue() {
        val decryptedValue = aesEventEncrypter.decrypt(encryptedValue?.toByteArray())
        assertThat(decryptedValue).isEqualTo(expectedDecryptedData)
    }

    @Test
    fun encryptionMode() {
        assertThat(encryptedCounterReport.mEventEncryptionMode).isEqualTo(EventEncryptionMode.AES_VALUE_ENCRYPTION)
    }

    private fun createAESEncrypterMock(): AESEncrypter {
        val aesEncrypter: AESEncrypter = mock {
            on { encrypt(any()) }.thenAnswer {
                it.getArgument<ByteArray>(0)
            }
            on { decrypt(any()) }.thenAnswer {
                it.getArgument<ByteArray>(0)
            }
        }
        return aesEncrypter
    }

    companion object {
        private const val SHORT_STRING: String = "short test string"
        private val LONG_STRING: String = RandomStringGenerator(256 * 1024).nextString()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "For input data = {3}")
        fun data(): List<Array<Any?>> = listOf(
            arrayOf(null, null, ByteArray(0), "[0] null"),
            arrayOf("", null, ByteArray(0), "[1] empty array"),
            arrayOf(
                SHORT_STRING, SHORT_STRING,
                SHORT_STRING.toByteArray(), "[2] short string"
            ),
            arrayOf(
                LONG_STRING, LONG_STRING,
                LONG_STRING.toByteArray(), "[3] long string"
            )
        )
    }
}
