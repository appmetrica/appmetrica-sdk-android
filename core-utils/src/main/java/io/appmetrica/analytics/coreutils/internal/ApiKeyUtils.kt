package io.appmetrica.analytics.coreutils.internal

object ApiKeyUtils {

    @JvmStatic
    fun createPartialApiKey(fullApiKey: String?): String {
        val apiKeyRequiredLength = 36
        if (fullApiKey == null || fullApiKey.length != apiKeyRequiredLength) {
            return StringUtils.UNDEFINED
        }
        val apiKeyPrefixLength = 8
        val apiKeySuffixLength = 4
        val apiKeyMiddleMask = "-xxxx-xxxx-xxxx-xxxxxxxx"
        return StringBuilder(fullApiKey).also {
            it.replace(apiKeyPrefixLength, fullApiKey.length - apiKeySuffixLength, apiKeyMiddleMask)
        }.toString()
    }
}
