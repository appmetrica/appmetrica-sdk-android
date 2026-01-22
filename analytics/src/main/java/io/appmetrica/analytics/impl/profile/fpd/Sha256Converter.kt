package io.appmetrica.analytics.impl.profile.fpd

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

internal class Sha256Converter(
    private val attributeValueNormalizer: AttributeValueNormalizer
) {

    private val tag = "[Sha256Converter]"

    fun convert(inputs: Iterable<String>): List<String> {
        return inputs.mapNotNull { convert(it) }
    }

    private fun convert(input: String): String? {
        return try {
            attributeValueNormalizer.normalize(input)?.let { normalizedInput ->
                MessageDigest.getInstance("SHA-256")
                    .digest(normalizedInput.toByteArray())
                    .let { StringUtils.toHexString(it) }
            } ?: run {
                DebugLogger.error(tag, "Input $input is not a valid data")
                PublicLogger.getAnonymousInstance().info("Input $input is not a valid data")
                null
            }
        } catch (e: NoSuchAlgorithmException) {
            DebugLogger.error(tag, e, "SHA-256 algorithm not found")
            null
        }
    }
}
