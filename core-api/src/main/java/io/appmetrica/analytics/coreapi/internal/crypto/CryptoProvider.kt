package io.appmetrica.analytics.coreapi.internal.crypto

interface CryptoProvider {

    val aesRsaEncrypter: Encrypter
}
