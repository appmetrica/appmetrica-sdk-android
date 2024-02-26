package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.encryption.AESRSAEncrypter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class CryptoProviderImplTest : CommonTest() {

    @get:Rule
    val aesRsaEncrypterMockedConstructionRule = constructionRule<AESRSAEncrypter>()

    private val cryptoProviderImpl: CryptoProviderImpl by setUp {
        CryptoProviderImpl()
    }

    @Test
    fun aesRsaEncrypter() {
        assertThat(cryptoProviderImpl.aesRsaEncrypter)
            .isEqualTo(aesRsaEncrypterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(aesRsaEncrypterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(aesRsaEncrypterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
