package io.appmetrica.analytics.impl.utils.encryption

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class EventEncrypterProviderByEncryptionModeTests(
    private val eventEncryptionModeId: Int?,
    private val eventEncrypterClassName: String?,
    private val expectedEventEncryptionModeId: Int?,
    @Suppress("unused") eventEncryptionModeCaption: String?
) : EventEncrypterProviderBaseTest() {
    private var eventEncryptionMode: EventEncryptionMode? = null
    private var expectedEventEncryptionMode: EventEncryptionMode? = null

    @Before
    override fun setUp() {
        super.setUp()
        eventEncryptionMode =
            if (eventEncryptionModeId == null) {
                null
            } else {
                EventEncryptionMode.valueOf(eventEncryptionModeId)
            }
        expectedEventEncryptionMode =
            if (expectedEventEncryptionModeId == null) {
                null
            } else {
                EventEncryptionMode.valueOf(expectedEventEncryptionModeId)
            }
    }

    @Test
    fun returnExpectedEncrypter() {
        assertThat(eventEncrypterProvider.getEventEncrypter(eventEncryptionMode).javaClass.getName())
            .isEqualTo(eventEncrypterClassName)
    }

    @Test
    fun returnEncrypterWithExpectedMode() {
        assertThat(eventEncrypterProvider.getEventEncrypter(eventEncryptionMode).getEncryptionMode())
            .isEqualTo(expectedEventEncryptionMode)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "Return {1} for encryptionMode = {3}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf(
                    EventEncryptionMode.NONE.modeId,
                    DummyEventEncrypter::class.java.getName(),
                    EventEncryptionMode.NONE.modeId,
                    EventEncryptionMode.NONE.name
                ),
                arrayOf(
                    EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.modeId,
                    ExternallyEncryptedEventCrypter::class.java.getName(),
                    EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.modeId,
                    EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.name
                ),
                arrayOf(
                    EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId,
                    AESEventEncrypter::class.java.getName(),
                    EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId,
                    EventEncryptionMode.AES_VALUE_ENCRYPTION.name
                ),

                arrayOf(
                    null,
                    DummyEventEncrypter::class.java.getName(),
                    EventEncryptionMode.NONE.modeId,
                    "null"
                )
            )
        }
    }
}
