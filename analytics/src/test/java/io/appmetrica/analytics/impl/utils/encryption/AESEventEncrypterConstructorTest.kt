package io.appmetrica.analytics.impl.utils.encryption

import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AESEventEncrypterConstructorTest : AESEventEncrypterTestBase() {

    @Test
    fun constructorWithContext() {
        aesEventEncrypter = AESEventEncrypter()
        val aesEncrypter = aesEventEncrypter.aesEncrypter

        assertThat(aesEncrypter.algorithm).isEqualTo(AESEncrypter.DEFAULT_ALGORITHM)

        assertThat(aesEncrypter.password)
            .isEqualTo(byteArrayOf(73, -106, -125, 31, 77, -66, 83, -33, -8, -96, 19, -68, 80, 109, -11, -47))

        assertThat(aesEncrypter.iv)
            .isEqualTo(byteArrayOf(-108, -106, 25, 95, -44, -21, 17, -7, -116, -79, 104, -3, -45, 64, 24, -82))
    }

    @Test
    fun constructorWithCredentialProvider() {
        val aesCredentialProvider: AESCredentialProvider = mock()

        val password = byteArrayOf(73, -106, -125, 31, 77, -66, 83, -33, -8, -96, 19, -68, 80, 109, -11, -47)
        val iv = byteArrayOf(-108, -106, 25, 95, -44, -21, 17, -7, -116, -79, 104, -3, -45, 64, 24, -82)

        whenever(aesCredentialProvider.getPassword()).thenReturn(password)
        whenever(aesCredentialProvider.getIV()).thenReturn(iv)

        aesEventEncrypter = AESEventEncrypter(aesCredentialProvider)
        val aesEncrypter = aesEventEncrypter.aesEncrypter

        assertThat(aesEncrypter.algorithm).isEqualTo(AESEncrypter.DEFAULT_ALGORITHM)
        assertThat(aesEncrypter.password).isEqualTo(password)
        assertThat(aesEncrypter.iv).isEqualTo(iv)
    }
}
