package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.os.Build

open class AbiResolver(private val supportedAbi: Set<String>) {
    fun getAbi(): String? {
        return lookupAbi(Build.SUPPORTED_64_BIT_ABIS) ?: lookupAbi(Build.SUPPORTED_32_BIT_ABIS)
    }

    private fun lookupAbi(from: Array<String>): String? = from.firstOrNull { it in supportedAbi }

    companion object Default : AbiResolver(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
}
