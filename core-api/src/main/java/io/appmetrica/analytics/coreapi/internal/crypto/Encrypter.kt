package io.appmetrica.analytics.coreapi.internal.crypto

interface Encrypter {

    fun encrypt(input: ByteArray): ByteArray?
}
