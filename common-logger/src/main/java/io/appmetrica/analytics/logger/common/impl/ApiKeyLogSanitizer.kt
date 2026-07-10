package io.appmetrica.analytics.logger.common.impl

/**
 * Masks AppMetrica API keys (UUID format) in public log messages.
 */
internal object ApiKeyLogSanitizer {

    private const val API_KEY_PREFIX_LENGTH = 8
    private const val API_KEY_SUFFIX_LENGTH = 4
    private const val API_KEY_MIDDLE_MASK = "-xxxx-xxxx-xxxx-xxxxxxxx"

    private val apiKeyUuidPattern: Regex by lazy {
        Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
    }

    @JvmStatic
    fun maskApiKeysInLog(message: String): String {
        if (message.isEmpty()) {
            return message
        }
        return apiKeyUuidPattern.replace(message) { maskSingleApiKey(it.value) }
    }

    private fun maskSingleApiKey(fullApiKey: String): String {
        return fullApiKey.replaceRange(
            API_KEY_PREFIX_LENGTH,
            fullApiKey.length - API_KEY_SUFFIX_LENGTH,
            API_KEY_MIDDLE_MASK
        )
    }
}
