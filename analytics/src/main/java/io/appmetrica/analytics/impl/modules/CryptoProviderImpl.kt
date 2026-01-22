package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.crypto.CryptoProvider
import io.appmetrica.analytics.coreapi.internal.crypto.Encrypter
import io.appmetrica.analytics.coreutils.internal.encryption.AESRSAEncrypter

internal class CryptoProviderImpl : CryptoProvider {

    override val aesRsaEncrypter: Encrypter = AESRSAEncrypter()
}
